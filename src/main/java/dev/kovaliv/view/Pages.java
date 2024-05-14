package dev.kovaliv.view;

import dev.kovaliv.data.dto.StatisticDto;
import dev.kovaliv.data.entity.Visit;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HtmlTag;
import org.springframework.data.util.Pair;
import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.color.Color;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.javascript.JavaScriptFunction;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.scales.LinearScale;
import software.xdev.chartjs.model.options.scales.Scales;
import software.xdev.chartjs.model.options.ticks.LinearTicks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.data.Repos.visitRepo;
import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

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

    public static HtmlTag getStatisticByEmailAndName(String email, String name) {
        return getPage("Статиcтика для " + email, getStatisticContentByEmailAndName(email, name));
    }

    private static DivTag getStatisticContentByEmailAndName(String email, String name) {
        List<Visit> visits = visitRepo().findByLinkName(name);
        return div(
                h1("Статистика для " + email + " по " + name),
                div(
                        div(
                                canvas().withId("lineChart").withHeight("70%")
                        ),
                        script().withSrc("https://cdn.jsdelivr.net/npm/chart.js"),
                        script(String.format("""
                                const ctx = document.getElementById('lineChart');
                                new Chart(document.getElementById('lineChart').getContext('2d'), %s);
                                """, getLineChart(visits).toJson()))
                ).withClass("home-content")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static DivTag getStatisticContentByEmail(String email) {
        List<StatisticDto> statisticByEmail = linkRepo().getStatisticByEmail(email);

        return div(
                h1("Статистика для " + email),
                div(
                        div(
                                canvas().withId("barChart").withHeight("70%")
                        ),
                        script().withSrc("https://cdn.jsdelivr.net/npm/chart.js"),
                        script(String.format("""
                                const ctx = document.getElementById('barChart');
                                new Chart(document.getElementById('barChart').getContext('2d'), %s);
                                """, getBarChart(statisticByEmail, email).toJson()))
                ).withClass("home-content")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static BarChart getBarChart(List<StatisticDto> statisticByEmail, String email) {
        BarDataset dataset = new BarDataset()
                .setLabel("Перегляди")
                .setData(statisticByEmail.stream()
                        .map(StatisticDto::getCount)
                        .map(BigDecimal::valueOf)
                        .toList())
                .setBorderWidth(2);
        dataset.addBackgroundColor(new Color(144, 238, 144));
        dataset.addBorderColor(Color.DARK_GREEN);

        BarData data = new BarData()
                .addLabels(statisticByEmail.stream().map(StatisticDto::getName).toArray(String[]::new))
                .addDataset(dataset);

        BarOptions options = new BarOptions()
                .setOnClick(new JavaScriptFunction(String.format("""
                    function(elements, item) {
                        let label = elements.chart.data.labels[item[0].index];
                        window.location.href = '/statistic/%s/' + label;
                    }
                """, email)))
                .setIndexAxis(BarOptions.IndexAxis.Y);
        return new BarChart()
                .setData(data)
                .setOptions(options);
    }

    private static LineChart getLineChart(List<Visit> visits) {
        Pair<List<String>, List<BigDecimal>> labelsAndData = formatVisits(visits);

        LineDataset dataset = new LineDataset()
                .setLabel("Перегляди")
                .setData(labelsAndData.getSecond().toArray(BigDecimal[]::new))
                .setBorderWidth(2)
                .setLineTension(0.3f);
        dataset.setBackgroundColor(new Color(144, 238, 144));
        dataset.setBorderColor(Color.DARK_GREEN);

        LineData data = new LineData()
                .addLabels(labelsAndData.getFirst().toArray(String[]::new))
                .addDataset(dataset);

        LineOptions options = new LineOptions()
                .setScales(new Scales()
                        .addScale(Scales.ScaleAxis.Y, new LinearScale()
                                .setMin(BigDecimal.ZERO)
                                .setTicks(new LinearTicks().setStepSize(1))));

        return new LineChart().setData(data).setOptions(options);
    }

    private static Pair<List<String>, List<BigDecimal>> formatVisits(List<Visit> visits) {
        if (visits.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }
        Map<String, Long> data = visits.stream()
                .collect(groupingBy(v -> v.getCreated().format(ISO_LOCAL_DATE), counting()));
        LocalDate min = data.keySet().stream()
                .map(LocalDate::parse)
                .min(LocalDate::compareTo)
                .get();
        LocalDate max = data.keySet().stream()
                .map(LocalDate::parse)
                .max(LocalDate::compareTo)
                .get()
                .plusDays(1);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (LocalDate date = min; date.isBefore(max); date = date.plusDays(1)) {
            labels.add(date.format(DateTimeFormatter.ofPattern("dd.MM")));
            values.add(BigDecimal.valueOf(data.getOrDefault(date.format(ISO_LOCAL_DATE), 0L)));
        }
        return Pair.of(labels, values);
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
