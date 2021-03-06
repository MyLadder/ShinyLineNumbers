package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * The colours from the Marshmallow loading animation
 */
public class SchemeMarshmallow extends ColourScheme {

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
        return Color.BLACK;
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(120,144,156);
    }

    @Override
    public boolean needsScrim() {
        return false;
    }

    @Override
    public String getName() {
        return "Google Dark";
    }
}
