package com.example.mobilesupervisor_patientapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.provider.FirebaseInitProvider;

import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    ActionBar actionBar;
    EditText messageEdit;
    ImageButton sendButton;
    String receiverUid;
    String senderUid;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageEdit = findViewById(R.id.messageEdit);
        sendButton = findViewById(R.id.sendButton);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text from EditText
                String message = messageEdit.getText().toString();
                //check if message is empty
                if (TextUtils.isEmpty(message)) {
                    //text is empty
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    //text is not empty
                    sendMessage(firebaseUser.getUid(), "tS1fyOTPLaPxjj8OfofcnfOKQk82", message);
                }
                messageEdit.setText("");
            }
        });




        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

    }

    private void sendMessage (String senderUid, String receiverUid, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderUid);
        hashMap.put("receiver", receiverUid);
        hashMap.put("message", message);

        reference.child("Messages").push().setValue(hashMap);
    }

}
