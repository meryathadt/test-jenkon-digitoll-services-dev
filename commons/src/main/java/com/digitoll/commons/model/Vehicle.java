package com.digitoll.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.digitoll.commons.dto.VehicleDTO;
import com.digitoll.commons.enumeration.EmissionClass;
import com.digitoll.commons.enumeration.VehicleType;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.commons.request.VehicleProperties;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Objects;

@Document(collection = "vehicles")
public class Vehicle implements VehicleProperties {
    @Id
    private String id;

    @ApiModelProperty(notes = "The license plate number of the vehicle", example = "CM1229PP")
    private String lpn;
    @ApiModelProperty(notes = "The country of registration of the vehicle", example = "BG")
    private String countryCode;
    @ApiModelProperty(notes = "Vehicle type that the vignette is for")
    private VehicleType type;
    @ApiModelProperty(notes = "The emission class according to the euro standards")
    private EmissionClass emissionClass;
    private String username;

    public Vehicle(){

    }

    public static class KapschVehicle {
        @ApiModelProperty(notes = "The vehicle number that was entered " +
                "when creating the vignette sale", example = "CM8265PP")
        private String lpn;
        @ApiModelProperty(notes = "The code of the country that was selected " +
                "when creating the vignette sale", example = "BG")
        private String countryCode;
        public KapschVehicle(){}
        public KapschVehicle(String lpn, String countryCode){
            this.lpn=lpn;
            this.countryCode=countryCode;
        }

        public String getLpn() {
            return lpn;
        }

        public void setLpn(String lpn) {
            this.lpn = lpn;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KapschVehicle that = (KapschVehicle) o;
            return Objects.equals(lpn, that.lpn) &&
                    Objects.equals(countryCode, that.countryCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lpn, countryCode);
        }

        @Override
        public String toString() {
            return "KapschVehicle{" +
                    "lpn='" + lpn + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    '}';
        }
    }

    public Vehicle(KapschVehicle kapschVehicle){
        this.lpn = kapschVehicle.getLpn();
        this.countryCode = kapschVehicle.getCountryCode();
    }

    public Vehicle(VehicleDTO vehicleDTO){
       BasicUtils.copyProps(vehicleDTO,this,new ArrayList<>());
    }


    public String getLpn() {
        return lpn;
    }

    public void setLpn(String lpn) {
        this.lpn = lpn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public EmissionClass getEmissionClass() {
        return emissionClass;
    }

    public void setEmissionClass(EmissionClass emissionClass) {
        this.emissionClass = emissionClass;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonIgnore
    public KapschVehicle getKapschVehicle(){
        return new KapschVehicle(this.getLpn(),this.getCountryCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id) &&
                Objects.equals(lpn, vehicle.lpn) &&
                Objects.equals(countryCode, vehicle.countryCode) &&
                type == vehicle.type &&
                emissionClass == vehicle.emissionClass &&
                Objects.equals(username, vehicle.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lpn, countryCode, type, emissionClass, username);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", lpn='" + lpn + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", type=" + type +
                ", emissionClass=" + emissionClass +
                ", username='" + username + '\'' +
                '}';
    }
}