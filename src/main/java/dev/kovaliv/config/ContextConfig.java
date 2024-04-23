package dev.kovaliv.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContextConfig {
    private static ApplicationContext context;

    public static ApplicationContext context() {
        if (context == null) {
            context = new AnnotationConfigApplicationContext("dev.kovaliv");
        }
        return context;
    }
}
