package com.giza.center_reservation.service;

import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
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

    @Transactional
    public String reserveHour(ReservationModel hourReservationModel) {
        var hours = checkHours(hourReservationModel);
        saveHourReservation(hours);
        return "success";


    }

    @Transactional
    public String reserveDays(ReservationModel dayReservationModel) {
        var daysDb = checkDays(dayReservationModel);
        saveDaysReservation(daysDb);
        return "success";

    }


    @Transactional
    public String reserveMonths(ReservationModel monthReservationModel) {
        var daysIds = checkMonths(monthReservationModel);
        saveDaysReservation(daysIds);
        return "success";
    }

    private List<Long> checkMonths(ReservationModel monthReservationModel) {
        var originalStartTime = monthReservationModel.getStartTime().toLocalDate();
        var originalEndTime = originalStartTime.plusMonths(monthReservationModel.getNumberOfAdditionalUnits());
        var startTime = originalStartTime;
        if(startTime.isBefore(LocalDate.now())){
            throw new RuntimeBusinessException("the selected day " + startTime.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }
        var dayIds = new ArrayList<Long>();
        var numberOfIteration = monthReservationModel.getNumberOfAdditionalUnits();
        if (startTime.getDayOfMonth() > 1) {
            var d = startTime.with(TemporalAdjusters.firstDayOfNextMonth());
            Period period = Period.between(startTime, d);
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startTime.atTime(0, 0, 0), period.getDays(), monthReservationModel.getCustomerName())));
            numberOfIteration--;
            startTime = d;
        }
        var monthIds = new ArrayList<Long>();
        for (int i = 1; i <= numberOfIteration; i++) {
            var monthDb = monthRepository.findRemainingCapacity(CalenderUtil.getMonthId(startTime.atTime(0, 0, 0), monthReservationModel.getCenterId()));
            if (monthDb.getRemainingCapacity() <= 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected month " + startTime.format(DateTimeFormatter.ISO_DATE));
            }
            monthIds.add(monthDb.getId());
            startTime = startTime.plusMonths(1);
        }

        if (startTime.isBefore(originalEndTime)) {
            Period period = Period.between(startTime, originalEndTime.plusDays(1));
            dayIds.addAll(checkDays(new ReservationModel(monthReservationModel.getCenterId(), startTime.atTime(0, 0, 0), period.getDays(), monthReservationModel.getCustomerName())));
        }

        dayIds.addAll(dayRepository.findRemainingCapacityForMonths(monthIds));

        return dayIds;


    }


    private List<Long> checkDays(ReservationModel dayReservationModel) {
        var startTime = dayReservationModel.getStartTime().toLocalDate();
        if(startTime.isBefore(LocalDate.now())){
            throw new RuntimeBusinessException("the start date " + startTime.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }
        var response = new ArrayList<Long>();
        for (int i = 1; i <= dayReservationModel.getNumberOfAdditionalUnits(); i++) {
            var dayOfTheMonthDb = dayRepository.findRemainingCapacity(CalenderUtil.getDayId(startTime.atTime(0, 0), dayReservationModel.getCenterId()));
            if (dayOfTheMonthDb.getRemainingCapacity() == -1) {
                log.info("the selected day {} is not a working day ", startTime.format(DateTimeFormatter.ISO_DATE));
                continue;
            }
            if (dayOfTheMonthDb.getRemainingCapacity() == 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected day " + startTime.format(DateTimeFormatter.ISO_DATE));
            }
            response.add(dayOfTheMonthDb.getId());
            startTime = startTime.plusDays(1);
        }
        return response;
    }

    private List<Long> checkHours(ReservationModel dayReservationModel) {
        var startTime = dayReservationModel.getStartTime();
        if(startTime.isBefore(LocalDateTime.now())){
            throw new RuntimeBusinessException("the start date " + startTime.format(DateTimeFormatter.ISO_DATE) + " is in the past");
        }
        var hourIds = new ArrayList<Long>();
        //Duration validation
        for (int i = 1; i <= dayReservationModel.getNumberOfAdditionalUnits(); i++) {
            var hourDb = hourRepository.findRemainingCapacity(CalenderUtil.getHourId(startTime, dayReservationModel.getCenterId()));
            if (hourDb.getRemainingCapacity() == -1) {
                throw new RuntimeBusinessException("the selected hour " + startTime.toLocalTime().format(DateTimeFormatter.ISO_TIME) + " Is not a working hour");
            }
            if (hourDb.getRemainingCapacity() == 0) {
                throw new RuntimeBusinessException("No Enough Capacity in the selected hour " + startTime.toLocalTime().format(DateTimeFormatter.ISO_TIME));
            }
            hourIds.add(hourDb.getId());
            startTime = startTime.plusHours(1);
        }
        return hourIds;
    }

    private void saveDaysReservation(List<Long> daysIds) {
        List<Long> hours = hourRepository.findRemainingCapacityForDay(daysIds);
        saveHourReservation(hours);
    }

    private void saveHourReservation(List<Long> hoursId) {
        hourRepository.decreaseRemainingCapacity(hoursId);
    }

}
