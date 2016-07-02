package com.example.wenzhao.helpinghand.ble.pro.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.ti.ble.sensortag.R;

/**
 * Created by lenovo on 2016/3/5.
 */
public class Player extends View{

    int COMPONENT_WIDTH;//控件的宽度
    int COMPONENT_HEIGHT;//控件的高度
    boolean initflag = false;

    Bitmap[] bmp;
    Bitmap[] bmp1;//用来存放图片的数组
    Bitmap[] bmp2;
    Bitmap[] bmp3;
    int currPicIndex = 0;//当前播放图片的ID

    int[] bitmapId;
    int[] bitmapId1;//图片编号ID
    int[] bitmapId2;
    int[] bitmapId3;
    boolean workFlag = true;//播放图片的线程标识位


    public Player(Context context, int number) {
        super(context);
        bitmapId1 = new int[]{R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4, R.drawable.a5, R.drawable.a6,};
        bitmapId2 = new int[]{R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4, R.drawable.b5, R.drawable.b6, R.drawable.b7,};
        bitmapId3 = new int[]{R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9};
        bmp1 = new Bitmap[bitmapId1.length];
        bmp2 = new Bitmap[bitmapId2.length];
        bmp3 = new Bitmap[bitmapId3.length];
        switch (number){
            case 0: initBitmap(bmp1 ,bitmapId1);
                    break;
            case 1:initBitmap(bmp2, bitmapId2);
                break;
            case 2 :initBitmap(bmp3, bitmapId3);
                break;
        }
        new Thread() {
            public void run() {
                // TODO Auto-generated method stub
                while (workFlag) {
                    currPicIndex = (currPicIndex + 1) % bitmapId.length;//更改图片的ID
                    Player.this.postInvalidate();//刷新屏幕，导致屏幕重绘
                    if(currPicIndex == bitmapId.length-1 ){
                        break;
                    }
                    try {
                        Thread.sleep(1500);//到此处暂停3秒钟,然后继续执行run函数,即实现每隔3秒钟刷新屏幕一次
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }

    private void initBitmap(Bitmap localbmp[],int[] localbitmapId) {
        Resources res = this.getResources();
        bmp = localbmp;
        bitmapId = localbitmapId;
        for(int i=0;i<bitmapId.length;i++){
            bmp[i] = BitmapFactory.decodeResource(res, bitmapId[i]);
        }
    }

    @Override
    protected void onDraw(Canvas canvas )
    {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if(!initflag)
        {
            COMPONENT_WIDTH = this.getWidth();
            COMPONENT_HEIGHT = this.getHeight();
            initflag = true;
        }
        canvas.drawBitmap(bmp[currPicIndex], 0, 0, null);//绘制图片
    }
}


