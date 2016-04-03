package com.darvds.shinywatchface.preferences;

import android.content.SharedPreferences;

import com.darvds.shinywatchface.model.DisplayMode;

/**
 * User preferences for the watch face
 */
public class WatchPreferences {

    private static final String DISPLAY_MODE = "displaymode";
    private static final String COLOUR_SCHEME = "colourscheme";

    private SharedPreferences mSharedPreferences;

    public WatchPreferences(SharedPreferences sharedPreferences){
        mSharedPreferences = sharedPreferences;
    }

    public int getDisplayMode(){
        return mSharedPreferences.getInt(DISPLAY_MODE, DisplayMode.TIME);
    }

    public void setDisplayMode(int mode){
        setInt(DISPLAY_MODE, mode);
    }

    public int getColourScheme(){
        return mSharedPreferences.getInt(COLOUR_SCHEME, 0);
    }

    public void setColourScheme(int colourScheme){
        setInt(COLOUR_SCHEME, colourScheme);
    }


    private void setInt(String key, int value){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }



}
