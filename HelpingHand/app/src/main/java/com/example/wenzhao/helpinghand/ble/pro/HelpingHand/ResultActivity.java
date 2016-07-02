package com.example.wenzhao.helpinghand.ble.pro.HelpingHand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;
import com.example.wenzhao.helpinghand.ble.pro.Database.ChildInfo;
import com.example.wenzhao.helpinghand.ble.pro.Database.DatabaseHandler;
import com.example.wenzhao.helpinghand.ble.pro.Fragment.ActivityChoiceFragment;
import com.example.wenzhao.helpinghand.ble.pro.Fragment.InputFragment;
import com.example.wenzhao.helpinghand.ble.pro.Fragment.ProcessFragment;

public class ResultActivity extends Activity {
    private Button btnReplay;
    private Button btnChange;
    private Button btnShow;
    private Button btnSticker;
    boolean speakOnce;

    public static int finalRatio = 0;
    private String text;
    TextView resultText;
    RatingBar ratingBar;
    ImageView imageView;

    public static int Replace = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        speakOnce = true;
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/SimpleLife.ttf");
        Typeface font1 = Typeface.createFromAsset(getAssets(), "fonts/snoopy_reg-webfont.otf");
        resultText = (TextView)findViewById(R.id.textView17);
        resultText.setTypeface(font1);
        btnReplay = (Button)findViewById(R.id.btn_replay);
        btnReplay.setTypeface(font);
        btnChange = (Button)findViewById(R.id.btn_exit);
        btnChange.setTypeface(font);
        btnShow = (Button)findViewById(R.id.show);
        btnShow.setTypeface(font);
        btnSticker = (Button)findViewById(R.id.sticker_btn);
        btnSticker.setTypeface(font);

        ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        imageView = (ImageView)findViewById(R.id.imageView2);

        setImage(finalRatio);
        setRating(finalRatio);

        //add to database
        ChildInfo newentry = new ChildInfo(ActivityChoiceFragment.TableActivity,finalRatio,ProcessFragment.time);
        DatabaseHandler.getHandler().addValue(newentry);
        Log.e("error", String.valueOf(DatabaseHandler.getHandler().getDBCount()));
            resultText.setText("Your " + InputFragment.WeakArm + " hand did "
                    + String.format("%d", finalRatio) + "% of the work! Try again to beat your" +
                    " score.");

        text = InputFragment.ChildName+". Try again to beat your score.";



        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Replace = 1;
                finish();
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this,ShowDataActivity.class);
                startActivity(intent);
            }
        });

        btnSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this,StickerActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!InputFragment.AbleToRead && speakOnce) {
            final Handler handler = new Handler();
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                   if (finalRatio>= 40)
                   { ProcessFragment.getTts().speak("Awesome! "+text, TextToSpeech.QUEUE_FLUSH, null);
                   }else{
                    ProcessFragment.getTts().speak(text, TextToSpeech.QUEUE_FLUSH, null);
                   }
                }
            };
            handler.postDelayed(mRunnable, 1000);
            speakOnce = false;
        }
    }


    private void setRating(double rating){
        if(rating >=0 && rating <5) ratingBar.setRating((float)0.5);
        if(rating >=5 && rating <10) ratingBar.setRating((float)1);
        if(rating >=10 && rating <15) ratingBar.setRating((float)1.5);
        if(rating >=15 && rating <20) ratingBar.setRating((float)2);
        if(rating >=20 && rating <25) ratingBar.setRating((float)2.5);
        if(rating >=25 && rating <30) ratingBar.setRating((float)3);
        if(rating >=30 && rating <35) ratingBar.setRating((float)3.5);
        if(rating >=35 && rating <40) ratingBar.setRating((float)4);
        if(rating >=40 && rating <45) ratingBar.setRating((float)4.5);
        if(rating >=45) ratingBar.setRating((float)5);
    }

    private void setImage(double rating){
        if(rating >=0) imageView.setImageResource(R.drawable.sadface);
        if(rating >=25) imageView.setImageResource(R.drawable.smileface);
    }

}
