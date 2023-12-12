package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "calender_month")
@Data
public class MonthEntity {
    @Id
    private Long id;
    private String name;
    @ManyToOne
    private Center center;

    private int remainingCapacity;
    @ManyToOne
    private YearEntity year;

    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

    @Column(name = "year_id", insertable = false, updatable = false)
    private Long yearId;

    @OneToMany(mappedBy = "month", cascade = CascadeType.ALL)
    private List<DayEntity> days;

}
