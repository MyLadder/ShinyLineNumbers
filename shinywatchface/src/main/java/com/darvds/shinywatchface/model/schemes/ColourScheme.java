package com.darvds.shinywatchface.model.schemes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;

import java.util.ArrayList;

/**
 * Base class for all colour schemes
 */
public abstract class ColourScheme {

    /**
     * The different colours to display while animating
     * @return list of colours
     */
    @ColorInt public abstract ArrayList<Integer> getLineColours();

    /**
     * @return the background colour for the view
     */
    @ColorInt public abstract int getBackground();

    /**
     * @return the colour of the digit when not animating
     */
    @ColorInt public abstract int getDefaultColor();

    /**
     * @return if need to display a scrim behind the system icons and hotword
     */
    public abstract boolean needsScrim();

    /**
     * @return the name of the scheme
     */
    public abstract String getName();



    public Bitmap getSettingsBitmap(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(100, 100, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);

        ArrayList<Integer> colours = getLineColours();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        RectF r = new RectF(20, 20, 80, 80);

        float angle = 360 / colours.size();

        for(int i = 0; i < colours.size(); i++){
            paint.setColor(colours.get(i));

            canvas.drawArc(r, angle*i, angle, true, paint);
        }

        return bmp;
    }

}
