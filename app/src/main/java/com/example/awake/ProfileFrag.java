package com.example.awake;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;


public class ProfileFrag extends Fragment implements View.OnTouchListener{
    private static final String TAG = "ProfileFrag";
    ConstraintLayout profileLogout,edit, changePass,cancelSave,showInfoLayout, changPassLayout, profileAppDev, profileAppAbout, profileAppPrivacy;
    LinearLayout toBeGone;
    TextInputEditText fName,mName,lName,suffix,contact,age,address,newPassword,conPassword, oldPassword;
    TextInputLayout fNameIL,mNameIL,lNameIL,suffixIL,contactIL,ageIL,addressIL,newPasswordIL,conPasswordIL, oldPasswordIL;
    TextView profileName, profileAddress, save, cancel;
    CircleImageView profileImage;
    ImageView editArrow, profileCam, changePassArrow;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();
    CameraActivity cameraActivity;
    private DialogHelper dialogHelper;
    String dialogAction="";
    CountryCodePicker ccp;
    public static final int PICK_IMAGE= 20;
    boolean profilePicChanged = false;
    Bitmap picBitmap, oldPic;
    Switch toggleBGDetectionSW;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fNameIL=view.findViewById(R.id.PE_FNameLayout);
        mNameIL=view.findViewById(R.id.PE_MNameLayout);
        lNameIL=view.findViewById(R.id.PE_LNameLayout);
        suffixIL=view.findViewById(R.id.PE_SuffixLayout);
        profileLogout =view.findViewById(R.id.profileLogout);
        edit = view.findViewById(R.id.profileEdit);
        contactIL=view.findViewById(R.id.PE_ContactLayout);
        ageIL=view.findViewById(R.id.PE_AgeLayout);
        addressIL=view.findViewById(R.id.PE_AddressLayout);
        newPasswordIL=view.findViewById(R.id.PE_PassLayout);
        conPasswordIL=view.findViewById(R.id.PE_ConPassLayout);
        oldPasswordIL=view.findViewById(R.id.PE_OldPassLayout);
        changePass=view.findViewById(R.id.profileChangePass);
        toBeGone=view.findViewById(R.id.profileTobeGone);
        cancelSave=view.findViewById(R.id.ProfileSaveCancel);
        profileName = view.findViewById(R.id.profileName);
        profileAddress = view.findViewById(R.id.profileAddress);
        save=view.findViewById(R.id.ProfileSave);
        cancel=view.findViewById(R.id.ProfileCancel);
        profileCam=view.findViewById(R.id.profileCam);
        profileImage=view.findViewById(R.id.profilePicIV);
        editArrow=view.findViewById(R.id.editArrow);
        showInfoLayout=view.findViewById(R.id.EditProfileLayout);
        fName=view.findViewById(R.id.PE_FName);
        mName=view.findViewById(R.id.PE_MName);
        lName=view.findViewById(R.id.PE_LName);
        suffix=view.findViewById(R.id.PE_Suffix);
        age=view.findViewById(R.id.PE_Age);
        address=view.findViewById(R.id.PE_Address);
        oldPassword=view.findViewById(R.id.PE_OldPass);
        newPassword=view.findViewById(R.id.PE_Pass);
        conPassword=view.findViewById(R.id.PE_ConPass);
        changePassArrow=view.findViewById(R.id.changPassArrow);
        changPassLayout=view.findViewById(R.id.ChangePassLayout);
        contact=view.findViewById(R.id.PE_Contact);
        profileAppDev = view.findViewById(R.id.profileAppDevs);
        profileAppAbout = view.findViewById(R.id.profileAbout);
        profileAppPrivacy=view.findViewById(R.id.profileAppPrivacy);
        toggleBGDetectionSW = view.findViewById(R.id.profileToggleDetectionSW);

        fName.setOnTouchListener(this);
        mName.setOnTouchListener(this);
        lName.setOnTouchListener(this);
        suffix.setOnTouchListener(this);
        age.setOnTouchListener(this);
        contact.setOnTouchListener(this);
        address.setOnTouchListener(this);
        oldPassword.setOnTouchListener(this);
        newPassword.setOnTouchListener(this);
        conPassword.setOnTouchListener(this);
        


        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        cameraActivity = (CameraActivity) getActivity();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(cameraActivity, GoogleSignInOptions.DEFAULT_SIGN_IN);
        dialogHelper = new DialogHelper(cameraActivity);


