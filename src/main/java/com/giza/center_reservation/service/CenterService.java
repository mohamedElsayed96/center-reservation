package com.giza.center_reservation.service;

import com.giza.center_reservation.entities.Center;
import com.giza.center_reservation.entities.DayEntity;
import com.giza.center_reservation.entities.HourEntity;
import com.giza.center_reservation.entities.WorkingDay;
import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
import com.giza.center_reservation.model.*;
import com.giza.center_reservation.repository.ICenterRepository;
import com.giza.center_reservation.repository.IDayRepository;
import com.giza.center_reservation.repository.IHourRepository;
import com.giza.center_reservation.repository.IMonthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Author: Mohamed Eid, mohamed.eid@gizasystems.com
 * Date: Dec, 2023,
 * Description: Center creation and update handler.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CenterService {

    private final ICenterRepository centerRepository;

    private final IMonthRepository monthRepository;
    private final IDayRepository dayRepository;
    private final IHourRepository hourRepository;

    private final CalenderUtil calenderUtil;

    @Value("${center_number_of_additional_years}")
    private int numberOfAdditionalYears;

    public ResourceCreated createCenter(CenterCreationModel centerCreationModel) {
        var startDate = LocalDate.now().plusDays(1);
        var endDate = startDate.plusYears(numberOfAdditionalYears);
        var center = new Center();
        center.setName(centerCreationModel.getName());
        center.setMaxCapacity(centerCreationModel.getMaxCapacity());
        center.setStartWorkingHour(centerCreationModel.getStartTime());
        center.setEndWorkingHour(centerCreationModel.getEndTime());
        center.setCreatedDate(LocalDateTime.now());
        center.setEveningStartWorkingHour(centerCreationModel.getEveningStartTime());
        center.setEveningEndWorkingHour(centerCreationModel.getEveningEndTime());
        center.setEveningMaxCapacity(centerCreationModel.getEveningMaxCapacity());
        center.setWorkingDays(centerCreationModel.getWorkingDates().stream().map(dayOfWeek -> new WorkingDay(dayOfWeek, center)).toList());
        center.setEndLicenseDate(LocalDate.now().plusYears(numberOfAdditionalYears));
        centerRepository.saveAndFlush(center);
        CompletableFuture<?> completableFuture = CompletableFuture.runAsync(() -> {
            try {
                var months = calenderUtil.createMonths(center, startDate, endDate);
                monthRepository.saveAll(months);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeBusinessException(ex.getMessage());
            }

        });
        CompletableFuture<?> completableFuture2 = CompletableFuture.runAsync(() -> {
            try {
                var hours = calenderUtil.creatHours(center, startDate, endDate);
                hourRepository.saveAll(hours);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeBusinessException(ex.getMessage());
            }
        });
        CompletableFuture<?> completableFuture3 = CompletableFuture.runAsync(() -> {
            try {
                var days = calenderUtil.createDays(center, startDate, endDate);
                dayRepository.saveAll(days);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeBusinessException(ex.getMessage());
            }
        });
        CompletableFuture.allOf(completableFuture3, completableFuture, completableFuture2).join();
        return new ResourceCreated(center.getId());

    }

    @Transactional
    public ResourceUpdated updateCapacity(UpdateCapacityModel updateCapacityModel) {
        var center = centerRepository.findById(updateCapacityModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateCapacityModel.getCenterId()));

        if(!updateCapacityModel.isEvening()){
            var deltaCapacity = updateCapacityModel.getCapacity() - center.getMaxCapacity();
            center.setMaxCapacity(updateCapacityModel.getCapacity());
            hourRepository.updateCapacity(center.getId(), deltaCapacity);
        }
        else {
            var deltaCapacity = updateCapacityModel.getCapacity() - center.getEveningMaxCapacity();
            center.setEveningMaxCapacity(updateCapacityModel.getCapacity());
            hourRepository.updateCapacityEvening(center.getId(), deltaCapacity);
        }
        centerRepository.save(center);

        return new ResourceUpdated(true);

    }

    @Transactional
    public ResourceUpdated updateWorkingHours(UpdateWorkingHoursModel updateWorkingHoursModel) {
        var center = centerRepository.findById(updateWorkingHoursModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateWorkingHoursModel.getCenterId()));
        var start2 = updateWorkingHoursModel.getStartTime();
        var end2 = updateWorkingHoursModel.getEndTime();
        var start1 = updateWorkingHoursModel.isEvening() ? center.getEveningStartWorkingHour() : center.getStartWorkingHour();
        var end1 = updateWorkingHoursModel.isEvening() ? center.getEveningEndWorkingHour() : center.getEndWorkingHour();
        LocalTime intervalStart;
        LocalTime intervalEnd;

        if (end2.isBefore(start1) || start2.isAfter(end1)) {
            intervalStart = start2;
            intervalEnd = end2;
        } else if (start2.isBefore(start1)) {
            intervalStart = start2;
            intervalEnd = start1;

        } else if (end2.isAfter(end1)) {
            intervalStart = end1;
            intervalEnd = end2;
        } else {
            intervalEnd = null;
            intervalStart = null;
        }
        center.setStartWorkingHour(updateWorkingHoursModel.getStartTime());
        center.setEndWorkingHour(updateWorkingHoursModel.getEndTime());
        centerRepository.save(center);

        CompletableFuture<?> deleteRemovedHours = CompletableFuture.runAsync(() -> {
            hourRepository.deleteOldWorkingHours(center.getId(), start2, end2, updateWorkingHoursModel.isEvening());
            hourRepository.updateTreeState(center.getId(), CalenderUtil.getDayId(LocalDate.now().atTime(0, 0), center.getId()), updateWorkingHoursModel.isEvening());
        });
        CompletableFuture<?> addNewHours = CompletableFuture.runAsync(() -> {
            var startDate = LocalDate.now();
            var endDate = center.getEndLicenseDate();
            if (intervalStart != null && intervalEnd != null) {
                List<HourEntity> newHours = calenderUtil.creatHours(center, startDate, endDate, intervalStart, intervalEnd, updateWorkingHoursModel.isEvening());
                hourRepository.saveAll(newHours);
            }
        });
        CompletableFuture.allOf(deleteRemovedHours, addNewHours).join();
        return new ResourceUpdated(true);

    }

    @Transactional
    public ResourceUpdated updateWorkingDays(UpdateWorkingDays updateWorkingHoursModel) {
        var center = centerRepository.findById(updateWorkingHoursModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateWorkingHoursModel.getCenterId()));
        var oldWorkingDays = center.getWorkingDays().stream().map(WorkingDay::getName).collect(Collectors.toSet());
        var removedWorkingDays = new HashSet<>(oldWorkingDays);
        removedWorkingDays.removeAll(updateWorkingHoursModel.getDays());

        var addedWorkingDays = new HashSet<>(updateWorkingHoursModel.getDays());
        addedWorkingDays.removeAll(oldWorkingDays);
        var futures = new ArrayList<CompletableFuture<?>>();
        if (!addedWorkingDays.isEmpty()) {
            var startDate = LocalDate.now();
            var endDate = center.getEndLicenseDate();
            CompletableFuture<?> hoursFuture = CompletableFuture.runAsync(() -> {
                var hours = calenderUtil.creatHours(center, startDate, endDate);
                hourRepository.saveAll(hours);
            });
            CompletableFuture<?> daysFuture = CompletableFuture.runAsync(() -> {
                var days = calenderUtil.createDays(center, startDate, endDate);
                dayRepository.saveAll(days);
            });
            futures.add(hoursFuture);
            futures.add(daysFuture);
        }

        if (!removedWorkingDays.isEmpty()) {
            CompletableFuture<?> completableFuture = CompletableFuture.runAsync(() -> {
                dayRepository.deleteAllByCenterIdAndNameIn(center.getId(), removedWorkingDays);
                CompletableFuture<?> c = CompletableFuture.runAsync(() -> dayRepository.updateTreeState(center.getId(), CalenderUtil.getMonthId(LocalDate.now().atTime(0, 0), center.getId()), true));
                CompletableFuture<?> c2 = CompletableFuture.runAsync(() -> dayRepository.updateTreeState(center.getId(), CalenderUtil.getMonthId(LocalDate.now().atTime(0, 0), center.getId()), false));
                CompletableFuture.allOf(c, c2);

            });
            futures.add(completableFuture);
        }
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
        return new ResourceUpdated(true);

    }
}
