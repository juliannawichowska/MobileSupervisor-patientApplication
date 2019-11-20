package com.example.mobilesupervisor_patientapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int receiverMessage = 0;
    private static final int senderMessage = 1;
    Context context;
    List<ModelChat> chatList;

    MainActivity mainActivity = new MainActivity();

    String myUid;

    //firebase auth
    FirebaseAuth firebaseAuth;

    public AdapterChat(Context context, List<ModelChat> chatList) {

        this.context = context;
        this.chatList = chatList;

        //firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layouts
        if(i==senderMessage){
            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat, viewGroup, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_chat, viewGroup, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String message = chatList.get(i).getMessage();
        String timestamp = chatList.get(i).getTimestamp();

        //convert time
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        //set data
        myHolder.messageContents.setText(message);
        myHolder.messageDate.setText(dateTime);

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        myUid = firebaseUser.getUid();
        //get currently signed in user
        if(chatList.get(position).getUserType().equals(mainActivity.userType)){
            return senderMessage;
        } else {
            return receiverMessage;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView imageAccount;
        TextView messageContents, messageDate;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            imageAccount = itemView.findViewById(R.id.imageAccount);
            messageContents = itemView.findViewById(R.id. messageContents);
            messageDate = itemView.findViewById(R.id.messageDate);

        }
    }
}