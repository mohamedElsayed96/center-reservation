package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "calender_hour")
@NoArgsConstructor
public class HourEntity {
    @Id
    private long id;

    private LocalTime time;

    private int remainingCapacity;

    private int remainingEveningCapacity;

    @Column(name = "center_id")
    private int centerId;

    @Column(name = "day_id")
    private long dayId;
}
