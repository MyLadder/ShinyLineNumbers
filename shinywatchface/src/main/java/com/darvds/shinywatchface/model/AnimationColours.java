package com.darvds.shinywatchface.model;

import com.darvds.shinywatchface.model.schemes.ColourScheme;
import com.darvds.shinywatchface.model.schemes.SchemeIO;
import com.darvds.shinywatchface.model.schemes.SchemeMarshmallow;
import com.darvds.shinywatchface.model.schemes.SchemeMaterial;

/**
 * Created by davidscott1 on 28/03/2016.
 */
public class AnimationColours {


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
