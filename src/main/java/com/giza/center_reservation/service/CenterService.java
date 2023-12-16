package com.giza.center_reservation.service;

import com.giza.center_reservation.entities.Center;
import com.giza.center_reservation.entities.HourEntity;
import com.giza.center_reservation.entities.PackageEntity;
import com.giza.center_reservation.entities.WorkingDay;
import com.giza.center_reservation.enumeration.PackageType;
import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
import com.giza.center_reservation.model.*;
import com.giza.center_reservation.repository.ICenterRepository;
import com.giza.center_reservation.repository.IDayRepository;
import com.giza.center_reservation.repository.IHourRepository;
import com.giza.center_reservation.repository.IMonthRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    @Value("${center_number_of_additional_years}")
    private int numberOfAdditionalYears;

    @Transactional
    @SneakyThrows
    public ResourceCreated createCenter(CenterCreationModel centerCreationModel) {
        var startDate = LocalDate.now();
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
        center.setPackages(centerCreationModel.getPackages().stream().map(packageType -> new PackageEntity(packageType, center)).toList());
        center.setEndLicenseDate(startDate.plusYears(numberOfAdditionalYears));
        centerRepository.save(center);
        CompletableFuture<List<HourEntity>> hoursFuture = null;
        List<CompletableFuture<?>> futures = new ArrayList<>();
        if(centerCreationModel.getPackages().contains(PackageType.HOURS)){
            hoursFuture = CompletableFuture.supplyAsync(() -> CalenderUtil.creatHours(center, startDate, endDate));
            futures.add(hoursFuture);
        }

        var monthsFuture = CompletableFuture.supplyAsync(() -> CalenderUtil.createMonths(center, startDate, endDate));
        futures.add(monthsFuture);

        var daysFuture = CompletableFuture.supplyAsync(() -> CalenderUtil.createDays(center, startDate, endDate));
        futures.add(daysFuture);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if(hoursFuture != null){
            hourRepository.saveAll(hoursFuture.get());
        }
        monthRepository.saveAll(monthsFuture.get());
        dayRepository.saveAll(daysFuture.get());
        return new ResourceCreated(center.getId());
    }
    @Transactional
    public ResourceUpdated updateCapacity(UpdateCapacityModel updateCapacityModel) {
        var center = centerRepository.findById(updateCapacityModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateCapacityModel.getCenterId()));
        if (!updateCapacityModel.isEvening()) {
            var deltaCapacity = updateCapacityModel.getCapacity() - center.getMaxCapacity();
            center.setMaxCapacity(updateCapacityModel.getCapacity());
            hourRepository.updateCapacity(center.getId(), deltaCapacity);
        } else {
            var deltaCapacity = updateCapacityModel.getCapacity() - center.getEveningMaxCapacity();
            center.setEveningMaxCapacity(updateCapacityModel.getCapacity());
            hourRepository.updateCapacityEvening(center.getId(), deltaCapacity);
        }
        centerRepository.save(center);
        return new ResourceUpdated(true);
    }

    @Transactional
    public ResourceUpdated updateWorkingHours(UpdateWorkingHoursModel updateWorkingHoursModel) {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        var center = centerRepository.findById(updateWorkingHoursModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateWorkingHoursModel.getCenterId()));
        var start2 = updateWorkingHoursModel.getStartTime();
        var end2 = updateWorkingHoursModel.getEndTime();
        var start1 = updateWorkingHoursModel.isEvening() ? center.getEveningStartWorkingHour() : center.getStartWorkingHour();
        var end1 = updateWorkingHoursModel.isEvening() ? center.getEveningEndWorkingHour() : center.getEndWorkingHour();
        List<Pair<LocalTime, LocalTime>> intervalsToBeAdded = new ArrayList<>();

        if (end2.isBefore(start1) || start2.isAfter(end1)) {
            intervalsToBeAdded.add(Pair.of(start2, end2));
        } else if (start2.isBefore(start1) && end2.isAfter(end1)) {
            intervalsToBeAdded.add(Pair.of(start2, start1));
            intervalsToBeAdded.add(Pair.of(end1, end2));
        } else if (start2.isBefore(start1)) {

            intervalsToBeAdded.add(Pair.of(start2, start1));

        } else if (end2.isAfter(end1)) {
            intervalsToBeAdded.add(Pair.of(end1, end2));
        }
        center.setStartWorkingHour(updateWorkingHoursModel.getStartTime());
        center.setEndWorkingHour(updateWorkingHoursModel.getEndTime());
        centerRepository.save(center);

        List<CompletableFuture<?>> completableFutures = new ArrayList<>();
        List<HourEntity> hourEntities = new ArrayList<>();

        intervalsToBeAdded.forEach(localTimeLocalTimePair -> completableFutures.add(CompletableFuture.runAsync(() -> {
            var startDate = LocalDate.now();
            var endDate = center.getEndLicenseDate();

            hourEntities.addAll(CalenderUtil.creatHours(center, startDate, endDate, localTimeLocalTimePair.getFirst(), localTimeLocalTimePair.getSecond(), updateWorkingHoursModel.isEvening()));

        })));
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        if (!hourEntities.isEmpty()) {
            hourRepository.saveAll(hourEntities);
        }
        hourRepository.deleteOldWorkingHours(center.getId(), start2, end2, updateWorkingHoursModel.isEvening());
        var nextWorkingDate = CalenderUtil.getTheNextWorkingDate(LocalDate.now(), center.getWorkingDays().stream().map(WorkingDay::getName).toList());
        hourRepository.updateTreeState(center.getId(), CalenderUtil.getDayId(nextWorkingDate.atTime(0, 0), center.getId()), updateWorkingHoursModel.isEvening());

        return new ResourceUpdated(true);

    }

    @Transactional
    @SneakyThrows
    public ResourceUpdated updateWorkingDays(UpdateWorkingDays updateWorkingHoursModel) {
        var center = centerRepository.findById(updateWorkingHoursModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateWorkingHoursModel.getCenterId()));
        var oldWorkingDays = center.getWorkingDays().stream().map(WorkingDay::getName).collect(Collectors.toSet());
        var removedWorkingDays = new HashSet<>(oldWorkingDays);
        removedWorkingDays.removeAll(updateWorkingHoursModel.getDays());

        var addedWorkingDays = new HashSet<>(updateWorkingHoursModel.getDays());
        addedWorkingDays.removeAll(oldWorkingDays);

        center.updateWorkingDays(updateWorkingHoursModel.getDays());


        if (!removedWorkingDays.isEmpty()) {
            dayRepository.deleteRemovedWorkingDays(center.getId(), removedWorkingDays);
            dayRepository.updateTreeState(center.getId(), CalenderUtil.getMonthId(LocalDate.now().atTime(0, 0), center.getId()), center.getEveningMaxCapacity(), true);
            dayRepository.updateTreeState(center.getId(), CalenderUtil.getMonthId(LocalDate.now().atTime(0, 0), center.getId()), center.getMaxCapacity(), false);

        }

        if (!addedWorkingDays.isEmpty()) {
            var startDate = LocalDate.now();
            var endDate = center.getEndLicenseDate();
            CompletableFuture<List<HourEntity>> hoursFuture = null;

            var daysFuture = CompletableFuture.supplyAsync(() -> CalenderUtil.createDays(center, startDate, endDate));

            List<CompletableFuture<?>> futures = new ArrayList<>();
            futures.add(daysFuture);

            if(center.getPackages().stream().anyMatch(packageEntity -> packageEntity.getType().equals(PackageType.HOURS))){
                hoursFuture = CompletableFuture.supplyAsync(() -> CalenderUtil.creatHours(center, startDate, endDate));
                futures.add(hoursFuture);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            if(hoursFuture != null){
                hourRepository.saveAll(hoursFuture.get());
            }

            dayRepository.saveAll(daysFuture.get());
        }

        centerRepository.save(center);

        return new ResourceUpdated(true);

    }
}
