package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.CenterReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICenterReservationRepository extends JpaRepository<CenterReservation, Integer> {
}
