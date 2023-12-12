package com.giza.center_reservation.controller;

import com.giza.center_reservation.model.ReservationModel;
import com.giza.center_reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/center/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("hour")
    public ResponseEntity<String> reserveHour(@RequestBody ReservationModel hourReservationModel){
        return ResponseEntity.ok(reservationService.reserveHour(hourReservationModel));
    }
    @PostMapping("day")
    public ResponseEntity<String> reserveDay(@RequestBody ReservationModel dayReservationModel){
        return ResponseEntity.ok(reservationService.reserveDays(dayReservationModel));
    }
    @PostMapping("month")
    public ResponseEntity<String> reserveMonth(@RequestBody ReservationModel monthReservationModel){
        return ResponseEntity.ok(reservationService.reserveMonths(monthReservationModel));
    }

}
