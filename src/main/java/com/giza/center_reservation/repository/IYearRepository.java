package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.YearEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface IYearRepository extends JpaRepository<YearEntity, String> {

//    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity) as remainingCapacity from YearEntity h where h.centerId = :centerId and h.name = :name")
//    RemainingCapacityModel findRemainingCapacity(int centerId, String name);
}
