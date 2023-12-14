package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int maxCapacity;

    private int eveningMaxCapacity;

    private LocalTime startWorkingHour;

    private LocalTime endWorkingHour;

    private LocalTime eveningStartWorkingHour;
    private LocalTime eveningEndWorkingHour;
    private LocalDate endLicenseDate;
    private LocalDateTime createdDate;
    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL)
    private List<WorkingDay> workingDays;

}
