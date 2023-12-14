package com.giza.center_reservation.model;

import lombok.Data;

import java.time.DayOfWeek;
import java.util.Set;

@Data
public class UpdateWorkingDays {
    private int centerId;
    private Set<DayOfWeek> days;
}
