package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Table(name = "calender_year")
@Entity
@Data
public class YearEntity {
    @Id
    private long id;
    private String name;
    @ManyToOne()
    private Center center;
    @Column(name = "center_id", insertable = false, updatable = false)
    private int centerId;

    private int remainingCapacity;
    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL)
    private List<MonthEntity> months;


}
