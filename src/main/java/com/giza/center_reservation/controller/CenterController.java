package com.giza.center_reservation.controller;

import com.giza.center_reservation.model.*;
import com.giza.center_reservation.service.CenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("center")
public class CenterController {

    private final CenterService centerService;

    @PostMapping("")
    public ResponseEntity<ResourceCreated> createCenter(@RequestBody CenterCreationModel centerCreationModel){
        return ResponseEntity.ok(centerService.createCenter(centerCreationModel));

    }
    @PutMapping("{centerId}/working_hours")
    public ResponseEntity<ResourceUpdated> updateWorkingHours(@PathVariable int centerId, @RequestBody UpdateWorkingHoursModel updateWorkingHoursModel){
        updateWorkingHoursModel.setCenterId(centerId);
        return ResponseEntity.ok(centerService.updateWorkingHours(updateWorkingHoursModel));

    }

    @PutMapping("{centerId}/capacity")
    public ResponseEntity<ResourceUpdated> updateWorkingHours(@PathVariable int centerId, @RequestBody UpdateCapacityModel updateCapacityModel){
        updateCapacityModel.setCenterId(centerId);
        return ResponseEntity.ok(centerService.updateCapacity(updateCapacityModel));

    }
    @PutMapping("{centerId}/working_days")
    public ResponseEntity<ResourceUpdated> updateWorkingDays(@PathVariable int centerId, @RequestBody UpdateWorkingDays updateCapacityModel){
        updateCapacityModel.setCenterId(centerId);
        return ResponseEntity.ok(centerService.updateWorkingDays(updateCapacityModel));

    }
}
