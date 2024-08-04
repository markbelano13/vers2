package com.example.awake;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awake.custom.classes.NotificationInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;

import firebase.classes.FirebaseDatabase;

public class NotificationsRecycleViewAdapter extends RecyclerView.Adapter<NotificationsRecycleViewAdapter.NotificationsViewHolder> {

    ArrayList<NotificationInfo> info;

    public NotificationsRecycleViewAdapter(ArrayList<NotificationInfo> data){
        this.info = data;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_notif_row,parent,false);
        NotificationsRecycleViewAdapter.NotificationsViewHolder notificationsViewHolder= new NotificationsRecycleViewAdapter.NotificationsViewHolder(view);

        return notificationsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        NotificationInfo notificationInfo = this.info.get(position);

        if(notificationInfo.getWasRead()){
            holder.readUnreadIV.setImageResource(R.drawable.ic_read_message);
            holder.notifDate.setTypeface(null, Typeface.NORMAL);
            holder.notifTitle.setTypeface(null, Typeface.NORMAL);
        }else{
            holder.readUnreadIV.setImageResource(R.drawable.ic_unread_message);
            holder.notifTitle.setTypeface(null, Typeface.BOLD);
            holder.notifDate.setTypeface(null, Typeface.BOLD);
        }
        holder.notifTitle.setText(notificationInfo.getNotifTitle());
        holder.notifDate.setText(notificationInfo.getNotifDateShort());
        holder.info = notificationInfo;
        ;
    }

    @Override
    public int getItemCount() {
        return this.info.size();
    }

    public static class NotificationsViewHolder extends  RecyclerView.ViewHolder{

        ImageView readUnreadIV;
        TextView notifTitle;
        TextView notifDate;
        TextView notificationName;
        NotificationInfo info;
        ConstraintLayout notifLayout;
        FirebaseDatabase firebaseDB = new FirebaseDatabase();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = mAuth = FirebaseAuth.getInstance();


        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);

            readUnreadIV= itemView.findViewById(R.id.notificationReadUnread);
            notifTitle= itemView.findViewById(R.id.notificationTitle);
            notifDate= itemView.findViewById(R.id.notificationDate);
            notifLayout=itemView.findViewById(R.id.notificationLayout);
            notificationName = itemView.findViewById(R.id.notificationName);


            notifLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NotificationFragment.notificationMessageLayout.setVisibility(View.VISIBLE);
                    NotificationFragment.notificationListLayout.setVisibility(View.GONE);

                    NotificationFragment.notificationName.setText(info.getNotifTitle());
                    NotificationFragment.notificationMessageDate.setText(info.getNotifDate());

                    String escapedString = info.getMessage();
                    String unescapedString = StringEscapeUtils.unescapeJava(escapedString);
                    NotificationFragment.notificationMessage.setText(unescapedString);

                    NotificationFragment.chartGenerator.setLineChart(NotificationFragment.notificationChart);
                    NotificationFragment.chartGenerator.setRange("yesterday");
                    NotificationFragment.chartGenerator.setDrowsyList(info.getDrowsyList());
                    NotificationFragment.chartGenerator.setYawnList(info.getYawnList());
                    NotificationFragment.chartGenerator.setLowestTime(info.getLowestTime());
                    NotificationFragment.chartGenerator.setHighestTime(info.getHighestTime());
                    NotificationFragment.chartGenerator.setHighestYLength(info.getHighestYLength());
                    NotificationFragment.chartGenerator.setCheckCount(2);
                    NotificationFragment.chartGenerator.processChart();

                    firebaseDB.updateUserInfo(new HashMap<String, Object>() {{
                        put("wasRead", true);
                    }}, db.collection("users/" + mAuth.getUid() + "/user_notifications").document(info.getDocumentName()), new FirebaseDatabase.TaskCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d("NotificationRecycleView", "success wasRead");
                            //CameraActivity.notificationsInfo.get()
                            info.setWasRead(true);
                            NotificationFragment.refreshRV();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.d("NotificationRecycleView", "fail wasRead "+errorMessage);
                        }
                    });






                }
            });

        }

    }
}
