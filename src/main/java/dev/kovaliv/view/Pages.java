package dev.kovaliv.view;

import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HtmlTag;

import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.*;

public class Pages {


    public static HtmlTag getIndex() {
        return getPage("Лінк сервіс kovaliv.dev", getHomeContent());
    }

    private static DivTag getHomeContent() {
        return div(
                h1("Додавання посилання"),
                form(
                        label().attr("for", "name"),
                        input()
                                .withId("name")
                                .withType("text")
                                .withName("name")
                                .attr("required")
                                .withPlaceholder("Назва посилання"),
                        br(),
                        label().attr("for", "email"),
                        input()
                                .withId("email")
                                .withType("email")
                                .withName("email")
                                .attr("required")
                                .withPlaceholder("Ваш email"),
                        br(),
                        label().attr("for", "link"),
                        input()
                                .withId("link")
                                .withType("url")
                                .withName("link")
                                .attr("required")
                                .withPlaceholder("Посилання"),
                        br(),
                        textarea()
                                .withCondMaxlength(true, "3000")
                                .withName("description")
                                .withPlaceholder("Опис посилання"),
                        br(),
                        button("Додати")
                                .withId("add-link")
                                .withClasses("btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/add")
                        .withStyle("margin-top: 3%")
        )
                .withId("remove-user")
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }
}
