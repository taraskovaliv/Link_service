package dev.kovaliv.tasks;

import dev.kovaliv.data.entity.Visit;
import dev.kovaliv.services.SlackService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static dev.kovaliv.config.ContextConfig.context;
import static dev.kovaliv.data.Repos.visitRepo;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

@Service
public class DailyNotification {

    @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Kyiv")
    @SchedulerLock(name = "link-daily-visits", lockAtMostFor = "PT60S")
    public void sendDailyNotification() {
        LocalDateTime yesterday = LocalDate.now().atStartOfDay().minusDays(1);
        List<Visit> visits = visitRepo().findAllByCreatedDateBetween(yesterday, LocalDate.now().atStartOfDay());
        if (!visits.isEmpty()) {
            Map<String, Integer> visitsByName = visits.stream()
                    .collect(groupingBy(v -> v.getLink().getName(), summingInt(v -> 1)));
            StringBuilder message = new StringBuilder("Yesterday's visits:\n");
            visitsByName.forEach((k, v) -> message.append(k).append(": ").append(v).append("\n"));
            context().getBean(SlackService.class).send(message.toString(), "link-visits");
        }
    }
}
