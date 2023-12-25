package com.giza.center_reservation.service;

import com.giza.center_reservation.entities.Center;
import com.giza.center_reservation.enumeration.PackageType;
import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
import com.giza.center_reservation.model.ReservationModel;
import com.giza.center_reservation.repository.ICenterRepository;
import com.giza.center_reservation.repository.IDayRepository;
import com.giza.center_reservation.repository.IHourRepository;
import com.giza.center_reservation.repository.IMonthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mohamed Eid, mohamed.eid@gizasystems.com
 * Date: Dec, 2023,
 * Description: Reservation handler.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final IHourRepository hourRepository;
    private final IMonthRepository monthRepository;
    private final IDayRepository dayRepository;
    private final ICenterRepository centerRepository;

    @Transactional
    public String reserveHour(ReservationModel hourReservationModel) {
        var hours = checkHours(hourReservationModel);
        saveHourReservation(hours.getSecond(), hourReservationModel.isEvening());
        return "success";


    }

    @Transactional
    public String reserveDays(ReservationModel dayReservationModel) {
        var daysDb = checkDays(dayReservationModel);
        saveDaysReservation(daysDb.getSecond(), daysDb.getFirst(), dayReservationModel.isEvening());
        return "success";

    }


    @Transactional
    public String reserveMonths(ReservationModel monthReservationModel) {
        var daysIds = checkMonths(monthReservationModel);
        saveDaysReservation(daysIds.getSecond(), daysIds.getFirst(), monthReservationModel.isEvening());
        return "success";
    }

    private Pair<Center, List<Long>> checkMonths(ReservationModel monthReservationModel) {
        var center = centerRepository.findById(monthReservationModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + monthReservationModel.getCenterId()));
        var originalStartDate = monthReservationModel.getStartTime().toLocalDate();
        var originalEndDate = monthReservationModel.getEndTime().toLocalDate();

        var startDate = originalStartDate;
        var monthEndDate = originalEndDate.with(TemporalAdjusters.firstDayOfMonth());

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeBusinessException("the selected day " + startDate.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }
        if (originalEndDate.isAfter(center.getEndLicenseDate())) {
            throw new RuntimeBusinessException("the selected day " + startDate.format(DateTimeFormatter.ISO_DATE) + " is after the licence end date");
        }
        var dayIds = new ArrayList<Long>();
        if (startDate.getDayOfMonth() > 1) {
            var tempEndDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startDate.atTime(0, 0), tempEndDate.atTime(0, 0), monthReservationModel.getCustomerName(), monthReservationModel.isEvening())).getSecond());
            startDate = tempEndDate.plusDays(1);
        }
        var monthIds = new ArrayList<Long>();
        for (var start = startDate; start.isBefore(monthEndDate); start = start.plusMonths(1)) {
            var monthDb = monthRepository.findRemainingCapacity(CalenderUtil.getMonthId(start.atTime(0, 0, 0), monthReservationModel.getCenterId()));
            if (!monthReservationModel.isEvening() && monthDb.getRemainingCapacity() <= 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected month " + start.format(DateTimeFormatter.ISO_DATE));
            }
            if (monthReservationModel.isEvening() && monthDb.getEveningRemainingCapacity() <= 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected month " + start.format(DateTimeFormatter.ISO_DATE));
            }
            monthIds.add(monthDb.getId());
            startDate = startDate.plusMonths(1);
        }
        if (startDate.isBefore(originalEndDate) || startDate.equals(originalEndDate) ) {
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startDate.atTime(0, 0), originalEndDate.atTime(0, 0), monthReservationModel.getCustomerName(), monthReservationModel.isEvening())).getSecond());
        }
        dayIds.addAll(dayRepository.findRemainingCapacityForMonths(monthIds));
        return Pair.of(center, dayIds);


    }


    private Pair<Center, List<Long>> checkDays(ReservationModel dayReservationModel) {
        var center = centerRepository.findById(dayReservationModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + dayReservationModel.getCenterId()));
        var startDate = dayReservationModel.getStartTime().toLocalDate();
        var endDate = dayReservationModel.getEndTime().toLocalDate();

        if (endDate.isAfter(center.getEndLicenseDate())) {
            throw new RuntimeBusinessException("the selected day " + startDate.format(DateTimeFormatter.ISO_DATE) + " is after the licence end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeBusinessException("the start date " + startDate.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }

        long startId = CalenderUtil.getDayId(dayReservationModel.getStartTime(), dayReservationModel.getCenterId());
        long endId = CalenderUtil.getDayId(dayReservationModel.getEndTime(), dayReservationModel.getCenterId());

        var result = dayRepository.checkAvailableCapacityInPeriod(startId, endId, dayReservationModel.isEvening());

        if (result > 0) {
            throw new RuntimeBusinessException("No Enough Capacity in the selected period ");
        }

        return Pair.of(center, dayRepository.selectDaysIdsInPeriod(startId, endId));
    }

    private Pair<Center, List<Long>> checkHours(ReservationModel hoursReservationModel) {
        var center = centerRepository.findById(hoursReservationModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + hoursReservationModel.getCenterId()));
        if (hoursReservationModel.getStartTime().toLocalDate().isAfter(center.getEndLicenseDate())) {
            throw new RuntimeBusinessException("the selected day " + hoursReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE) + " is after the licence end date");
        }
        if (hoursReservationModel.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeBusinessException("the start date " + hoursReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME) + " is in the past");
        }
        if (!hoursReservationModel.getStartTime().toLocalDate().equals(hoursReservationModel.getEndTime().toLocalDate())) {
            throw new RuntimeBusinessException("the start time " + hoursReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME) + " and end time " + hoursReservationModel.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME) + " is not in the same day.");
        }
        boolean inWorkingHours = isInWorkingHours(hoursReservationModel, center);

        if (!inWorkingHours) {
            throw new RuntimeBusinessException("the selected period Is not in the working hours");
        }
        long startId = CalenderUtil.getHourId(hoursReservationModel.getStartTime(), hoursReservationModel.getCenterId());
        long endId = CalenderUtil.getHourId(hoursReservationModel.getEndTime().minusHours(1), hoursReservationModel.getCenterId());

        var result = hourRepository.checkAvailableCapacityInPeriod(startId, endId, hoursReservationModel.isEvening());

        if (result > 0) {
            throw new RuntimeBusinessException("No Enough Capacity in the selected period ");
        }

        return Pair.of(center, hourRepository.selectHoursIdsInPeriod(startId, endId));

    }

    private static boolean isInWorkingHours(ReservationModel hoursReservationModel, Center center) {
        boolean isEvening = hoursReservationModel.isEvening();
        LocalTime startTime = hoursReservationModel.getStartTime().toLocalTime();
        LocalTime endTime = hoursReservationModel.getEndTime().toLocalTime();
        LocalTime start;
        LocalTime end;

        if (isEvening) {
            start = center.getEveningStartWorkingHour();
            end = center.getEveningEndWorkingHour();
        } else {
            start = center.getStartWorkingHour();
            end = center.getEndWorkingHour();
        }

        return (startTime.isAfter(start) || startTime.equals(start)) && (endTime.isBefore(end) || endTime.equals(end));
    }

    private void saveDaysReservation(List<Long> daysIds, Center center, boolean evening) {
        if (center.getPackages().stream().anyMatch(packageEntity -> packageEntity.getType().equals(PackageType.HOURS))) {
            List<Long> hours;
            if (evening) {
                hours = hourRepository.findRemainingCapacityForDayEvening(daysIds);

            } else {
                hours = hourRepository.findRemainingCapacityForDay(daysIds);
            }
            saveHourReservation(hours, evening);
        } else {
            if (evening) {
                dayRepository.decreaseEveningRemainingCapacity(daysIds);

            } else {
                dayRepository.decreaseRemainingCapacity(daysIds);
            }
        }

    }

    private void saveHourReservation(List<Long> hoursId, boolean evening) {
        if (evening)
            hourRepository.decreaseEveningRemainingCapacity(hoursId);
        else
            hourRepository.decreaseRemainingCapacity(hoursId);
    }

}
