package com.example.esignaturecardreader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.acs.smartcard.Features;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.Charset;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

    private static final String ACTION_USB_PERMISSION = "com.android.esignaturecardreader.USB_PERMISSION";

    private static final String CHANNEL = "com.example.esignaturecardreader.usb_channel";

//    private ArrayAdapter<String> mReaderAdapter;
    private List<String> mReaderAdapter;
    private UsbManager mManager;
    private Reader mReader;
    private List<String> mSlotAdapter;
    private Features mFeatures = new Features();
//    private PendingIntent mPermissionIntent;
    private Spinner mReaderSpinner;
    private List<String> mSlotSpinner;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {

                            // Open reader
                            Log.d("log", "Opening reader: " + device.getDeviceName());
                            new OpenTask().execute(device);
                        }

                    } else {
                        Log.d("log","Permission denied for device "
                                + device.getDeviceName());
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                synchronized (this) {

                    // Update reader list
                    mReaderAdapter.clear();
                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        if (mReader.isSupported(device)) {
                            mReaderAdapter.add(device.getDeviceName());
                        }
                    }

                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {

                        // Clear slot items
                        mSlotAdapter.clear();

                        // Close reader
                        Log.d("log","Closing reader...");
                        new CloseTask().execute();
                    }
                }
            }
        }
    };

    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            mReader.close();
            return null;
        }
    }

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

        @Override
        protected Exception doInBackground(UsbDevice... params) {

            Exception result = null;

            try {

                mReader.open(params[0]);

            } catch (Exception e) {

                result = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {

            if (result != null) {

                Log.d("log",result.toString());

            } else {

                Log.d("log","Reader name: " + mReader.getReaderName());

                int numSlots = mReader.getNumSlots();
                Log.d("log","Number of slots: " + numSlots);

                // Add slot items
                mSlotAdapter.clear();
                for (int i = 0; i < numSlots; i++) {
                    mSlotAdapter.add(Integer.toString(i));
                }

                // Remove all control codes
                mFeatures.clear();

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum;
            }
        });

        // Register receiver for USB permission
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
//                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);

        // Initialize reader spinner
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (mReader.isSupported(device)) {
                mReaderAdapter.add(device.getDeviceName());
            }
        }

        // Initialize slot spinner
//        mSlotSpinner.add(mSlotAdapter);

//        mReaderAdapter.clear();


//        String deviceName = (String) mReaderSpinner.getSelectedItem();
//
//        if (deviceName != null) {
//
//            // For each device
//            for (UsbDevice device : mManager.getDeviceList().values()) {
//
//                // If device name is found
//                if (deviceName.equals(device.getDeviceName())) {
//
//                    // Request permission
////                    mManager.requestPermission(device,
////                            mPermissionIntent);
//
//                    break;
//                }
//            }
//        }

        // create method channel for Flutter.
        BinaryMessenger messenger = null;
        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        mReaderAdapter = new ArrayList<>();
                        Boolean isSupport = false;
                        String deviceDetail = "";
                        UsbDevice usbDevice = null;
                        for (UsbDevice device : mManager.getDeviceList().values()) {
                            if (mReader.isSupported(device)) {
                                isSupport = true;
                                deviceDetail = device.toString();
                                usbDevice = device;
                                mReaderAdapter.add(device.getDeviceName());
                            }
                        }


                        switch (call.method){
                            case "create":{
                                try {
                                    mReader.open(usbDevice);
                                }catch (Exception e){
                                    result.error("001","error when create",e.getMessage());
                                }
                                result.success("data: "+mReader.getDevice() + " isOpen:"+mReader.isOpened());
                                break;
                            }

                            case "close":{
                                try {
                                    mReader.close();
                                }catch (Exception e) {
                                    result.error("002", "error when close", e.getMessage());
                                }
                                result.success(" isOpen:"+mReader.isOpened());
                                break;
                            }
                            case "resetCard":{
                                try {
                                    resetCard(mReader);
                                    result.success("reset card success");
                                } catch (ReaderException e) {
                                    e.printStackTrace();
                                    result.error("003","Error When reset card",e.getMessage());
                                }
                            }
                            case "setProtocol":{
                                try {
                                    byte[] req = (byte[]) call.argument("selectHex");
                                    setProtocol(mReader,req);
                                    result.success("set Protocol success");
                                } catch (ReaderException e) {
                                    e.printStackTrace();
                                    result.error("003","Error When set Protocol",e.getMessage());
                                }
                            }
                            case "send":{
                                try {
                                    byte[] req = (byte[]) call.argument("requestHex");
                                    byte[] respArray = new byte[500];
                                    int responsLength =  mReader.transmit(0, req, req.length, respArray, respArray.length);
                                    String res = byteArrayToHexString(respArray, 0, responsLength);
                                    result.success(res);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    result.error("003","Error When sent data",e.getMessage());
                                }
                            }

                            case "sendGetByteArray":{
                                try {
                                    byte[] req = (byte[]) call.argument("requestHex");
                                    byte[] respArray = new byte[500];
                                    int responsLength = mReader.transmit(0, req, req.length, respArray, respArray.length);

                                    result.success(hex(respArray,0,responsLength));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    result.error("003","Error When Sent Data",e.getMessage());
                                }
                            }
                        }

