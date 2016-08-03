package org.verg.rcon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import serial.protocol.MSP;
import serial.protocol.MSP_CMD;
import serial.protocol.RC;
import serial.protocol.SP;
import serial.protocol.VSP;
import serial.protocol.VSP_CMD;

/**
 * bluetooth服务，管理一切后台进程
 * Created by verg on 15/5/24.
 */
public class GoService extends Service {

    private static final int CONNECTFAIL = 0;
    private static final int CONNECTSUCCESS = 1;

    private IBinder goBinder;
    private VBT vbt;
    private Callback dataReturnListener;
    private SharedPreferences prefs;
    private BluetoothStates bluetoothStatus;
    private boolean isCommunicated = false;
    private List<SP> sendList = new ArrayList<SP>();

    private RC rc;

    private Timer timer;
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTFAIL:
                    GoService.this.dataReturnListener.connectStatusChanged(false);
                    break;
                case MSP_CMD.MSP_ANALOG: {
                    GoService.this.dataReturnListener.RCDataReturn(
                            MSP_CMD.MSP_ANALOG, msg.arg1, msg.arg2);
                    break;
                }
                case MSP_CMD.MSP_SET_RAW_RC: {
                    GoService.this.dataReturnListener.RCDataReturn(
                            MSP_CMD.MSP_SET_RAW_RC, 0, 0);
                    break;
                }
                case (VSP_CMD.VSP << 8) | VSP_CMD.VSP_REPORT:
                    GoService.this.dataReturnListener.relayStatus((short) msg.arg1,
                            (short) msg.arg2);
                    break;
                case (VSP_CMD.VSP << 8) | VSP_CMD.VSP_STATUS: {
                    GoService.this.dataReturnListener
                            .relayHardware((byte) msg.arg2);
                    break;
                }
                case (VSP_CMD.VSP << 8) | VSP_CMD.VSP_ANALOG:
                    GoService.this.dataReturnListener.relayStatus((short) 0,
                            (short) msg.arg2);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     *
     */
    public GoService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vbt = new VBT();
        if (!vbt.cheakDevice()) {
            // onDataChangeListener.onDataChange("本机没有蓝牙设备。");
            return;
        }
        goBinder = new GoBinder();
        bluetoothStatus = new BluetoothStates();
        // 注册广播监听器
        registerReceiver(bluetoothStatus, new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(bluetoothStatus, new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(bluetoothStatus, new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        rc = new RC(MSP_CMD.MSP_SET_RAW_RC, Integer.parseInt(prefs.getString(
                getString(R.string.intervalTime), String.valueOf(1000))));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!vbt.cheakDevice())
            return;
        unregisterReceiver(bluetoothStatus);
        if (isCommunicated()) {
            stopCommunicating();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return goBinder;
    }

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param onDataChangeListener
     */
    public void setOnDataChangeListener(Callback onDataChangeListener) {
        this.dataReturnListener = onDataChangeListener;
    }

    public boolean cheakDevice() {
        return vbt.cheakDevice();
    }

    public boolean isEnableBT() {
        return vbt.isEnabled();
    }

    public void enableBT() {
        vbt.enable(); // 使能bluetooth
    }

    public String[] getBondedDevicesName() {
        return vbt.getBondedDevicesName();
    }

    public String[] getBondedDevicesAddress() {
        return vbt.getBondedDevicesAddress();
    }

    public String getIntervalTime() {
        return prefs.getString(getString(R.string.intervalTime),
                String.valueOf(1000));
    }

    public void setRC(int channel, int val) {
        rc.setRC(channel, val);
    }

    public int getRC(int channel) {
        return rc.getRC(channel);
    }

    /**
     * 开始通讯
     */
    public void startCommunication() {

        if ("".equals(prefs.getString(
                getString(R.string.selectBluetoothDevice), ""))) {
            Toast.makeText(GoService.this, "没有要连接的目标", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (!vbt.isEnabled()) { // 没有使能蓝牙
            return;
        }
        if (isCommunicated)
            return;

        sendList.removeAll(sendList);
        rc.setIntervalTime(Integer.parseInt(prefs.getString(
                getString(R.string.intervalTime), "1000")));
        sendList.add(rc);

        // if ((configEntity.switchVSP & VSP_FLAG.VSP_REPORT) != 0) {
        if (prefs.getBoolean(getString(R.string.relaySwitch), true)) {
            VSP request = new VSP(VSP_CMD.VSP_REPORT, new byte[0], true, 3000);
            sendList.add(request);
        }

        // if ((configEntity.switchVSP & VSP_FLAG.VSP_STATUS) != 0) {
        if (prefs.getBoolean(getString(R.string.relaySwitch), true)) {
            sendList.add(new VSP(VSP_CMD.VSP_STATUS));
        }

        // if ((configEntity.switchMSP & MSP_FLAG.MSP_ANALOk) {
        if (prefs.getBoolean(getString(R.string.rcSwitch), true)) {
            MSP analog = new MSP(MSP_CMD.MSP_ANALOG, new byte[0], true, 3000);
            sendList.add(analog);
        }

        new Thread(new ConnNRecorder()).start();
    }

    public boolean isCommunicated() {
        return isCommunicated;
    }

    public void stopCommunicating() {
        this.isCommunicated = false;
    }

    private class ConnNRecorder implements Runnable {

        private static final String TAG = "ConnNRecorder";
        private Message message;
        private int recSize;
        private byte[] recBuf;
        private SerialCom serialCom;

        public ConnNRecorder() {
            message = myHandler.obtainMessage();
            serialCom = new SerialCom();
            recBuf = new byte[256];
        }

        @Override
        public void run() {

            isCommunicated = vbt.connect(prefs.getString(
                    getString(R.string.selectBluetoothDevice), ""));

            message.what = isCommunicated ? CONNECTSUCCESS : CONNECTFAIL;
            GoService.this.myHandler.sendMessage(message);

            if (!isCommunicated) {
                return;
            }
            timer = new Timer();
            for (SP sp : sendList) {
                if (sp.isCyle()) {
                    timer.schedule(new SendTasker(sp), 0, sp.getInterval());
                } else {
                    timer.schedule(new SendTasker(sp), 0);
                }
            }

            while (isCommunicated) { // record
                recSize = vbt.record(recBuf);
                if (recSize > 0) {
                    serialCom.serialCom();// 处理数据
                } else if (-1 == recSize) {
                    recSize = 256; // full buffer
                } else if (-2 == recSize) {
                    break;
                }
            }
            Log.d(TAG, "end recorder.");
        }

        class SerialCom {

            private static final int INBUF_SIZE = 32;
            private static final byte IDLE = 0;
            private static final byte HEADER_START = 1;
            private static final byte HEADER_M = 2;
            private static final byte HEADER_ARROW = 3;
            private static final byte HEADER_SIZE = 4;
            private static final byte HEADER_CMD = 5;
            private byte c_state;
            private int dataSize;
            private boolean isMSP;
            private byte offset;
            private byte cmdSP;
            private byte[] inBuf;
            private byte indRX;
            private byte checksum;

            public SerialCom() {
                inBuf = new byte[INBUF_SIZE];
            }

            private void serialCom() {
                for (int i = 0; i < recSize; i++) {
                    if (c_state == IDLE) {
                        // c_state = (recBuf[i] == '@') ? HEADER_START : IDLE;
                        if (recBuf[i] == '@') {
                            c_state = HEADER_START;
                            isMSP = false;
                        } else if (recBuf[i] == '$') {
                            c_state = HEADER_START;
                            isMSP = true;
                        } else {
                            c_state = IDLE;
                        }
                    } else if (c_state == HEADER_START) {
                        c_state = (recBuf[i] == 'M') ? HEADER_M : IDLE;
                    } else if (c_state == HEADER_M) {
                        c_state = (recBuf[i] == '>') ? HEADER_ARROW : IDLE;
                    } else if (c_state == HEADER_ARROW) {
                        if (recBuf[i] > INBUF_SIZE) { // now we are expecting
                            // the
                            // payload size
                            c_state = IDLE;
                            continue;
                        }
                        dataSize = recBuf[i];
                        offset = 0;
                        checksum = 0;
                        indRX = 0;
                        checksum ^= recBuf[i];
                        c_state = HEADER_SIZE; // the command is to follow
                    } else if (c_state == HEADER_SIZE) {
                        cmdSP = recBuf[i];
                        checksum ^= recBuf[i];
                        c_state = HEADER_CMD;
                    } else if (c_state == HEADER_CMD && offset < dataSize) {
                        checksum ^= recBuf[i];
                        inBuf[offset++] = recBuf[i];
                    } else if (c_state == HEADER_CMD && offset >= dataSize) {
                        if (checksum == recBuf[i]) { // compare calculated and
                            // transferred checksum
                            evaluateCommand(); // we got a valid packet,
                            // evaluate it
                        }
                        c_state = IDLE;
                    }
                }
            }

            private void evaluateCommand() {
                message = myHandler.obtainMessage();
                if (isMSP) {
                    switch (cmdSP) {
                        case MSP_CMD.MSP_ANALOG: {
                            message.what = MSP_CMD.MSP_ANALOG;
                            message.arg1 = read8() << 16 | read16();
                            message.arg2 = read16() << 16 | read16();
                            break;
                        }
                        case MSP_CMD.MSP_SET_RAW_RC: {
                            message.what = MSP_CMD.MSP_SET_RAW_RC;
                            break;
                        }
                    }
                } else {
                    message.what = VSP_CMD.VSP << 8;
                    switch (cmdSP) {
                        case VSP_CMD.VSP_REPORT: {
                            message.what |= VSP_CMD.VSP_REPORT;
                            message.arg1 = read16(); // 时间
                            message.arg2 = read16(); // 电压
                            break;
                        }
                        case VSP_CMD.VSP_STATUS: {
                            message.what |= VSP_CMD.VSP_STATUS;
                            message.arg1 = read16(); // cycleTime
                            message.arg2 = read8(); // hasNRF()
                            break;
                        }
                        case VSP_CMD.VSP_ANALOG: {
                            message.what |= VSP_CMD.VSP_ANALOG;
                            message.arg1 = read16(); // 电压
                            break;
                        }
                    }
                }
                GoService.this.myHandler.sendMessage(message);
            }

            private byte read8() {
                return (byte) (inBuf[indRX++] & 0xff);
            }

            private short read16() {
                short t = (short) (read8() & 0xff);
                t |= (short) ((read8() & 0xff) << 8);
                // Log.d(TAG, String.valueOf(t));
                return t;
            }

        }

    }

    private class SendTasker extends TimerTask {

        private static final String TAG = "SendTasker";
        private SP sp;

        public SendTasker(SP sp) {
            this.sp = sp;
        }

        @Override
        public void run() {
            if (isCommunicated) {
                vbt.send(sp.getSPString());
                Log.d(TAG, sp.bytes2Hex(sp.getSPString()));
            } else {
                timer.cancel();
                if (vbt.isConnected())
                    vbt.close();
                Log.d(TAG, "end sender.");
            }
        }
    }

    public class GoBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public GoService getService() {
            return GoService.this;
        }
    }

    class VSP_FLAG {
        public static final byte VSP_SET_RAW_RC = (byte) (0x1);
        public static final byte VSP_REPORT = (byte) (0x2);
        public static final byte VSP_STATUS = (byte) (0x4);
    }

    class MSP_FLAG {
        public static final int MSP_ANALOG = (byte) 1;
    }

    private class BluetoothStates extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
                GoService.this.dataReturnListener.connectStatusChanged(true);
                Toast.makeText(GoService.this, "Connected", Toast.LENGTH_SHORT)
                        .show();
            }

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent
                    .getAction())) {
                GoService.this.dataReturnListener.connectStatusChanged(false);
                Toast.makeText(GoService.this, "Disconnected",
                        Toast.LENGTH_SHORT).show();
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED
                    .equals(intent.getAction())) {
                int state = intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        GoService.this.dataReturnListener.localStatusChanged(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
					/*
					 * Toast.makeText(GoService.this, "正在自动打开蓝牙",
					 * Toast.LENGTH_SHORT).show();
					 */break;
                    case BluetoothAdapter.STATE_ON:
                        GoService.this.dataReturnListener.localStatusChanged(true);
					/*
					 * Toast.makeText(GoService.this, "使用期间请不要关闭蓝牙",
					 * Toast.LENGTH_SHORT).show();
					 */break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(GoService.this, "请不要关闭蓝牙",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}