package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the colours from the I/O 2016 countdown
 * https://events.google.com/io2016/
 */
public class SchemeRainbow extends ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.parseColor("#F44336"));
        colours.add(Color.parseColor("#FF9800"));
        colours.add(Color.parseColor("#FFEB3B"));
        colours.add(Color.parseColor("#4CAF50"));
        colours.add(Color.parseColor("#03A9F4"));
        colours.add(Color.parseColor("#3F51B5"));
        colours.add(Color.parseColor("#9C27B0"));

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
        return "Rainbow";
    }
}
