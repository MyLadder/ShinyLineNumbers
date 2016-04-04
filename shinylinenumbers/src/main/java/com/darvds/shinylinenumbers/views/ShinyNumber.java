package com.darvds.shinylinenumbers.views;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.darvds.shinylinenumbers.animation.NumberEvaluator;
import com.darvds.shinylinenumbers.model.LineSegment;
import com.darvds.shinylinenumbers.model.NumberUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The basis for drawing the different numbers
 */
public class ShinyNumber {

    public static final int DEFAULT_DURATION = 250;
    public static final int DEFAULT_VELOCITY = 100;
    public static final int DEFAULT_STROKE_WIDTH = 5;
    public static final int DEFAULT_COLOUR = Color.LTGRAY;


    private int animationDuration = DEFAULT_DURATION;
    private double velocity = DEFAULT_VELOCITY;
    private int strokeWidth = DEFAULT_STROKE_WIDTH;
    private int colour = DEFAULT_COLOUR;


    private Path path;
    private PathMeasure pathMeasure;
    private Paint paint;
    private HashMap<Integer, Paint> paintArray;
    private HashMap<Integer, Path> pathArray;
    private float animationOffset = 0;
    private float[][] points;
    private int currentNumber = -1;

    private boolean mAlwaysAnimating;
    private boolean mAnimateChanges;

    /**
     * The last time since the segments were drawn
     */
    private long mLastTime;


    private static final Property<ShinyNumber, float[][]> POINTS_PROPERTY = new Property<ShinyNumber, float[][]>(float[][].class, "points") {
        @Override
        public float[][] get(ShinyNumber shinyNumber) {
            return shinyNumber.getPoints();
        }

        @Override
        public void set(ShinyNumber shinyNumber, float[][] value) {
            shinyNumber.setPoints(value);
        }
    };


    public ShinyNumber(){
        init();
    }

    public ShinyNumber(int duration, int velocity, int strokeWidth){
        this.animationDuration = duration;
        this.velocity = velocity;
        this.strokeWidth = strokeWidth;

        init();
    }

    public ShinyNumber(int duration, double velocity, int strokeWidth, ArrayList<Integer> colours){
        this.animationDuration = duration;
        this.velocity = velocity;
        this.strokeWidth = strokeWidth;

        init();

        setColours(colours);
    }


    private void init(){
        paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(colour);

        path = new Path();
        pathMeasure = new PathMeasure();
        pathArray = new HashMap<>();

        mLastTime = System.currentTimeMillis();

    }


    public boolean setNumber(int number, boolean animate) throws InvalidParameterException{

        if(number == currentNumber) return false;

        boolean needsUpdate;

        if (currentNumber == -1 || !animate) {
            //set initial number
            points = NumberUtils.getPoints(number);

            needsUpdate = true;

        } else {
            //Animate

            float[][] startPoints = NumberUtils.getPoints(currentNumber);
            float[][] endPoints = NumberUtils.getPoints(number);

            ObjectAnimator anim = ObjectAnimator.ofObject(this, POINTS_PROPERTY,
                    new NumberEvaluator(), startPoints, endPoints);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());

            anim.setDuration(animationDuration);
            anim.start();

            needsUpdate = false;

            if(!mAlwaysAnimating){
                animationOffset = 0;
                mAnimateChanges = true;
            }

        }


        currentNumber = number;

