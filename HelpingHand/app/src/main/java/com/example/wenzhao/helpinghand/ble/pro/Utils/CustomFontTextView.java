package com.example.wenzhao.helpinghand.ble.pro.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by lenovo on 2016/3/22.
 */
public class CustomFontTextView extends TextView{
    public CustomFontTextView(Context context) {
        super(context);
        init(context);
    }
    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CustomFontTextView(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
        init(context);
    }
    private void init(Context context) {
        AssetManager assertMgr = context.getAssets();
        Typeface font = Typeface.createFromAsset(assertMgr,"fonts/snoopy_reg-webfont.otf" );
        setTypeface(font);

    }

}
