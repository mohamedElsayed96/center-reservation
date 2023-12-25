package com.giza.center_reservation.infrastructure;

import com.giza.center_reservation.entities.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Author: Mohamed Eid, mohamed.eid@gizasystems.com
 * Date: Dec, 2023,
 * Description: Calendar Utility class.
 */

public class CalenderUtil {
    private CalenderUtil() {
    }



    public static List<MonthEntity> createMonths(Center center, LocalDate startDate, LocalDate endDate) {



        List<MonthEntity> months = new ArrayList<>();

        for (var start = startDate; start.isBefore(endDate) || start.equals(endDate); start = start.plusMonths(1)) {
            MonthEntity month = new MonthEntity();
            var yearId = center.getId() + "" + start.getYear();
            var monthId = yearId + String.format("%02d", start.getMonthValue());
            month.setName(getMonthName(start.getMonthValue(), start.getYear()));
            month.setRemainingCapacity(center.getMaxCapacity());
            month.setRemainingEveningCapacity(center.getEveningMaxCapacity());
            month.setCenterId(center.getId());
            month.setId(Long.parseLong(monthId));
            months.add(month);
        }


        return months;
    }

    public static List<DayEntity> createDays(Center center, LocalDate startDate, LocalDate endDate, List<DayOfWeek> workingDays) {


        List<DayEntity> dayEntities = new ArrayList<>();
        for (var start = startDate; start.isBefore(endDate) || start.equals(endDate); start = start.plusDays(1)) {
            var yearId = center.getId() + "" + start.getYear();
            var monthId = yearId + String.format("%02d", start.getMonthValue());
            var dayName = start.getDayOfWeek();
            if (!workingDays.contains(dayName)) {
                continue;
            }
            DayEntity day = new DayEntity();
            day.setDayOfTheMonth(start.getDayOfMonth());
            day.setName(dayName);
            day.setCenterId(center.getId());
            day.setRemainingCapacity(center.getMaxCapacity());
            day.setRemainingEveningCapacity(center.getEveningMaxCapacity());
            var dayId = monthId + String.format("%02d", start.getDayOfMonth());
            day.setId(Long.parseLong(dayId));
            day.setMonthId(Long.parseLong(monthId));
            dayEntities.add(day);

        }
        return dayEntities;
    }

    public static List<HourEntity> creatHours(Center center, LocalDate startDate, LocalDate endDate, List<DayOfWeek> workingDays) {
        List<HourEntity> response = creatHours(center, startDate, endDate, center.getStartWorkingHour(), center.getEndWorkingHour(), workingDays,false);
        response.addAll(creatHours(center, startDate, endDate, center.getEveningStartWorkingHour(), center.getEveningEndWorkingHour(), workingDays,true));
        return response;
    }

    public static List<HourEntity> creatHours(Center center, LocalDate startDate, LocalDate endDate, LocalTime startWorkingHour, LocalTime endWorkingHour, List<DayOfWeek> workingDays  ,boolean evening) {
        List<HourEntity> hourEntities = new ArrayList<>();
        for (var start = startDate; start.isBefore(endDate) || start.equals(endDate); start = start.plusDays(1)) {
            var yearId = center.getId() + "" + start.getYear();
            var monthId = yearId + String.format("%02d", start.getMonthValue());

            var dayName = start.getDayOfWeek();
            var dayId = monthId + String.format("%02d", start.getDayOfMonth());
            if (!workingDays.contains(dayName)) {
                continue;
            }

            for (var startTime = startWorkingHour; startTime.isBefore(endWorkingHour) || startTime.equals(endWorkingHour); startTime = startTime.plusHours(1)) {
                var hour = new HourEntity();
                hour.setTime(startTime);
                hour.setCenterId(center.getId());
                var hourId = dayId + String.format("%02d", startTime.getHour());
                hour.setId(Long.parseLong(hourId));
                hour.setRemainingEveningCapacity(evening ? center.getEveningMaxCapacity() : -1);
                hour.setRemainingCapacity(evening ? -1 : center.getMaxCapacity());
                hour.setDayId(Long.parseLong(dayId));
                hourEntities.add(hour);
            }

        }
        return hourEntities;

    }

    public static LocalDate getTheNextWorkingDate(LocalDate startDate, List<DayOfWeek> workingDays){

        while (!workingDays.contains(startDate.getDayOfWeek())){
            startDate = startDate.plusDays(1);
        }
       return startDate;
    }

    public static boolean isBetween(LocalDateTime timeToCheck, LocalDateTime startTime, LocalDateTime endTime) {
        return !timeToCheck.isBefore(startTime) && !timeToCheck.isAfter(endTime);
    }

    public static DayOfWeek getDayName(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        return date.getDayOfWeek();
    }

    public static String getMonthName(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.getMonth().name();
    }

    public static int getNumberOfDaysInMonth(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.lengthOfMonth();
    }

    public static Long getHourId(LocalDateTime time, int centerId) {
        return Long.parseLong(getDayId(time, centerId) + String.format("%02d", time.getHour()));
    }

    public static Long getDayId(LocalDateTime time, int centerId) {
        return Long.parseLong(getMonthId(time, centerId) + String.format("%02d", time.getDayOfMonth()));
    }

    public static Long getMonthId(LocalDateTime time, int centerId) {
        return Long.parseLong(getYearId(time, centerId) + String.format("%02d", time.getMonthValue()));
    }

    public static Long getYearId(LocalDateTime time, int centerId) {
        return Long.parseLong(centerId + "" + time.getYear());
    }

}
