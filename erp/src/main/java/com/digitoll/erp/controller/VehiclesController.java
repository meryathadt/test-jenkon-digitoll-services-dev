package com.digitoll.erp.controller;
import java.security.Principal;
import java.util.List;

import com.digitoll.commons.dto.DeleteVehicleDTO;
import com.digitoll.commons.dto.VehicleDTO;
import com.digitoll.erp.service.VehiclesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehiclesController {

    @Autowired
    VehiclesService vehicleService;

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/vehicles", method = {RequestMethod.GET, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public List<VehicleDTO> getAllVehicles(Principal principal) {
        String username = principal.getName();
        return vehicleService.findAllByUsername(username);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/vehicles/delete", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public void deleteVehicle(@RequestBody DeleteVehicleDTO toDelete) {
        vehicleService.delete(toDelete.getIdList());
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/vehicles/create", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public VehicleDTO createVehicle(@RequestBody VehicleDTO vehicleDto, Principal principal) {
        String username = principal.getName();
        vehicleDto.setUsername(username);
        return vehicleService.create(vehicleDto);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/vehicles/update", method = {RequestMethod.POST, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public VehicleDTO updateVehicle(@RequestBody VehicleDTO toUpdate) {
        return vehicleService.update(toUpdate);
    }

    @CrossOrigin
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping(value = "/vehicles/test", method = {RequestMethod.GET, RequestMethod.OPTIONS}, produces = "application/json;charset=UTF-8")
    public List<VehicleDTO> getAllVehiclesForever() {
        return vehicleService.findAll();
    }

}
