package com.giza.center_reservation.repository;

import com.giza.center_reservation.entities.DayEntity;
import com.giza.center_reservation.entities.MonthEntity;
import com.giza.center_reservation.model.RemainingCapacityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IDayRepository extends JpaRepository<DayEntity, String> {

    @Query("select new com.giza.center_reservation.model.RemainingCapacityModel(h.id , h.remainingCapacity) from DayEntity h where h.id=:id")
    RemainingCapacityModel findRemainingCapacity(Long id);
    @Query("select h.id from DayEntity h where h.monthId in :monthIds and h.remainingCapacity >= 0")
    List<Long> findRemainingCapacityForMonths(List<Long> monthIds);
}
