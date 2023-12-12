package com.giza.center_reservation.service;

import com.giza.center_reservation.entities.Center;
import com.giza.center_reservation.exception.RuntimeBusinessException;
import com.giza.center_reservation.infrastructure.CalenderUtil;
import com.giza.center_reservation.model.*;
import com.giza.center_reservation.repository.ICenterRepository;
import com.giza.center_reservation.repository.IHourRepository;
import com.giza.center_reservation.repository.IYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Author: Mohamed Eid, mohamed.eid@gizasystems.com
 * Date: Dec, 2023,
 * Description: Center creation and update handler.
 */
@Service
@RequiredArgsConstructor
public class CenterService {

    private final ICenterRepository centerRepository;

    private final IYearRepository yearRepository;
    private final IHourRepository hourRepository;

    private final CalenderUtil calenderUtil;


    @Transactional
    public ResourceCreated createCenter(CenterCreationModel centerCreationModel) {
        var center = new Center();
        center.setName(centerCreationModel.getName());
        center.setMaxCapacity(centerCreationModel.getMaxCapacity());
        center.setStartWorkingHour(centerCreationModel.getStartTime());
        center.setEndWorkingHour(centerCreationModel.getEndTime());
        center.setLaunchDate(centerCreationModel.getLaunchDate());
        centerRepository.save(center);
        var years = calenderUtil.createCalenderForCenter(center, centerCreationModel.getNonWorkingDates(), centerCreationModel.getLaunchDate());
        yearRepository.saveAll(years);
        return new ResourceCreated(center.getId());

    }

    @Transactional
    public ResourceUpdated updateCapacity(UpdateCapacityModel updateCapacityModel) {
        var center = centerRepository.findById(updateCapacityModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateCapacityModel.getCenterId()));
        var deltaCapacity = updateCapacityModel.getCapacity() - center.getMaxCapacity();
        center.setMaxCapacity(updateCapacityModel.getCapacity());
        centerRepository.save(center);
        hourRepository.updateCapacity(center.getId(), deltaCapacity);
        return new ResourceUpdated(true);

    }

    @Transactional
    public ResourceUpdated updateWorkingHours(UpdateWorkingHoursModel updateWorkingHoursModel) {
        var center = centerRepository.findById(updateWorkingHoursModel.getCenterId()).orElseThrow(() -> new RuntimeBusinessException("Center ID Not found : " + updateWorkingHoursModel.getCenterId()));
        center.setStartWorkingHour(updateWorkingHoursModel.getStartTime());
        center.setEndWorkingHour(updateWorkingHoursModel.getEndTime());
        centerRepository.save(center);
        hourRepository.updateWorkingHours(center.getId(), center.getStartWorkingHour(), center.getEndWorkingHour());
        hourRepository.setNewWorkingHours(center.getId(), center.getMaxCapacity(), center.getStartWorkingHour(), center.getEndWorkingHour());

        return new ResourceUpdated(true);

    }

}
