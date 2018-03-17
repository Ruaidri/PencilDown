package com.example.ruaid.myapplication;

/**
 * Created by ruaid on 08/02/2018.
 */

public class ShadeSelector {

    public String shade( int value) {
        if(value <= 21){
            return "B7";
        }
        if(value <= 42 && value > 21){
            return "B6";
        }
        if(value <= 63 && value > 42){
            return "B5";
        }
        if(value <= 84 && value > 63){
            return "B4";
        }
        if(value <= 105 && value > 84){
            return "B3";
        }
        if(value <= 126 && value > 105){
            return "B2";
        }
        if(value<= 147 && value > 126){
            return "HB";
        }
        if(value <= 168 && value > 147){
            return "H2";
        }
        if(value <= 189 && value > 168){
            return "H3";
        }
        if(value <= 210 && value > 189){
            return "H4";
        }
        if(value <= 235 && value > 210){
            return "H5";
        }
        if(value <= 256 && value > 235){
            return "H6";
        }


        return "Outside the Range";
    }



}
