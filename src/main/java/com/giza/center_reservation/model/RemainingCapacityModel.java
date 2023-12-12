package com.giza.center_reservation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemainingCapacityModel {
    private Long id;
    private int remainingCapacity;
}
