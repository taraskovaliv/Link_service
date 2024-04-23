package dev.kovaliv;

import io.javalin.Javalin;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.LocalDateTime;

import static dev.kovaliv.config.ContextConfig.context;
import static java.time.LocalDateTime.now;

@Log4j2
public class Main {
    public static void main(String[] args) {
        LocalDateTime start = now();
        Javalin app = App.app();

        app.start(7071);
        log.info("App started in {} seconds", Duration.between(start, now()).getSeconds());
        start = now();
        boolean contextStarted = false;
        int retries = 0;
        while (!contextStarted && retries < 10) {
            try {
                context();
                contextStarted = true;
                log.info("Context started in {} seconds", Duration.between(start, now()).getSeconds());
            } catch (Exception e) {
                retries++;
                log.warn("Context not started yet: {}", e.getMessage(), e);
            }
        }
        if (!contextStarted) {
            log.error("Context not started");
            app.stop();
            System.exit(1);
        }
    }
}