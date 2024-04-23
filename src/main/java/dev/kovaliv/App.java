package dev.kovaliv;

import dev.kovaliv.data.entity.Link;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.extern.log4j.Log4j2;

import static dev.kovaliv.data.Repos.linkRepo;

@Log4j2
public class App {
    public static Javalin app() {
        return Javalin.create()
                .get("/{id}", App::redirectById);
    }

    private static void redirectById(Context ctx) {
        String id = ctx.pathParam("id");
        linkRepo().findByName(id).ifPresent(link -> {
            ctx.redirect(link.getUrl());
            new Thread(() -> incrementRedirects(link)).start();
        });
        //TODO else redirect to 404
    }

    private static void incrementRedirects(Link link) {
        synchronized (String.valueOf(link.getId())) {
            linkRepo().findById(link.getId()).ifPresent(l -> {
                link.setCountVisits(link.getCountVisits() + 1);
                linkRepo().save(link);
            });
        }
    }
}
