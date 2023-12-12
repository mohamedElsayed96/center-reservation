package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.HourEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface IHourRepository extends JpaRepository<HourEntity, String> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity) from HourEntity h where h.id=:id")
    RemainingCapacityModel findRemainingCapacity(Long id);

    @Query("select h.id from HourEntity h where h.dayId in :dayIds and h.remainingCapacity >= 0")
    List<Long> findRemainingCapacityForDay(List<Long> dayIds);


    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = h.remainingCapacity - 1 where h.id in :hourIds")
    void decreaseRemainingCapacity(List<Long> hourIds);
    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = -1  where h.centerId = :centerId and (h.time < :start or h.time > :end) and h.remainingCapacity >= 0")
    void updateWorkingHours(int centerId, LocalTime start, LocalTime end);

    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = (case when h.remainingCapacity + :delta < 0 then 0 else (h.remainingCapacity + :delta) end) where h.centerId = :centerId and h.remainingCapacity >= 0")
    void updateCapacity(int centerId, int delta);

    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = :maxCapacity where h.centerId = :centerId and h.time >= :start and h.time <= :end and h.remainingCapacity < 0")
    void setNewWorkingHours(int centerId, int maxCapacity , LocalTime start, LocalTime end);


}
