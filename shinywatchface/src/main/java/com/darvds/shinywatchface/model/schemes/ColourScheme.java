package com.darvds.shinywatchface.model.schemes;

import android.support.annotation.ColorInt;

import java.util.ArrayList;

/**
 * Created by Dave on 02/04/2016.
 */
public interface ColourScheme {

    @ColorInt ArrayList<Integer> getLineColours();

    @ColorInt int getBackground();

    @ColorInt int getDefaultColor();

    boolean needsScrim();

}
