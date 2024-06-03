package dev.kovaliv.view;

import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HeadTag;
import j2html.tags.specialized.HtmlTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static j2html.TagCreator.*;

public class Base {

    public static HeadTag getHead(String title, List<DomContent> additionalTags) {
        List<DomContent> tags = new ArrayList<>(List.of(
                meta().withCharset("UTF-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),
                link().withRel("stylesheet").withHref("https://kovaliv.dev/css/main.css?1.8"),
                link().withRel("stylesheet").withHref("https://kovaliv.dev/css/icons.min.css"),
                link().withRel("preconnect").withHref("https://fonts.macpaw.com").attr("crossorigin"),
                link().withRel("stylesheet").withHref("https://fonts.macpaw.com/css?family=FixelDisplay:300"),
                link().withRel("icon").withType("image/x-icon").withHref("https://kovaliv.dev/img/favicon.ico"),
                title(title)
        ));
        if (additionalTags != null && !additionalTags.isEmpty()) {
            tags.addAll(additionalTags);
        }
        return head(tags.toArray(DomContent[]::new));
    }

    public static DivTag getNavBar() {
        return div(
                div(
                        a("LinkService").withHref("/")
                ).withClass("logo")
        ).withClasses("header");
    }

    public static DivTag getFooter() {
        return div(
                hr(),
                div("©2024 kovaliv.dev")
        ).withClass("text-center");
    }

    public static HtmlTag getPage(String title, List<DomContent> contents) {
        return getPage(title, new ArrayList<>(), contents.toArray(DomContent[]::new));
    }

    public static HtmlTag getPage(String title, List<DomContent> contents, List<DomContent> additionalTags) {
        return getPage(title, additionalTags, contents.toArray(DomContent[]::new));
    }

    public static HtmlTag getPage(String title, DomContent... contents) {
        return getPage(title, new ArrayList<>(), contents);
    }

    public static HtmlTag getPage(String title, List<DomContent> additionalTags, DomContent... contents) {
        return html(
                getHead(title, additionalTags),
                body(
                        getNavBar(),
                        hr(),
                        each(Arrays.asList(contents), c -> c),
                        getFooter()
                )
        ).withLang("uk");
    }
}
