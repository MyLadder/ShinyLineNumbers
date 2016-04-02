package com.darvds.shinywatchface.model;

import com.darvds.shinywatchface.model.schemes.ColourScheme;
import com.darvds.shinywatchface.model.schemes.SchemeIO;
import com.darvds.shinywatchface.model.schemes.SchemeMarshmallow;
import com.darvds.shinywatchface.model.schemes.SchemeMaterial;

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
               return new SchemeMarshmallow();
           case 2:
               return new SchemeMaterial();
           default:
               return new SchemeIO();
       }
    }
}
