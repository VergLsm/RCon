package org.verg.rcon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import serial.protocol.MSP_CMD;
import serial.protocol.RC;


/*
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
*/

public class MainActivity extends Activity {

    private static final int CONNECT = 0;

    private GoService goService;
    private ServiceConnection conn;
    private TextView tvLocal;
    private TextView tvRF;
    private TextView tvRelay;
    private TextView tvRC;
    private Switch swCtrl;
    private Button btnR;
    private Button btnL;
    private ProgressBar pbYaw;
    private ProgressBar pbThrot;
    private ProgressBar hpbThrot;
    private ProgressBar pbRoll;
    private ProgressBar pbPitch;
    private ProgressBar hpbPitch;
    private RadioGroup rgAUX1;
    private RadioGroup rgAUX2;

    private GestureDetector mLGestureDetector;
    private MyOnTouchListener mOnTouchListener;
    private GestureDetector mRGestureDetector;

    /**
     * first touch
     */
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        tvLocal = (TextView) findViewById(R.id.tvLocal);
        tvRF = (TextView) findViewById(R.id.tvRF);
        tvRelay = (TextView) findViewById(R.id.tvRelay);
        tvRC = (TextView) findViewById(R.id.tvRC);
        swCtrl = (Switch) findViewById(R.id.swCtrl);
        btnL = (Button) findViewById(R.id.btnL);
        pbYaw = (ProgressBar) findViewById(R.id.pbYaw);
        hpbThrot = (ProgressBar) findViewById(R.id.hpbThrot);
        pbThrot = (ProgressBar) findViewById(R.id.pbThrot);
        pbPitch = (ProgressBar) findViewById(R.id.pbPitch);
        hpbPitch = (ProgressBar) findViewById(R.id.hpbPitch);
        pbRoll = (ProgressBar) findViewById(R.id.pbRoll);
        rgAUX1 = (RadioGroup) this.findViewById(R.id.rgAUX1);
        rgAUX2 = (RadioGroup) this.findViewById(R.id.rgAUX2);
        btnR = (Button) findViewById(R.id.btnR);
        conn = new GoServiceConnection();

