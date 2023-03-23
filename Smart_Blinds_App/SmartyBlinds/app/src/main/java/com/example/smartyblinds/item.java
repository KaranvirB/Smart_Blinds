package com.example.smartyblinds;

public class item {

    private String title, serial;

    public item(String title, String serial){
        this.title = title;
        this.serial = serial;
    }

    public String get_title(){
        return title;
    }

    public String get_serial(){
        return serial;
    }

}

