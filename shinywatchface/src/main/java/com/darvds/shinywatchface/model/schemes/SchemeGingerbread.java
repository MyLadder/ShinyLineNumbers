package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Based on the main colours of gingerbread
 */
public class SchemeGingerbread implements ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(153, 194, 36));
        colours.add(Color.rgb(132, 132, 132));
        colours.add(Color.rgb(153, 194, 36));
        colours.add(Color.rgb(132, 132, 132));
    //    colours.add(Color.rgb(255, 181, 0));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.BLACK;
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
