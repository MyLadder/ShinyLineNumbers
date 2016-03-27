package com.darvds.shinylinenumbers.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.darvds.shinylinenubers.R;
import com.darvds.shinylinenumbers.animation.NumberEvaluator;
import com.darvds.shinylinenumbers.model.NumberUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by davidscott1 on 26/03/2016.
 */
public class NumberView extends View {

    private static final int DEFAULT_DURATION = 300;
    private static final int DEFAULT_VELOCITY = 10;
    private static final int DEFAULT_STROKE_WIDTH = 5;

    public static final int DELAY_MILLISECONDS = 32;

    private Path path;
    private PathMeasure pathMeasure;
    private Paint paint;
    private HashMap<Integer, Paint> paintArray;
    private HashMap<Integer, Path> pathArray;
    private float animationOffset = 0;
    private float[][] points;
    private int currentNumber = -1;

    private int animationDuration = DEFAULT_DURATION;
    private int velocity = DEFAULT_VELOCITY;
    private int strokeWidth = DEFAULT_STROKE_WIDTH;


    private static final Property<NumberView, float[][]> POINTS_PROPERTY = new Property<NumberView, float[][]>(float[][].class, "points") {
        @Override
        public float[][] get(NumberView numberView) {
            return numberView.getPoints();
        }

        @Override
        public void set(NumberView numberView, float[][] value) {
            numberView.setPoints(value);
        }
    };



    public NumberView(Context context) {
        super(context);
        init();
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.NumberView);
        int number =typedArray.getInt(R.styleable.NumberView_number, 0);
        animationDuration = typedArray.getInt(R.styleable.NumberView_duration, DEFAULT_DURATION);
        velocity = typedArray.getInt(R.styleable.NumberView_velocity, DEFAULT_VELOCITY);
        strokeWidth = typedArray.getInt(R.styleable.NumberView_strokeWidth, DEFAULT_STROKE_WIDTH);
        typedArray.recycle();

        init();

        setNumber(number);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){
        paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        path = new Path();
        pathMeasure = new PathMeasure();
        pathArray = new HashMap<>();



        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(239, 83, 80));
        colours.add(Color.rgb(92, 107, 192));
        colours.add(Color.rgb(38, 198, 218));
        colours.add(Color.rgb(140, 242, 242));

        setColours(colours);
    }




    public void setNumber(int number) throws InvalidParameterException {

        if(currentNumber == -1) {
            //set initial number

            points = NumberUtils.getPoints(number);

            postInvalidate();
        } else {
            //Animate

            float[][] startPoints = NumberUtils.getPoints(currentNumber);
            float[][] endPoints = NumberUtils.getPoints(number);

            ObjectAnimator anim = ObjectAnimator.ofObject(this, POINTS_PROPERTY,
                    new NumberEvaluator(), startPoints, endPoints);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());

            anim.setDuration(animationDuration);
            anim.start();

        }


        currentNumber = number;
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

    public void setDuration(int duration){
        this.animationDuration = duration;
    }

    public void setVelocity(int velocity){
        this.velocity = velocity;
    }

    public void setStrokeWidth(int strokeWidth){
        this.strokeWidth = strokeWidth;
        paint.setStrokeWidth(strokeWidth);

        for(int i = 0; i < paintArray.size(); i++){
            paintArray.get(i).setStrokeWidth(strokeWidth);
        }
    }





    private float[][] getPoints() {
        return points;
    }

    private void setPoints(float[][] points) {
        this.points = points;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (points == null) return;

        int length = points.length;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        float minDimen = height > width ? width : height;
        minDimen -= 10;


        //Create the path

        path.reset();
        path.moveTo(minDimen * points[0][0], minDimen * points[0][1]);
        for (int i = 1; i < length; i += 3) {
            path.cubicTo(minDimen * points[i][0], minDimen * points[i][1],
                    minDimen * points[i + 1][0], minDimen * points[i + 1][1],
                    minDimen * points[i + 2][0], minDimen * points[i + 2][1]);
        }
        path.close();

        if(this.paintArray != null && this.paintArray.size() > 1) {

            for (LineSegment lineSegment : getSegments(path)) {
                canvas.drawPath(lineSegment.path, lineSegment.paint);
            }

            updateOffset();

            postInvalidateDelayed(DELAY_MILLISECONDS);
        } else {
            canvas.drawPath(path, paint);
        }


    }

    private void updateOffset() {
        animationOffset += velocity;

        if(animationOffset > pathMeasure.getLength()){
            animationOffset = 0;
        }
    }


    private ArrayList<LineSegment> getSegments(Path path){


        pathMeasure.setPath(path, true);

        float length = pathMeasure.getLength();

        float segmentLength = length / this.paintArray.size();

        ArrayList<LineSegment> segmentArray = new ArrayList<>();

        int j= 0;

        for(int i = 0; i < this.paintArray.size(); i++){

            float start = animationOffset + (segmentLength*i);
            float end =  start + segmentLength;


            if(end > length ){
                Path p = getSegmentPath(j++);
                pathMeasure.getSegment(start, length, p, true);

                LineSegment segment = new LineSegment();
                segment.path = p;
                segment.paint = paintArray.get(i);

                Path p2 = getSegmentPath(j++);
                pathMeasure.getSegment(start-length, end - length, p2, true);

                LineSegment segment2 = new LineSegment();
                segment2.path = p2;
                segment2.paint = paintArray.get(i);

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

        return segmentArray;

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



    class LineSegment{
        Path path;
        Paint paint;
    }


}
