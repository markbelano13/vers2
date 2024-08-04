package com.example.awake;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.awake.custom.classes.ChartGenerator;
import com.example.awake.custom.classes.NotificationInfo;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {

    public static LinearLayout notificationMessageLayout;
    public static ConstraintLayout notificationListLayout;
    public static NotificationsRecycleViewAdapter recyleViewAdapter;
    public static ArrayList<NotificationInfo> info = new ArrayList<>();
    public static LineChart notificationChart;
    public static TextView notificationTitle,notificationMessage, notificataionSummary, notificationMessageDate, notificationMessageBack, notificationName;
    public static CameraActivity cameraActivity;
    public static ChartGenerator chartGenerator;
    ImageButton closeNotif;
    public static RecyclerView notificationsListRV;
    LinearLayoutManager linearLayoutManager;
    ProgressBar progressBar;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_notification, container, false);
        cameraActivity= (CameraActivity) getActivity();
        chartGenerator = new ChartGenerator();

        notificationMessageLayout = view.findViewById(R.id.notificationMessageLayout);
        notificationListLayout = view.findViewById(R.id.notificationListLayout);
        notificationChart = view.findViewById(R.id.notificationChart);
        notificationMessage = view.findViewById(R.id.notificationMessageGreetings);;
        notificationMessageDate = view.findViewById(R.id.notifMessageDateTime);
        notificationMessageBack = view.findViewById(R.id.notificationMessageBack);
        notificationTitle = view.findViewById(R.id.notificationName);
        closeNotif = view.findViewById(R.id.closeNotification);
        notificationsListRV = view.findViewById(R.id.notificationsRV);
        progressBar= view.findViewById(R.id.detectionLogsPB);
        notificationName = view.findViewById(R.id.notificationName);

        notificationsListRV.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        notificationsListRV.setLayoutManager(linearLayoutManager);
        info=CameraActivity.notificationsInfo;
        recyleViewAdapter = new NotificationsRecycleViewAdapter(info);
        notificationsListRV.setAdapter(recyleViewAdapter);
        progressBar.setVisibility(View.GONE);


        notificationMessageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationMessageLayout.setVisibility(View.GONE);
                notificationListLayout.setVisibility(View.VISIBLE);
                notificationName.setText("Notifications");

            }
        });

        closeNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CameraActivity.lastFragment instanceof MenuFrag){
                    CameraActivity.lastFragment=null;
                    cameraActivity.addFragment(new MenuFrag());
                }else{
                    cameraActivity.removeFragment();
                }
            }
        });

        return view;
    }

    public static void refreshRV(){
        info=CameraActivity.notificationsInfo;
        recyleViewAdapter = new NotificationsRecycleViewAdapter(info);
        notificationsListRV.setAdapter(recyleViewAdapter);
        recyleViewAdapter.notifyDataSetChanged();
    }

}