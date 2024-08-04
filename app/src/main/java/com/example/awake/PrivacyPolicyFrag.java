package com.example.awake;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class PrivacyPolicyFrag extends Fragment {
    ImageButton closAppPrivacy;
    CameraActivity cameraActivity=null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_privacy_policy, container, false);


        closAppPrivacy= view.findViewById(R.id.closeAppPrivacy);


        if(!(getActivity() instanceof Sign_up)){
             cameraActivity =(CameraActivity) getActivity();
        }


        closAppPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraActivity!=null){
                    cameraActivity.addFragment(new ProfileFrag());
                }else{
                    getActivity().getSupportFragmentManager().beginTransaction().remove(PrivacyPolicyFrag.this).commit();
                }
            }
        });

        return view;
    }
}