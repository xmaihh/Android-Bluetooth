package com.ex.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SafeHandler.HandlerContainer {


    /**
     * Called when the activity is first created.
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    private TextView device;
    private SafeHandler<MainActivity> mHandler;
    private Button auto_match;
    private Button scan;
    private ListView listView;
    private List<String> list;
    private ArrayAdapter<String> adapter;
    // UUID，蓝牙建立链接需要的
    private final UUID MY_UUID = UUID
            .fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
    // 为其链接创建一个名称
    private final String NAME = "Bluetooth_Socket";
    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    private BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    private OutputStream os;
    // 服务端利用线程不断接受客户端信息
    private AcceptThread thread;
    private BluetoothDevice btDev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ble);
        mHandler = new SafeHandler<MainActivity>(this);
        device = findViewById(R.id.device);
        device.setText("");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        auto_match = findViewById(R.id.auto_match);
        scan = findViewById(R.id.scan);
        listView = findViewById(R.id.list);
        list = new ArrayList<>();

        //适配器
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listView.setAdapter(adapter);
        listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.head_listview, null));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("chensy", "当前选中条目: " + list.get(i - 1));
                if (mBluetoothAdapter != null) {
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();//这句话是停止扫描蓝牙
                    }
                    String s = list.get(i - 1);
                    String address = s.substring(s.indexOf(":") + 1).trim();
                    String MAC = address.replaceAll(" ", "");
                    //根据蓝牙地址创建蓝牙对象
                    selectDevice = mBluetoothAdapter.getRemoteDevice(MAC);
                    //通过反射来配对对应的蓝牙
                    try {
                        //这里是调用的方法，此方法使用反射
                        ClsUtils.createBond(selectDevice.getClass(), selectDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (mBluetoothAdapter == null) {
            // 不支持蓝牙
            Snackbar.make(device, getString(R.string.snack_no_bt), Snackbar.LENGTH_LONG)
                    .setAction("Go!", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    })
                    .setActionTextColor(Color.BLUE).show();
        } else {
            //支持蓝牙
            if (mBluetoothAdapter.isEnabled()) {
                //蓝牙已打开
                Toast tst = Toast.makeText(MainActivity.this, "蓝牙已经打开", Toast.LENGTH_SHORT);
                tst.show();

                //获取所以已经绑定的蓝牙设备
                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                //遍历已绑定的蓝牙设备
                device.clearComposingText();
                device.setText("已配对");
                for (BluetoothDevice btdevice : devices) {
                    Log.d("chensy", "遍历已绑定蓝牙设备: [" + btdevice.getName() + "] " + btdevice.getAddress());
                    device.append("\n[" + btdevice.getName() + "]\n" + btdevice.getAddress() + "\n");
                    list.add("已配对: [" + btdevice.getName() + "] " + btdevice.getAddress());
                }
                //注册蓝牙查询的广播，可在BroadcastReceiver中接收到查询的蓝牙设备
                IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, mFilter);
                //注册搜索完时的receive
                mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mReceiver, mFilter);
                //注册蓝牙连接状态发生改变时，接收状态
                mFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(mReceiver, mFilter);
            } else {
                //蓝牙未打开
                if (turnOnBluetooth()) {
                    Toast tst = Toast.makeText(MainActivity.this, "打开蓝牙成功", Toast.LENGTH_SHORT);
                    tst.show();

                    //获取所以已经绑定的蓝牙设备
                    Set<BluetoothDevice> btdevices = mBluetoothAdapter.getBondedDevices();
                    //遍历已绑定的蓝牙设备
                    for (BluetoothDevice btdevice : btdevices) {
                        Log.d("chensy", "遍历已绑定蓝牙设备: [" + btdevice.getName() + "] " + btdevice.getAddress());
                        list.add("已配对蓝牙设备: [" + btdevice.getName() + "] " + btdevice.getAddress());
                    }
                    //注册蓝牙查询的广播，可在BroadcastReceiver中接收到查询的蓝牙设备
                    IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, mFilter);
                    //注册搜索完时的receive
                    mFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    registerReceiver(mReceiver, mFilter);
                    //注册蓝牙连接状态发生改变时，接收状态
                    mFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    registerReceiver(mReceiver, mFilter);
                } else {
                    Toast tst = Toast.makeText(MainActivity.this, "打开蓝牙失败！！", Toast.LENGTH_SHORT);
                    tst.show();
                }
            }
        }
        // 实例接收客户端传过来的数据线程
        thread = new AcceptThread();
        // 线程开始
        thread.start();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                //通过此方法获取搜索到的蓝牙设备
                BluetoothDevice btdevice = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 获取搜索到的蓝牙绑定状态,看看是否是已经绑定过的蓝牙
                if (btdevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 如果没有绑定过则将蓝牙名称和地址显示在TextView上
//                    device.append("[ " + btdevice.getName() + " ]\n"
//                            + btdevice.getAddress() + "\n");
                    list.add("[" + btdevice.getName() + "]\t"
                            + btdevice.getAddress());
                    adapter.notifyDataSetChanged();
                    //如果指定地址的蓝牙和搜索到的蓝牙相同,则我们停止扫描
                    if ("rk312x".equals(btdevice.getName())) {
                        mBluetoothAdapter.cancelDiscovery();//这句话是停止扫描蓝牙
                        //根据蓝牙地址创建蓝牙对象
                        btDev = mBluetoothAdapter.getRemoteDevice(btdevice.getAddress());
                        //通过反射来配对对应的蓝牙
                        try {
                            //这里是调用的方法，此方法使用反射
                            ClsUtils.createBond(btDev.getClass(), btDev);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 搜索完成
            } else if (action
                    .equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                setTitle("搜索完成");
                setTitle(getString(R.string.app_name));
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                //获取发生改变的蓝牙对象
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //根据不同的状态显示提示
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d("chensy", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("chensy", "完成配对");
                        mHandler.sendEmptyMessageDelayed(1, 2000);
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d("chensy", "取消配对");
                    default:
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                //再次得到的action,会等于PAIRING_REQUEST配对请求
                if (btDev.getName().contains("rk312x")) {
                    try {
                        //1. 确认配对
                        ClsUtils.setPairingConfirmation(btDev.getClass(), btDev, true);
                        //2. 终止有序广播
                        //如果没有将广播终止,会出现一个一闪而过的配对框
                        Log.d("chensy", "order... isOrderBroadcast:" + isOrderedBroadcast() + ",isInitialStickyBroadcast: " + isInitialStickyBroadcast());
                        abortBroadcast();
                        //3. 调用setPin()方法配对
                        ClsUtils.setPin(btDev.getClass(), btDev, "12345");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    Log.e("chensy", "这个设备不是目标蓝牙设备");
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Comm.MSG.MSG_SCAN_TIMEOUT:
                //扫描超时,停止扫描
                if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                scan.setText(getString(R.string.menu_scan));
                break;
            default:
                break;
        }
    }

    public void automatch(View view) {
        // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 如果选择设备为空则代表还没有选择设备
        if (selectDevice == null) {
            //通过地址获取到该设备
            Toast.makeText(this, "连接设备为空", Toast.LENGTH_SHORT).show();
        }
        // 这里需要try catch一下，以防异常抛出
        try {
            // 判断客户端接口是否为空
            if (clientSocket == null) {
                // 获取到客户端接口
                clientSocket = selectDevice
                        .createRfcommSocketToServiceRecord(MY_UUID);
                // 向服务端发送连接
                clientSocket.connect();
                // 获取到输出流，向外写数据
                os = clientSocket.getOutputStream();

            }
            // 判断是否拿到输出流
            if (os != null) {
                // 需要发送的信息
                String text = "成功发送信息";
                // 以utf-8的格式发送出去
                os.write(text.getBytes("UTF-8"));
            }
            // 吐司一下，告诉用户发送成功
            Toast.makeText(this, "发送信息成功，请查收", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // 如果发生异常则告诉用户发送失败
            Toast.makeText(this, "发送信息失败", Toast.LENGTH_SHORT).show();
        }

    }

    public void scan(View view) {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isDiscovering()) { //开始扫描
                mBluetoothAdapter.startDiscovery();
                setTitle("搜索蓝牙设备...");
                list.clear();
                scan.setText(getString(R.string.menu_stop));
                mHandler.sendEmptyMessageDelayed(Comm.MSG.MSG_SCAN_TIMEOUT, 10000);
            } else {  //停止扫描
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                setTitle(getString(R.string.app_name));
                scan.setText(getString(R.string.menu_scan));
            }
        }
    }


    /**
     * 强制开启当前 Android 设备的 Bluetooth
     *
     * @return true：强制打开 Bluetooth　成功　false：强制打开 Bluetooth 失败
     */
    public static boolean turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null) {
            return bluetoothAdapter.enable();
        }

        return false;
    }

    // 服务端接收信息线程
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;// 服务端接口
        private BluetoothSocket socket;// 获取到客户端的接口
        private InputStream is;// 获取到输入流
        private OutputStream os;// 获取到输出流

        public AcceptThread() {
            try {
                // 通过UUID监听请求，然后获取到对应的服务端接口
                serverSocket = mBluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void run() {
            try {
                // 接收其客户端的接口
                socket = serverSocket.accept();
                // 获取到输入流
                is = socket.getInputStream();
                // 获取到输出流
                os = socket.getOutputStream();

                // 无线循环来接收数据
                while (true) {
                    // 创建一个128字节的缓冲
                    byte[] buffer = new byte[128];
                    // 每次读取128字节，并保存其读取的角标
                    int count = is.read(buffer);
                    // 创建Message类，向handler发送数据
                    Message msg = new Message();
                    // 发送一个String的数据，让他向上转型为obj类型
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    // 发送数据
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除注册
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }
}
