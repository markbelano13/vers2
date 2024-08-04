package com.example.awake;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class MenuFrag extends Fragment {
    ImageButton map,scan, weather,notif;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_menu, container, false);

        scan= view.findViewById(R.id.scanMenu);
        map= view.findViewById(R.id.mapMenu);
        weather= view.findViewById(R.id.weatherMenu);
        notif= view.findViewById(R.id.notificationMenu);
        CameraActivity cameraActivity = (CameraActivity) getActivity();

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.elevation= 30;
                cameraActivity.changeFrameLayoutElevation();
            }
        });

        notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new MenuFrag();
                cameraActivity.addFragment(new NotificationFragment());
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cameraActivity.addFragment(new HomeFrag());
                HomeFrag.maximizeMap();
                CameraActivity.lastFragment= new MenuFrag();
            }
        });

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new MenuFrag();
                cameraActivity.addFragment(new WeatherFrag());
            }
        });







        return view;
    }

}