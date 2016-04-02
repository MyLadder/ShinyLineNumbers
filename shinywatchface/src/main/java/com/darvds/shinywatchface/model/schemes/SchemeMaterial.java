package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Dave on 02/04/2016.
 */
public class SchemeMaterial implements ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(51, 181, 229));
        colours.add(Color.rgb(170, 102, 204));
        colours.add(Color.rgb(153, 204, 0));
        colours.add(Color.rgb(255, 187, 51));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.WHITE;
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(113,113,113);
    }

    @Override
    public boolean needsScrim() {
        return true;
    }
}
