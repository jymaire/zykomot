package org.zenika.zykomot;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class Patient extends PanacheEntity {
    public String name;
    public String firstName;
}
