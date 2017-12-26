package com.adit.java.model;

import java.util.Objects;

public class Actor {
    private String firstname, lastname;

    public Actor(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return Objects.equals(getFirstname(), actor.getFirstname()) &&
                Objects.equals(getLastname(), actor.getLastname());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getFirstname(), getLastname());
    }
}
