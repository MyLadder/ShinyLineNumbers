package com.darvds.shinywatchface.animation;

import android.util.Property;

import com.darvds.shinywatchface.MyWatchFace;

/**
 * Properties for animating positions of the views
 */
public class AnimationProperties {

    private final Property<MyWatchFace.Engine, Integer> mYPositionProperty;

    private final Property<MyWatchFace.Engine, Integer> mCardSpacerProperty;


    public AnimationProperties(){

        mYPositionProperty = new Property<MyWatchFace.Engine, Integer>(Integer.class, "mCurrentTimeY") {
            @Override
            public Integer get(MyWatchFace.Engine engine) {
                return engine.getCurrentTimeY();
            }

            @Override
            public void set(MyWatchFace.Engine engine, Integer value) {
                engine.setCurrentTimeY(value);
            }
        };

        mCardSpacerProperty = new Property<MyWatchFace.Engine, Integer>(Integer.class, "mPeekCardSpacer") {
            @Override
            public Integer get(MyWatchFace.Engine engine) {
                return engine.getPeekCardSpacer();
            }

            @Override
            public void set(MyWatchFace.Engine engine, Integer value) {
                engine.setPeekCardSpacer(value);
            }
        };

    }

    public Property<MyWatchFace.Engine, Integer> getYPositionProperty() {
        return mYPositionProperty;
    }

    public Property<MyWatchFace.Engine, Integer> getCardSpacerProperty() {
        return mCardSpacerProperty;
    }


}
