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


public class AppDevFragment extends Fragment {

    ImageButton closAppDev;
    ImageButton gitImageButton, linkImageButton, emailImageButton, phoneImageButton,gitImageButton2, linkImageButton2, emailImageButton2, phoneImageButton2;
    DialogHelper helper;
    String actionType="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_dev, container, false);

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        helper = new DialogHelper(getContext(), new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {

            }

            @Override
            public void onActionClicked() {
                if(actionType.equals("git1")){
                    String url = "https://github.com/kethtacatani"; // Your URL here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if(actionType.equals("linkedIn")){
                    String url = "https://www.linkedin.com/in/keth-dominic-tacatani-11566a164/i"; // Your URL here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }  else if(actionType.equals("email1")){
                    String[] recipient = {"tacatanikethdominic@gmail.com"};
                    String subject = "Email from StayAlert App";
                    String body = "Good Day";
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, recipient);
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, body);

                    startActivity(Intent.createChooser(intent, "Send email"));
                }else if(actionType.equals("phone1")){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:639750229443"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity((intent));
                }else if(actionType.equals("git2")){
                    String url = "https://github.com/missgwenchana"; // Your URL here
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }
        });

        closAppDev= view.findViewById(R.id.closeAppDev);

        closAppDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.addFragment(new ProfileFrag());
            }
        });


        gitImageButton=view.findViewById(R.id.gitAppDev);
        linkImageButton=view.findViewById(R.id.linkedInAppDev);
        emailImageButton=view.findViewById(R.id.emailAppDev);
        phoneImageButton=view.findViewById(R.id.phoneAppDev);
        gitImageButton2=view.findViewById(R.id.gitAppDev2);
        linkImageButton2=view.findViewById(R.id.linkedInAppDev2);
        emailImageButton2=view.findViewById(R.id.emailAppDev2);
        phoneImageButton2=view.findViewById(R.id.phoneAppDev2);


        gitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="git1";
                helper.customDialog("Redirect anyway");
                helper.showDialog("Github Profile", "This action will exit the app");
            }
        });

        linkImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="linkedIn";
                helper.customDialog("Redirect anyway");
                helper.showDialog("LinkedIn Profile", "This action will exit the app");
            }
        });
        emailImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="email1";
                helper.customDialog("Redirect anyway");
                helper.showDialog("Email", "This action will exit the app");
            }
        });
        phoneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="phone1";
                helper.customDialog("Redirect anyway");
                helper.showDialog("Phone", "This action will exit the app");
            }
        });
        gitImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionType="git2";
                helper.customDialog("Redirect anyway");
                helper.showDialog("Github Profilw", "This action will exit the app");
            }
        });

        linkImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        emailImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        phoneImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        return  view;
    }
}