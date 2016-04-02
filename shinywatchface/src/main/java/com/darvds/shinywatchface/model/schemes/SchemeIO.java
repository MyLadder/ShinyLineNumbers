package com.darvds.shinywatchface.model.schemes;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Dave on 02/04/2016.
 */
public class SchemeIO implements ColourScheme {

    @Override
    public ArrayList<Integer> getLineColours() {
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(239, 83, 80));
        colours.add(Color.rgb(92, 107, 192));
        colours.add(Color.rgb(38, 198, 218));
        colours.add(Color.rgb(140, 242, 242));

        return colours;
    }

    @Override
    public int getBackground() {
        return Color.WHITE;
    }

    @Override
    public int getDefaultColor() {
        return Color.rgb(120,144,156);
    }

    @Override
    public boolean needsScrim() {
        return true;
    }
}