        return needsUpdate;
    }


    public boolean setNumber(int number) throws InvalidParameterException {
        return setNumber(number, true);
    }


    public void setColours(List<Integer> colours){

        paintArray = new HashMap<>();

        int i = 0;
        for(Integer color : colours){

            Paint p = new Paint(paint);
            p.setColor(color);

            //Add paint
            paintArray.put(i, p);

            i++;
        }

    }


    public void setDuration(int duration) {
        this.animationDuration = duration;
    }


    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }


    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        paint.setStrokeWidth(strokeWidth);

        for (int i = 0; i < paintArray.size(); i++) {
            paintArray.get(i).setStrokeWidth(strokeWidth);
        }
    }


    public void setAlwaysAnimating(boolean alwaysAnimating){
        this.mAlwaysAnimating = alwaysAnimating;
    }


    public void startSingleColourLoop(){
        mAnimateChanges = true;
    }


    public void setAntiAlias(boolean antiAlias){
        paint.setAntiAlias(antiAlias);
        for(int i = 0; i<paintArray.size(); i++){
            paintArray.get(i).setAntiAlias(antiAlias);
        }
    }


    public void setColour(int colour){
        this.colour = colour;
        paint.setColor(colour);
    }


    private float[][] getPoints() {
        return points;
    }


    private void setPoints(float[][] points) {
        this.points = points;
    }


    private Path getPathForSize(float size){

        int length = points.length;

        path.reset();
        path.moveTo(size * points[0][0], size * points[0][1]);
        for (int i = 1; i < length; i += 3) {
            path.cubicTo(size * points[i][0], size * points[i][1],
                    size * points[i + 1][0], size * points[i + 1][1],
                    size * points[i + 2][0], size * points[i + 2][1]);
        }
        path.close();

        return path;
    }


    public ArrayList<LineSegment> getSegments(float size){

        //Update the position
        updateOffset();

        if (points == null) return null;

        path = getPathForSize(size);

        ArrayList<LineSegment> segmentArray = new ArrayList<>();

        if(this.paintArray != null && this.paintArray.size() > 1
                && (mAlwaysAnimating  || mAnimateChanges)) {

        pathMeasure.setPath(path, true);

        float length = pathMeasure.getLength();

        float segmentLength = length / this.paintArray.size();

        int j= 0;

        for(int i = 0; i < this.paintArray.size(); i++) {

            float start = animationOffset + (segmentLength * i);
            float end = start + segmentLength;


            if (end > length) {
                Path p = getSegmentPath(j++);
                pathMeasure.getSegment(start, length, p, true);

                LineSegment segment = new LineSegment();
                segment.path = p;
                segment.paint = paintArray.get(i);

                Path p2 = getSegmentPath(j++);
                pathMeasure.getSegment(start - length, end - length, p2, true);

                LineSegment segment2 = new LineSegment();
                segment2.path = p2;
              //  if(!mAlwaysAnimating && !mAnimateChanges){
              //     segment2.paint = paint;
              //  } else {
                    segment2.paint = paintArray.get(i);
              //  }

                segmentArray.add(segment);
                segmentArray.add(segment2);


            } else {
                Path p = getSegmentPath(j++);
                pathMeasure.getSegment(start, end, p, true);

                LineSegment segment = new LineSegment();
                segment.path = p;
                segment.paint = paintArray.get(i);

                segmentArray.add(segment);
            }
        }


        } else {
            LineSegment segment = new LineSegment();
            segment.path = path;
            segment.paint = paint;
            segmentArray.add(segment);
        }

        return segmentArray;

    }


    /**
     * Update the animation offset based on the velocity
     */
    private void updateOffset(){
        long time = System.currentTimeMillis();

        long timeSinceLastUpdate = time - mLastTime;

        float update = (float) (velocity / 1000) * timeSinceLastUpdate;

        float position = animationOffset + update;

        if(position < pathMeasure.getLength()){
            animationOffset = position;
        } else if(position == pathMeasure.getLength()){
            animationOffset = 0;
        } else {
            int remove = (int) Math.floor(position / pathMeasure.getLength());

            animationOffset = position - (remove * pathMeasure.getLength());
        }

        if(animationOffset < 0) animationOffset = 0;

        mLastTime = time;

    }



    private Path getSegmentPath(int index){
        Path p = pathArray.get(index);
        if(p == null){
            p = new Path();
        }else {
            p.reset();
        }

        return p;
    }


}
