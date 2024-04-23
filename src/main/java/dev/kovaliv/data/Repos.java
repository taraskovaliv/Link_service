package dev.kovaliv.data;

import dev.kovaliv.data.repo.LinkRepo;

import static dev.kovaliv.config.ContextConfig.context;

public class Repos {

    public static LinkRepo linkRepo() {
        return context().getBean(LinkRepo.class);
    }
}
