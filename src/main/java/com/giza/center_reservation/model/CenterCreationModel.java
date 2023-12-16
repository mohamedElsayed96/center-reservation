package com.giza.center_reservation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.giza.center_reservation.enumeration.PackageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class CenterCreationModel {
    private String name;
    private int maxCapacity;
    private int eveningMaxCapacity;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime endTime;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime eveningStartTime;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime eveningEndTime;
    private Set<DayOfWeek> workingDates;
    private Set<PackageType> packages;
}
