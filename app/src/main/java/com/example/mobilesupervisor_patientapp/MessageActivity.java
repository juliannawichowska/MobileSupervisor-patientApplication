package com.example.mobilesupervisor_patientapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    ActionBar actionBar;

    RecyclerView recyclerView;
    EditText messageEdit;
    ImageButton sendButton;

    MainActivity mainActivity = new MainActivity();
    String myUid;

    //firebase auth
    FirebaseAuth firebaseAuth;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageEdit = findViewById(R.id.messageEdit);
        sendButton = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.chat_recyclerView);

        //Layout for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //ActionBar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Wiadomości");

        //firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text from EditText
                String message = messageEdit.getText().toString();
                //check if message is empty
                if (TextUtils.isEmpty(message)) {
                    //text is empty
                    Toast.makeText(MessageActivity.this, "Nie możesz wysłać pustej wiadomości..", Toast.LENGTH_SHORT).show();
                } else {
                    //text is not empty
                    sendMessage(message);
                }
                messageEdit.setText("");
            }
        });

        readMessages();

    }

    private void readMessages() {

        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Messages");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    chatList.add(chat);

                    //adapter
                    adapterChat = new AdapterChat (MessageActivity.this, chatList);
                    adapterChat.notifyDataSetChanged();

                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

        reference.child("Messages").push().setValue(hashMap);
    }
}
