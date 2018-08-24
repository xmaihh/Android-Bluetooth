package com.ex.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import javax.net.ssl.SSLException;

public class BluetoothReceiver extends BroadcastReceiver {
    public static final String TAG = BluetoothReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.e(TAG, "onReceive: action:// " + action);
        BluetoothDevice mBtDevice = null;
        //从Intent中获取设备对象
        mBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Log.e(TAG, "发现设备: " + "[" + mBtDevice.getName() + "]" + " : " + mBtDevice.getAddress());
            if (mBtDevice.getName().contains("Mi Band 3")) {
                //如果多个设备重名的情况下，第一个搜到的会被尝试
                if (mBtDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    //BluetoothDevice.BOND_NONE表示未绑定(已配对)
                    Log.e(TAG, "正在尝试绑定: [" + mBtDevice.getName() + "]");
                    try {
                        // 通过工具类ClsUtils,调用createBond()方法
                        ClsUtils.createBond(mBtDevice.getClass(), mBtDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else
                Log.e(TAG, "error: is failed!");
        } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            //再次得到的action,会等于PAIRING_REQUEST配对请求
            if (mBtDevice.getName().contains("Mi Band 3")) {
                try {
                    //1. 确认配对
                    ClsUtils.setPairingConfirmation(mBtDevice.getClass(), mBtDevice, true);
                    //2. 终止有序广播
                    //如果没有将广播终止,会出现一个一闪而过的配对框
                    Log.d(TAG, "order... isOrderBroadcast:" + isOrderedBroadcast() + ",isInitialStickyBroadcast: " + isInitialStickyBroadcast());
                    abortBroadcast();
                    //3. 调用setPin()方法配对
                    ClsUtils.setPin(mBtDevice.getClass(), mBtDevice, "12345");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Log.e(TAG, "这个设备不是目标蓝牙设备");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
