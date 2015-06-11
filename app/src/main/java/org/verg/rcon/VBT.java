package org.verg.rcon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by verg on 15/5/24.
 */
public class VBT {

    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String TAG = VBT.class.getName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private InputStream btInputStream;
    private OutputStream btOutputStream;

    public VBT() {
        // TODO 自动生成的构造函数存根
        // 得到BluetoothAdapter对象
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean cheakDevice() {
        // 判断BluetoothAdapter对象是否为空，如果为空，则表明本机没有蓝牙设备
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }

    public String getLocalName() {
        return mBluetoothAdapter.getName();
    }

    public void enable() {
        // TODO 自动生成的方法存根
        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    public boolean isEnabled() {
        // TODO 自动生成的方法存根
        return mBluetoothAdapter.isEnabled();
    }

    public void disable() {
        // TODO 自动生成的方法存根
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
    }

//	public List<String> getPairedDevices() {
//
//		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
//				.getBondedDevices();
//		List<String> mArrayList = new ArrayList<String>();
//		if (pairedDevices.size() > 0) {
//			// Loop through paired devices
//			for (BluetoothDevice device : pairedDevices) {
//				// Add the name and address to an array adapter to show in a
//				// ListView
//				mArrayList.add(device.getName() + "\n" + device.getAddress());
//			}
//		}
//		return mArrayList;
//	}

    public String[] getBondedDevicesName() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        String[] str = new String[pairedDevices.size()];
        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            str[i++] = device.getName();
        }
        return str;
    }

    public String[] getBondedDevicesAddress() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        String[] str = new String[pairedDevices.size()];
        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            str[i++] = device.getAddress();
        }
        return str;
    }

    public boolean connect(String address) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString(SPP_UUID);
        // String Address = mBluetoothAdapter
        // mBluetoothSocket =
        // mBluetoothAdapter.createRfcommSocketToServiceRecord(String, UUID);
        try {
            mBluetoothSocket = mBluetoothDevice
                    .createInsecureRfcommSocketToServiceRecord(uuid);
            mBluetoothSocket.connect();
            btInputStream = mBluetoothSocket.getInputStream();
            btOutputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            // e.printStackTrace();
            Log.e(TAG, "can't connect to the bluetooth device.");
            return false;
        }
        return true;
    }

    public boolean isConnected() {
        if (null == mBluetoothSocket)
            return false;
        else
            return mBluetoothSocket.isConnected();
    }

    public void close() {
        try {
            if (null != btInputStream)
                btInputStream.close();
            if (null != btOutputStream)
                btOutputStream.close();
            if (null != mBluetoothSocket)
                if (mBluetoothSocket.isConnected())
                    mBluetoothSocket.close();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    public boolean send(byte[] data) {
        if (null == btOutputStream)
            return false;
        try {
            btOutputStream.write(data);
            btOutputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "can't send data out.");
            return false;
        }
        return true;
    }

    public int record(byte[] buffer) {
        try {
            return btInputStream.read(buffer);
        } catch (IOException e) {
            Log.e(TAG, "can't record data in.");
            return -2;
        }
    }
}
