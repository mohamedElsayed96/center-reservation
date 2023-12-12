package com.giza.center_reservation.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ReservationInsight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    private Center center;
}
