package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface ICenterRepository extends JpaRepository<Center, Integer> {
    @Modifying
    @Query("update Center c set c.startWorkingHour=:start, c.endWorkingHour=:end where c.id=:id")
    void updateWorkingHour(LocalTime start, LocalTime end, int id);

}
