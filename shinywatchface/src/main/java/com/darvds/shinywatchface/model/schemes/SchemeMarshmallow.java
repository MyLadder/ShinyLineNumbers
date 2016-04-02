package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Dave on 02/04/2016.
 */
public class SchemeMarshmallow implements ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(66, 132, 244));
        colours.add(Color.rgb(253, 192, 5));
        colours.add(Color.rgb(237, 67, 55));
        colours.add(Color.rgb(74, 174, 78));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.BLACK;
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(249,249,249);
    }

    @Override
    public boolean needsScrim() {
        return false;
    }
}
