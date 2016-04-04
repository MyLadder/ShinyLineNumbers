package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the colours from the I/O 2016 countdown
 * https://events.google.com/io2016/
 */
public class SchemeGoogle extends ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(234, 67, 53));
        colours.add(Color.rgb(251, 188, 5));
        colours.add(Color.rgb(52, 168, 83));
        colours.add(Color.rgb(66, 133, 244));

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
        return "Google";
    }
}
