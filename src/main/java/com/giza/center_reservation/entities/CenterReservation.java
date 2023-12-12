package com.giza.center_reservation.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reservation")
@NoArgsConstructor
public class CenterReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @SequenceGenerator(name = "seq", sequenceName = "center_reservation_id_seq", allocationSize = 25000)
    private long id;

    @ManyToOne
    private Center center;

    private String customerName;

    @ManyToOne
    private HourEntity hour;

    public CenterReservation(Center center, String customerName, HourEntity hour) {
        this.center = center;
        this.customerName = customerName;
        this.hour = hour;
    }
}
