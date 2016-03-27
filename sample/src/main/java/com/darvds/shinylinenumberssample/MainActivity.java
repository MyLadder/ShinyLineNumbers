package com.darvds.shinylinenumberssample;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.darvds.shinylinenumbers.views.NumberView;


public class MainActivity extends AppCompatActivity {

    private int number = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final NumberView nv = (NumberView) findViewById(R.id.numberView);

        final Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                number++;
                if(number>9){
                    number = 0;
                }

                nv.setNumber(number);

                handler.postDelayed(this, 1000);
            }
        };

        r.run();



     //   NumberView numberView = (NumberView) findViewById(R.id.number_view0);
     //   NumberView numberView1 = (NumberView) findViewById(R.id.number_view1);
     //   NumberView numberView4 = (NumberView) findViewById(R.id.number_view4);
     //   NumberView numberView7 = (NumberView) findViewById(R.id.number_view7);

//        ArrayList<Integer> colours = new ArrayList<>();
//        colours.add(Color.rgb(239,83,80));
//        colours.add(Color.rgb(92,107,192));
//        colours.add(Color.rgb(38,198,218));
//        colours.add(Color.rgb(140,242,242));
//
//        numberView.setColours(colours);
//        numberView1.setColours(colours);
//        numberView7.setColours(colours);
//        numberView4.setColours(colours);
//
//        numberView.setNumber(0);
//        numberView1.setNumber(1);
//        numberView4.setNumber(4);
//        numberView7.setNumber(7);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
