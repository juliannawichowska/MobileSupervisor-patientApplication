package com.example.mobilesupervisor_patientapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SOSActivity extends FragmentActivity {

    private static final String TAG = "SOSActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    public static long total;
    Button sendChat, SmartbandResult, sendPreparedMessage, videoChat;
    ImageButton sosBtn;
    GoogleSignInAccount account;
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothDevice mBluetoothDevice;
    public BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;
    Button startScanningButton;
    Button stopScanningButton;
    Button connectButton;
    Button disconnectButton;
    public static List<ParcelUuid> MY_UUID;
    UUID CLIENT_CHARACTERISTIC_CONFIG_UUID, HEART_RATE_SERVICE_UUID, HEART_RATE_MEASUREMENT_CHAR_UUID, HEART_RATE_CONTROL_POINT_CHAR_UUID;

    TextView peripheralTextView;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public String mDeviceAddress;
    public static Map<ParcelUuid, byte[]> mDeviceData;
    private BluetoothLeScanner btScanner;
    final UUID BATTERY_UUID = convertFromInteger(0xfee0);
    final UUID BATTERY_LEVEL = convertFromInteger(0xff0c);

    FirebaseAuth firebaseAuth;

    //uid of the users
    String hisUid = "tS1fyOTPLaPxjj8OfofcnfOKQk82";
    String myUid = "pXXgJXa0dwbGxdr5XOAyzvAxlJf1";

    int i = 0;
    private static final int REQUEST_CALL = 1;
    String number = "737641092";

    public static final String EXTRA_MESSAGE = "ja";
    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    final int BODY_SENSORS_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);
        HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37);
        HEART_RATE_CONTROL_POINT_CHAR_UUID = convertFromInteger(0x2A39);
        CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);

        setContentView(R.layout.activity_sos);

        sendChat = findViewById(R.id.sendChat);
        SmartbandResult = findViewById(R.id.checkResults);
        sendPreparedMessage = findViewById(R.id.sendPreparedMessage);
        sosBtn = findViewById(R.id.sosBtn);
        videoChat = findViewById(R.id.videoChat);
        sosBtn = findViewById(R.id.sosBtn);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BODY_SENSORS},
                BODY_SENSORS_PERMISSIONS_REQUEST_CODE);

        account = GoogleSignIn.getLastSignedInAccount(this);
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build();
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions
            );

        }
        else {
            accessGoogleFit();
        }



        //firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                i ++;

                Handler handler  = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if (i == 1){

                            SmsManager mySmsManager = SmsManager.getDefault();
                            mySmsManager.sendTextMessage(number, null, "SOS - wezwanie o pomoc", null, null);

                            sendMessage("SOS - wezwanie o pomoc");
                        } else if (i == 2){

                            makePhoneCall();

                        }
                        i = 0;
                    }
                }, 500);
            }
        });

        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(SOSActivity.this,MessageActivity.class);
                startActivity(a);
            }
        });

        SmartbandResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(SOSActivity.this,SmartbandActivity.class);
                startActivity(a);
            }
        });

        sendPreparedMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent c = new Intent(SOSActivity.this,PreparedMessageActivity.class);
                startActivity(c);
            }
        });

        videoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.messenger.com/t"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        startScanning();
    }

    public void startScanning() {
        System.out.println("start scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }
    public void stopScanning() {
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }
    public void connect() {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();

        final BluetoothDevice device = btAdapter
                .getRemoteDevice(mDeviceAddress);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }
    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public void disconnect() {
        mBluetoothGatt.disconnect();
    }


    public ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        private void processResult(ScanResult result){
            mBluetoothDevice = result.getDevice();
            mDeviceAddress = result.getDevice().getAddress();
            mDeviceData = result.getScanRecord().getServiceData();
            Log.v("test", "ZNALZEZIONO" + mDeviceAddress);
            if (mDeviceAddress.equals("F9:3C:B6:95:A4:1C")) {
                Log.v("test", "ZNALZEZIONOooooooooooooooo" + mDeviceAddress);
                MY_UUID = result.getScanRecord().getServiceUuids();
                stopScanning();
                connect();
            }
        }
    };
    BluetoothGattCallback mGattCallback  = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattCharacteristic characteristic =
                    gatt.getService(HEART_RATE_SERVICE_UUID)
                            .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID);
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

        }
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            final Integer batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            if (batteryLevel != null) {
                Log.d(TAG, "battery level: " + batteryLevel);
            }
        }
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            Log.e(TAG, "status"+status);
            BluetoothGattCharacteristic characteristic = gatt.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_CONTROL_POINT_CHAR_UUID);
            characteristic.setValue(new byte[] {1,1});
            gatt.writeCharacteristic(characteristic);
        }
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            Log.e(TAG, "DATA: "+value[1]);
            int HR = value[1];
            sendHRtodb(HR);
        }
    };

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(SOSActivity.this,
                Manifest.permission.CALL_PHONE)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SOSActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:"+ number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void accessGoogleFit() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        Log.i(TAG, "Successfully subscribed");
                        long res = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        total = res;
                        Log.i(TAG, "Total steps: " + total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Problem and not subscribed");
                    }
                });
    }


    private void sendMessage (String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);

        reference.child("Messages").push().setValue(hashMap);
        Toast.makeText(SOSActivity.this, "Wiadomość SOS została wysłana", Toast.LENGTH_SHORT).show();

    }
    private void sendHRtodb(int HR) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Results");
        Map<String, Object> PulseData = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);
        PulseData.put("Date", strDate);
        PulseData.put("Heart Rate", HR);
        reference.child("Heart Rate").child(strDate).setValue(PulseData);
    }
}


