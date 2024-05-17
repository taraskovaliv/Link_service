package dev.kovaliv;

import dev.kovaliv.data.entity.Link;
import dev.kovaliv.tasks.SaveVisit;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
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
import static dev.kovaliv.view.Pages.*;
import static io.javalin.http.HttpStatus.BAD_REQUEST;
import static io.javalin.http.HttpStatus.NOT_FOUND;
import static java.lang.System.getenv;

@Log4j2
public class App {
    public static Javalin app() {
        return Javalin.create()
                .get("/", App::home)
                .post("/add", App::add)
                .post("/auth", App::auth)
                .post("/statistic", App::statisticOpen)
                .get("/statistic/{email}", App::statisticByEmail)
                .get("/statistic/{email}/{name}", App::statisticByEmailAndName)
                .get("/{id}", App::redirectById);
    }

    private static void statisticByEmail(Context context) {
        String email = context.pathParam("email");
        if (isAuthenticated(context)) {
            context.html(getStatisticByEmail(email).render());
        }
    }

    private static void statisticByEmailAndName(Context context) {
        String email = context.pathParam("email");
        String name = context.pathParam("name");
        if (isAuthenticated(context)) {
            context.html(getStatisticByEmailAndName(email, name).render());
        }
    }

    private static void statisticOpen(Context context) {
        String email = context.formParam("email");
        context.redirect("/statistic/" + email);
    }

    private static void home(Context ctx) {
        if (isAuthenticated(ctx)) {
            ctx.html(getIndex().render());
        }
    }

    private static boolean isAuthenticated(Context ctx) {
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
        ctx.html(getAuth().render());
        return false;
    }

    private static void auth(Context ctx) {
        String body = decode(ctx.body());
        Map<String, String> params = parseParams(body);
        ctx.sessionAttribute("auth", params.get("key"));
        String redirectPath = ctx.sessionAttribute("redirect_after_auth");
        if (redirectPath == null || redirectPath.isBlank()) {
            redirectPath = "/";
        }
        ctx.redirect(redirectPath);
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
            case "statistic":
                if (isAuthenticated(ctx)) {
                    ctx.html(getStatistic().render());
                }
                return;
        }
        linkRepo().findByName(id).ifPresentOrElse(
                link -> {
                    ctx.redirect(link.getUrl());
                    new SaveVisit(link, ctx.ip(), new HashMap<>(ctx.headerMap()), new HashMap<>(ctx.queryParamMap()))
                            .start();
                },
                () -> error(ctx, NOT_FOUND, "Помилка", "Посилання не знайдено")
        );
    }

    private static void add(Context ctx) {
        log.debug("Add link");
        String body = decode(ctx.body());
        Map<String, String> params = parseParams(body);
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
                    success(ctx, "Успішно додано", "Посилання успішно додано: " + getenv("HOST") + "/" + params.get("name"));
                }
        );
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
                result.put(keyValue[0].toLowerCase(), keyValue[1]);
            }
        });
        return result;
    }
}
