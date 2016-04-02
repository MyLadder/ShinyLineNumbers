package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the old holo blue colours
 */
public class SchemeHolo implements ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(51, 153, 204));
        colours.add(Color.rgb(51, 181, 229));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.rgb(50,50,50);
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(243,243,243);
    }

    @Override
    public boolean needsScrim() {
        return false;
    }
}
