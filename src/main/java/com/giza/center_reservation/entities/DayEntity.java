package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "calender_day")
@Data
public class DayEntity {
    @Id
    private long id;
    @Enumerated(EnumType.STRING)
    private DayOfWeek name;

    private int dayOfTheMonth;

    private int remainingCapacity;

    private int remainingEveningCapacity;

    @Column(name = "center_id")
    private int centerId;

    @Column(name = "month_id")
    private long monthId;

}