//                        List<String> selected = new ArrayList<>();
//                        selected.add("cif");
//                        if(call.method.equals("sayHello")){
//                                Log.d("MY_NATIVE_CODE","onMethodCall is called and method = "+call.method);
//                                ThaiADPU apdu = new ThaiADPU();
//                                try {
//                                    byte[]  cid = {
//                                            (byte)0x80,
//                                            (byte)0xB0,
//                                            (byte)0x00,
//                                            (byte)0x04,
//                                            (byte)0x02,
//                                            (byte)0x00,
//                                            (byte)0x0D};
//                                    byte[] respArray = new byte[500];
//                                    byte[]  cidGetdata =
//                                            {
//                                                    (byte) 0x00,
//                                                    (byte) 0xC0,
//                                                    (byte) 0x00,
//                                                    (byte) 0x00,
//                                                    (byte) 0x0D
//                                            };
//                                    int responsLength = 0;
//                                    int slotNum = 0;
//                                    mReader.open(usbDevice);
//                                    resetCard(mReader);
//                                    setProtocol(mReader);
//                                    mReader.transmit(0, cid, cid.length, respArray, respArray.length);
//                                    responsLength =
//                                            mReader.transmit(
//                                                    slotNum,
//                                                    cidGetdata,
//                                                    cidGetdata.length,
//                                                    respArray,
//                                                    respArray.length
//                                            );
//
//
////                                    String res =  apdu.readSpecific(mReader,selected);
//
//                                    result.success("data: "+hexToAscii(hex(respArray)));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    result.success("Error "+e.getMessage()+" "+e.getStackTrace());
//                                }
//
//                            //return to flutter
//                                if(mReaderAdapter.size()>0){
//                                    result.success("Hello Flutter. I come form native. \r\n"+mReaderAdapter.get(0).toString()
//                                            +"mReader isSupport: "+String.valueOf(isSupport) +"\r\n"+" deviceDetail: "+deviceDetail);
//                                }else{
//                                    result.success("Hello Flutter. I come form native. No device"+" mReader : "
//                                            +"mReader isSupport: "+String.valueOf(isSupport)+"\r\n"+"deviceDetail: "+deviceDetail);
//                                }
//                            }

//                        else if(call.method.equals("thai_idcard_reader_flutter_channel")){
//                            applicationContext = getFlutterEngine().getDartExecutor().getBinaryMessenger().applicationContext;
//
//                        }
                        result.notImplemented();
                    }
                });
    }

    public static byte[] hex(byte[] bytes,Integer index,Integer length) {

        if (length + index > bytes.length) {
            length = bytes.length - index;
        }
        byte[] selectBytes = new byte[length];
        System.arraycopy(bytes, index, selectBytes, 0, length);
//
//        StringBuilder result = new StringBuilder();
//        for (byte aByte : selectBytes) {
//            result.append(String.format("%02x", aByte));
//        }
        return selectBytes;
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    private void setProtocol(Reader r,byte[] input) throws ReaderException {
        byte[] select = {
                (byte) 0x00,(byte)0xA4,(byte)0x04,(byte)0x00,(byte)0x08,(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x54,(byte)0x48,(byte)0x00,(byte)0x01
        };
        r.setProtocol(0, Reader.PROTOCOL_T0);
        byte[] response = new byte[300];
        r.transmit(0, input, input.length, response, response.length);
    }

    private void resetCard(Reader r) throws ReaderException {
        r.power(0, Reader.CARD_WARM_RESET);
    }

    private String byteArrayToHexString(byte[] input,Integer index,Integer length){
        if (length + index > input.length) {
            length = input.length - index;
        }
        byte[] selectBytes = new byte[length];
        System.arraycopy(input, index, selectBytes, 0, length - 2);
        return showByteString(selectBytes);
    }

    private String showByteString(byte[] input) {
        StringBuilder output = new StringBuilder();
        for (int i=0;i<input.length;i++) {
            output.append(String.format("%02x", input[i]));
        }
        String result = null;
        result = new String(input,  Charset.forName("TIS620"));
        return result;
    }

}
