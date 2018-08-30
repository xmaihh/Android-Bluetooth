package com.ex.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * @author 创建者: chensy
 * @data 创建时间: 2018-8-27
 * @Description 描述: 蓝牙自动配对工具类
 * @Verion 版本:
 */
public class ClsPlusUtils {

    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        Log.e("绑定：", returnValue.toString());
        return returnValue;
    }

    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        Log.e("解除绑定：", returnValue.toString());
        return returnValue;
    }

    static public boolean setPin(Class btClass, BluetoothDevice btDevice,
                                 byte[] str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",
                    new Class[]
                            {byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, str);
            Log.e("设置Pin:", returnValue.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    //自动配对设置Pin值
    static public boolean autoBond(Class btClass, BluetoothDevice device, String strPin) throws Exception {
        Method autoBondMethod = btClass.getMethod("setPin", new Class[]{byte[].class});
        Boolean result = (Boolean) autoBondMethod.invoke(device, new Object[]{strPin.getBytes()});
        Log.e("自动配对:" , result.toString());
        return result;
    }

    static public byte[] convertPinToBytes(Class btClass, BluetoothDevice device, String strPin) throws Exception {
        Method convertPinToBytes = btClass.getMethod("convertPinToBytes", new Class[]{String.class});
        byte[] result = (byte[]) convertPinToBytes.invoke(device, strPin);
        Log.e("Pin转Bytes结束","");
        return result;
    }

    // 取消用户输入
    static public boolean cancelPairingUserInput(Class btClass,
                                                 BluetoothDevice device)
            throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        Log.e("取消用户输入：" , returnValue.toString());
//        cancelBondProcess(btClass, device);
        return returnValue;
    }

    // 取消配对
    static public boolean cancelBondProcess(Class btClass,
                                            BluetoothDevice device)
            throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        Log.e("取消配对进程：" , returnValue.toString());
        return returnValue;
    }

    /**
     * 打印所有方法和常量
     *
     * @param clsShow
     */
    static public void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                Log.e("method name", hideMethod[i].getName() + ";and the i is:"
                        + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                Log.e("Field name", allFields[i].getName());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
