package dev.kovaliv;

import dev.kovaliv.data.entity.Link;

import static dev.kovaliv.data.Repos.linkRepo;

public class TmpAddLink {
    public static void main(String[] args) {
        linkRepo().save(Link.builder()
                .name("google")
                .url("https://www.google.com")
                .responsibleEmail("taras19041@gmail.com")
                .build()
        );
    }
}
