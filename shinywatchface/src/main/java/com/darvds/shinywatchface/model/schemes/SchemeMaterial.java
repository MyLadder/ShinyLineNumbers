package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the 5 main material colours
 */
public class SchemeMaterial extends ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(51, 181, 229));
        colours.add(Color.rgb(170, 102, 204));
        colours.add(Color.rgb(153, 204, 0));
        colours.add(Color.rgb(255, 187, 51));
        colours.add(Color.rgb(255, 68, 68));

        return colours;
    }

    @Override
    public int getBackground() {
        return  Color.rgb(229,229,229);
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(113,113,113);
    }

    @Override
    public boolean needsScrim() {
        return true;
    }

    @Override
    public String getName() {
        return "Material";
    }
}