        displayUserInfo();



        ccp = view.findViewById(R.id.ccp);
        ccp.hideNameCode(true);
        ccp.registerPhoneNumberTextView(contact);


        dialogHelper = new DialogHelper(cameraActivity, new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                System.out.println("okay");
            }

            @Override
            public void onActionClicked() {
                if(dialogAction.equals("logout")){
                    Intent intent = new Intent(getActivity(), Sign_in.class);
                    cameraActivity.stopActivity();
                    cameraActivity.ringtone.stop();
                    startActivity(intent);
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut();

                }else if (dialogAction.equals("signup")){
                    Intent intent = new Intent(getActivity(), Sign_up.class);
                    cameraActivity.stopActivity();
                    cameraActivity.ringtone.stop();
                    startActivity(intent);
                    FirebaseAuth.getInstance().signOut();
                    googleSignInClient.signOut();
                }else if(dialogAction.equals("discard")){
                    if(profilePicChanged){
                        profilePicChanged=false;
                        profileImage.setImageBitmap(oldPic);
                    }
                    displayTBG();
                }

                dialogAction="";
            }
        });



        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toBeGone.getVisibility()==View.VISIBLE){
                    if(!userData.isEmpty()){
                        showInfo();
                    }else{
                        dialogHelper.signUpDialog();
                        dialogAction="signup";
                        dialogHelper.errorTitle();
                        dialogHelper.showDialog("Change Password","You need to sing up first");
                    }
                }else{
                    if(!isNoChanges()){
                        dialogHelper.discardDialog();
                        dialogAction="discard";
                        dialogHelper.showDialog("Edit Profile","Discard changes?");
                    }else{
                        displayTBG();
                    }
                }

            }
        });

        toggleBGDetectionSW.setChecked(CameraActivity.canAlarmGlobal);

        toggleBGDetectionSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    CameraActivity.canAlarmGlobal =true;
                    CameraActivity.canAlarm=true;
                }else{
                    CameraActivity.canAlarmGlobal =false;
                    CameraActivity.canAlarm=false;
                    cameraActivity.vibrator.cancel();
                    cameraActivity.ringtone.stop();
                    cameraActivity.statusDriver=" ACTIVE ";
                    cameraActivity.statusDriverMouth=" ACTIVE ";
                }
            }
        });

        profileAppDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new ProfileFrag();
                cameraActivity.addFragment(new AppDevFragment());
            }
        });
        profileAppAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new ProfileFrag();
                cameraActivity.addFragment(new AppAboutFrag());
            }
        });

        profileAppPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new ProfileFrag();
                cameraActivity.addFragment(new PrivacyPolicyFrag());
            }
        });


        profileCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);

            }
        });
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toBeGone.getVisibility()==View.VISIBLE) {
                    if (!userData.isEmpty()) {
                        if (!userData.get("sign_in_method").toString().equals("google")) {
                            showChangePass();
                        } else {
                            dialogHelper.errorTitle();
                            dialogHelper.showDialog("Change Password","You are signed in using a Google");
                        }
                    } else {
                        dialogHelper.signUpDialog();
                        dialogAction="signup";
                        dialogHelper.errorTitle();
                        dialogHelper.showDialog("Change Password","You need to sing up first");
                    }
                }else{
                    displayTBG();
                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showInfoLayout.getVisibility()==View.VISIBLE){
                    Map<String, Object> userData = new HashMap<>();

                    userData.put("first_name", fName.getText().toString());
                    userData.put("middle_name", mName.getText().toString().trim().isEmpty() ? "" : mName.getText().toString());
                    userData.put("last_name", lName.getText().toString());
                    userData.put("suffix", suffix.getText().toString().trim().isEmpty() ? "" : suffix.getText().toString());
                    userData.put("contact", contact.getText().toString().replace(" ", ""));
                    userData.put("address", address.getText().toString());
                    userData.put("age", age.getText().toString());

                    if(!isNoChanges()){
                        if(!fName.getText().toString().trim().isEmpty()  && !lName.getText().toString().trim().isEmpty() && !contact.getText().toString().trim().isEmpty()
                                && !address.getText().toString().trim().isEmpty() && !age.getText().toString().trim().isEmpty()){
                            if(ccp.isValid()){
                                if(!contact.getText().toString().replace(" ", "").equals(ProfileFrag.this.userData.get("contact"))){
                                    firebaseDB.isContactUnique(contact.getText().toString().replace(" ", ""), (isUnique, errorMessage) -> {
                                        if(isUnique && errorMessage==null){
                                            updateUserInfo(userData, ProfileFrag.this.userData.get("contact").toString());
                                        }else if(!isUnique && errorMessage==null){
                                            dialogHelper.errorTitle();
                                            dialogHelper.showDialog("Edit Profile","Contact number was already taken");

                                        }else if (errorMessage.contains("offline")){
                                            dialogHelper.errorTitle();
                                            dialogHelper.showDialog("Edit Profile","Cannot change contact number when offline");
                                        }else{
                                            dialogHelper.errorTitle();
                                            dialogHelper.showDialog("Edit Profile","There is an error with the database");
                                            Log.e(TAG,errorMessage);
                                        }
                                    });
                                }else{
                                    updateUserInfo(userData,null);
                                    if(profilePicChanged){
                                        saveProfilePicToServer();
                                        profilePicChanged=false;
                                    }
                                }
                            }else{
                                contactIL.setError("Invalid phone number*");
                            }
                        }else{
                            fNameIL.setError((fName.getText().toString().trim().isEmpty())?"Required*":null);
                            lNameIL.setError((lName.getText().toString().trim().isEmpty())?"Required*":null);
                            addressIL.setError((address.getText().toString().trim().isEmpty())?"Required*":null);
                            ageIL.setError((age.getText().toString().trim().isEmpty())?"Required*":null);
                            contactIL.setError((contact.getText().toString().trim().isEmpty())?"Required*":null);
                        }
                    }else{
                        dialogHelper.errorTitle();
                        dialogHelper.showDialog("Edit Profile","No changes have been made");
                    }

                }else if(changPassLayout.getVisibility()==View.VISIBLE){
                    Map<String, Object> userNewPass = new HashMap<>();

                    userNewPass.put("password", newPassword.getText().toString());

                    if(!passIsEmpty()){
                        if(oldPassword.getText().toString().equals(userData.get("password"))){
                            if(newPassword.getText().toString().equals(conPassword.getText().toString())){
                                if(newPassword.getText().toString().length()>5){
                                    if(!newPassword.getText().toString().equals(userData.get("password"))){
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(user.getEmail(), userData.get("password").toString());
                                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentReference ref = db.collection("users").document(auth.getUid());
                                                                firebaseDB.updateUserInfo(userNewPass,ref, new FirebaseDatabase.TaskCallback<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void result) {
                                                                        dialogHelper.showDialog("Edit Profile","Password successfully changed");
                                                                        displayTBG();
                                                                        getUserInfo();
                                                                    }
                                                                    @Override
                                                                    public void onFailure(String errorMessage) {
                                                                        dialogHelper.errorTitle();
                                                                        dialogHelper.showDialog("Change Password",errorMessage);
                                                                    }
                                                                });

                                                            } else {
                                                                Log.d(TAG, "Error password not updated "+task.getException().getLocalizedMessage());
                                                            }
                                                        }
                                                    });
                                                }else if (task.getException().getLocalizedMessage().contains("network error")){
                                                    // If sign in fails, handle different error scenarios
                                                    dialogHelper.errorTitle();
                                                    dialogHelper.showDialog("Sign in Failed", "No connection to the database");
                                                } else {
                                                    dialogHelper.errorTitle();
                                                    dialogHelper.showDialog("Change Password",firebaseDB.failureDialog(task));
                                                    Log.d(TAG, "Error auth failed "+task.getException().getLocalizedMessage());
                                                }
                                            }
                                        });
                                    }else{
                                        newPasswordIL.setError("New password is the same with the current one*");
                                        conPasswordIL.setError("New password is the same with the current one*");
                                        newPasswordIL.setErrorIconDrawable(null);
                                        conPasswordIL.setErrorIconDrawable(null);
                                        newPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                        conPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                    }
                                }else{

                                }
                            }else{
                                newPasswordIL.setError("Password do not match*");
                                conPasswordIL.setError("Password do not match*");
                                newPasswordIL.setErrorIconDrawable(null);
                                conPasswordIL.setErrorIconDrawable(null);
                                newPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                                conPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                            }
                        }else{
                            oldPasswordIL.setError("Wrong password");
                            oldPasswordIL.setErrorIconDrawable(null);
                            oldPasswordIL.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                        }

                    }else{
                        oldPasswordIL.setError((oldPassword.getText().toString().trim().isEmpty())?"Required*":null);
                        newPasswordIL.setError((newPassword.getText().toString().trim().isEmpty())?"Required*":null);
                        conPasswordIL.setError((conPassword.getText().toString().trim().isEmpty())?"Required*":null);
                    }


                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showInfoLayout.getVisibility()==View.VISIBLE){
                    if(!isNoChanges()){
                        dialogHelper.discardDialog();
                        dialogAction="discard";
                        dialogHelper.showDialog("Edit Profile","Discard changes?");
                    }else{
                        displayTBG();
                    }
                }else if (changPassLayout.getVisibility()==View.VISIBLE){
                    if(!isNoPassChanges()){
                        dialogHelper.discardDialog();
                        dialogAction="discard";
                        dialogHelper.showDialog("Change Password","Discard changes?");
                    }else{
                        displayTBG();
                    }
                }

            }
        });

        profileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogHelper.logoutDialog();
                dialogHelper.showDialog("Logout","Confirm logout?");
                dialogAction="logout";
            }
        });

        displayProfilePic();

        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                Uri imageUri = data.getData();
                try {
                    picBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    profileImage.setImageBitmap(picBitmap);
                    profilePicChanged=true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveProfilePicToServer(){
        String result[] =firebaseDB.saveImageToLocal("profile_pic",picBitmap,"userRes");
        Map<String, Object> imageInfo = new HashMap<>();
        imageInfo.put("image_info","profile_pic");
        imageInfo.put("file_name",result[1]);
        imageInfo.put("local_path",result[0]);
        imageInfo.put("firestore_path","users/"+user.getUid()+"/user_res/"+result[1]);
        imageInfo.put("storage_path","users/"+user.getUid()+"/user_res");

        firebaseDB.saveFileInfoToFirestore(imageInfo, "upload_queue");
        firebaseDB.saveFileInfoToFirestore(imageInfo, "user_res");
    }

    public void displayProfilePic(){
        Bitmap bitmap = firebaseDB.getImageFromLocal(cameraActivity, auth.getUid()+"/userRes","profile_pic.jpg");
        if(bitmap!=null){
            profileImage.setImageBitmap(bitmap);
        }else{
            firebaseDB.getImageFromServer("profile_pic.jpg",auth.getUid()+"/user_res",  new FirebaseDatabase.BitmapTaskCallback() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    oldPic=bitmap;
                    profileImage.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.w("ViewDetectionHelper", errorMessage);
                }
            });
        }
        oldPic=bitmap;
    }

    public void updateUserInfo(Map userData, String oldContact){
        //update user information

        DocumentReference ref =db.collection("users").document(auth.getUid());
        firebaseDB.updateUserInfo(userData,ref, new FirebaseDatabase.TaskCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                if(oldContact!=null){
                    //write contact
                    Map<String, Object> userContact = new HashMap<>();
                    userContact.put("userID", auth.getUid());
                    firebaseDB.writeUserInfo(userContact,"contact_numbers", userData.get("contact").toString(), new FirebaseDatabase.TaskCallback<Void>() {
                        //delete the old contact
                        @Override
                        public void onSuccess(Void result) {
                            firebaseDB.deleteDocument("contact_numbers", oldContact, new FirebaseDatabase.TaskCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    updateSuccessDialog();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.w(TAG+" Delete Document ",errorMessage);
                                    dialogHelper.showDialog("Edit Profile", errorMessage);
                                }
                            });
                        }
                        @Override
                        public void onFailure(String errorMessage) {
                            Log.w(TAG+" Delete Document ",errorMessage);
                            dialogHelper.showDialog("Edit Profile", errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                dialogHelper.showDialog("Edit Profile",errorMessage);
            }
        });
        if(oldContact==null){
            updateSuccessDialog();
        }
    }

    public void updateSuccessDialog(){
            dialogHelper.showDialog("Edit Profile","Profile information successfully updated");
            displayTBG();
            getUserInfo();
    }


    public void getUserInfo(){
        firebaseDB.readData("users", auth.getUid(),"cache", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
                cameraActivity.userInfo=documentSnapshot.getData();
                displayUserInfo();
            }

            @Override
            public void onStart() {
                Log.d(TAG, "Start getting user info");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Failed getting user info: "+e);
                Toast.makeText(cameraActivity, "Unable to save user information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void displayUserInfo(){
        userData= cameraActivity.userInfo;
        if(!userData.isEmpty()){profileName.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
//            HomeFrag. nameDriverTV.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
        //fixthis
            profileAddress.setText(userData.get("address").toString());
        }
    }



    public void showChangePass(){
        changPassLayout.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);
        toBeGone.setVisibility(View.GONE);
        oldPassword.setText("");
        newPassword.setText("");
        conPassword.setText("");
        oldPasswordIL.setErrorEnabled(false);
        newPasswordIL.setErrorEnabled(false);
        conPasswordIL.setErrorEnabled(false);

        cancelSave.setVisibility(View.VISIBLE);
        changePassArrow.setRotation(90);
    }

    public void showInfo(){
        fName.setText(userData.get("first_name").toString());
        mName.setText(userData.get("middle_name").toString());
        lName.setText(userData.get("last_name").toString());
        suffix.setText(userData.get("suffix").toString());
        contact.setText(userData.get("contact").toString());
        age.setText(userData.get("age").toString());
        address.setText(userData.get("address").toString());

        changePass.setVisibility(View.GONE);
        toBeGone.setVisibility(View.GONE);
        cancelSave.setVisibility(View.VISIBLE);
        showInfoLayout.setVisibility(View.VISIBLE);
        editArrow.setRotation(90);
        profileCam.setVisibility(View.VISIBLE);
    }

    public boolean isNoChanges(){
        if(fName.getText().toString().trim().equals(userData.get("first_name").toString())  && mName.getText().toString().trim().equals(userData.get("middle_name").toString())
                && lName.getText().toString().trim().equals(userData.get("last_name").toString()) && suffix.getText().toString().trim().equals(userData.get("suffix").toString())
                && contact.getText().toString().replace(" ", "").equals(userData.get("contact").toString())  && address.getText().toString().trim().equals(userData.get("address").toString())
                && age.getText().toString().trim().equals(userData.get("age").toString()) && !profilePicChanged){
            return true;
        }
        return false;
    }

    public boolean isNoPassChanges(){
        if(oldPassword.getText().toString().trim().isEmpty() && newPassword.getText().toString().trim().isEmpty() && conPassword.getText().toString().trim().isEmpty()){
            return true;
        }
        return false;
    }

    public void displayTBG(){
        edit.setVisibility(View.VISIBLE);
        changePass.setVisibility(View.VISIBLE);
        toBeGone.setVisibility(View.VISIBLE);
        cancelSave.setVisibility(View.GONE);
        showInfoLayout.setVisibility(View.GONE);
        changPassLayout.setVisibility(View.GONE);
        profileCam.setVisibility(View.GONE);
        editArrow.setRotation(0);
        changePassArrow.setRotation(0);
    }

    public boolean passIsEmpty(){
        if(oldPassword.getText().toString().trim().isEmpty()  || newPassword.getText().toString().trim().isEmpty() || conPassword.getText().toString().trim().isEmpty()){
            return true;
        }
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.PE_FName:
                fNameIL.setErrorEnabled(false);
                break;
            case R.id.PE_LName:
                lNameIL.setErrorEnabled(false);
                break;
            case R.id.PE_Age:
                ageIL.setErrorEnabled(false);
                break;
            case R.id.PE_Contact:
                contactIL.setErrorEnabled(false);
                break;
            case R.id.PE_Address:
                addressIL.setErrorEnabled(false);
                break;
            case R.id.PE_OldPass:
                oldPasswordIL.setErrorEnabled(false);
                break;
            case R.id.PE_Pass:
                newPasswordIL.setErrorEnabled(false);
                break;
            case R.id.PE_ConPass:
                conPasswordIL.setErrorEnabled(false);
                break;
        }
        return false;
    }
}