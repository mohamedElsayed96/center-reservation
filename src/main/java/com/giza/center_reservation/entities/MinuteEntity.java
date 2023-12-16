package com.giza.center_reservation.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "calender_minute")
@Data
public class MinuteEntity {
    @Id
    private long id;

    private LocalTime time;

    private int remainingCapacity;

    private int remainingEveningCapacity;

    @Column(name = "center_id")
    private int centerId;

    @Column(name = "day_id")
    private long hourId;
}
