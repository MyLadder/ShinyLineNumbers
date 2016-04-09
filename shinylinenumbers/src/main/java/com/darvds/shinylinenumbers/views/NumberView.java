package com.darvds.shinylinenumbers.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import com.darvds.shinylinenumbers.R;
import com.darvds.shinylinenumbers.model.LineSegment;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple view for the number
 */
public class NumberView extends View {

    public static final int DELAY_MILLISECONDS = 33; //for 30fps

    /**
     * Default values
      */
    private int animationDuration = ShinyNumber.DEFAULT_DURATION;
    private int velocity = ShinyNumber.DEFAULT_VELOCITY;
    private int strokeWidth = ShinyNumber.DEFAULT_STROKE_WIDTH;


    private ShinyNumber mShinyNumber;


    public NumberView(Context context) {
        super(context);
        init();
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberView);
        int number = typedArray.getInt(R.styleable.NumberView_number, 0);
        animationDuration = typedArray.getInt(R.styleable.NumberView_duration, ShinyNumber.DEFAULT_DURATION);
        velocity = typedArray.getInt(R.styleable.NumberView_velocity, ShinyNumber.DEFAULT_VELOCITY);
        strokeWidth = typedArray.getInt(R.styleable.NumberView_strokeWidth, ShinyNumber.DEFAULT_STROKE_WIDTH);
        typedArray.recycle();

        init();

        setNumber(number);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
       mShinyNumber = new ShinyNumber(animationDuration, velocity, strokeWidth);
    }



    public ShinyNumber getShinyNumber() {
        return mShinyNumber;
    }

    /**
     * Set the current number
     * @param number to display
     * @throws InvalidParameterException
     */
    public void setNumber(int number) throws InvalidParameterException {
        if(mShinyNumber.setNumber(number, true)){
            postInvalidate();
        }
    }

    /**
     * Set the array of colours to split the line into
     * @param colours as ints
     */
    public void setColours(@ColorInt List<Integer> colours) {
        mShinyNumber.setColours(colours);
    }

    /**
     * Set the duration of the tween animation
     * @param duration in milliseconds
     */
    public void setDuration(int duration) {
        mShinyNumber.setDuration(duration);
    }

    /**
     * Set the speed that the colours will animate along the lines
     * @param velocity the speed
     */
    public void setVelocity(double velocity) {
        mShinyNumber.setVelocity(velocity);
    }

    /**
     * Set the width of the line
     * @param strokeWidth line width
     */
    public void setStrokeWidth(int strokeWidth) {
        mShinyNumber.setStrokeWidth(strokeWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        float minDimen = height > width ? width : height;
        minDimen -= 10;


        ArrayList<LineSegment> lineSegments = mShinyNumber.getSegments(minDimen);

        if (lineSegments != null) {
            for (LineSegment lineSegment : lineSegments) {
                canvas.drawPath(lineSegment.path, lineSegment.paint);
            }

            if (lineSegments.size() > 1) {
                postInvalidateDelayed(DELAY_MILLISECONDS);
            }
        }


    }

}







