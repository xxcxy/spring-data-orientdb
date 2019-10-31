package org.springframework.data.orientdb3.test.sample;

import org.springframework.data.orientdb3.repository.ElementEntity;
import org.springframework.data.orientdb3.repository.Embedded;
import org.springframework.data.orientdb3.repository.OrientdbId;

import java.util.Date;
import java.util.List;

@ElementEntity
public class QueryElement {
    @OrientdbId
    private String id;
    private String name;
    private Date date;
    private Boolean activated;
    private Double score;
    private String description;
    @Embedded
    private List<String> emailAddresses;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public void setActivated(final Boolean activated) {
        this.activated = activated;
    }

    public void setScore(final Double score) {
        this.score = score;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setEmailAddresses(final List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public Boolean getActivated() {
        return activated;
    }

    public Double getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }
}
