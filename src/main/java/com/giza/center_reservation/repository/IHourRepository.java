package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.HourEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IHourRepository extends JpaRepository<HourEntity, Long> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity, h.remainingEveningCapacity) from HourEntity h where h.id=:id and h.remainingCapacity >= 0")
    Optional<RemainingCapacityModel> findRemainingCapacity(long id);
    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity, h.remainingEveningCapacity) from HourEntity h where h.id=:id and h.remainingEveningCapacity >= 0")
    Optional<RemainingCapacityModel> findRemainingCapacityEvening(long id);

    @Query("select h.id from HourEntity h where h.dayId in :dayIds and h.remainingCapacity >= 0")
    List<Long> findRemainingCapacityForDay(List<Long> dayIds);
    @Query("select h.id from HourEntity h where h.dayId in :dayIds and h.remainingEveningCapacity >= 0")
    List<Long> findRemainingCapacityForDayEvening(List<Long> dayIds);

    @Query("select COUNT(*) from HourEntity h where h.id >= :startId and h.id <= :endId and ((:evening=true and h.remainingEveningCapacity =0) or (:evening=false and h.remainingCapacity =0 ))")
    long checkAvailableCapacityInPeriod(long startId, long endId, boolean evening);

    @Query("select h.id from HourEntity h where h.id >= :startId and h.id <= :endId")
    List<Long> selectHoursIdsInPeriod(long startId, long endId);

    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = h.remainingCapacity - 1 where h.id in :hourIds")
    void decreaseRemainingCapacity(List<Long> hourIds);

    @Modifying
    @Query("update HourEntity h set h.remainingEveningCapacity = h.remainingEveningCapacity - 1 where h.id in :hourIds")
    void decreaseEveningRemainingCapacity(List<Long> hourIds);


    @Modifying
    @Query("delete HourEntity h where h.centerId=:centerId and (h.time < :start or h.time > :end) and ((:evening=true and h.remainingEveningCapacity >=0) or (:evening=false and h.remainingCapacity >=0 ))")
    void deleteOldWorkingHours(int centerId, LocalTime start, LocalTime end, boolean evening);

    @Modifying
    @Query("update HourEntity h set h.remainingCapacity = (case when h.remainingCapacity + :delta < 0 then 0 else (h.remainingCapacity + :delta) end) where h.centerId = :centerId and h.remainingCapacity >= 0")
    void updateCapacity(int centerId, int delta);
    @Modifying
    @Query("update HourEntity h set h.remainingEveningCapacity = (case when h.remainingEveningCapacity + :delta < 0 then 0 else (h.remainingEveningCapacity + :delta) end) where h.centerId = :centerId and  h.remainingEveningCapacity >= 0")
    void updateCapacityEvening(int centerId, int delta);

    @Procedure("update_tree_state_from_hour_node")
    void updateTreeState(int centerId, long startId, boolean evening);

}
