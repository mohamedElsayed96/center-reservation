package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.DayEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Repository
public interface IDayRepository extends JpaRepository<DayEntity, Long> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity, h.remainingEveningCapacity) from DayEntity h where h.id=:id")
    RemainingCapacityModel findRemainingCapacity(Long id);
    @Query("select h.id from DayEntity h where h.monthId in :monthIds")
    List<Long> findRemainingCapacityForMonths(List<Long> monthIds);

    @Modifying
    @Query("delete from DayEntity d where d.centerId=:centerId and d.name in :names")
    void deleteRemovedWorkingDays(int centerId, Set<DayOfWeek> names);

    @Procedure("update_tree_state_from_day_node")
    void updateTreeState(int centerId, long startId, int defaultCapacity , boolean evening);

    @Query("select COUNT(*) from DayEntity h where h.id >= :startId and h.id <= :endId and ((:evening=true and h.remainingEveningCapacity =0) or (:evening=false and h.remainingCapacity =0 ))")
    long checkAvailableCapacityInPeriod(long startId, long endId, boolean evening);

    @Query("select h.id from DayEntity h where h.id >= :startId and h.id <= :endId")
    List<Long> selectHoursIdsInPeriod(long startId, long endId);


    @Modifying
    @Query("update DayEntity h set h.remainingCapacity = h.remainingCapacity - 1 where h.id in :dayIds")
    void decreaseRemainingCapacity(List<Long> dayIds);

    @Modifying
    @Query("update DayEntity h set h.remainingEveningCapacity = h.remainingEveningCapacity - 1 where h.id in :dayIds")
    void decreaseEveningRemainingCapacity(List<Long> dayIds);
}
