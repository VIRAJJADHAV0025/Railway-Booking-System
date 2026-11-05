package com.railway;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coach_classes")
public class CoachClass {

    @Id
    @Column(length = 5)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    protected CoachClass() {
        // Required by Hibernate.
    }

    public CoachClass(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
