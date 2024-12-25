package com.ema.practica.core.home.model;

import com.ema.practica.core.user.model.User;

public class Home {

    private Long idHome;
    private String street;
    private String  city;
    private String  Location;
    private int nro;
    private int postcode;
    public Home(){

    }

    private Home(Long idHome, String street, String city,String Location, int nro, int postcode){
        this.idHome = idHome;
        this.street = street;
        this.city = city;
        this.Location = Location;
        this.nro = nro;
        this.postcode = postcode;
    }

    public static Home getInstance(Long idHome, String street, String city, String Location, int nro, int postcode){
        return new Home(idHome, street, city, Location, nro, postcode);
    }

    public Long getIdHome() {
        return idHome;
    }

    public void setIdHome(Long idHome) {
        this.idHome = idHome;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public int getNro() {
        return nro;
    }

    public void setNro(int nro) {
        this.nro = nro;
    }

    public int getPostcode() {
        return postcode;
    }

    public void setPostcode(int postcode) {
        this.postcode = postcode;
    }
}
