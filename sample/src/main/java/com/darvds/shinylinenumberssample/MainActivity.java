package com.darvds.shinylinenumberssample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.darvds.shinylinenumbers.views.NumberView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private int mNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final NumberView nv = (NumberView) findViewById(R.id.numberView);
        ArrayList<Integer> colours = new ArrayList<>();
        colours.add(Color.rgb(239, 83, 80));
        colours.add(Color.rgb(92, 107, 192));
        colours.add(Color.rgb(38, 198, 218));
        colours.add(Color.rgb(140, 242, 242));

        nv.setColours(colours);

        final Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                mNumber++;
                if(mNumber >9){
                    mNumber = 0;
                }

                nv.setNumber(mNumber);

                handler.postDelayed(this, 1000);
            }
        };

        r.run();

    }

}
