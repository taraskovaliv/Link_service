package dev.kovaliv.data;

import dev.kovaliv.data.repo.HeaderRepo;
import dev.kovaliv.data.repo.LinkRepo;
import dev.kovaliv.data.repo.VisitRepo;

import static dev.kovaliv.config.ContextConfig.context;

public class Repos {

    public static LinkRepo linkRepo() {
        return context().getBean(LinkRepo.class);
    }

    public static VisitRepo visitRepo() {
        return context().getBean(VisitRepo.class);
    }

    public static HeaderRepo headerRepo() {
        return context().getBean(HeaderRepo.class);
    }
}
