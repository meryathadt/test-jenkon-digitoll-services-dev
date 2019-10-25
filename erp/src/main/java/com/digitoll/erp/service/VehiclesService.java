package com.digitoll.erp.service;

import com.digitoll.commons.dto.VehicleDTO;
import com.digitoll.commons.model.Vehicle;
import com.digitoll.erp.repository.VehicleRepository;
import com.digitoll.commons.util.BasicUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VehiclesService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public VehicleDTO create(VehicleDTO vehicle) {

        return vehicleRepository.save(vehicle);
    }

    public VehicleDTO createIfExists(VehicleDTO vehicle) {
        if(vehicle == null){
            return null;
        }
        return vehicleRepository.save(vehicle);
    }

    public VehicleDTO update(VehicleDTO newData) {
        Vehicle current;
        current = vehicleRepository.findById(newData.getId()).get();

        BasicUtils.copyProps(newData,current, new ArrayList<>());
        return new VehicleDTO(vehicleRepository.save(current));
    }

    public void delete(ArrayList<String> idToDelete) {
        Vehicle toDelete;

        for (String id: idToDelete) {
            toDelete = vehicleRepository.findById(id).get();
            vehicleRepository.delete(toDelete);
        }
    }

    public List<VehicleDTO> findAllByUsername(String username) {
        List<VehicleDTO> result = new ArrayList<>();

        List<Vehicle> vehiclesForUser = vehicleRepository.findByUsername(username);
        vehiclesForUser.forEach(v ->{
            result.add(new VehicleDTO(v));
        });
        return result;
    }

    public VehicleDTO findById(String id) {
        Vehicle vehicle = vehicleRepository.findById(id).get();
        VehicleDTO result = new VehicleDTO(vehicle);
        return result;
    }

    public List<VehicleDTO> findAll() {
        List<VehicleDTO> result = new ArrayList<>();

        List<Vehicle> vehiclesForUser = vehicleRepository.findAll();
        vehiclesForUser.forEach(v ->{
            result.add(new VehicleDTO(v));
        });
        return result;
    }
}
