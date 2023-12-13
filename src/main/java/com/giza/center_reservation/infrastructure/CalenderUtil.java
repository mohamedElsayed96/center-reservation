package com.giza.center_reservation.infrastructure;

import com.giza.center_reservation.entities.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CalenderUtil {
    @Value("${center_number_of_additional_years}")
    private int numberOfAdditionalYears;
    public List<YearEntity> createCalenderForCenter(Center center,
                                                    Set<LocalDate> nonWorkingDays,
                                                    LocalDate startDate) {

        List<YearEntity> yearEntities = new ArrayList<>();
        int yearNumber = startDate.getYear();
        int endYear = yearNumber + numberOfAdditionalYears;
        while(yearNumber <= endYear) {

            var year = new YearEntity();
            year.setCenter(center);
            year.setRemainingCapacity(center.getMaxCapacity());

            year.setName(yearNumber + "");

            var yearId = center.getId() + "" + yearNumber;

            year.setId(Long.parseLong(yearId));

            List<MonthEntity> months2023 = new ArrayList<>();
            for (int monthIndex = startDate.getMonthValue(); monthIndex <= 12; monthIndex++) {
                MonthEntity month = new MonthEntity();
                month.setName(getMonthName(monthIndex, yearNumber));
                month.setRemainingCapacity(year.getRemainingCapacity());
                month.setCenter(center);
                var monthId = yearId + String.format("%02d", monthIndex);
                month.setId(Long.parseLong(monthId));
                List<DayEntity> daysInMonth = new ArrayList<>();
                int daysInThisMonth = getDaysInMonth(monthIndex, yearNumber);

                for (int dayIndex = startDate.getDayOfMonth(); dayIndex <= daysInThisMonth; dayIndex++) {
                    DayEntity day = new DayEntity();
                    day.setDayOfTheMonth(dayIndex);
                    day.setName(getDayName(yearNumber, monthIndex, dayIndex));
                    day.setCenter(center);
                    day.setRemainingCapacity(month.getRemainingCapacity());
                    day.setMonth(month);
                    var dayId = monthId + String.format("%02d", dayIndex);
                    day.setId(Long.parseLong(dayId));
                    List<HourEntity> hoursOfTheDay = new ArrayList<>();

                    if (nonWorkingDays.contains(LocalDate.of(yearNumber, monthIndex, dayIndex))) {
                        day.setRemainingCapacity(-1);
                    }
                    var startTime = LocalTime.of(0, 0);
                    var endTime = LocalTime.of(23, 0);
                    while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                        var hour = new HourEntity();
                        hour.setTime(startTime);
                        hour.setDay(day);
                        hour.setCenter(center);
                        var hourId = dayId + String.format("%02d", startTime.getHour());
                        hour.setId(Long.parseLong(hourId));

                        if (day.getRemainingCapacity() < 0) {
                            hour.setRemainingCapacity(day.getRemainingCapacity());
                        } else if (isBetween(startTime, center.getStartWorkingHour(), center.getEndWorkingHour())) {
                            hour.setRemainingCapacity(center.getMaxCapacity());
                        } else {
                            hour.setRemainingCapacity(-1);
                        }

                        hoursOfTheDay.add(hour);
                        startTime = startTime.plusHours(1);
                        if (startTime.equals(LocalTime.of(0, 0))) break;
                    }
                    day.setWorkingHours(hoursOfTheDay);
                    daysInMonth.add(day);

                }
                month.setDays(daysInMonth);
                month.setYear(year);
                months2023.add(month);
            }
            year.setMonths(months2023);
            yearEntities.add(year);
            startDate = LocalDate.of(++yearNumber, 1, 1);
        }
        return yearEntities;
    }

    private static boolean isBetween(LocalTime timeToCheck, LocalTime startTime, LocalTime endTime) {
        return !timeToCheck.isBefore(startTime) && !timeToCheck.isAfter(endTime);
    }

    public static String getDayName(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        return date.getDayOfWeek().name();
    }

    public String getMonthName(int month, int year) {
        LocalDate date = LocalDate.of(year, month, 1);
        return date.getMonth().name();
    }

    public int getDaysInMonth(int month, int year) {
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
