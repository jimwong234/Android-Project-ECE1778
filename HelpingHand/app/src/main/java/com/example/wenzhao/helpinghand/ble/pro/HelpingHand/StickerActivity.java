package com.example.wenzhao.helpinghand.ble.pro.HelpingHand;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;
import com.example.wenzhao.helpinghand.ble.pro.Database.DatabaseHandler;
import com.example.wenzhao.helpinghand.ble.pro.Fragment.ActivityChoiceFragment;

public class StickerActivity extends Activity {
    private TextView activityText;
    private TextView alertText;
    private ImageView s1;
    private ImageView s2;
    private ImageView s3;
    private ImageView s4;
    private ImageView s5;
    private ImageView s6;
    private Button backBtn;

    private ColorMatrix matrix;
    private ColorMatrixColorFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);
        activityText = (TextView)findViewById(R.id.textView21);
        alertText = (TextView)findViewById(R.id.textView22);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/snoopy_reg-webfont.otf");
        Typeface font1 = Typeface.createFromAsset(getAssets(), "fonts/SimpleLife.ttf");
        activityText.setTypeface(font1);
        alertText.setTypeface(font);

        matrix = new ColorMatrix();
        matrix.setSaturation(0);
        filter = new ColorMatrixColorFilter(matrix);

        backBtn = (Button)findViewById(R.id.btn_sback);

        s1 = (ImageView)findViewById(R.id.imageView3);
        s2 = (ImageView)findViewById(R.id.imageView4);
        s3 = (ImageView)findViewById(R.id.imageView5);
        s4 = (ImageView)findViewById(R.id.imageView6);
        s5 = (ImageView)findViewById(R.id.imageView7);
        s6 = (ImageView)findViewById(R.id.imageView8);
        s1.setImageResource(R.drawable.s1);
        s2.setImageResource(R.drawable.s2);
        s3.setImageResource(R.drawable.s3);
        s4.setImageResource(R.drawable.s4);
        s5.setImageResource(R.drawable.s5);
        s6.setImageResource(R.drawable.s6);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityText.setText(ActivityChoiceFragment.TableActivity);
        int con = DatabaseHandler.getHandler().getActivityValuesCount(ActivityChoiceFragment.TableActivity,(float)0.4);
        if(con >= 30){
            alertText.setText("Great Job! You have unlocked all stickers!");
            s1.setColorFilter(1);
            s2.setColorFilter(1);
            s3.setColorFilter(1);
            s4.setColorFilter(1);
            s5.setColorFilter(1);
            s6.setColorFilter(1);
        }else if(con >= 25){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(30 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(1);
            s2.setColorFilter(1);
            s3.setColorFilter(1);
            s4.setColorFilter(1);
            s5.setColorFilter(1);
            s6.setColorFilter(filter);
        }else if(con >= 20){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(25 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(1);
            s2.setColorFilter(1);
            s3.setColorFilter(1);
            s4.setColorFilter(1);
            s5.setColorFilter(filter);
            s6.setColorFilter(filter);
        }else if(con >= 15){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(20 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(1);
            s2.setColorFilter(1);
            s3.setColorFilter(1);
            s4.setColorFilter(filter);
            s5.setColorFilter(filter);
            s6.setColorFilter(filter);
        }else if(con >= 10){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(15 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(1);
            s2.setColorFilter(1);
            s3.setColorFilter(filter);
            s4.setColorFilter(filter);
            s5.setColorFilter(filter);
            s6.setColorFilter(filter);
        }else if(con >= 5){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(10 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(1);
            s2.setColorFilter(filter);
            s3.setColorFilter(filter);
            s4.setColorFilter(filter);
            s5.setColorFilter(filter);
            s6.setColorFilter(filter);
        }else if(con >= 0){
            alertText.setText("Keep going! \nComplete "+ String.valueOf(5 - con)+ " times activities(above 40%) to unlock the next sticker!");
            s1.setColorFilter(filter);
            s2.setColorFilter(filter);
            s3.setColorFilter(filter);
            s4.setColorFilter(filter);
            s5.setColorFilter(filter);
            s6.setColorFilter(filter);
        }
    }
}
