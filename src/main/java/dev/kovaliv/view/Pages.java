package dev.kovaliv.view;

import dev.kovaliv.data.dto.StatisticDto;
import dev.kovaliv.data.entity.Visit;
import io.javalin.http.Context;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.HtmlTag;
import org.springframework.data.util.Pair;
import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.enums.IndexAxis;
import software.xdev.chartjs.model.javascript.JavaScriptFunction;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.scale.Scales;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearScaleOptions;
import software.xdev.chartjs.model.options.scale.cartesian.linear.LinearTickOptions;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import static dev.kovaliv.data.Repos.linkRepo;
import static dev.kovaliv.data.Repos.visitRepo;
import static dev.kovaliv.view.Base.chartsJs;
import static dev.kovaliv.view.Base.getPage;
import static j2html.TagCreator.*;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static software.xdev.chartjs.model.color.RGBAColor.DARK_GREEN;
import static software.xdev.chartjs.model.options.scale.Scales.ScaleAxis.Y;

public class Pages {

    public static final Color BACKGROUND_COLOR = new Color(144, 238, 144);

    public static HtmlTag getIndex(Context ctx) {
        return getPage("Лінк сервіс kovaliv.dev", getHomeContent(), ctx);
    }

    public static HtmlTag getQr(Context ctx) {
        return getPage("QR-код генератор", getQrContent(), ctx);
    }

    public static HtmlTag getQr(String id, Context ctx) {
        return getPage("QR-код", div(
                img().withSrc("/img/" + id + ".png")
                        .withStyle("margin-top: 5%; margin-bottom: 5%")
                        .withAlt("QR-код")
        ).withClass("text-center"), ctx);
    }

    public static HtmlTag getAuth(Context ctx) {
        return getPage("Авторизація", getAuthContent(), ctx);
    }

    public static HtmlTag getStatistic(Context ctx) {
        return getPage("Статиcтика", getStatisticEnter(), ctx);
    }

    public static HtmlTag getStatisticByEmail(String email, Context ctx) {
        return getPage("Статиcтика для " + email, getStatisticContentByEmail(email), ctx);
    }

    public static HtmlTag getStatisticByEmailAndName(String email, String name, Context ctx) {
        return getPage("Статиcтика для " + email, getStatisticContentByEmailAndName(email, name), ctx);
    }

