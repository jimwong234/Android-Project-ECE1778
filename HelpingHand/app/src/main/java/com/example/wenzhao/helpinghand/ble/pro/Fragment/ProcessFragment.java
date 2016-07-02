package com.example.wenzhao.helpinghand.ble.pro.Fragment;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ti.ble.sensortag.R;
import com.example.wenzhao.helpinghand.ble.pro.ACCInfo.SensorTagGatt;
import com.example.wenzhao.helpinghand.ble.pro.BLEManager.BluetoothLeService;
import com.example.wenzhao.helpinghand.ble.pro.BLEManager.GenericBluetoothProfile;
import com.example.wenzhao.helpinghand.ble.pro.HelpingHand.MainActivity;
import com.example.wenzhao.helpinghand.ble.pro.HelpingHand.ResultActivity;
import com.example.wenzhao.helpinghand.ble.pro.Utils.Player;
import com.example.wenzhao.helpinghand.ble.pro.Utils.Player2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ProcessFragment extends Fragment {
    // BLE
    private BluetoothLeService mBtLeService = null;
    private ArrayList<BluetoothDevice> mBluetoothDevice = null;
    private ArrayList<BluetoothGatt> mBtGatt = null;
    private List<GenericBluetoothProfile> mProfiles;
    private  double realtimesum1;
    private  double realtimesum2;
    public static TextToSpeech mTts;
    private boolean speakFirstTime = true;
    private final static int CHECK_CODE = 1;

    //GUI
    private TextView infoText = null;
    private TextView ratioText1 = null;
    private TextView indicatorText = null;
    private LinearLayout linearLayout;
    private Player myView;
    private Player2 myView2;
    private int number;
    private ImageView activity_image;

    private Button btnFinish;
    private Button btnNext;
    public int ratio;
    private double notifyTimes = 0;

    public static float time = 0;
    long startTime;

    //low-pass filter
    final double alpha = 0.8;
    private double[] gravity1 = new double[]{0,0,0};
    private double[] gravity2 = new double[]{0,0,0};

    public static ProcessFragment newInstance() {
        ProcessFragment fragment = new ProcessFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        startTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        // BLE
        mBtLeService = BluetoothLeService.getInstance();
        mBluetoothDevice = BluetoothLeService.getDevice();
        mBtGatt = BluetoothLeService.getBtGatt();
        mProfiles = new ArrayList<GenericBluetoothProfile>();
        realtimesum1 = 0;
        realtimesum1 = 0;
        number = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SimpleLife.ttf");
        Typeface font1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/snoopy_reg-webfont.otf");
        View view = inflater.inflate(R.layout.fragment_process, container, false);
        ratioText1 = (TextView) view.findViewById(R.id.ratio1);
        ratioText1.setTypeface(font1);
        infoText = (TextView) view.findViewById(R.id.textView18);
        indicatorText = (TextView) view.findViewById(R.id.textView19);
        indicatorText.setTypeface(font);
        btnFinish = (Button) view.findViewById(R.id.btn_finish);
        btnFinish.setTypeface(font);
        btnNext = (Button) view.findViewById(R.id.btn_next);
        btnNext.setTypeface(font);
        linearLayout = (LinearLayout)view.findViewById(R.id.linear);
        activity_image = (ImageView)view.findViewById(R.id.activity_image);
        System.out.println(ActivityChoiceFragment.TableActivity);
        if(ActivityChoiceFragment.TableActivity.equals("Block tower")){
            myView = new Player(this.getActivity(), number);
            linearLayout.addView(myView);
        }
        if(ActivityChoiceFragment.TableActivity.equals("Coin sorting")){
            Resources res = this.getResources();
            activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.coins));
        }
        if(ActivityChoiceFragment.TableActivity.equals("Playdough fun")){
            Resources res = this.getResources();
            activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd1));
        }

        if(ActivityChoiceFragment.TableActivity.equals("Necklace making")){
            Resources res = this.getResources();
            activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n1));
        }
        if(ActivityChoiceFragment.TableActivity.equals("Pantomime (on-screen)")){
            myView2 = new Player2(this.getActivity(), number);
            linearLayout.addView(myView2);
        }

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                for (BluetoothGattService s : MainActivity.serviceList1) {
                    if (s.getUuid().toString().compareTo(SensorTagGatt.UUID_MOV_SERV.toString()) == 0) {
                        GenericBluetoothProfile mov1 = new GenericBluetoothProfile(getActivity(), mBluetoothDevice.get(0),mBtGatt.get(0), s, mBtLeService);
                        mProfiles.add(mov1);
                    }
                }
                for (BluetoothGattService s : MainActivity.serviceList2) {
                    if (s.getUuid().toString().compareTo(SensorTagGatt.UUID_MOV_SERV.toString()) == 0) {
                        GenericBluetoothProfile mov2 = new GenericBluetoothProfile(getActivity(), mBluetoothDevice.get(1),mBtGatt.get(1), s, mBtLeService);
                        mProfiles.add(mov2);
                    }
                }
                for (final GenericBluetoothProfile p : mProfiles) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            p.enableService();
                            Log.i("Device Activity", " enabled");
                        }
                    });
                }


            }
        });
        worker.start();

        checkTts();

        final Intent mResultIntent = new Intent(getActivity(), ResultActivity.class);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long endTime = System.currentTimeMillis() - startTime;
                time = (float) endTime / 1000;
                ResultActivity.finalRatio = ratio;
                getActivity().unregisterReceiver(mGattUpdateReceiver1);
                startActivity(mResultIntent);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityChoiceFragment.TableActivity.equals("Block tower")){
                    number = (number+1)%3;
                    linearLayout.removeAllViews();
                    Player newplayer = new Player(getActivity(),number);
                    linearLayout.addView(newplayer);
                }
                if(ActivityChoiceFragment.TableActivity.equals("Playdough fun")){
                    Resources res = getActivity().getResources();
                    number = (number+1)%5;
                    if (number == 0 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd1));
                    }else if (number == 1 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd2));
                    }else if (number == 2 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd3));
                    }else if (number == 3 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd4));
                    }else if (number == 4 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.pd5));
                    }
                }

                if(ActivityChoiceFragment.TableActivity.equals("Necklace making")){
                    Resources res = getActivity().getResources();
                    number = (number+1)%8;
                    if (number == 0 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n1));
                    }else if (number == 1 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n2));
                    }else if (number == 2 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n3));
                    }else if (number == 3 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n4));
                    }else if (number == 4 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n5));
                    }else if (number == 5 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n6));
                    }else if (number == 6 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n7));
                    }else if (number == 7 ){
                        activity_image.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.n8));
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        gravity1[0]= 0.0;
        gravity1[1]= 0.0;
        gravity1[2]= 0.0;
        gravity2[0]= 0.0;
        gravity2[1]= 0.0;
        gravity2[2]= 0.0;
        realtimesum1 = 0;
        realtimesum2 = 0;
        notifyTimes = 0;
        if(ResultActivity.Replace == 1){
            ResultActivity.Replace = 0;
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            Fragment activityChoiceFragment = ActivityChoiceFragment.newInstance();
            ft.replace(R.id.DevContainer, activityChoiceFragment);
            ft.addToBackStack("Switch to Activity Choice Fragment");
            ft.commit();
        }
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLeService.ACTION_DATA_READ);
        fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY1);
        getActivity().registerReceiver(mGattUpdateReceiver1, fi);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBtGatt.size() != 0) {
            for (BluetoothGatt gatt : mBtGatt) {
                gatt.close();
                gatt = null;
            }
        }
        mBtGatt = null;
        mBluetoothDevice =null;
        getActivity().unregisterReceiver(mGattUpdateReceiver1);
        this.mProfiles = null;
    }

    private final BroadcastReceiver mGattUpdateReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
                    BluetoothGatt.GATT_SUCCESS);

            if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
                //Log.i("Device Activity","  # 1 sensor notified");
                // Notification
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                for (BluetoothGattCharacteristic tempC : MainActivity.charList1) {
                    if ((tempC.getUuid().toString().equals(uuidStr))) {
                        GenericBluetoothProfile p = mProfiles.get(0);
                        if (p.isDataC(tempC)) {
                            p.didUpdateValueForCharacteristic(tempC, 0);
                        }
                        break;
                    }
                }
            }

            if (BluetoothLeService.ACTION_DATA_NOTIFY1.equals(action)) {
                String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                for (BluetoothGattCharacteristic tempC : MainActivity.charList2) {
                    if ((tempC.getUuid().toString().equals(uuidStr))) {
                        GenericBluetoothProfile p = mProfiles.get(1);
                        if (p.isDataC(tempC)) {
                            p.didUpdateValueForCharacteristic(tempC, 1);
                            double ax2 = GenericBluetoothProfile.accData2.x;
                            double ay2 = GenericBluetoothProfile.accData2.y;
                            double az2 = GenericBluetoothProfile.accData2.z;
                            double ax1 = GenericBluetoothProfile.accData1.x;
                            double ay1 = GenericBluetoothProfile.accData1.y;
                            double az1 = GenericBluetoothProfile.accData1.z;

                            //low-pass filter
                            gravity1[0] = alpha * gravity1[0] + (1 - alpha) * ax1;
                            gravity1[1] = alpha * gravity1[1] + (1 - alpha) * ay1;
                            gravity1[2] = alpha * gravity1[2] + (1 - alpha) * az1;
                            ax1 = ax1 - gravity1[0];
                            ay1 = ay1 - gravity1[1];
                            az1 = az1 - gravity1[2];
                            gravity2[0] = alpha * gravity2[0] + (1 - alpha) * ax2;
                            gravity2[1] = alpha * gravity2[1] + (1 - alpha) * ay2;
                            gravity2[2] = alpha * gravity2[2] + (1 - alpha) * az2;
                            ax2 = ax2 - gravity2[0];
                            ay2 = ay2 - gravity2[1];
                            az2 = az2 - gravity2[2];

                            if((Math.sqrt(ax2*ax2 + ay2*ay2 + az2*az2)>0.06)&&(notifyTimes == 15)){
                                realtimesum2 += Math.sqrt(ax2 * ax2 + ay2 * ay2 + az2 * az2);
                                Log.e("first",String.valueOf(Math.sqrt(ax2 * ax2 + ay2 * ay2 + az2 * az2)));
                            }

                            if((Math.sqrt(ax1*ax1 + ay1*ay1 + az1*az1)>0.06)&&(notifyTimes == 15)){
                                realtimesum1 +=Math.sqrt(ax1*ax1 + ay1*ay1 + az1*az1);
                                Log.e("second",String.valueOf(Math.sqrt(ax1*ax1 + ay1*ay1 + az1*az1)));
                            }
                            if(notifyTimes <= 14){
                                notifyTimes++;
                                realtimesum2 = 1;
                                realtimesum1 = 1;
                                indicatorText.setText("Ready...");
                            }else{
                                indicatorText.setText("Go!");
                            }

                            Log.e("times",String.valueOf(notifyTimes));

                            if (InputFragment.WeakArm == "Left") {
                                ratio = (int)(100 *realtimesum1/(realtimesum1+realtimesum2));
                            }else {
                                ratio = (int)(100 *realtimesum2/(realtimesum1+realtimesum2));
                            }
                            ratioText1.setText("Weak arm(" + InputFragment.WeakArm
                                    + ")" + String.format(":%d", ratio) + "%");
                            // real-time feedback
                            if(ratio<20.0) {
                                    if ((!InputFragment.AbleToRead)&&(speakFirstTime)) {
                                        String text = InputFragment.ChildName + ". Please use your " + InputFragment.WeakArm + " hand more.";
                                        sayTts(text);
                                        speakFirstTime = false;
                                    } else if(!InputFragment.AbleToRead){
                                        String text = " Please use your " + InputFragment.WeakArm + " hand more.";
                                        infoText.setText("Keep going, " + InputFragment.ChildName + "\n "
                                                + text);
                                    }else if(InputFragment.AbleToRead){
                                        String text = ". Please use your " + InputFragment.WeakArm + " hand more.";
                                        infoText.setText("Keep going, " + InputFragment.ChildName + "\n "
                                                + text);
                                    }
                            }
                            else {
                                infoText.setText("");
                                speakFirstTime = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
    };

    //~~~~~~~~~~~~~~~~~~~~~~TextToSpeech~~~~~~~~~~~~~~~~~~~~~~~~~//
    public void checkTts(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, CHECK_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

                mTts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {

                        if(status == TextToSpeech.SUCCESS){

                            int result = mTts.setLanguage(Locale.US);

                            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                                Log.e("error","not supported");
                            }
                        }
                    }
                });
            }else{
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
    private void sayTts(String text){
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public static TextToSpeech getTts(){
        return mTts;
    }

}
