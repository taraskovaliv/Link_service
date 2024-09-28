package dev.kovaliv;

import dev.kovaliv.data.entity.Link;
import dev.kovaliv.services.UserValidation;
import dev.kovaliv.tasks.SaveVisit;
import dev.kovaliv.view.def.AbstractBasicGetNav;
import dev.kovaliv.view.def.GetNav;
import io.github.simonscholz.qrcode.QrCodeApi;
import io.github.simonscholz.qrcode.QrCodeConfig;
import io.github.simonscholz.qrcode.QrCodeFactory;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.sentry.Sentry;
import j2html.tags.DomContent;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.utils.ExecutorUtils.executor;
import static dev.kovaliv.view.Pages.*;
import static io.javalin.http.HttpStatus.NOT_FOUND;
import static j2html.TagCreator.link;
import static java.lang.System.getenv;

@Log4j2
public class App extends AbstractApp {

    @Override
    public void addEndpoints(Javalin app) {
        app
                .get("/", this::home)
                .post("/qr", App::qr)
                .get("/qr/{id}", App::qrById)
                .get("/img/{name}", App::getImg)
                .post("/add", App::add)
                .post("/auth", App::auth)
                .post("/statistic", App::statisticOpen)
                .get("/statistic/{email}", this::statisticByEmail)
                .get("/statistic/{email}/{name}", this::statisticByEmailAndName)
                .get("/{id}", this::redirectById);
    }

    @Override
    protected GetNav nav() {
        return new AbstractBasicGetNav() {
            @Override
            public Map<String, String> getMenuItems(String lang, boolean isAuth) {
                if (isAuth) {
                    return Map.of(
                            "QR генератор", "/qr",
                            "Статистика відвідувань", "/statistic"
                    );
                }
                return Map.of("QR генератор", "/qr");
            }

            @Override
            public Logo getLogo(String s) {
                //TODO add logo.svg
                return new Logo("/img/logo.svg", "LinkService");
            }
        };
    }

    @Override
    protected UserValidation userValidation() {
        return new UserValidation() {
            @Override
            public boolean isAuthenticated(Context ctx) {
                String auth = ctx.sessionAttribute("auth");
                return auth != null && auth.equals(getenv("MODERATION_KEY"));
            }

            @Override
            public boolean authenticate(Context ctx) {
                String key = ctx.queryParam("key");
                if (key != null && key.equals(getenv("MODERATION_KEY"))) {
                    ctx.sessionAttribute("auth", key);
                    ctx.redirect(ctx.path());
                    return false;
                }
                String auth = ctx.sessionAttribute("auth");
                if (auth != null && auth.equals(getenv("MODERATION_KEY"))) {
                    return true;
                }
                ctx.status(401);
                ctx.sessionAttribute("redirect_after_auth", ctx.path());
                ctx.html(getAuth(ctx).render());
                return false;
            }
        };
    }

    @Override
    protected List<DomContent> defaultHeadAdditionalTags() {
        return List.of(
                link().withRel("stylesheet").withHref("/css/main.css")
        );
    }

    private static void qrById(Context ctx) {
        String id = ctx.pathParam("id");
        ctx.html(getQr(id, ctx).render());
    }

    @SneakyThrows
    private static void getImg(Context ctx) {
        String name = ctx.pathParam("name");
        ctx.result(Files.readAllBytes(Path.of("img/" + name)))
                .contentType(ContentType.IMAGE_PNG);
    }

    @SneakyThrows
    private static void qr(Context ctx) {
        log.debug("Generate QR code");
        Map<String, String> params = parseParams(ctx.body());
        QrCodeApi qrCodeApi = QrCodeFactory.createQrCodeApi();
        QrCodeConfig config = new QrCodeConfig.Builder(params.get("url"))
                .qrCodeSize(Integer.parseInt(params.get("size")))
                .qrCodeColorConfig(Color.decode(params.get("bg_color")), Color.decode(params.get("color")))
                .build();
        final var qrCode = qrCodeApi.createQrCodeImage(config);
        File output = createNewRandomImage();
        ImageIO.write(qrCode, "png", output);
        ctx.redirect("/qr/" + output.getName().replaceAll("\\.png", ""));
    }

    private static File createNewRandomImage() {
        try {
            String filePath = "img/" + new Random().nextInt(10000, 100000) + ".png";
            Path newFilePath = Paths.get(filePath);
            Files.createDirectories(newFilePath.getParent());
            Files.createFile(newFilePath);
            log.trace("File created: {}", newFilePath.toAbsolutePath());
            return newFilePath.toFile();
        } catch (Exception e) {
            Sentry.captureException(e);
            throw new RuntimeException("Failed to create file", e);
        }
    }

    private void statisticByEmail(Context ctx) {
        if (authenticate(ctx)) {
            String email = ctx.pathParam("email");
            ctx.html(getStatisticByEmail(email, ctx).render());
        }
    }

    private void statisticByEmailAndName(Context ctx) {
        if (authenticate(ctx)) {
            String email = ctx.pathParam("email");
            String name = ctx.pathParam("name");
            ctx.html(getStatisticByEmailAndName(email, name, ctx).render());
        }
    }

    private static void statisticOpen(Context context) {
        String email = context.formParam("email");
        context.redirect("/statistic/" + email);
    }

    private void home(Context ctx) {
        if (authenticate(ctx)) {
            ctx.html(getIndex(ctx).render());
        }
    }

    private static void auth(Context ctx) {
        Map<String, String> params = parseParams(ctx.body());
        ctx.sessionAttribute("auth", params.get("key"));
        String redirectPath = ctx.sessionAttribute("redirect_after_auth");
        if (redirectPath == null || redirectPath.isBlank()) {
            redirectPath = "/";
        }
        ctx.redirect(redirectPath);
    }

    private void redirectById(Context ctx) {
        String id = ctx.pathParam("id");
        switch (id) {
            case "statistic":
                if (authenticate(ctx)) {
                    ctx.html(getStatistic(ctx).render());
                }
                return;
            case "qr":
                ctx.html(getQr(ctx).render());
                return;
        }
        AtomicReference<SaveVisit> visit = new AtomicReference<>();
        linkRepo().findByName(id).ifPresentOrElse(
                link -> {
                    ctx.redirect(link.getUrl());
                    visit.set(new SaveVisit(
                            link, ctx.ip(),
                            new HashMap<>(ctx.headerMap()),
                            new HashMap<>(ctx.queryParamMap())
                    ));
                },
                () -> error(ctx, NOT_FOUND, "Помилка", "Посилання не знайдено")
        );
        if (visit.get() != null) {
            executor().execute(visit.get());
        }
    }

    private static void add(Context ctx) {
        log.debug("Add link");
        Map<String, String> params = parseParams(ctx.body());
        linkRepo().findByName(params.get("name")).ifPresentOrElse(
                link -> error(ctx, "Помилка", "Посилання з такою назвою вже існує"),
                () -> {
                    //TODO add validations
                    linkRepo().save(Link.builder()
                            .name(params.get("name"))
                            .url(formatUrl(params.get("link")))
                            .responsibleEmail(params.get("email"))
                            .description(params.get("description"))
                            .build());
                    success(ctx, "Успішно додано", "Посилання успішно додано: " + getenv("HOST_URI") + "/" + params.get("name"));
                }
        );
    }

    private static String formatUrl(String link) {
        if (link.startsWith("http://") || link.startsWith("https://")) {
            return link;
        }
        return "https://" + link;
    }
}
