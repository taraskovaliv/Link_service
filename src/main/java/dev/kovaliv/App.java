package dev.kovaliv;

import dev.kovaliv.data.entity.Link;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import lombok.extern.log4j.Log4j2;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.view.BasicPages.getError;
import static dev.kovaliv.view.BasicPages.getSuccess;
import static dev.kovaliv.view.Pages.getIndex;
import static io.javalin.http.HttpStatus.BAD_REQUEST;
import static io.javalin.http.HttpStatus.NOT_FOUND;

@Log4j2
public class App {
    public static Javalin app() {
        return Javalin.create(conf -> conf.staticFiles.add("/static", Location.CLASSPATH))
                .get("/", App::home)
                .post("/add", App::add)
                .get("/{id}", App::redirectById);
    }

    private static void home(Context ctx) {
        ctx.html(getIndex().render());
    }

    private static void redirectById(Context ctx) {
        String id = ctx.pathParam("id");
        switch (id) {
            case "success":
                success(ctx);
                return;
            case "error":
                error(ctx);
                return;
        }
        linkRepo().findByName(id).ifPresent(link -> {
            ctx.redirect(link.getUrl());
            new Thread(() -> incrementRedirects(link)).start();
        });
        error(ctx, NOT_FOUND, "Помилка", "Посилання не знайдено");
    }

    private static void add(Context ctx) {
        log.debug("Add link");
        String body = decode(ctx.body());
        Map<String, String> params = parseParams(body);
        linkRepo().findByName("name").ifPresentOrElse(
                link -> error(ctx, "Помилка", "Посилання з такою назвою вже існує"),
                () -> {
                    //TODO add validations
                    linkRepo().save(Link.builder()
                            .name(params.get("name"))
                            .url(formatUrl(params.get("link")))
                            .responsibleEmail(params.get("email"))
                            .description(params.get("description"))
                            .build());
                }
        );
        //TODO add "copy link to clipboard" button to response
        success(ctx, "Успішно додано", "Посилання успішно додано");
    }


    public static void success(Context ctx, String title, String message) {
        success(ctx, title, message, null);
    }

    public static void success(Context ctx, String title, String message, String description) {
        ctx.sessionAttribute("title", title);
        ctx.sessionAttribute("message", message);
        ctx.sessionAttribute("description", description);
        ctx.redirect("/success");
    }

    private static void success(Context ctx) {
        ctx.html(getSuccess(ctx).render());
        ctx.sessionAttribute("title", null);
        ctx.sessionAttribute("message", null);
        ctx.sessionAttribute("description", null);
    }

    public static void error(Context ctx, String title, String error) {
        error(ctx, null, title, error);
    }

    public static void error(Context ctx, HttpStatus status, String title, String error) {
        ctx.status(Objects.requireNonNullElse(status, BAD_REQUEST));
        ctx.sessionAttribute("title", title);
        ctx.sessionAttribute("error", error);
        ctx.redirect("/error");
    }

    private static void error(Context ctx) {
        ctx.html(getError(ctx).render());
        ctx.sessionAttribute("title", null);
        ctx.sessionAttribute("error", null);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String formatUrl(String link) {
        if (link.startsWith("http://") || link.startsWith("https://")) {
            return link;
        }
        return "https://" + link;
    }

    private static Map<String, String> parseParams(String body) {
        Map<String, String> result = new HashMap<>();
        Arrays.stream(body.split("&")).forEach(param -> {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        });
        return result;
    }

    private static void incrementRedirects(Link link) {
        synchronized (String.valueOf(link.getId())) {
            linkRepo().findById(link.getId()).ifPresent(l -> {
                link.setCountVisits(link.getCountVisits() + 1);
                linkRepo().save(link);
            });
        }
    }
}
