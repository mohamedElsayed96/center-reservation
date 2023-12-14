package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.DayEntity;
import com.giza.center_reservation.entities.MonthEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface IDayRepository extends JpaRepository<DayEntity, Long> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity, h.remainingEveningCapacity) from DayEntity h where h.id=:id")
    RemainingCapacityModel findRemainingCapacity(Long id);
    @Query("select h.id from DayEntity h where h.monthId in :monthIds")
    List<Long> findRemainingCapacityForMonths(List<Long> monthIds);

    @Modifying
    void deleteAllByCenterIdAndNameIn(int centerId, Set<DayOfWeek> name);

    @Procedure("update_tree_state_from_day_node")
    void updateTreeState(int centerId, long startId, boolean evening);
}
