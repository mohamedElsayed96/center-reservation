package com.giza.center_reservation.service;

import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
import com.giza.center_reservation.model.RemainingCapacityModel;
import com.giza.center_reservation.model.ReservationModel;
import com.giza.center_reservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
        saveHourReservation(hours, hourReservationModel.isEvening());
        return "success";


    }

    @Transactional
    public String reserveDays(ReservationModel dayReservationModel) {
        var daysDb = checkDays(dayReservationModel);
        saveDaysReservation(daysDb, dayReservationModel.isEvening());
        return "success";

    }


    @Transactional
    public String reserveMonths(ReservationModel monthReservationModel) {
        var daysIds = checkMonths(monthReservationModel);
        saveDaysReservation(daysIds, monthReservationModel.isEvening());
        return "success";
    }

    private List<Long> checkMonths(ReservationModel monthReservationModel) {
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
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startDate.atTime(0, 0), tempEndDate.atTime(0, 0), monthReservationModel.getCustomerName(), monthReservationModel.isEvening())));
            startDate = tempEndDate.plusDays(1);
        }
        var monthIds = new ArrayList<Long>();
        for (var start = startDate; start.isBefore(monthEndDate) || start.equals(monthEndDate); start = start.plusMonths(1)) {
            var monthDb = monthRepository.findRemainingCapacity(CalenderUtil.getMonthId(start.atTime(0, 0, 0), monthReservationModel.getCenterId()));
            if (!monthReservationModel.isEvening() && monthDb.getRemainingCapacity() <= 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected month " + start.format(DateTimeFormatter.ISO_DATE));
            }
            if (monthReservationModel.isEvening() && monthDb.getEveningRemainingCapacity() <= 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected month " + start.format(DateTimeFormatter.ISO_DATE));
            }
            monthIds.add(monthDb.getId());
        }
        if (startDate.isBefore(originalEndDate)) {
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startDate.atTime(0, 0), originalEndDate.atTime(0, 0), monthReservationModel.getCustomerName(), monthReservationModel.isEvening())));
        }
        dayIds.addAll(dayRepository.findRemainingCapacityForMonths(monthIds));
        return dayIds;


    }


    private List<Long> checkDays(ReservationModel dayReservationModel) {
        var center = centerRepository.findById(dayReservationModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + dayReservationModel.getCenterId()));
        var startDate = dayReservationModel.getStartTime().toLocalDate();
        var endDate = dayReservationModel.getEndTime().toLocalDate();

        if (endDate.isAfter(center.getEndLicenseDate())) {
            throw new RuntimeBusinessException("the selected day " + startDate.format(DateTimeFormatter.ISO_DATE) + " is after the licence end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeBusinessException("the start date " + startDate.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }

        var response = new ArrayList<Long>();

        for (var start = startDate; start.isBefore(endDate) || start.equals(endDate); start = start.plusDays(1)) {
            var dayOfTheMonthDb = dayRepository.findRemainingCapacity(CalenderUtil.getDayId(start.atTime(0, 0), dayReservationModel.getCenterId()));
            if (dayOfTheMonthDb == null) {
                log.info("the selected day {} is not a working day ", start.format(DateTimeFormatter.ISO_DATE));
                continue;
            }
            if (dayReservationModel.isEvening() && dayOfTheMonthDb.getEveningRemainingCapacity() == 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected day " + start.format(DateTimeFormatter.ISO_DATE));
            }
            if (!dayReservationModel.isEvening() && dayOfTheMonthDb.getRemainingCapacity() == 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected day " + start.format(DateTimeFormatter.ISO_DATE));
            }
            response.add(dayOfTheMonthDb.getId());
        }
        return response;
    }

    private List<Long> checkHours(ReservationModel dayReservationModel) {
        var center = centerRepository.findById(dayReservationModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + dayReservationModel.getCenterId()));
        if (dayReservationModel.getStartTime().toLocalDate().isAfter(center.getEndLicenseDate())) {
            throw new RuntimeBusinessException("the selected day " + dayReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE) + " is after the licence end date");
        }
        if (dayReservationModel.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeBusinessException("the start date " + dayReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME) + " is in the past");
        }
        if (!dayReservationModel.getStartTime().toLocalDate().equals(dayReservationModel.getEndTime().toLocalDate())) {
            throw new RuntimeBusinessException("the start time " + dayReservationModel.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME) + " and end time " + dayReservationModel.getEndTime().format(DateTimeFormatter.ISO_DATE_TIME) + " is not in the same day.");
        }

        var hourIds = new ArrayList<Long>();
        //Duration validation
        for (var start = dayReservationModel.getStartTime(); start.isBefore(dayReservationModel.getEndTime()) || start.equals(dayReservationModel.getEndTime()); start = start.plusHours(1)) {
            RemainingCapacityModel hourDb;
            if (dayReservationModel.isEvening()) {
                LocalDateTime finalStart = start;
                hourDb = hourRepository.findRemainingCapacityEvening(CalenderUtil.getHourId(start, dayReservationModel.getCenterId())).orElseThrow(() -> new RuntimeBusinessException("the selected hour " + finalStart.format(DateTimeFormatter.ISO_TIME) + " Is not a working hour"));
                if (hourDb.getEveningRemainingCapacity() == 0) {
                    throw new RuntimeBusinessException("No Enough Capacity in the selected hour " + start.format(DateTimeFormatter.ISO_TIME));
                }
            } else {
                LocalDateTime finalStart = start;
                hourDb = hourRepository.findRemainingCapacity(CalenderUtil.getHourId(start, dayReservationModel.getCenterId())).orElseThrow(() -> new RuntimeBusinessException("the selected hour " + finalStart.format(DateTimeFormatter.ISO_TIME) + " Is not a working hour"));

                if (hourDb.getRemainingCapacity() == 0) {
                    throw new RuntimeBusinessException("No Enough Capacity in the selected hour " + start.format(DateTimeFormatter.ISO_TIME));
                }
            }

            hourIds.add(hourDb.getId());
        }
        return hourIds;
    }

    private void saveDaysReservation(List<Long> daysIds, boolean evening) {
        List<Long> hours;
        if (evening) {
            hours = hourRepository.findRemainingCapacityForDayEvening(daysIds);

        } else {
            hours = hourRepository.findRemainingCapacityForDay(daysIds);
        }
        saveHourReservation(hours, evening);
    }

    private void saveHourReservation(List<Long> hoursId, boolean evening) {
        if (evening)
            hourRepository.decreaseEveningRemainingCapacity(hoursId);
        else
            hourRepository.decreaseRemainingCapacity(hoursId);
    }

}
