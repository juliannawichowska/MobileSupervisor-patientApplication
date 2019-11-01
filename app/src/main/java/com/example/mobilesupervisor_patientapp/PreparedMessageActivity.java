package com.example.mobilesupervisor_patientapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PreparedMessageActivity extends AppCompatActivity {

    Button preparedMessageBtn1, preparedMessageBtn2, preparedMessageBtn3, preparedMessageBtn4, preparedMessageBtn5, preparedMessageBtn6 ;

    //firebase auth
    FirebaseAuth firebaseAuth;

    //uid of the users
    String hisUid = "tS1fyOTPLaPxjj8OfofcnfOKQk82";
    String myUid = "pXXgJXa0dwbGxdr5XOAyzvAxlJf1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepared_message);

        preparedMessageBtn1 = findViewById(R.id.preparedMessageBtn1);
        preparedMessageBtn2 = findViewById(R.id.preparedMessageBtn2);
        preparedMessageBtn3 = findViewById(R.id.preparedMessageBtn3);
        preparedMessageBtn4 = findViewById(R.id.preparedMessageBtn4);
        preparedMessageBtn5 = findViewById(R.id.preparedMessageBtn5);
        preparedMessageBtn6 = findViewById(R.id.preparedMessageBtn6);

        //firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        preparedMessageBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    sendMessage("Proszę zadzwoń");
            }
        });

        preparedMessageBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Proszę odwiedź mnie dzisiaj");
            }
        });

        preparedMessageBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Źle sie czuję");
            }
        });

        preparedMessageBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Już czuję się lepiej");
            }
        });

        preparedMessageBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Przyjedź jak najszybciej");
            }
        });

        preparedMessageBtn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Nie musisz mnie dzisaj odwiedzać - czuje się dobrze");
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
        Toast.makeText(PreparedMessageActivity.this, "Wiadomość została wysłana", Toast.LENGTH_SHORT).show();
    }

}
