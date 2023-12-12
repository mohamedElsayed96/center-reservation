package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "calender_day")
@Data
public class DayEntity {
    @Id
    private long id;
    private String name;
    private int dayOfTheMonth;
    @ManyToOne
    private  Center center;

    private int remainingCapacity;

    @ManyToOne
    private MonthEntity month;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL)
    private List<HourEntity> workingHours;

    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

    @Column(name = "month_id", insertable = false, updatable = false)
    private Long monthId;

}
