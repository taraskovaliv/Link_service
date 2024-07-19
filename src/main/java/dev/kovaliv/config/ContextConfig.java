package dev.kovaliv.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.TimeZone;

public class ContextConfig {
    private static ApplicationContext context;

    public synchronized static ApplicationContext context() {
        if (context == null) {
            TimeZone.setDefault(TimeZone.getTimeZone("Kyiv/Europe"));
            context = new AnnotationConfigApplicationContext("dev.kovaliv");
        }
        return context;
    }
}
