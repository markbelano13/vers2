package com.example.awake;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awake.custom.classes.ContactsInfo;

import java.util.ArrayList;

import helper.classes.DialogHelper;

public class PhoneFrag extends Fragment implements View.OnClickListener {

    RecyclerView contactsRV;
    ContactsRecycleViewAdapter contactsRecycleViewAdapter;
    LinearLayoutManager linearLayoutManager;
    TextView contactsTV, favoritesTV;
    ImageButton dialPadBtn;
    EditText searchET, phoneNumberET;
    TextView phone1, phone2, phone3,phone4,phone5,phone6,phone7,phone8,phone9,phone0,phoneAsterisk,phoneNumberSign, closeBtn;
    ImageButton callBtn, deleteBtn;
    ConstraintLayout dialPadLayout;
    DialogHelper helper;
    ArrayList<ContactsInfo> mergedList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_phone, container, false);

        if (ActivityCompat.checkSelfPermission(CameraActivity.context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CameraActivity.context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)  {
            activityResultLauncher.launch(Manifest.permission.READ_CONTACTS);
            activityResultLauncher.launch(Manifest.permission.CALL_PHONE);
        }


        contactsRV = view.findViewById(R.id.contactsRV);
        contactsTV = view.findViewById(R.id.phoneContacts);
        favoritesTV= view.findViewById(R.id.contactFavorites);
        dialPadBtn = view.findViewById(R.id.dialPadBtn);
        searchET = view.findViewById(R.id.phoneSearchTV);
        phone0 = view.findViewById(R.id.phone0tv);
        phone1 = view.findViewById(R.id.phone1tv);
        phone2 = view.findViewById(R.id.phone2tv);
        phone3 = view.findViewById(R.id.phone3tv);
        phone4 = view.findViewById(R.id.phone4tv);
        phone5 = view.findViewById(R.id.phone5tv);
        phone6 = view.findViewById(R.id.phone6tv);
        phone7 = view.findViewById(R.id.phone7tv);
        phone8 = view.findViewById(R.id.phone8tv);
        phone9 = view.findViewById(R.id.phone9tv);
        phoneAsterisk = view.findViewById(R.id.phoneAsterisktv);
        phoneNumberSign = view.findViewById(R.id.phoneNumberSigntv);
        callBtn = view.findViewById(R.id.callBtn);
        deleteBtn= view.findViewById(R.id.deleteBtn);
        phoneNumberET = view.findViewById(R.id.phoneNumberET);
        dialPadLayout = view.findViewById(R.id.dialPadLayout);
        closeBtn= view.findViewById(R.id.phoneClosetv);

        helper = new DialogHelper(getActivity(), new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                //used to cancel
            }

            @Override
            public void onActionClicked() {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+phoneNumberET.getText().toString()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity((intent));
            }
        });




        contactsRV.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        contactsRV.setLayoutManager(linearLayoutManager);
        CameraActivity cameraActivity=(CameraActivity)getActivity();



        displayContactList(CameraActivity.contactInfoList);

        contactsTV.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                favoritesTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.black_primary_selector)
                        .getDefaultColor());
                contactsTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.primary_black_selector)
                        .getDefaultColor());

                contactsTV.setBackgroundResource(R.drawable.bg_line_below_text);
                favoritesTV.setBackground(null);

                displayContactList(CameraActivity.contactInfoList);


            }
        });

        favoritesTV.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                contactsTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.black_primary_selector)
                        .getDefaultColor());
                favoritesTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.primary_black_selector)
                        .getDefaultColor());

                favoritesTV.setBackgroundResource(R.drawable.bg_line_below_text);
                contactsTV.setBackground(null);
                displayContactList(CameraActivity.favoritesInfoList);


            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called whenever the text is changed.
                // The new text is s.toString().
            }

            @Override
            public void afterTextChanged(Editable s) {
                ArrayList<ContactsInfo> searchedInfoList = new ArrayList<>(searchContacts(CameraActivity.contactInfoList,searchET.getText().toString().trim()));
                displayContactList(searchedInfoList);
            }
        });

        phoneNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called whenever the text is changed.
                // The new text is s.toString().
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(phoneNumberET.getText().toString().trim().isEmpty()){
                    phoneNumberET.setVisibility(View.GONE);
                }else{
                    ArrayList<ContactsInfo> searchedInfoList = new ArrayList<>(searchContacts(CameraActivity.contactInfoList,phoneNumberET.getText().toString().trim()));
                    displayContactList(searchedInfoList);
                }
            }
        });

        dialPadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPadLayout.setVisibility(View.VISIBLE);
                dialPadBtn.setVisibility(View.GONE);
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPadLayout.setVisibility(View.GONE);
                displayContactList(CameraActivity.contactInfoList);
                dialPadBtn.setVisibility(View.VISIBLE);
            }
        });

        phone0.setOnClickListener(this);
        phone1.setOnClickListener(this);
        phone2.setOnClickListener(this);
        phone3.setOnClickListener(this);
        phone4.setOnClickListener(this);
        phone5.setOnClickListener(this);
        phone6.setOnClickListener(this);
        phone7.setOnClickListener(this);
        phone8.setOnClickListener(this);
        phone9.setOnClickListener(this);
        phoneAsterisk.setOnClickListener(this);
        phoneNumberSign.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        callBtn.setOnClickListener(this);

        return view;
    }



