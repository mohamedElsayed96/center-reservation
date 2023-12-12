package com.giza.center_reservation.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int maxCapacity;

    private LocalTime startWorkingHour;

    private LocalTime endWorkingHour;

    private LocalDate launchDate;

    public Center(int id) {
        this.id = id;
    }
}
