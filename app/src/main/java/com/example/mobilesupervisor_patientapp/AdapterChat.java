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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;

    FirebaseUser firebaseUser;


    public AdapterChat(Context context, List<ModelChat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layouts
        if(i==MSG_TYPE_RIGHT){
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

        //Date date = Calendar.getInstance().getTime();
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //String dateTime  = dateFormat.format(date);


        //set data
        myHolder.messageContents.setText(message);
        myHolder.messageDate.setText(dateTime);


        //set if the message has been seen or delivered
        if (i==chatList.size()) {
            if (chatList.get(i).isSeen()){
                myHolder.isSeenTx.setText("Seen");
            } else {
                myHolder.isSeenTx.setText("Delivered");
            }
        }
        else {
            myHolder.isSeenTx.setVisibility(View.GONE);
        }


    }


    @Override
    public int getItemCount() {

        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView imageAccount;
        TextView messageContents, messageDate, isSeenTx;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            imageAccount = itemView.findViewById(R.id.imageAccount);
            messageContents = itemView.findViewById(R.id. messageContents);
            messageDate = itemView.findViewById(R.id.messageDate);
            isSeenTx = itemView.findViewById(R.id.isSeenTx);




        }
    }




}


