package com.example.awake;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class WeatherFrag extends Fragment {
     
    TextView tempCloud, humid, wind, cloudCoverage, feelsLike, cityCountry;
    ImageView weatherDescIV;
    ImageButton closeWeatherIB;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_weather, container, false);

        tempCloud = view.findViewById(R.id.weatherTempAndDesc);
        humid = view.findViewById(R.id.weatherHumidity);
        cloudCoverage = view.findViewById(R.id.weatherCloudCoverage);
        wind = view.findViewById(R.id.weatherWindSpeed);
        feelsLike = view.findViewById(R.id.weatherFeelsLike);
        cityCountry = view.findViewById(R.id.weatherCityCountry);
        closeWeatherIB= view.findViewById(R.id.weatherClose);
        weatherDescIV= view.findViewById(R.id.weatherTypeFullIV);

        CameraActivity cameraActivity = (CameraActivity) getActivity();

        closeWeatherIB.setOnClickListener(new View.OnClickListener() {
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

        if(CameraActivity.weatherMap!=null){

            tempCloud.setText(String.format("%.0f", CameraActivity.weatherMap.get("temp"))+"° | "+ (String) CameraActivity.weatherMap.get("description"));
            humid.setText(CameraActivity.weatherMap.get("humidity") + "%");
            cloudCoverage.setText(CameraActivity.weatherMap.get("clouds") + "%");
            feelsLike.setText(String.format("%.0f", CameraActivity.weatherMap.get("feels_like")) + "°");
            wind.setText(String.format("%.1f", CameraActivity.weatherMap.get("wind")) + " km/hr");
            cityCountry.setText(CameraActivity.weatherMap.get("city")+", "+CameraActivity.weatherMap.get("country"));
            if(cameraActivity.weatherMap.get("description")!=null){
                weatherDescIV.setImageDrawable(HomeFrag.getweatherDrawable(cameraActivity.weatherMap.get("description").toString()));
            }
        }
        return view;
    }
}