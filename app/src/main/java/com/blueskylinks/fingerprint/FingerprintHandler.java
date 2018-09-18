package com.blueskylinks.fingerprint;

/**
 * Created by omsai on 9/18/2018.
 */
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FingerprintHandler  extends FingerprintManager.AuthenticationCallback{
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner scanner;
    private BluetoothDevice ble_device;
    ScanRecord scan_rec;
    private Context context;
    String message;
    private BluetoothGatt mGatt;

    public static String nrf_service = "000000f1-0000-1000-8000-00805f9b34fb";
    public final static UUID NRF_UUID_SERVICE = UUID.fromString(nrf_service);

    public BluetoothGattCharacteristic characteristicTX;

    public static String nrf_tx = "0000a001-0000-1000-8000-00805f9b34fb";
    public final static UUID NRF_UUID_TX = UUID.fromString(nrf_tx);


    // Constructor
    public FingerprintHandler(Context mContext) {
        context = mContext;
        initialize();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i("BleScanning:", "initilizing.......");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.i("BleScanning:", "Unable to initialize BluetoothManager.");
                return false;
            }

        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.i("BleScanning:", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startscand();
        return true;
    }

    //================  Start BLE Scanning  ===============
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscand() {
        Log.i("BLE------", "Start Scanning");
        //final ParcelUuid UID_SERVICE =
       ParcelUuid.fromString("000000f1-0000-1000-8000-00805f9b34fb");
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter beaconFilter = new ScanFilter.Builder() // this filter will be used to get only specific device based on service UUID
                //.setServiceUuid(UID_SERVICE)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(beaconFilter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(filters, settings, mcallback);
    }
    // This callback method will be automatically called every time the scanner get the device adv data
    public ScanCallback mcallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi;
            rssi = result.getRssi();
            scan_rec = result.getScanRecord();
            Log.i("Scan result", String.valueOf(rssi));
            Log.i("record", scan_rec.toString());
            ble_device = result.getDevice();
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopscand() {
        Log.i("BLE-----", "Stop Scanning");
        scanner.stopScan(mcallback);
    }

    //===========================================================================
    public BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    Intent intent = new Intent();
                    intent.setAction("CUSTOM_INTENT");
                    intent.putExtra("D1", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.i("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            BluetoothGattService service1;
            Log.i("onServicesDiscovered", services.toString());
            service1=gatt.getService(NRF_UUID_SERVICE);
            characteristicTX = services.get(2).getCharacteristic(NRF_UUID_TX);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            final byte b1[]=characteristic.getValue();
            for (int i=0;i<b1.length;i++){
                Log.i(":",String.valueOf(b1[i]));
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite??", characteristic.toString());
            final byte b1[]=characteristic.getValue();
            for (int i=0;i<b1.length;i++){
                Log.i(":",String.valueOf(b1[i]));
            }
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connect_device() {
        stopscand();
        Log.i("","...........");
        if (ble_device != null){
            mGatt = ble_device.connectGatt(context, false, gattCallback);
            Log.i("BLE", "Device Connected...");
        }
        else {}
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        senddata();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void disconnect() {
        mGatt.disconnect();
        startscand();
    }

    public void senddata() {
        message = "a";
        characteristicTX.setValue(message);
        mGatt.writeCharacteristic(characteristicTX);

    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }
    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }


    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }


    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }


    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        connect_device();
        this.update("Fingerprint Authentication succeeded.", true);

    }


    public void update(String e, Boolean success){
        TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
        textView.setText(e);
        if(success){
            textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        }
    }
}
