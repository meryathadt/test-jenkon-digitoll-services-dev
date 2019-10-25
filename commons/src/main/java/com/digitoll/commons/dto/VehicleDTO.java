package com.digitoll.commons.dto;

import com.digitoll.commons.model.Vehicle;
import com.digitoll.commons.util.BasicUtils;

import java.util.ArrayList;

public class VehicleDTO extends Vehicle {


    public VehicleDTO(){

    }

    public VehicleDTO(Vehicle vehicle){
        BasicUtils.copyProps(vehicle,this,new ArrayList<>());
    }
}
