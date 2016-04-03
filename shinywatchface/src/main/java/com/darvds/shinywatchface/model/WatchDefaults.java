package com.darvds.shinywatchface.model;

import android.content.res.Resources;
import android.support.annotation.ColorInt;

import com.darvds.shinywatchface.R;

/**
 * Default values for the WatchFace, such as colours, dimensions etc
 */
public class WatchDefaults {

    @ColorInt
    private int mColourAmbientBackground;

    @ColorInt
    private int mColourAmbientText;


    /**
     * Load the watch default values from the resources
     * @param resources
     */
    public WatchDefaults(Resources resources){

        mColourAmbientBackground = resources.getColor(R.color.background_ambient);

        mColourAmbientText = resources.getColor(R.color.text_ambient);

    }


    /**
     * Get tha background colour for ambient mode
     * @return background colour
     */
    @ColorInt
    public int getColourAmbientBackground() {
        return mColourAmbientBackground;
    }

    /**
     * Get the text colour for ambient mode
     * @return text colour
     */
    @ColorInt
    public int getColourAmbientText() {
        return mColourAmbientText;
    }



}