//    public void displayDetectionLogs(){
//        info= cameraActivity.detectionLogsInfo;
//        if(info!=null && !info.isEmpty()){
//            progressBar.setVisibility(View.GONE);
//            recyleViewAdapter = new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
//            detectionLogsRV.setAdapter(recyleViewAdapter);
//            recyleViewAdapter.notifyDataSetChanged();
//        }else{
//            queryDetection();
//        }
//    }



    public void displayContactList(ArrayList<ContactsInfo> info){
        if(CameraActivity.contactInfoList!=null){
            contactsRV.setLayoutManager( new LinearLayoutManager(getContext()));
            contactsRecycleViewAdapter= new ContactsRecycleViewAdapter(info,getContext());
            contactsRV.setAdapter(contactsRecycleViewAdapter);
        }


    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(CameraActivity.context, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    public ArrayList<ContactsInfo> searchContacts(ArrayList<ContactsInfo> info,String searchTerm) {
        ArrayList<ContactsInfo> searchedList = new ArrayList<>();
        for (ContactsInfo contact : info) {
            if (contact.getContactName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    contact.getContactNumber().contains(searchTerm)) {
                searchedList.add(contact);
            }
        }
        return searchedList;
    }

    public void dialpadOnclick(String dialBtn){
        phoneNumberET.setVisibility(View.VISIBLE);
        phoneNumberET.setText(phoneNumberET.getText().toString()+dialBtn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phone1tv:
                dialpadOnclick("1");
                break;
            case R.id.phone2tv:
                dialpadOnclick("2");
                break;
            case R.id.phone3tv:
                dialpadOnclick("3");
                break;
            case R.id.phone4tv:
                dialpadOnclick("4");
                break;
            case R.id.phone5tv:
                dialpadOnclick("5");
                break;
            case R.id.phone6tv:
                dialpadOnclick("6");
                break;
            case R.id.phone7tv:
                dialpadOnclick("7");
                break;
            case R.id.phone8tv:
                dialpadOnclick("8");
                break;
            case R.id.phone9tv:
                dialpadOnclick("9");
                break;
            case R.id.phone0tv:
                dialpadOnclick("0");
                break;
            case R.id.phoneAsterisktv:
                dialpadOnclick("*");
                break;
            case R.id.phoneNumberSigntv:
                dialpadOnclick("#");
                break;
            case R.id.callBtn:
                String currentText = phoneNumberET.getText().toString();
                if (currentText.length() > 2) {
                    helper.customDialog("Call anyway");
                    helper.showDialog("Phone Call", "This action will exit the app");
                }

                break;
            case R.id.deleteBtn:
                String currentTexts = phoneNumberET.getText().toString();
                if (currentTexts.length() > 0) {
                    String newText = currentTexts.substring(0, currentTexts.length() - 1);
                    phoneNumberET.setText(newText);
                }else{
                    phoneNumberET.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }
}