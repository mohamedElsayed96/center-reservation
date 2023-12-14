package com.giza.center_reservation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateWorkingHoursModel {
    @JsonIgnore
    private int centerId;
    private boolean evening;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string",  pattern = "HH:mm")
    private LocalTime endTime;
}
