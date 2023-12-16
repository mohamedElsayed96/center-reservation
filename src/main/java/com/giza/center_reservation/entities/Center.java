package com.giza.center_reservation.entities;

import com.giza.center_reservation.enumeration.PackageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
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

    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<WorkingDay> workingDays;

    @OneToMany(mappedBy = "center", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PackageEntity> packages;

    public void updateWorkingDays(Set<DayOfWeek> dayOfWeeks){
       workingDays.clear();
       dayOfWeeks.forEach(dayOfWeek -> workingDays.add(new WorkingDay(dayOfWeek, this)));

    }
    public void updatePackages(Set<PackageType> packageTypes){
        packages.clear();
        packageTypes.forEach(packageType -> packages.add(new PackageEntity(packageType, this)));
    }

}
