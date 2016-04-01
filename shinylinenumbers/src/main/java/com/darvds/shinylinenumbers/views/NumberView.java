package com.darvds.shinylinenumbers.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import com.darvds.shinylinenumbers.R;
import com.darvds.shinylinenumbers.model.LineSegment;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple view for the number
 */
public class NumberView extends View {

    public static final int DELAY_MILLISECONDS = 32;


    private int animationDuration = ShinyNumber.DEFAULT_DURATION;
    private int velocity = ShinyNumber.DEFAULT_VELOCITY;
    private int strokeWidth = ShinyNumber.DEFAULT_STROKE_WIDTH;

    private ShinyNumber mShinyNumber;

    private ArrayList<LineSegment> mLineSegments;





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


    public void setNumber(int number) throws InvalidParameterException {
        if(mShinyNumber.setNumber(number)){
            postInvalidate();
        }

    }

    public void setColours(List<Integer> colours) {
        mShinyNumber.setColours(colours);
    }

    public void setDuration(int duration) {
        mShinyNumber.setDuration(duration);
    }

    public void setVelocity(int velocity) {
        mShinyNumber.setVelocity(velocity);
    }

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


        mLineSegments = mShinyNumber.getSegments(minDimen);

        if (mLineSegments != null) {
            for (LineSegment lineSegment : mLineSegments) {
                canvas.drawPath(lineSegment.path, lineSegment.paint);
            }

            if (mLineSegments.size() > 1) {
                mShinyNumber.updateOffset();
                postInvalidateDelayed(DELAY_MILLISECONDS);
            }
        }


    }

}







