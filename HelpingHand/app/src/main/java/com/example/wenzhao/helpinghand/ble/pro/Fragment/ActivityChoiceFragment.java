package com.example.wenzhao.helpinghand.ble.pro.Fragment;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;


public class ActivityChoiceFragment extends Fragment {
    TextView textViewName;
    TextView textViewWeakArm;
    TextView textViewRead;
    TextView textViewChoice;
    RadioGroup radioGroup;
    RadioButton radioButton;
    //public String text;
    public static String TableActivity;

    Button btnNextChoice;

    public static ActivityChoiceFragment newInstance(){
        ActivityChoiceFragment fragment = new ActivityChoiceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_activity_choice, container, false);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SimpleLife.ttf");
        TextView t0 = (TextView) view.findViewById(R.id.textView10);
        t0.setTypeface(font);
        TextView t1 = (TextView) view.findViewById(R.id.textView11);
        t1.setTypeface(font);
        TextView t2 = (TextView) view.findViewById(R.id.textView12);
        t2.setTypeface(font);
        TextView t3 = (TextView) view.findViewById(R.id.textView16);
        t3.setTypeface(font);

        textViewName = (TextView)view.findViewById(R.id.textView12);
        textViewWeakArm = (TextView)view.findViewById(R.id.textView14);
        textViewRead = (TextView)view.findViewById(R.id.textView15);
        textViewChoice = (TextView)view.findViewById(R.id.textView16);
        btnNextChoice = (Button)view.findViewById(R.id.btn_next_choice);
        btnNextChoice.setTypeface(font);
        radioGroup = (RadioGroup)view.findViewById(R.id.myRadioGroup);

        RadioButton radioButton0 = (RadioButton)view.findViewById(R.id.radioGroupButton0);
        RadioButton radioButton1 = (RadioButton)view.findViewById(R.id.radioGroupButton1);
        RadioButton radioButton2 = (RadioButton)view.findViewById(R.id.radioGroupButton2);
        RadioButton radioButton3 = (RadioButton)view.findViewById(R.id.radioGroupButton3);
        RadioButton radioButton4 = (RadioButton)view.findViewById(R.id.radioGroupButton4);
        Typeface font1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/snoopy_reg-webfont.otf");
        radioButton0.setTypeface(font1);
        radioButton1.setTypeface(font1);
        radioButton2.setTypeface(font1);
        radioButton3.setTypeface(font1);
        radioButton4.setTypeface(font1);
        radioButton0.setTextSize(20);
        radioButton1.setTextSize(20);
        radioButton2.setTextSize(20);
        radioButton3.setTextSize(20);
        radioButton4.setTextSize(20);

        radioButton = (RadioButton)view.findViewById(radioGroup.getCheckedRadioButtonId());
        TableActivity = radioButton.getText().toString();
        Log.i("radioGroup",TableActivity);

        textViewName.setText("For "+InputFragment.ChildName+":");
        textViewWeakArm.setText("Weak arm: "+InputFragment.WeakArm);

        String temp = null;
        if(InputFragment.AbleToRead){
            temp = "Yes";
        }else{
            temp = "No";
        }
        textViewRead.setText("Able to read: "+ temp);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //在这个函数里面用来改变选择的radioButton的数值，以及与其值相关的 //任何操作，详见下文
                selectRadioBtn(view);
            }
        });


        btnNextChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment processFragment = ProcessFragment.newInstance();
                ft.replace(R.id.DevContainer, processFragment);
                ft.addToBackStack("Switch to Process Fragment");
                ft.commit();
            }
        });

        return view;
    }


    private void selectRadioBtn(View view){
        radioButton = (RadioButton)view.findViewById(radioGroup.getCheckedRadioButtonId());
        TableActivity = radioButton.getText().toString();
        Log.i("radioGroup",TableActivity);

    }
}
