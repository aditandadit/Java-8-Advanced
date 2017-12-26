package com.adit.java.model;

import java.util.HashSet;
import java.util.Set;

public class Movie {

    private String title;
    private int releaseYear;
    private Set<Actor> actors  = new HashSet<>();

    public Set<Actor> getActors() {
        return actors;
    }

    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Movie(String title, int releaseYear, Set<Actor> actors) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.actors = actors;
    }

    public Movie(String title, int releaseYear) {
        this.title = title;
        this.releaseYear = releaseYear;
    }

}