    private static DivTag getStatisticContentByEmailAndName(String email, String name) {
        List<Visit> visits = visitRepo().findByLinkNameAndNotBot(name);
        if (visits.isEmpty()) {
            return div(
                    h1("Статистика для " + email + " по " + name),
                    div(
                            p("Немає унікальних відвідувачів")
                    ).withClasses("home-content", "text-center").withStyle("margin-top: 3%")
            )
                    .withClasses("content", "text-center")
                    .withStyle("flex-direction: column; margin-top: 3%");
        }
        return div(
                h1("Статистика для " + email + " по " + name),
                div(
                        p("Унікальних відвідувачів: " + countUnique(visits))
                ).withClasses("home-content", "text-center").withStyle("margin-top: 3%"),
                div(
                        chartsJs(),
                        div(
                                canvas().withId("lineChart")
                        ),
                        script(String.format("""
                                let ctx = document.getElementById('lineChart');
                                new Chart(document.getElementById('lineChart').getContext('2d'), %s);
                                """, getVisitsLineChart(visits).toJson())),
                        div(
                                canvas().withId("barChartCountry")
                        ),
                        script(String.format("""
                                ctx = document.getElementById('barChartCountry');
                                new Chart(document.getElementById('barChartCountry').getContext('2d'), %s);
                                """, getBarChartCountry(visits).toJson())),
                        div(
                                canvas().withId("barChartRegion")
                        ),
                        script(String.format("""
                                ctx = document.getElementById('barChartRegion');
                                new Chart(document.getElementById('barChartRegion').getContext('2d'), %s);
                                """, getBarChartRegion(visits).toJson())),
                        div(
                                canvas().withId("barChartCity")
                        ),
                        script(String.format("""
                                ctx = document.getElementById('barChartCity');
                                new Chart(document.getElementById('barChartCity').getContext('2d'), %s);
                                """, getBarChartCity(visits).toJson())),
                        div(
                                canvas().withId("barChartSource")
                        ),
                        script(String.format("""
                                ctx = document.getElementById('barChartSource');
                                new Chart(document.getElementById('barChartSource').getContext('2d'), %s);
                                """, getBarChartSource(visits).toJson()))
                ).withClass("home-content")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static long countUnique(List<Visit> visits) {
        return visits.stream()
                .map(Visit::getIp)
                .distinct()
                .count();
    }

    private static DivTag getStatisticContentByEmail(String email) {
        List<StatisticDto> statisticByEmail = linkRepo().getStatisticByEmail(email);

        if (statisticByEmail.isEmpty()) {
            return div(
                    h1("Статистика для " + email),
                    div(
                            h3("Немає статистики для даного email")
                    ).withClass("home-content")
            )
                    .withClasses("content", "text-center")
                    .withStyle("flex-direction: column; margin-top: 3%");
        }
        return div(
                h1("Статистика для " + email),
                div(
                        div(
                                canvas().withId("barChart").withHeight("70%")
                        ),
                        chartsJs(),
                        script(String.format("""
                                const ctx = document.getElementById('barChart');
                                new Chart(document.getElementById('barChart').getContext('2d'), %s);
                                """, getViewsBarChart(statisticByEmail, email).toJson()))
                ).withClass("home-content")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static BarChart getViewsBarChart(List<StatisticDto> statistics, String email) {
        BarDataset dataset = new BarDataset()
                .setLabel("Перегляди")
                .setData(statistics.stream()
                        .map(StatisticDto::getCount)
                        .toArray(Long[]::new))
                .setBorderWidth(2);
        dataset.addBackgroundColor(BACKGROUND_COLOR);
        dataset.addBorderColor(DARK_GREEN);

        BarData data = new BarData()
                .addLabels(statistics.stream().map(StatisticDto::getName).toArray(String[]::new))
                .addDataset(dataset);

        BarOptions options = new BarOptions()
                .setOnClick(new JavaScriptFunction(String.format("""
                            function(elements, item) {
                                let label = elements.chart.data.labels[item[0].index];
                                window.location.href = '/statistic/%s/' + label;
                            }
                        """, email)))
                .setIndexAxis(IndexAxis.Y);
        return new BarChart()
                .setData(data)
                .setOptions(options);
    }

    private static BarChart getBarChartCountry(List<Visit> visits) {
        var statistic = visits.stream().collect(groupingBy(Visit::getCountry, counting()));
        return getViewsBarChart(statistic, "Country");
    }

    private static BarChart getBarChartRegion(List<Visit> visits) {
        var statistic = visits.stream().collect(groupingBy(Visit::getRegion, counting()));
        return getViewsBarChart(statistic, "Region");
    }

    private static BarChart getBarChartCity(List<Visit> visits) {
        var statistic = visits.stream().collect(groupingBy(Visit::getCity, counting()));
        return getViewsBarChart(statistic, "City");
    }

    private static BarChart getBarChartSource(List<Visit> visits) {
        var statistic = visits.stream().collect(groupingBy(Visit::getSource, counting()));
        return getViewsBarChart(statistic, "Source");
    }

    private static BarChart getViewsBarChart(Map<String, Long> statistic, String label) {
        if (statistic.get("") != null) {
            statistic.put("Unknown", statistic.get(""));
            statistic.remove("");
        }
        SortedMap<String, Long> sorted = new TreeMap<>((a, b) -> statistic.get(b).compareTo(statistic.get(a)));
        sorted.putAll(statistic);

        BarDataset dataset = new BarDataset()
                .setLabel(label)
                .setData(sorted.values().toArray(Long[]::new))
                .setBorderWidth(2);
        dataset.addBackgroundColor(BACKGROUND_COLOR);
        dataset.addBorderColor(DARK_GREEN);

        BarData data = new BarData()
                .addLabels(sorted.keySet().toArray(String[]::new))
                .addDataset(dataset);

        BarOptions options = new BarOptions()
                .setIndexAxis(IndexAxis.Y);
        return new BarChart()
                .setData(data)
                .setOptions(options);
    }

    private static LineChart getVisitsLineChart(List<Visit> visits) {
        Pair<List<String>, List<BigDecimal>> labelsAndData = formatVisits(visits);

        LineDataset dataset = new LineDataset()
                .setLabel("Перегляди")
                .setData(labelsAndData.getSecond().toArray(BigDecimal[]::new))
                .setBorderWidth(2)
                .setLineTension(0.3f);
        dataset.setBackgroundColor(BACKGROUND_COLOR);
        dataset.setBorderColor(DARK_GREEN);

        LineData data = new LineData()
                .addLabels(labelsAndData.getFirst().toArray(String[]::new))
                .addDataset(dataset);

        LineOptions options = new LineOptions()
                .setScales(new Scales()
                        .addScale(Y, new LinearScaleOptions()
                                .setMin(ZERO)
                                .setTicks(new LinearTickOptions().setStepSize(1))));

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
                .orElse(now());
        LocalDate max = data.keySet().stream()
                .map(LocalDate::parse)
                .max(LocalDate::compareTo)
                .orElse(now())
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
                                .withClasses("btn", "btn-primary")
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
                                .withClasses("btn", "btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/auth")
                        .withStyle("margin-top: 15px")
        )
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
                                .withClasses("btn", "btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/add")
                        .withStyle("margin-top: 3%")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }

    private static DivTag getQrContent() {
        return div(
                h1("Генератор QR-кодів"),
                form(
                        label().attr("for", "url"),
                        input()
                                .withId("url")
                                .withType("url")
                                .withName("url")
                                .attr("required")
                                .withPlaceholder("Посилання"),
                        br(),
                        input()
                                .withId("size")
                                .withType("number")
                                .withValue("300")
                                .withCondMin(true, "300")
                                .withName("size"),
                        label("Розмір QR-коду в px. (мінімум 300)")
                                .withStyle("margin-left: 14px")
                                .attr("for", "size"),
                        br(),
                        input()
                                .withId("bg_color")
                                .withType("color")
                                .withValue("#FFFFFF")
                                .withName("bg_color"),
                        label("Колір фону")
                                .withStyle("margin-left: 14px")
                                .attr("for", "bg_color"),
                        br(),
                        input()
                                .withId("color")
                                .withType("color")
                                .withValue("#000000")
                                .withName("color"),
                        label("Колір коду")
                                .withStyle("margin-left: 14px")
                                .attr("for", "color"),
                        br(),
                        button("Згенерувати")
                                .withClasses("btn", "btn-primary")
                ).withClasses("text-center")
                        .withMethod("post")
                        .withAction("/qr")
                        .withStyle("margin-top: 3%")
        )
                .withClasses("content", "text-center")
                .withStyle("flex-direction: column; margin-top: 3%");
    }
}
