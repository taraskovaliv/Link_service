package dev.kovaliv.view;

import io.javalin.http.Context;
import j2html.tags.Tag;
import j2html.tags.specialized.HtmlTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.p;
import static java.util.Objects.requireNonNull;

public class BasicPages {

    public static HtmlTag getSuccess(Context ctx) {
        String title = ctx.sessionAttribute("title");
        String message = requireNonNull(ctx.sessionAttribute("message"));
        String description = ctx.sessionAttribute("description");
        List<Tag> body = new ArrayList<>();
        body.add(h1(message).withClass("text-center").withStyle("margin-top: 10%; margin-bottom: 20px"));
        if (description != null && !description.isBlank()) {
            description = description.replaceAll("\n", "<br>");
            body.add(p(description).withClass("text-center"));
        }
        body.add(p().withStyle("margin-bottom: 10%"));
        return getPage(Objects.requireNonNullElse(title, "Успішно"), body);
    }

    public static HtmlTag getError(Context ctx) {
        String title = ctx.sessionAttribute("title");
        return getPage(Objects.requireNonNullElse(title, "Помилка"),
                h1("Помилка: " + ctx.sessionAttribute("error"))
                        .withClass("text-center")
                        .withStyle("margin-top: 10%; margin-bottom: 10%; color: red")
        );
    }
}
