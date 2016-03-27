package com.darvds.shinylinenumbers.animation;

import android.animation.TypeEvaluator;
import android.service.wallpaper.WallpaperService;

/**
 * Created by davidscott1 on 27/03/2016.
 */
public class NumberEvaluator implements TypeEvaluator<float[][]> {
    private float[][] _cachedPoints = null;

    private float[][] startValue;
    private float[][] endValue;

    @Override
    public float[][] evaluate(float fraction, float[][] startValue, float[][] endValue) {
    //    int pointsCountOld = startValue.length;
        int pointsCountNew = endValue.length;
        initCache(pointsCountNew);

        this.startValue = startValue;
        this.endValue = endValue;


        for(int i = 0; i< pointsCountNew; i++){
            _cachedPoints[i][0] = getStartValue(i, 0) + fraction * (endValue[i][0] - getStartValue(i, 0));
            _cachedPoints[i][1] = getStartValue(i, 1) + fraction * (endValue[i][1] - getStartValue(i, 1));

        }




        //TODO
        //Think I can somehow move the multiple points to the same one in the existing when the second has more points
        //If the second path has less points then it would do the same thing in reverse
        //Might need some nested for loops or something to do this
        //maybe with the points number divided by the others





        return _cachedPoints;
    }

    private void initCache(int pointsCount) {
        if(_cachedPoints == null || _cachedPoints.length != pointsCount) {
            _cachedPoints = new float[pointsCount][2];
        }
    }

    private float getStartValue(int index, int secondIndex){

        double pointsPerOldPoints = (double) startValue.length / (double) endValue.length;

        int position = (int) (index * pointsPerOldPoints);

        return startValue[position][secondIndex];

    }

}