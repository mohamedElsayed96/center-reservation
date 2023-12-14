package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.MonthEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface IMonthRepository extends JpaRepository<MonthEntity, Long> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity, h.remainingEveningCapacity) from MonthEntity h where h.id=:id")
    RemainingCapacityModel findRemainingCapacity(long id);


}
