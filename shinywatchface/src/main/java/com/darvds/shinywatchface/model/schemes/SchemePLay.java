package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the colours from the I/O 2016 countdown
 * https://events.google.com/io2016/
 */
public class SchemePlay extends ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.parseColor("#4faeb8"));
        colours.add(Color.parseColor("#c64664"));
        colours.add(Color.parseColor("#bbeda9"));
        colours.add(Color.parseColor("#fab679"));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.rgb(229,229,229);
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(120,144,156);
    }

    @Override
    public boolean needsScrim() {
        return true;
    }

    @Override
    public String getName() {
        return "Play";
    }
}
