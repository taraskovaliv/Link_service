package dev.kovaliv.view;

import io.javalin.http.Context;
import j2html.tags.Tag;
import j2html.tags.specialized.H1Tag;
import j2html.tags.specialized.HtmlTag;
import j2html.tags.specialized.PTag;
import j2html.tags.specialized.SpanTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.*;
import static java.util.Objects.requireNonNull;

public class BasicPages {

    public static HtmlTag getSuccess(Context ctx) {
        String title = ctx.sessionAttribute("title");
        String message = requireNonNull(ctx.sessionAttribute("message"));
        String description = ctx.sessionAttribute("description");
        List<Tag> body = new ArrayList<>();
        body.add(getH1WithLinks(message));
        if (description != null && !description.isBlank()) {
            description = description.replaceAll("\n", "<br>");
            body.add(getPWithLinks(description));
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

    private static H1Tag getH1WithLinks(String text) {
        if (text.contains("http://") || text.contains("https://")) {
            List<Tag> tags = splitToTagsWithLink(text);
            return h1(tags.toArray(Tag[]::new))
                    .withClass("text-center")
                    .withStyle("margin-top: 10%; margin-bottom: 20px");
        }
        return h1(text)
                .withClass("text-center")
                .withStyle("margin-top: 10%; margin-bottom: 20px");
    }

    private static PTag getPWithLinks(String text) {
        if (text.contains("http://") || text.contains("https://")) {
            List<Tag> tags = splitToTagsWithLink(text);
            return p(tags.toArray(Tag[]::new)).withClass("text-center");
        }
        return p(text).withClass("text-center");
    }

    private static @NotNull List<Tag> splitToTagsWithLink(String text) {
        List<String> links = getLinks(text);
        List<Tag> tags = new ArrayList<>();
        tags.add(span(text));
        for (String link : links) {
            List<Tag> newTags = new ArrayList<>();
            for (Tag tag : tags) {
                if (tag instanceof SpanTag spanTag) {
                    if (spanTag.render().contains(link)) {
                        String tmp = spanTag.render()
                                .replaceAll("<span>", "")
                                .replaceAll("</span>", "");
                        String[] parts = tmp.split(link);
                        newTags.add(span(parts[0]));
                        if (parts.length > 1) {
                            for (int i = 1; i < parts.length - 1; i++) {
                                newTags.add(a(trimLink(link)).withHref(link));
                                newTags.add(span(parts[i]));
                            }
                        } else {
                            newTags.add(a(trimLink(link)).withHref(link));
                        }
                    } else {
                        newTags.add(spanTag);
                    }
                } else {
                    newTags.add(tag);
                }
            }
            tags = newTags;
        }
        return tags;
    }

    private static String trimLink(String link) {
        link = link.replaceAll("http://", "");
        link = link.replaceAll("https://", "");
        link = link.replaceAll("www.", "");
        return link;
    }

    private static List<String> getLinks(String text) {
        List<String> links = new ArrayList<>();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.contains("http://") || word.contains("https://")) {
                links.add(word);
            }
        }
        return links;
    }
}
