package com.example.mobilesupervisor_patientapp;

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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SOSActivity extends  AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothDevice mBluetoothDevice;
    public BluetoothGatt mBluetoothGatt;
    private static final int STATE_CONNECTED = 2;
    public static List<ParcelUuid> MY_UUID;
    UUID CLIENT_CHARACTERISTIC_CONFIG_UUID, HEART_RATE_SERVICE_UUID, HEART_RATE_MEASUREMENT_CHAR_UUID, HEART_RATE_CONTROL_POINT_CHAR_UUID;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static Map<ParcelUuid, byte[]> mDeviceData;
    private BluetoothLeScanner btScanner;
    private Context context;
    public static long total;
    public static int pulse;
    private static final String TAG = "SOSActivity";
    Button sendChat, SmartbandResult, sendPreparedMessage, openMessenger, sosBtn;
    GoogleSignInAccount account;
    String myUid;
    MainActivity mainActivity = new MainActivity();
    String mDeviceAddress = "F9:3C:B6:95:A4:1C";

    ActionBar actionBar;
    String SOSMessage = "SOS - wezwanie o pomoc";

    //firebase auth
    FirebaseAuth firebaseAuth;

    int countClicks = 0;
    private static final int REQUEST_CALL = 1;
    private static final int REQUEST_SMS = 1;

    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    final int BODY_SENSORS_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);
        HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37);
        HEART_RATE_CONTROL_POINT_CHAR_UUID = convertFromInteger(0x2A39);
        CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
        context = this;

        sendChat = findViewById(R.id.sendChat);
        SmartbandResult = findViewById(R.id.checkResults);
        sendPreparedMessage = findViewById(R.id.sendPreparedMessage);
        sosBtn = findViewById(R.id.sosBtn);
        openMessenger = findViewById(R.id.openMessenger);
        sosBtn = findViewById(R.id.sosBtn);

        //ActionBar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Mobile supervisor");

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }


        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
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

        FirebaseMessaging.getInstance().subscribeToTopic("pacjent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.v("","success");
                        }
                        Log.v("", "failure66");
                    }
                });

        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countClicks ++;
                Handler handler  = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        if (countClicks == 1){
                            sendSMS(SOSMessage);
                            sendMessage(SOSMessage);
                        } else {
                            makePhoneCall();
                        }
                        countClicks = 0;
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
                Intent b = new Intent(SOSActivity.this,SmartbandActivity.class);
                startActivity(b);
            }
        });

        sendPreparedMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent c = new Intent(SOSActivity.this,PreparedMessageActivity.class);
                startActivity(c);
            }
        });

        openMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.messenger.com/t"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once

            new AlertDialog.Builder(context)
                    .setTitle("Ustawienia użytkownika")
                    .setMessage("W celu wysyłania na telefon nadzorcy alertu, zdefiniuj numery kontaktowe oraz indywidualny zakres tęna w zakładce USTAWIENIA")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Ustawienia", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent c = new Intent(SOSActivity.this,SettingsActivity.class);
                            startActivity(c);
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();



            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.commit();
        }

        startScanning();
    }

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }


    private void makePhoneCall() {
        String callphoneNumber = DefaultSettings.getUserCallNumber(context);
        if (ContextCompat.checkSelfPermission(SOSActivity.this,
                Manifest.permission.CALL_PHONE)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SOSActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:"+ callphoneNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    private void sendSMS(String SOSmessage) {
        String smsphoneNumber = DefaultSettings.getUserSMSNumber(context);
        if (ContextCompat.checkSelfPermission(SOSActivity.this,
                Manifest.permission.SEND_SMS)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SOSActivity.this,
                    new String[] {Manifest.permission.SEND_SMS}, REQUEST_SMS);
        } else {
            SmsManager mySmsManager = SmsManager.getDefault();
            mySmsManager.sendTextMessage(smsphoneNumber, null, SOSmessage, null, null);
            Toast.makeText(this, "Wiadmość SOS została wysłana", Toast.LENGTH_LONG).show();
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
        if (requestCode == REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS(SOSMessage);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
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
                        total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                        Log.i(TAG, "Total steps: " + total);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Problem and not subscribed");
                    }
                });
        final OnDataPointListener heartRateListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                Log.i(TAG, "JESTEM TU");
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                }
            }
        };
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .add(
                        new SensorRequest.Builder()
                        .setDataType(DataType.TYPE_HEART_RATE_BPM)
                        .setSamplingRate(1, TimeUnit.MINUTES)
                        .build(),
                        heartRateListener
                )
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "LISTENER COMPLETED");
                                }
                                else {
                                    Log.i(TAG, "LISTENER NOT COMPLETED", task.getException());
                                }
                            }
                        }
                );
    }

    private void sendMessage (String message) {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("userType", mainActivity.userType);
        hashMap.put("messageType", "sos message");

        reference.child("Messages").push().setValue(hashMap);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_up, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle logout click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user == null) {
                startActivity(new Intent(SOSActivity.this, MainActivity.class));
            }
        }
        else if (id==R.id.action_settings){
            Intent c = new Intent(SOSActivity.this,SettingsActivity.class);
            startActivity(c);
        }
        else if (id==R.id.action_camera){
            //obraz z kamery
        }
        return super.onOptionsItemSelected(item);
    }
    public void startScanning() {
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
    public void disconnect() {
        mBluetoothGatt.disconnect();
    }


    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        private void processResult(ScanResult result){
            mBluetoothDevice = result.getDevice();
            mDeviceAddress = result.getDevice().getAddress();
            mDeviceData = result.getScanRecord().getServiceData();
            Log.v("test", "ZNALZEZIONO" + mDeviceAddress);
            String MACAddress = DefaultSettings.getMACAddress(context);
            if (mDeviceAddress.equals(MACAddress)) {
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
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
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

            Log.e(TAG, "SWWWWWEEEEEEEEEEEETstatus"+status);
            BluetoothGattCharacteristic characteristic = gatt.getService(HEART_RATE_SERVICE_UUID).getCharacteristic(HEART_RATE_CONTROL_POINT_CHAR_UUID);
            characteristic.setValue(new byte[] {1,1});
            gatt.writeCharacteristic(characteristic);
        }
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();
            Log.e(TAG, "SWWWWWEEEEEEEEEEEET"+value[1]);
            pulse = value[1];
            sendHRtoDB(pulse);
            String minRange = DefaultSettings.getMinRange(context);
            String maxRange = DefaultSettings.getMaxRange(context);
            int minRangeInt = Integer.parseInt(minRange);
            int maxRangeInt = Integer.parseInt(maxRange);
            if((pulse!=0)&&((pulse<minRangeInt)|| (pulse>maxRangeInt))){
               String pulseInfo = "Uwaga! Tętno pacjenta wynosi : "+pulse;
               sendMessage(pulseInfo);
               sendSMS(pulseInfo);
            }
        }
    };
        public void sendHRtoDB(int pulse) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Results");
            Map<String, Object> Pulse = new HashMap<>();
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            String strDate = dateFormat.format(date);
            Pulse.put("Date", strDate);
            Pulse.put("Pulse", pulse);
            reference.child("Heart Rate").child(strDate).setValue(Pulse);
        }
}



