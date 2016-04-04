package com.darvds.shinywatchface.model;

import com.darvds.shinywatchface.model.schemes.ColourScheme;
import com.darvds.shinywatchface.model.schemes.SchemeGingerbread;
import com.darvds.shinywatchface.model.schemes.SchemeGoogle;
import com.darvds.shinywatchface.model.schemes.SchemeHolo;
import com.darvds.shinywatchface.model.schemes.SchemeIO;
import com.darvds.shinywatchface.model.schemes.SchemeMarshmallow;
import com.darvds.shinywatchface.model.schemes.SchemeMaterial;
import com.darvds.shinywatchface.model.schemes.SchemeOmbre;
import com.darvds.shinywatchface.model.schemes.SchemePlay;
import com.darvds.shinywatchface.model.schemes.SchemeRainbow;

import java.util.ArrayList;

/**
 * Helper tool for getting the different colour schemes. Uses an int so can easily be
 * stored in SharedPreferences
 */
public class AnimationColours {


    /**
     * Get the colour schemes
     * @param which which colour scheme to get
     * @return new instance of the colour scheme
     */
    public static ColourScheme getColourScheme(int which){
       switch(which){
           case 0:
               return new SchemeIO();
           case 1:
               return new SchemeGoogle();
           case 2:
               return new SchemeMarshmallow();
           case 3:
               return new SchemeMaterial();
           case 4:
               return new SchemeHolo();
           case 5:
               return new SchemeGingerbread();
           case 6:
               return new SchemeRainbow();
           case 7:
               return new SchemeOmbre();
           case 8:
               return new SchemePlay();
           default:
               return new SchemeIO();
       }
    }


    public static ArrayList<ColourScheme> getAllColourSchemes(){
        ArrayList<ColourScheme> schemes = new ArrayList<>();
        for(int i = 0; i < 9; i++){
            schemes.add(getColourScheme(i));
        }

        return schemes;
    }
}