        Intent intent = new Intent(this, GoService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        mOnTouchListener = new MyOnTouchListener();
        mLGestureDetector = new GestureDetector(this,
                new LeftOnGestureListener());
        mRGestureDetector = new GestureDetector(this,
                new RightOnGestureListener());
        mLGestureDetector.setIsLongpressEnabled(false);
        mRGestureDetector.setIsLongpressEnabled(false);
        btnL.setOnTouchListener(mOnTouchListener);
        btnR.setOnTouchListener(mOnTouchListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (null != goService) { // 防止第一次启动时抛出Null point exception
            // goService.startCommunication();
            // swCtrl.setChecked(true);
            tvLocal.setText("\tRC:" + goService.getIntervalTime());
        }
        // Log.d("MainActivity", bytes2Hex(vsp.getSPString()));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // update();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "请按菜单键选择退出", Toast.LENGTH_SHORT).show();
    }

    private void updateGUI(int flag, boolean isSth) {
        switch (flag) {
            case CONNECT: {
                swCtrl.setOnCheckedChangeListener(null);
                if (isSth) {
                    tvLocal.setText("\tRC:" + goService.getIntervalTime());
                    swCtrl.setChecked(true);
                } else {
                    tvLocal.setText("\tRC:" + goService.getIntervalTime());
                    tvRelay.setText(R.string.empty);
                    tvRF.setBackgroundColor(Color.rgb(255, 0, 0));
                    swCtrl.setChecked(false);
                }
                swCtrl.setOnCheckedChangeListener(new SwitchListener());
                swCtrl.setEnabled(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != goService) {
            goService.stopCommunicating();
            // goService.removeSP(msp_raw_rc);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null == goService) {
            Toast.makeText(MainActivity.this, "本机没有蓝牙设备。", Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            finish();
            return true;
        }
        // if (id == R.id.bluetooth_settings) {
        // Intent intent = new Intent(this, BluetoothSetting.class);
        // startActivity(intent);
        // return true;
        // }
        if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GoServiceConnection implements ServiceConnection {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // Service被杀断开才调用这个
            goService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
            // Service.onBind()有返回一个IBinder实例时调用
            goService = ((GoService.GoBinder) ibinder).getService();
            goService.setOnDataChangeListener(new MainActCallback());
            // goService.setRC();
            goService.startCommunication();
            updateGUI(CONNECT, false);
        }
    }

    private class MainActCallback extends Callback {

        private boolean isEnabled = false;
        private boolean hasTager = false;
        private boolean isRCBGGreen = false;

        @Override
        public void connectStatusChanged(boolean isConnected) {
            if (isConnected) {
                rgAUX1.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
                rgAUX2.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
            }
            updateGUI(CONNECT, isConnected);
        }

        @Override
        public void tagerStatus(boolean hasTager) {
            this.hasTager = hasTager;
            if (hasTager) {
                if (isEnabled) {
                    start();
                }
            } else {
                tvLocal.setText("没有要连接的蓝牙目标。");
                swCtrl.setEnabled(false);
            }
        }

        @Override
        public void localStatusChanged(boolean isEnable) {
            this.isEnabled = isEnable;
            if (hasTager && isEnabled) {
                start();
            }
        }

        @Override
        public void relayStatus(short cycleTime, short millivolt) {
            tvRelay.setText(millivolt + "mV\t" + cycleTime + "us");
        }

        private void start() {
            // goService.addSP(msp_raw_rc);
            // goService.addSP();
            goService.startCommunication();
            tvLocal.setText("正在连接蓝牙目标...");
            swCtrl.setEnabled(false);
            swCtrl.setOnCheckedChangeListener(null);
            swCtrl.setChecked(true);
        }

        @Override
        public void relayHardware(byte hasRF) {
            if (hasRF != 0) {
                tvRF.setBackgroundColor(Color.rgb(0, 255, 0));
            } else {
                tvRF.setBackgroundColor(Color.rgb(255, 0, 0));
            }
        }

        @Override
        public void RCDataReturn(byte cmd, int arg1, int arg2) {
            switch (cmd) {
                case MSP_CMD.MSP_ANALOG: {
                    tvRC.setText((arg1 >> 16) + "V rssi:" + (arg2 >> 16));
                    break;
                }
                case MSP_CMD.MSP_SET_RAW_RC: {
                    if (isRCBGGreen)
                        tvRC.setBackgroundColor(Color.WHITE);
                    else
                        tvRC.setBackgroundColor(Color.GREEN);
                    isRCBGGreen = !isRCBGGreen;
                    break;
                }
            }
        }
    }

    private class SwitchListener implements Switch.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            swCtrl.setEnabled(false);
            if (isChecked) {
                if (!goService.isCommunicated())
                    goService.startCommunication();
            } else {
                if (goService.isCommunicated())
                    goService.stopCommunicating();
            }
        }
    }

    class MyOnTouchListener implements Button.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() == R.id.btnL) {
                mLGestureDetector.onTouchEvent(event);
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    v.performClick();
                    pbYaw.setProgress(500);
                    goService.setRC(RC.CHANNEL.YAW.ordinal(),
                            pbYaw.getProgress() + 1000);
                    isFirst = true;
                }
            }
            if (v.getId() == R.id.btnR) {
                mRGestureDetector.onTouchEvent(event);
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    v.performClick();
                    pbRoll.setProgress(500);
                    goService.setRC(RC.CHANNEL.ROLL.ordinal(),
                            pbRoll.getProgress() + 1000);
                    pbPitch.setProgress(500);
                    hpbPitch.setProgress(500);
                    goService.setRC(RC.CHANNEL.PITCH.ordinal(),
                            hpbPitch.getProgress() + 1000);
                }
            }
            return true;
        }
    }

    class LeftOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final String TAG = LeftOnGestureListener.class.getName();

        private boolean isH = true;

        public LeftOnGestureListener() {
            super();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // Log.d(TAG, "onScroll() -- " + distanceX + "," + distanceY);
            if (isFirst) {
                isFirst = false;
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    // 横向，Yaw操作
                    isH = true;
                } else {
                    isH = false;
                }
            }
            if (isH) {
                pbYaw.setProgress(pbYaw.getProgress() - ((int) distanceX * 2));
                goService.setRC(RC.CHANNEL.YAW.ordinal(),
                        pbYaw.getProgress() + 1000);
            } else {
                hpbThrot.setProgress(hpbThrot.getProgress()
                        + ((int) distanceY));
                goService.setRC(RC.CHANNEL.THROTTLE.ordinal(),
                        hpbThrot.getProgress() + 1000);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    class RightOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final String TAG = LeftOnGestureListener.class.getName();

        public RightOnGestureListener() {
            super();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // Log.d(TAG, "onScroll() -- " + distanceX + "," + distanceY);

            pbRoll.setProgress(pbRoll.getProgress() - ((int) distanceX * 2));
            goService
                    .setRC(RC.CHANNEL.ROLL.ordinal(), pbRoll.getProgress() + 1000);
            hpbPitch.setProgress(hpbPitch.getProgress() + ((int) distanceY * 2));
            goService.setRC(RC.CHANNEL.PITCH.ordinal(),
                    hpbPitch.getProgress() + 1000);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup arg0, int arg1) {
            int radioButtonId = arg0.getCheckedRadioButtonId();
            switch (radioButtonId) {
                case R.id.aux1L: {
                    goService.setRC(RC.CHANNEL.AUX1.ordinal(), 1000);
                    break;
                }
                case R.id.aux1M: {
                    goService.setRC(RC.CHANNEL.AUX1.ordinal(), 1500);
                    break;
                }
                case R.id.aux1H: {
                    goService.setRC(RC.CHANNEL.AUX1.ordinal(), 2000);
                    break;
                }
                case R.id.aux2L: {
                    goService.setRC(RC.CHANNEL.AUX2.ordinal(), 1000);
                    break;
                }
                case R.id.aux2M: {
                    goService.setRC(RC.CHANNEL.AUX2.ordinal(), 1500);
                    break;
                }
                case R.id.aux2H: {
                    goService.setRC(RC.CHANNEL.AUX2.ordinal(), 2000);
                    break;
                }
            }
        }

    }
}
