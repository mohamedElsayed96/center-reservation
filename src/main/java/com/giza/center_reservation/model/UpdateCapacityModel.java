package com.giza.center_reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UpdateCapacityModel {
    @JsonIgnore
    private int centerId;
    private int capacity;
}
