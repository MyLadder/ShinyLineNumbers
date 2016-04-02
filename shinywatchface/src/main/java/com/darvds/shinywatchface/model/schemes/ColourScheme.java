package com.darvds.shinywatchface.model.schemes;

import android.support.annotation.ColorInt;

import java.util.ArrayList;

/**
 * Base class for all colour schemes
 */
public interface ColourScheme {

    /**
     * The different colours to display while animating
     * @return list of colours
     */
    @ColorInt ArrayList<Integer> getLineColours();

    /**
     * @return the background colour for the view
     */
    @ColorInt int getBackground();

    /**
     * @return the colour of the digit when not animating
     */
    @ColorInt int getDefaultColor();

    /**
     * @return if need to display a scrim behind the system icons and hotword
     */
    boolean needsScrim();

}
