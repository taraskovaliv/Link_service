package dev.kovaliv.view;

import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HeadTag;
import j2html.tags.specialized.HtmlTag;

import java.util.Arrays;
import java.util.List;

import static j2html.TagCreator.*;
import static java.lang.System.getenv;

public class Base {

    public static HeadTag getHead(String title) {
        return head(
                meta().withCharset("UTF-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),
                link().withRel("stylesheet").withHref("https://stoprumusic.kovaliv.dev/css/main.css?1.6"),
                link().withRel("stylesheet").withHref("https://stoprumusic.kovaliv.dev/css/icons.min.css"),
                link().withRel("preconnect").withHref("https://fonts.macpaw.com").attr("crossorigin"),
                link().withRel("stylesheet").withHref("https://fonts.macpaw.com/css?family=FixelDisplay:300"),
                //TODO add icon
                title(title)
        );
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
        ).withClass("footer");
    }

    public static HtmlTag getPage(String title, List<Tag> contents) {
        return getPage(title, contents.toArray(new Tag[0]));
    }

    public static HtmlTag getPage(String title, Tag... contents) {
        return html(
                getHead(title),
                body(
                        getNavBar(),
                        hr(),
                        each(Arrays.asList(contents), c -> c),
                        getFooter()
                )
        ).withLang("uk");
    }

    public static DivTag getArrow() {
        return div(
                div().withClass("arrow"),
                div().withClass("arrow")
        ).withClass("divider");
    }

    public static ATag getEmail() {
        return a(getenv("EMAIL")).withHref("mailto:" + getenv("EMAIL"));
    }

    public static DivTag getSaveLive() {
        return div(
                getSaveLiveLogo(),
                getSaveLiveButton()
        ).withClass("cba");
    }

    public static ATag getSaveLiveLogo() {
        return a(
                img().withSrc("https://savelife.in.ua/wp-content/themes/savelife/assets/images/new-logo-black-ua.svg")
                        .withAlt("SaveLife")
        ).withHref("https://savelife.in.ua").withClass("cba-logo");
    }

    public static ATag getSaveLiveButton() {
        return a(
                span(
                        new SvgTag().withStyle("transform: scale(0.95)")
                                .attr("width", "19")
                                .attr("height", "20")
                                .attr("viewBox", "0 0 19 20")
                                .attr("fill", "none")
                                .attr("xmlns", "http://www.w3.org/2000/svg")
                                .with(new PathTag()
                                        .attr("d", "M16.6159 7.98068L9.25075 17.7431L1.8856 7.98068L1.88557 7.98064C0.522531 6.17413 0.756095 3.66224 2.42693 2.135L2.42702 2.13492C3.33721 1.30274 4.56887 0.898143 5.79348 1.02191L5.79514 1.02207C6.84144 1.12605 7.806 1.60704 8.52511 2.36538L9.25074 3.13058L9.97636 2.36538C10.6946 1.60793 11.667 1.12601 12.7069 1.02201L12.7075 1.02196C13.94 0.898051 15.164 1.30246 16.0745 2.13492L16.076 2.13631C17.7532 3.66341 17.9862 6.17312 16.6173 7.97881L16.6159 7.98068Z")
                                        .attr("stroke", "white")
                                        .attr("stroke-width", "2"))
                ).withClass("icon"),
                span("ПІДТРИМАТИ").withClass("text")
        )
                .withClass("btn-heart")
                .withHref("https://savelife.in.ua/donate/");
    }

    static class PathTag extends ContainerTag<PathTag> {
        protected PathTag() {
            super("path");
        }
    }

    static class SvgTag extends ContainerTag<SvgTag> {
        protected SvgTag() {
            super("svg");
        }
    }
}
