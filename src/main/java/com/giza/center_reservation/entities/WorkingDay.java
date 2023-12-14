package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Entity
@Data
@NoArgsConstructor
public class WorkingDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek name;

    @ManyToOne
    private Center center;

    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

    public WorkingDay(DayOfWeek name, Center center) {
        this.name = name;
        this.center = center;
    }
}
