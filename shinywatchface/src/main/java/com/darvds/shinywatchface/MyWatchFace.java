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

import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Property;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.darvds.shinylinenumbers.animation.NumberEvaluator;
import com.darvds.shinylinenumbers.model.LineSegment;
import com.darvds.shinylinenumbers.views.ShinyNumber;
import com.darvds.shinywatchface.model.DigitItem;
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
        Paint mBackgroundPaint;
        boolean mAmbient;
        private boolean mRegisteredTimeZoneReceiver;

        private Calendar mCalendar;

        int mTapCount;

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        /**
         * Array of the different digits to display
         */
        HashMap<Integer, ShinyNumber> mShinyNumberArray;

        /**
         * Colours for the digits when not animating
         */
        int colourDark = Color.DKGRAY;
        int colourLight = Color.WHITE;

        /**
         * Current view mode
         */
        private int mDisplayMode = DISPLAY_SECONDS;

        /**
         * Size of the top digits
         */
        private float mDigitSizeLarge = 0;

        /**
         * Size of the smaller digits below the time
         */
        private float mDigitSizeSmall = 0;

        /**
         * Get the screen shape
         */
        private boolean mIsRound;

        /**
         * The colour scheme of the view
         */
        private ColourScheme mColourScheme;


        private Paint testPaint;

        private Paint mDividerPaint;


        private int mDefaultTimeY = 0;
        private int mTimeYWithExtra = 0;

        protected int mCurrentTimeY;

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
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            testPaint = new Paint();
            testPaint.setColor(Color.WHITE);
            testPaint.setStyle(Paint.Style.STROKE);


            mCalendar = Calendar.getInstance();

            createNumberArray();

            updateDigits();

        }



        /**
         * Load the colour scheme for the view. This needs to be done before setting the
         * watchfacestyle
         */
        private void loadColourScheme(){
            mColourScheme = new SchemeIO();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mColourScheme.getBackground());

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

            //Set the initial colours
            setColours();
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
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();

            updateDigits();

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
            Resources resources = MyWatchFace.this.getResources();
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
                    ValueAnimator anim = null;
                    if(currentMode == DISPLAY_NONE){
                        anim = ObjectAnimator.ofObject(this, Y_POSITION_PROPERTY,
                                new IntEvaluator(), mDefaultTimeY, mTimeYWithExtra);

                    } else if(mDisplayMode == DISPLAY_NONE){
                        anim = ObjectAnimator.ofObject(this, Y_POSITION_PROPERTY,
                                new IntEvaluator(), mTimeYWithExtra, mDefaultTimeY);
                    }

                    if(anim != null){
                        anim.setInterpolator(new AccelerateDecelerateInterpolator());
                        anim.setDuration(400);
                        anim.start();
                    }



                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }




            //testing
          /*  int initialPosY = (canvas.getHeight()/8)*2;

       //     int initialPosY = 80;

            int drawWidth = (int) getDrawableWidth(canvas, initialPosY);

            int digitHeight = drawWidth/4;



            float left;
            float right;
            float top = initialPosY;
            float bottom = initialPosY + digitHeight;

            for(int i = 0; i<4; i++){
                left = (float) ((canvas.getWidth() - drawWidth) / 2f) + (digitHeight*i);
                right = left + digitHeight;
                canvas.drawRect(left, top, right, bottom, testPaint);
            }
*/



            //Draw the main time digits
            drawTime(canvas, getDigitY(canvas, true));



          /*
            //Draw the optional digits
            if(!mAmbient) {
                if (mDisplayMode == DISPLAY_SECONDS) {
                    drawSeconds(canvas);
                } else if (mDisplayMode == DISPLAY_DATE) {
                    drawDate(canvas);
                }
            }*/

        }



        private int getDigitY(Canvas canvas, boolean top){
            if(top) {
                return getTimeYPosition(canvas);
            } else {
                    return 0;
            }
        }

        private int getTimeYPosition(Canvas canvas){

            if(mDefaultTimeY == 0){
                int canvasHeight = canvas.getHeight();

                mDefaultTimeY = (canvasHeight/8 * 3);

                mTimeYWithExtra = (canvasHeight/8 * 2);

                if(mDisplayMode == DISPLAY_NONE){
                    mCurrentTimeY = mDefaultTimeY;
                } else {
                    mCurrentTimeY = mTimeYWithExtra;
                }
            }

            return mCurrentTimeY;
        }



        private void drawTime(Canvas canvas, int yPosition){
            float drawableWidth = getDrawableWidth(canvas, yPosition);
            float dividerSize = drawableWidth/16;

            float digitSize = (drawableWidth-dividerSize-(NUMBER_GAP*8))/4;

            float edgeGap = (canvas.getWidth()-drawableWidth) / 2;

            float y = yPosition + (((drawableWidth/4)-digitSize)/2);

            for(int i = DigitItem.HOUR1; i<DigitItem.SEC1; i++){

                float dx = edgeGap + NUMBER_GAP
                        + (digitSize * i)
                        + (NUMBER_GAP * (i*2));

                if(i > 1){
                    dx += dividerSize;
                }



                drawDigit(canvas, dx, y, mShinyNumberArray.get(i).getSegments(digitSize));

             //   canvas.drawRect(dx, y, dx+ digitSize, y+digitSize, testPaint);
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

        private void drawSeconds(Canvas canvas){
            ShinyNumber shinyNumber;
            for(int i = 0; i < 2; i++){
                float size = getDigitSize(canvas, false);
                float drawableWidth = getDrawableWidth(canvas);



                float dx = ((canvas.getWidth()-drawableWidth)/2) + (size * (i==0?2:3)
                        + (NUMBER_GAP * (i==0?2:3)));

                float dy = (canvas.getHeight() / 4f) + (NUMBER_GAP * 3) + getDigitSize(canvas, true);

                if(i == 0){
                    shinyNumber = mShinyNumberArray.get(DigitItem.SEC1);
                } else {
                    shinyNumber = mShinyNumberArray.get(DigitItem.SEC2);
                }

                drawDigit(canvas, dx, dy, shinyNumber.getSegments(size));
            }
        }

        private void drawDate(Canvas canvas){

            for(int i = 0; i < 4; i++){
                float size = getDigitSize(canvas, false);
                float drawableWidth = getDrawableWidth(canvas);



                float dx = ((canvas.getWidth()-drawableWidth)/2) + (size * (i+1)
                        + (NUMBER_GAP * (i+1)));

                float dy = (canvas.getHeight() / 4f) + (NUMBER_GAP * 3) + getDigitSize(canvas, true);

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

                drawDigit(canvas, dx, dy, mShinyNumberArray.get(index).getSegments(size));
            }
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


/*
        private double getDigitHeight(Canvas canvas, int type){

            double width = getDrawableWidth()
            //TODO remove padding

            switch(type){
                case DigitItem.HOUR1:
                case DigitItem.HOUR2:
                case DigitItem.MIN1:
                case DigitItem.MIN2:
                    return height / 4f;
                default:
                    return height / 6f;
            }

        }*/


        private int getYPosition(Canvas canvas){
            return canvas.getWidth()/2;
        }






        private float getDigitSize(Canvas canvas, boolean large){
            if(large){
                if(mDigitSizeLarge > 0) return mDigitSizeLarge;
                float width = getDrawableWidth(canvas);
                return (width - (NUMBER_GAP*3)) / 4f;
            } else {
                if(mDigitSizeSmall > 0) return mDigitSizeSmall;
                float width = getDrawableWidth(canvas);
                return (width - (NUMBER_GAP*5)) / 6f;
            }
        }

        /**
         * Get the width to draw the digits in, depending on the size of the screen
         * @return available width
         */
        private float getDrawableWidth(Canvas canvas){
            float screenWidth = canvas.getWidth();

            if(mIsRound){
                float radius = screenWidth / 2;
                float segmentHeight = screenWidth/4f;

                double chordLength = 2 * Math.sqrt((Math.pow(radius, 2) - Math.pow(segmentHeight, 2)));

                return (float) chordLength;

            } else {
                return screenWidth - NUMBER_GAP *2;
            }
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





        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
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
            return isVisible() && !isInAmbientMode();
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

            int hourStart = hour / 10;
            mShinyNumberArray.get(DigitItem.HOUR1).setNumber(hourStart, !mAmbient);
            mShinyNumberArray.get(DigitItem.HOUR2).setNumber(hour - (hourStart * 10), !mAmbient);

            int minStart = min / 10;
            mShinyNumberArray.get(DigitItem.MIN1).setNumber(minStart, !mAmbient);
            mShinyNumberArray.get(DigitItem.MIN2).setNumber(min - (minStart * 10), !mAmbient);

            int secStart = sec / 10;
            mShinyNumberArray.get(DigitItem.SEC1).setNumber(secStart, !mAmbient);
            mShinyNumberArray.get(DigitItem.SEC2).setNumber(sec - (secStart * 10), !mAmbient);
        }

        /**
         * Update the date digits
         */
        private void updateDate(){
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            int month = mCalendar.get(Calendar.MONTH);

            int dayStart = day / 10;
            mShinyNumberArray.get(DigitItem.DAY1).setNumber(dayStart, !mAmbient);
            mShinyNumberArray.get(DigitItem.DAY2).setNumber(day - (dayStart * 10), !mAmbient);

            int monthStart = month / 10;
            mShinyNumberArray.get(DigitItem.MON1).setNumber(monthStart, !mAmbient);
            mShinyNumberArray.get(DigitItem.MON2).setNumber(month - (monthStart * 10), !mAmbient);


        }



        /**
         * Toggle anti-aliasing to conserve battery life
         * @param antiAlias enabled or disabled
         */
        private void setAntiAlias(boolean antiAlias){
            for(int i = 0; i < mShinyNumberArray.size(); i++){
                mShinyNumberArray.get(i).setAntiAlias(antiAlias);
            }
        }


        /**
         * Toggle animations and colours in ambient mode
         */
        private void toggleAmbientMode(){

            mShinyNumberArray.get(DigitItem.SEC1).setAlwaysAnimating(!mAmbient);
            mShinyNumberArray.get(DigitItem.SEC2).setAlwaysAnimating(!mAmbient);

            setColours();

        }


        /**
         * Set the light and dark colours depending on the ambient mode
         */
        private void setColours(){

            for(int i =0; i< mShinyNumberArray.size(); i++){
                if(mAmbient){
                    mShinyNumberArray.get(i).setColour(colourDark);
                } else {
                    switch(i){
                        case DigitItem.MIN1:
                        case DigitItem.MIN2:
                        case DigitItem.MON1:
                        case DigitItem.MON2:
                            mShinyNumberArray.get(i).setColour(mColourScheme.getDefaultColor());
                            break;
                        default:
                            mShinyNumberArray.get(i).setColour(mColourScheme.getDefaultColor());
                    }
                }
            }

        }
    }


}
