package com.example.mobilesupervisor_patientapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/*

 */
public class SmartbandActivity extends FragmentActivity {
    private long totalSteps;
    FirebaseUser firebaseUser;
    Button checkBox, checkBox2, checkBox3, checkBox4, checkBox5;
    public static int period;

    protected void onCreate(Bundle savedInstanceState) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        totalSteps = SOSActivity.total;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartband);
        TextView stepsCount = (TextView)findViewById(R.id.textView3);
        stepsCount.setText(String.valueOf(totalSteps));
        TextView info = (TextView)findViewById(R.id.textView2);
        info.setText("Ilość kroków dziś: ");
        TextView smart = (TextView)findViewById(R.id.textView5);
        smart.setText("Wybierz okres podglądu wyników tętna: ");
        sendStepstoDB(totalSteps);
        makeStepsOutput(totalSteps, stepsCount);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");
        checkBox = findViewById(R.id.checkBox);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = 1;
                Intent a = new Intent(SmartbandActivity.this,HeartRateActivity.class);
                startActivity(a);
            }
        });
        checkBox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = 2;
                Intent a = new Intent(SmartbandActivity.this,HeartRateActivity.class);
                startActivity(a);
            }
        });
        checkBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = 3;
                Intent a = new Intent(SmartbandActivity.this,HeartRateActivity.class);
                startActivity(a);
            }
        });
        checkBox4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = 4;
                Intent a = new Intent(SmartbandActivity.this,HeartRateActivity.class);
                startActivity(a);
            }
        });
        checkBox5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                period = 5;
                Intent a = new Intent(SmartbandActivity.this,HeartRateActivity.class);
                startActivity(a);
            }
        });
    }

    private void makeStepsOutput(long Steps, TextView info) {
        TextView output = (TextView)findViewById(R.id.textView4);
        if (Steps < 2000) {
            output.setText("Wybierz się na spacer :)");
            info.setTextColor(this.getResources().getColor(R.color.Orange));
        }
        else {
            output.setText("Świetnie!");
            info.setTextColor(this.getResources().getColor(R.color.LightGreen));
        }
    }

    private void sendStepstoDB(long steps) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Results");
        Map<String, Object> Steps = new HashMap<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date);
        Steps.put("Date", strDate);
        Steps.put("Steps", steps);
        reference.child("Steps").child(strDate).setValue(Steps);
    }
}
