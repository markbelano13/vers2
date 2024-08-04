package com.example.awake;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import helper.classes.DialogHelper;


public class AppAboutFrag extends Fragment {

    ImageButton closAppAbout, gitBtn;
    String actionType="";
    DialogHelper helper;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_about, container, false);

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        closAppAbout= view.findViewById(R.id.closeAppAbout);
        gitBtn= view.findViewById(R.id.gitAppAbout);

        helper = new DialogHelper(getContext(), new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                if(actionType.equals("git")){
                    String url = "https://github.com/kethtacatani/Stay-Alert-Driver-Drowsiness-Detection-Using-CNN"; // Your URL here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }

            @Override
            public void onActionClicked() {

            }
        });

        closAppAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.addFragment(new ProfileFrag());
            }
        });

        gitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="git";
                helper.customDialog("Redirect anyway");
                helper.showDialog("Github Repository", "This action will exit the app");
            }
        });

        return view;
    }
}