package com.example.mobilesupervisor_patientapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SOSActivity extends  AppCompatActivity {


    private Context context;
    private static final String TAG = "SOSActivity";
    Button sendChat, SmartbandResult, sendPreparedMessage, openMessenger, sosBtn;
    GoogleSignInAccount account;
    String myUid;
    MainActivity mainActivity = new MainActivity();

    ActionBar actionBar;

    //firebase auth
    FirebaseAuth firebaseAuth;

    int i = 0;
    private static final int REQUEST_CALL = 1;
    private static final int REQUEST_SMS = 1;

    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    final int BODY_SENSORS_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
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

                            sendSMS();
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
                    .setTitle("Ustawienie numeru kontaktowego")
                    .setMessage("W celu wysyłania na telefon nadzorcy alertu, zdefiniuj numery kontaktowe w zakładce 'ustawienia'")

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

    private void sendSMS() {
        String smsphoneNumber = DefaultSettings.getUserSMSNumber(context);
        if (ContextCompat.checkSelfPermission(SOSActivity.this,
                Manifest.permission.SEND_SMS)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SOSActivity.this,
                    new String[] {Manifest.permission.SEND_SMS}, REQUEST_SMS);
        } else {

            SmsManager mySmsManager = SmsManager.getDefault();
            mySmsManager.sendTextMessage(smsphoneNumber, null, "SOS - wezwanie o pomoc", null, null);
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
                sendSMS();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
         }
       }
    }


    public void ShowResults(View view){
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        //startActivity(intent);
    }
    private void accessGoogleFit() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        Log.i(TAG, "Successfully subscribed");
                        long total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
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
}



