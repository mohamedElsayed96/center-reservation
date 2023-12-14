package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "calender_month")
@Data
public class MonthEntity {
    @Id
    private long id;
    private String name;
    @ManyToOne
    private Center center;

    private int remainingCapacity;

    private int remainingEveningCapacity;

    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

}
