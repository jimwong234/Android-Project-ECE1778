package com.example.wenzhao.helpinghand.ble.pro.HelpingHand;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ti.ble.sensortag.R;
import com.example.wenzhao.helpinghand.ble.pro.Database.ChildInfo;
import com.example.wenzhao.helpinghand.ble.pro.Database.DatabaseHandler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ShowDataActivity extends Activity {
    Button btnBack;
    Button btnClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/SimpleLife.ttf");
        btnClear = (Button)findViewById(R.id.clear);
        btnClear.setTypeface(font);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setTypeface(font);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler.getHandler().deleteDatabase();
                Toast.makeText(ShowDataActivity.this, "Already Cleared", Toast.LENGTH_SHORT).show();
                onResume();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();
        ArrayList<Entry> entries3 = new ArrayList<>();
        ArrayList<Entry> entries4 = new ArrayList<>();
        ArrayList<Entry> entries5 = new ArrayList<>();

        int blockCount = DatabaseHandler.getHandler().getActivityCount("Block tower");
        int coinCount = DatabaseHandler.getHandler().getActivityCount("Coin sorting");
        int playdoughCount = DatabaseHandler.getHandler().getActivityCount("Playdough fun");
        int necklaceCount = DatabaseHandler.getHandler().getActivityCount("Necklace making");
        int pantomimeCount = DatabaseHandler.getHandler().getActivityCount("Pantomime (on-screen)");
        int maxCount = Math.max(blockCount,coinCount);
        maxCount = Math.max(maxCount,playdoughCount);
        maxCount = Math.max(maxCount,necklaceCount);
        maxCount = Math.max(maxCount,pantomimeCount);

        List<ChildInfo> tempBlockList = DatabaseHandler.getHandler().getActivityValues("Block tower");
        if(blockCount > 0){
            for(int i = 0;i < blockCount;i++){
                entries1.add(new Entry((float)tempBlockList.get(i).getFinalRatio(),i));
            }
        }
        List<ChildInfo> tempCoinList = DatabaseHandler.getHandler().getActivityValues("Coin sorting");
        if(coinCount > 0){
            for(int i = 0;i < coinCount;i++){
                entries2.add(new Entry((float)tempCoinList.get(i).getFinalRatio(), i));
            }
        }
        List<ChildInfo> tempPlaydoughList = DatabaseHandler.getHandler().getActivityValues("Playdough fun");
        if(playdoughCount > 0){
            for(int i = 0;i < playdoughCount;i++){
                entries3.add(new Entry((float)tempPlaydoughList.get(i).getFinalRatio(), i));
            }
        }
        List<ChildInfo> tempNecklaceList = DatabaseHandler.getHandler().getActivityValues("Necklace making");
        if(necklaceCount > 0){
            for(int i = 0;i < necklaceCount;i++){
                entries4.add(new Entry((float)tempNecklaceList.get(i).getFinalRatio(), i));
            }
        }
        List<ChildInfo> tempPantomimeList = DatabaseHandler.getHandler().getActivityValues("Pantomime (on-screen)");
        if(pantomimeCount > 0){
            for(int i = 0;i < pantomimeCount;i++){
                entries5.add(new Entry((float)tempPantomimeList.get(i).getFinalRatio(), i));
            }
        }

        LineDataSet dataSet1 = new LineDataSet(entries1,"Block tower");
        dataSet1.setColors(new int[]{Color.rgb(255, 0, 0)});
        LineDataSet dataSet2 = new LineDataSet(entries2,"Coin sorting");
        dataSet2.setColors(new int[]{Color.rgb(0, 255, 255)});
        LineDataSet dataSet3 = new LineDataSet(entries3,"Playdough fun");
        dataSet3.setColors(new int[]{Color.rgb(105, 139, 34)});
        LineDataSet dataSet4 = new LineDataSet(entries4,"Necklace making");
        dataSet4.setColors(new int[]{Color.rgb(139, 69, 19)});
        LineDataSet dataSet5 = new LineDataSet(entries5,"Pantomime (on-screen)");
        dataSet5.setColors(new int[]{Color.rgb(186, 85, 211)});

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        dataSets.add(dataSet3);
        dataSets.add(dataSet4);
        dataSets.add(dataSet5);

        ArrayList<String> labels = new ArrayList<String>();
        for(int i = 1;i <= maxCount;i++)
            labels.add(String.valueOf(i));

        LineData data = new LineData(labels,dataSets);
        lineChart.setData(data);
        lineChart.animateY(2000);
        lineChart.setDescription("Helping Hand");
        lineChart.invalidate();

    }

}
