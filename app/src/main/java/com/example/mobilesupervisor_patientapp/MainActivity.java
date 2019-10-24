package com.example.mobilesupervisor_patientapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

public class MainActivity extends FragmentActivity {
    public static final String EXTRA_MESSAGE = "ja";
    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                FitnessOptions fitnessOptions = FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
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
                Intent intent = getIntent();
    }
    public void ShowResults(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }
    private void accessGoogleFit() {
    }
}
