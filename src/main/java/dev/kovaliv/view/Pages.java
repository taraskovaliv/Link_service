package dev.kovaliv.view;

import dev.kovaliv.data.dto.StatisticDto;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HtmlTag;
import j2html.tags.specialized.PTag;

import java.util.List;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.*;

public class Pages {


    public static HtmlTag getIndex() {
        return getPage("Лінк сервіс kovaliv.dev", getHomeContent());
    }

    public static HtmlTag getAuth() {
        return getPage("Авторизація", getAuthContent());
    }

    public static HtmlTag getStatistic() {
        return getPage("Статиcтика", getStatisticEnter());
    }

    public static HtmlTag getStatisticByEmail(String email) {
        return getPage("Статиcтика для " + email, getStatisticContentByEmail(email));
    }

    private static DivTag getStatisticContentByEmail(String email) {
        List<StatisticDto> statisticByEmail = linkRepo().getStatisticByEmail(email);
        return div(
                h1("Статистика для " + email),
                div(
                        statisticByEmail.stream()
                                .map(Pages::getStatisticP)
                                .toArray(PTag[]::new)
                ).withClass("home-content")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static PTag getStatisticP(StatisticDto s) {
        if (s.getDescription() != null && !s.getDescription().isEmpty()) {
            return p(s.getName() + " (" + s.getDescription() + ") : " + s.getCount() + " переглядів");
        } else {
            return p(s.getName() + " : " + s.getCount() + " переглядів");
        }
    }

    private static DivTag getStatisticEnter() {
        return div(
                h1("Введіть емейл по якому хочете глянути статистику"),
                form(
                        label().attr("for", "key"),
                        input()
                                .withId("email")
                                .withType("email")
                                .withName("email")
                                .attr("required")
                                .withPlaceholder("Email"),
                        br(),
                        button("Переглянути статистику")
                                .withClasses("btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/statistic")
                        .withStyle("margin-top: 15px")
        )
                .withId("remove-user")
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static DivTag getAuthContent() {
        return div(
                h1("Авторизація"),
                form(
                        label().attr("for", "key"),
                        input()
                                .withId("key")
                                .withType("text")
                                .withName("key")
                                .attr("required")
                                .withPlaceholder("Ваш ключ доступу"),
                        br(),
                        button("Увійти")
                                .withClasses("btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/auth")
                        .withStyle("margin-top: 15px")
        )
                .withId("remove-user")
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
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
