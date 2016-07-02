package com.example.wenzhao.helpinghand.ble.pro.Fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;
import com.example.wenzhao.helpinghand.ble.pro.Database.ChildInfo;

public class InputFragment extends Fragment {
    public static ChildInfo curChild;

    public static String ChildName;
    public static String WeakArm;
    public static boolean AbleToRead;

    Button btnNextInput;
    EditText nickNameText;
    CheckBox checkBoxLeft;
    CheckBox checkBoxRight;
    CheckBox checkBoxYes;
    CheckBox checkBoxNo;
    int checker = 0;

    public static InputFragment newInstance(){
        InputFragment fragment = new InputFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        curChild = new ChildInfo();

        View view = inflater.inflate(R.layout.fragment_input, container, false);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SimpleLife.ttf");
        TextView t0 = (TextView) view.findViewById(R.id.textView2);
        t0.setTypeface(font);
        TextView t1 = (TextView) view.findViewById(R.id.textView3);
        t1.setTypeface(font);
        TextView t2 = (TextView) view.findViewById(R.id.textView6);
        t2.setTypeface(font);


        btnNextInput = (Button)view.findViewById(R.id.btn_next_input);
        btnNextInput.setTypeface(font);
        nickNameText = (EditText)view.findViewById(R.id.editText);
        nickNameText.setTypeface(font);
        checkBoxLeft = (CheckBox)view.findViewById(R.id.checkBoxLeft);
        checkBoxLeft.setTypeface(font);
        checkBoxRight = (CheckBox)view.findViewById(R.id.checkBoxRight);
        checkBoxRight.setTypeface(font);
        checkBoxYes = (CheckBox)view.findViewById(R.id.checkBoxYes);
        checkBoxYes.setTypeface(font);
        checkBoxNo = (CheckBox)view.findViewById(R.id.checkBoxNo);
        checkBoxNo.setTypeface(font);
        checkBoxLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checkBoxRight.setChecked(false);
                    WeakArm = "Right";
                }
            }
        });

        checkBoxRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checkBoxLeft.setChecked(false);
                    WeakArm = "Left";
                }
            }
        });

        checkBoxYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBoxNo.setChecked(false);
                    AbleToRead = true;
                    checker = 1;
                } else {
                    AbleToRead = false;
                    checker = 0;
                }
            }
        });

        checkBoxNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBoxYes.setChecked(false);
                    AbleToRead =false;
                    checker = 1;
                } else {
                    AbleToRead = false;
                    checker = 0;
                }
            }
        });

        btnNextInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempName = nickNameText.getText().toString();
                ChildName = tempName;
                if (ChildName.isEmpty()) {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
                    adBuilder.setMessage("Please Enter your nickname").setCancelable(true);
                    adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = adBuilder.create();
                    alertDialog.show();
                    return;
                }

                if (WeakArm == null) {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
                    adBuilder.setMessage("Please Enter your dominant arm").setCancelable(true);
                    adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = adBuilder.create();
                    alertDialog.show();
                    return;
                }

                if (checker == 0) {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
                    adBuilder.setMessage("Please choose able to read").setCancelable(true);
                    adBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = adBuilder.create();
                    alertDialog.show();
                    return;
                }
                getActivity().finish();
            }
        });

        return view;
    }



}
