package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the colours from the I/O 2016 countdown
 * https://events.google.com/io2016/
 */
public class SchemeOmbre extends ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.parseColor("#ae70ec"));
        colours.add(Color.parseColor("#997de6"));
        colours.add(Color.parseColor("#8c85e3"));
        colours.add(Color.parseColor("#7495dd"));
        colours.add(Color.parseColor("#60a1d7"));
        colours.add(Color.parseColor("#52abd4"));
        colours.add(Color.parseColor("#3ab9cd"));
        colours.add(Color.parseColor("#2ac4c9"));
        colours.add(Color.parseColor("#16d1c4"));
        colours.add(Color.parseColor("#2ac4c9"));
        colours.add(Color.parseColor("#3ab9cd"));
        colours.add(Color.parseColor("#52abd4"));
        colours.add(Color.parseColor("#60a1d7"));
        colours.add(Color.parseColor("#7495dd"));
        colours.add(Color.parseColor("#8c85e3"));
        colours.add(Color.parseColor("#997de6"));

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
        return "Ombre";
    }
}
