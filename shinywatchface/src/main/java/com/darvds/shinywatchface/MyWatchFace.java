/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.darvds.shinywatchface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Property;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.darvds.shinylinenumbers.model.LineSegment;
import com.darvds.shinylinenumbers.views.ShinyNumber;
import com.darvds.shinywatchface.model.DigitItem;
import com.darvds.shinywatchface.model.WatchDefaults;
import com.darvds.shinywatchface.model.schemes.ColourScheme;
import com.darvds.shinywatchface.model.schemes.SchemeIO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFace extends CanvasWatchFaceService {

    public static final int MODE_ANIMATION_DURATION = 250;
    /**
     * The different view modes that can be toggled by tapping
     */
    private static final int DISPLAY_NONE = 0;
    private static final int DISPLAY_SECONDS = 1;
    private static final int DISPLAY_DATE = 2;

    /**
     * Spacing between digits when drawn
     */
    private static final int NUMBER_GAP = 2;

    /**
     * Size of the stroke for each digit
     */
    private static final int STROKE_WIDTH_LARGE = 2;
    private static final int STROKE_WIDTH_SMALL = 2;

    /**
     * The velocity of the animation
     */
    private static final double VELOCITY = 1.5;

    /**
     * Update rate in milliseconds for interactive mode. Updates for 30fps animation
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = 33;  //TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        private boolean mRegisteredTimeZoneReceiver;

        /**
         * Calendar for working out the correct time and displaying it
         */
        private Calendar mCalendar;

        /**
         * Default values for the watch face from the resources
         */
        private WatchDefaults mWatchDefaults;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        /**
         * Array of the different digits to display
         */
        private HashMap<Integer, ShinyNumber> mShinyNumberArray;

        /**
         * Current view mode
         */
        private int mDisplayMode = DISPLAY_SECONDS;

        /**
         * Get the screen shape
         */
        private boolean mIsRound;

        /**
         * The colour scheme of the view
         */
        private ColourScheme mColourScheme;

        /**
         * Paint for the dividers between the time and the dates
         */
        private Paint mDividerPaint;

        /**
         * Default position for the time
         */
        private int mDefaultTimeY = 0;

        /**
         * Defualt position for the time when the seconds / date is showing
         */
        private int mTimeYWithExtra = 0;

        /**
         * The current position of the time
         */
        protected int mCurrentTimeY;

        /**
         * Store whether in ambient mode so no to animate between the two
         */
        private boolean mAmbient;

        /**
         * Hold value of is animating so we can still update the screen when moving to ambient
         * mode
         */
        private boolean mIsAnimating;

        /**
         * Property for smooth animation of the y positions
         */
        private final Property<MyWatchFace.Engine, Integer> Y_POSITION_PROPERTY = new Property<MyWatchFace.Engine, Integer>(Integer.class, "points") {
            @Override
            public Integer get(MyWatchFace.Engine engine) {
                return engine.mCurrentTimeY;
            }

            @Override
            public void set(MyWatchFace.Engine engine, Integer value) {
                engine.mCurrentTimeY = value;
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            //Load the colour scheme first, to set the properties of the WatchFaceStyle
            loadColourScheme();

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .setStatusBarGravity(Gravity.END)
                    .setHotwordIndicatorGravity(Gravity.START)
                    .setViewProtectionMode(mColourScheme.needsScrim() ?
                            WatchFaceStyle.PROTECT_STATUS_BAR |
                                    WatchFaceStyle.PROTECT_HOTWORD_INDICATOR
                            : 0)
                    .build());

            Resources resources = MyWatchFace.this.getResources();
            mWatchDefaults = new WatchDefaults(resources);

           initCalendar();

            createNumberArray();

            updateDigits();

        }


        private void initCalendar(){
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeZone(TimeZone.getDefault());
        }



        /**
         * Load the colour scheme for the view. This needs to be done before setting the
         * watchfacestyle
         */
        private void loadColourScheme(){
            mColourScheme = new SchemeIO();

            mDividerPaint = new Paint();
            mDividerPaint.setColor(mColourScheme.getDefaultColor());
            mDividerPaint.setStyle(Paint.Style.STROKE);
            mDividerPaint.setStrokeWidth(STROKE_WIDTH_SMALL);
            mDividerPaint.setAntiAlias(true);
        }

        /**
         * Initialise the array of digits to display in each mode
         */
        private void createNumberArray() {
            mShinyNumberArray = new HashMap<>();

            for(int i = 0; i< DigitItem.TOTAL; i++){
                ShinyNumber number = new ShinyNumber(250, VELOCITY, STROKE_WIDTH_LARGE,
                        mColourScheme.getLineColours());

                number.setAlwaysAnimating(true);

                //Set default colour for when not animating
                number.setColour(mWatchDefaults.getColourAmbientText());

                //Set always animating
                switch(i){
                    case DigitItem.SEC1:
                    case DigitItem.SEC2:
                  //      number.setAlwaysAnimating(true);
                        number.setStrokeWidth(STROKE_WIDTH_SMALL);
                        break;
                    default:
                  //      number.setAlwaysAnimating(false);
                }

                mShinyNumberArray.put(i, number);
            }

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                updateDigits();
                registerReceiver();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            mIsRound = insets.isRound();
       //     mXOffset = resources.getDimension(isRound
       //             ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);

        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    //   mTextPaint.setAntiAlias(!inAmbientMode);
                    setAntiAlias(!inAmbientMode);
                }

                toggleAmbientMode();

                updateDigits();

                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    int currentMode = mDisplayMode;

                    mDisplayMode++;
                    if(mDisplayMode > DISPLAY_DATE){
                        mDisplayMode = DISPLAY_NONE;
                    }

                    if(currentMode == DISPLAY_NONE){
                        animateToTimeAndSubText();

                    } else if(mDisplayMode == DISPLAY_NONE){
                        animateToJustTime();
                    }

                    break;
            }
            invalidate();
        }

        private void animateToJustTime(){
            ValueAnimator animator = ObjectAnimator.ofObject(this, Y_POSITION_PROPERTY,
                    new IntEvaluator(), mTimeYWithExtra, mDefaultTimeY);

            startPositionAnimation(animator);
        }

        private void animateToTimeAndSubText(){
            ValueAnimator animator = ObjectAnimator.ofObject(this, Y_POSITION_PROPERTY,
                    new IntEvaluator(), mDefaultTimeY, mTimeYWithExtra);

            startPositionAnimation(animator);
        }

        private void startPositionAnimation(ValueAnimator animator){
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(MODE_ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    updateTimer();
                }
            });
            animator.start();
            mIsAnimating = true;
        }




        //region Draw watch face

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(mWatchDefaults.getColourAmbientBackground());
            } else {
                canvas.drawColor(mColourScheme.getBackground());
            }

            //Draw the main time digits
            drawTime(canvas, getTimeYPosition(canvas));

            //Draw the optional digits
            if(!isInAmbientMode()) {
                if (mDisplayMode == DISPLAY_SECONDS) {
                    drawSeconds(canvas, mCurrentTimeY);
                } else if (mDisplayMode == DISPLAY_DATE) {
                    drawDate(canvas, mCurrentTimeY);
                }
            }

        }


        private int getTimeYPosition(Canvas canvas){

            if(mDefaultTimeY == 0){
                int canvasHeight = canvas.getHeight();

                mDefaultTimeY = (canvasHeight/8) * 3;

                mTimeYWithExtra = (int) ((canvasHeight / 8) * 2.5);

                if(mDisplayMode == DISPLAY_NONE){
                    mCurrentTimeY = mDefaultTimeY;
                } else {
                    mCurrentTimeY = mTimeYWithExtra;
                }
            }

            return mCurrentTimeY;
        }

        /**
         * Get the top position to draw the sub text below the time
         * @param canvas the canvas that it will be drawn on
         * @return the y position
         */
        private int getSubTextYPosition(Canvas canvas){
            float drawableWidth = getDrawableWidth(canvas, mCurrentTimeY);
            return mCurrentTimeY
                    + (int) getDigitSize(drawableWidth, drawableWidth/16f, true)
                    + (NUMBER_GAP * 8);
        }

        /**
         * Get the size of the digits depending whether they are the main time or the seconds
         * / date
         * @param drawableWidth the available width to draw the digit
         * @param dividerSize the size of the divider between the digits
         * @param large if this is large text or not
         * @return the size of the digit (width and height)
         */
        private float getDigitSize(float drawableWidth, float dividerSize, boolean large) {
            return (drawableWidth-dividerSize-(NUMBER_GAP*8))/(large ? 4 : 6);
        }

        private void drawTime(Canvas canvas, int yPosition){
            float drawableWidth = getDrawableWidth(canvas, yPosition);

            float dividerSize = drawableWidth/16;

            float digitSize = getDigitSize(drawableWidth, dividerSize, true);

            float edgeGap = (canvas.getWidth()-drawableWidth) / 2;

            float y = yPosition + (((drawableWidth/4)-digitSize)/2);

            for(int i = DigitItem.HOUR1; i<DigitItem.SEC1; i++){

                float dx = edgeGap + NUMBER_GAP
                        + (digitSize * i)
                        + (NUMBER_GAP * (i*2))
                        + (i > 1 ? dividerSize : 0);

                drawDigit(canvas, dx, y, mShinyNumberArray.get(i).getSegments(digitSize));
            }

            //Draw the time dividers
            for(int i = 1; i<3; i++) {
                canvas.drawCircle(
                        canvas.getWidth() / 2,
                        y + ((digitSize/3)*i),
                        dividerSize /8,
                        mDividerPaint);
            }

        }

        private void drawSeconds(Canvas canvas, int yPosition){
            float drawableWidth = getDrawableWidth(canvas, yPosition);
            float size = getDigitSize(drawableWidth, 0, false);
            float dy = getSubTextYPosition(canvas);
            float edgeGap = (canvas.getWidth()-drawableWidth) / 2;

            ShinyNumber shinyNumber;
            for(int i = 0; i < 2; i++){

                float dx = edgeGap
                        + (size * (i+2))
                        + (NUMBER_GAP * (i+2));

                if(i == 0){
                    shinyNumber = mShinyNumberArray.get(DigitItem.SEC1);
                } else {
                    shinyNumber = mShinyNumberArray.get(DigitItem.SEC2);
                }

                drawDigit(canvas, dx, dy, shinyNumber.getSegments(size));
            }
        }

        private void drawDate(Canvas canvas, int yPosition){
            float drawableWidth = getDrawableWidth(canvas, yPosition);
            float dividerSize = drawableWidth / 12;
            float digitSize = getDigitSize(drawableWidth, dividerSize, false);
            float dy = getSubTextYPosition(canvas);

            float edgeGap = (canvas.getWidth()-drawableWidth) / 2;

            for(int i = 0; i < 4; i++){

                float dx = edgeGap
                        + (NUMBER_GAP * ((i*2)+3))
                        + (digitSize * (i+1))
                        + (i>1 ? dividerSize : 0);


               int index = 0;
                switch(i){
                    case 0:
                        index = DigitItem.DAY1;
                        break;
                    case 1:
                        index = DigitItem.DAY2;
                        break;
                    case 2:
                        index = DigitItem.MON1;
                        break;
                    case 3:
                        index = DigitItem.MON2;
                        break;
                }

                drawDigit(canvas, dx, dy, mShinyNumberArray.get(index).getSegments(digitSize));
            }

            float left = edgeGap + (NUMBER_GAP * 6) + (digitSize *3);

            //draw divider
            canvas.drawLine(left, dy + digitSize, left+dividerSize, dy, mDividerPaint);
        }

        private void drawDigit(Canvas canvas, float dx, float dy,
                               ArrayList<LineSegment> lineSegments){
            int restoreCount = canvas.save();

            canvas.translate(dx, dy);

            for(LineSegment lineSegment : lineSegments) {
                canvas.drawPath(lineSegment.path, lineSegment.paint);
            }

            canvas.restoreToCount(restoreCount);

        }

        /**
         * Get the width to draw the digits in, depending on the size of the screen
         * @return available width
         */
        private float getDrawableWidth(Canvas canvas, int yPosition){
            float screenWidth = canvas.getWidth();

            if(mIsRound){
                float radius = screenWidth / 2;

                float distance = Math.abs(radius-yPosition);

                double chordLength = 2 * Math.sqrt((Math.pow(radius, 2) - Math.pow(distance, 2)));

                return (float) chordLength;

            } else {
                return screenWidth - NUMBER_GAP *2;
            }


        }

        //endregion

        //region Timer

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();

            updateDigits();

        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && (!isInAmbientMode() || mIsAnimating);
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            updateOffsets();
            updateDigits();
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        //endregion

        //region Update digits

        /**
         * Update the position of the animated lines
         */
        private void updateOffsets(){
            for(int i = 0; i < mShinyNumberArray.size(); i++){
                mShinyNumberArray.get(i).updateOffset();
            }
        }

        /**
         * Update each digit for the correct time and date
         */
        private void updateDigits(){

            mCalendar.setTimeInMillis(System.currentTimeMillis());

            updateTime();
            updateDate();

        }

        /**
         * Update the time digits
         */
        private void updateTime(){
            int hour = mCalendar.get(Calendar.HOUR);
            int min = mCalendar.get(Calendar.MINUTE);
            int sec = mCalendar.get(Calendar.SECOND);

            //If 12 hour set, make sure 12pm shows as 12.
            //If 24 hour set, show the time value + 12
            int amPm = mCalendar.get(Calendar.AM_PM);
            if(amPm == Calendar.PM && hour == 0){
                hour = 12;
            } else if(amPm == Calendar.PM && DateFormat.is24HourFormat(MyWatchFace.this)){
                hour += 12;
            }

            boolean ambient = isInAmbientMode();

            int hourStart = hour / 10;
            mShinyNumberArray.get(DigitItem.HOUR1).setNumber(hourStart, !ambient);
            mShinyNumberArray.get(DigitItem.HOUR2).setNumber(hour - (hourStart * 10), !ambient);

            int minStart = min / 10;
            mShinyNumberArray.get(DigitItem.MIN1).setNumber(minStart, !ambient);
            mShinyNumberArray.get(DigitItem.MIN2).setNumber(min - (minStart * 10), !ambient);

            int secStart = sec / 10;
            mShinyNumberArray.get(DigitItem.SEC1).setNumber(secStart, !ambient);
            mShinyNumberArray.get(DigitItem.SEC2).setNumber(sec - (secStart * 10), !ambient);
        }

        /**
         * Update the date digits
         */
        private void updateDate(){
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            int month = mCalendar.get(Calendar.MONTH) + 1; //Add 1 to month because starts at 0

            boolean ambient = isInAmbientMode();

            int dayStart = day / 10;
            mShinyNumberArray.get(DigitItem.DAY1).setNumber(dayStart, !ambient);
            mShinyNumberArray.get(DigitItem.DAY2).setNumber(day - (dayStart * 10), !ambient);

            int monthStart = month / 10;
            mShinyNumberArray.get(DigitItem.MON1).setNumber(monthStart, !ambient);
            mShinyNumberArray.get(DigitItem.MON2).setNumber(month - (monthStart * 10), !ambient);


        }

        //endregion



        /**
         * Toggle anti-aliasing to conserve battery life
         * @param antiAlias enabled or disabled
         */
        private void setAntiAlias(boolean antiAlias){
            for(int i = 0; i < mShinyNumberArray.size(); i++){
                mShinyNumberArray.get(i).setAntiAlias(antiAlias);
            }

            mDividerPaint.setAntiAlias(antiAlias);

        }

        /**
         * Toggle animations for ambient mode
         */
        private void toggleAmbientMode(){

            for(int i =0; i< mShinyNumberArray.size(); i++){
                mShinyNumberArray.get(i).setAlwaysAnimating(!isInAmbientMode());
            }

            mDividerPaint.setColor(isInAmbientMode() ? mWatchDefaults.getColourAmbientText()
                                    : mColourScheme.getDefaultColor());

            if(isInAmbientMode() && mDisplayMode != DISPLAY_NONE){
                //animate into position
                animateToJustTime();

            } else if(!isInAmbientMode() && mDisplayMode != DISPLAY_NONE){
                //animate back to position
                animateToTimeAndSubText();
            }
        }

    }


}
