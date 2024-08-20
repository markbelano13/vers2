package com.example.awake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;

public class Sign_up extends AppCompatActivity {


    private static final String TAG = "Sign_up";
    EditText email,pass,conPass;
    Button signUpBtn;
    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    FirebaseAuth auth;
    FirebaseFirestore db;
    Timer timer = new Timer();
    int signInRetries=0;
    String userId;
    GoogleSignInClient googleSignInClient;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    String signUpMethod="email";
    String userEmail="";
    FirebaseDatabase firebaseDB;
    boolean cameraPermissionDialog=false, userDataExist=false;
    private DialogHelper dialogHelper;
    String dialogAction="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,options);

        dialogHelper=new DialogHelper(Sign_up.this);
        TextView loginAccTV;

        loginAccTV = (TextView)findViewById(R.id.TVloginAcc);
        signUpBtn =  findViewById(R.id.BTNsignup);

        buttonAnimation=findViewById(R.id.BTNSign_upLoading);
        email= findViewById(R.id.ETemailSignup);
        pass= findViewById(R.id.ETpassSignup);
        conPass= findViewById(R.id.ETconPassSignup);




        email.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(email, hasFocus));
        pass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(pass, hasFocus));
        conPass.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(conPass, hasFocus));





        dialogHelper = new DialogHelper(Sign_up.this, new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                if(cameraPermissionDialog){
                    viewCameraPermission();
                    cameraPermissionDialog=false;
                }
            }

            @Override
            public void onActionClicked() {
                if(dialogAction.equals("signin")){
                    signInUser();
                }else if (dialogAction.equals("signInEmail")){
                    Intent intent = new Intent(getApplicationContext(), Sign_in.class);
                    intent.putExtra("email",email.getText().toString().trim());
                    startActivity(intent);
                    auth.signOut();
                    googleSignInClient.signOut();
                    stopActivity();
                }

                dialogAction="";
            }

        });






        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Map<String, Object> userData = new HashMap<>();

                userData.put("first_name", "");
                userData.put("middle_name", "");
                userData.put("last_name", "");
                userData.put("suffix", "");
                userData.put("contact", "00");
                userData.put("address", "");
                userData.put("age", "");
                userData.put("email", email.getText().toString().trim());
                userData.put("password", pass.getText().toString());
                userData.put("sign_in_method", signUpMethod);
                userData.put("last_sign_in", new Date());


                if(!email.getText().toString().trim().isEmpty()  && !pass.getText().toString().trim().isEmpty()
                        && !conPass.getText().toString().trim().isEmpty()){
                    if((pass.getText().toString().trim().length()<6 || conPass.getText().toString().trim().length()<6 ||
                            !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) && signUpMethod.equals("email")){
                        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                            email.setError("Invalid email*");
                        }
                        if(pass.getText().toString().trim().length()<6 ){
                            pass.setError("Password must be atleast 6 characters long*");
                        }
                        if(conPass.getText().toString().trim().length()<6 ){
                            conPass.setError("Password must be atleast 6 characters long*");
                        }


                    }else if(pass.getText().toString().trim().equals(conPass.getText().toString().trim())){

                        if(signInRetries<3){
                            auth.createUserWithEmailAndPassword(email.getText().toString().trim(),pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        createUserInfo(userData);
                                    }
                                    else if (task.getException().getLocalizedMessage().contains("email address is already in use")){
                                        // If sign in fails, handle different error scenarios
                                        dialogAction="signInEmail";
                                        dialogHelper.signInDialog();
                                        dialogHelper.showDialog("Sign in Failed","Account already exist");
                                        hideLoading();
                                    }
                                    else if (task.getException().getLocalizedMessage().contains("network error")){
                                        // If sign in fails, handle different error scenarios
                                        dialogHelper.showDialog("Sign in Failed", "No connection to the database");
                                        hideLoading();
                                    }else{
                                        System.out.println("task "+task.getException().getLocalizedMessage());
                                        dialogHelper.showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                        hideLoading();
                                    }
                                }
                            });
                        }
                        else{
                            dialogHelper.showDialog("Sign up Failed","Too much requests, please try again later");
                        }
                    }
                    else{
                        pass.setError("Password do not match*");
                        conPass.setError("Password do not match*");
                    }
                }
                else{

                    email.setError(email.getText().toString().trim().isEmpty()?"Requried*":null);
                    pass.setError(pass.getText().toString().trim().isEmpty()?"Requried*":null);
                    conPass.setError(conPass.getText().toString().trim().isEmpty()?"Requried*":null);
                }






            }
        });

        loginAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Sign_in.class);
                startActivity(intent);
                auth.signOut();
                googleSignInClient.signOut();
                stopActivity();
            }
        });

        String email =getIntent().getStringExtra("email");
        if (!email.equals("")){
            createAccountWithGoogle(email);
        }


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                userEmail = account.getEmail();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    playLoadingAnim();
                                    isUserHasInfo(null);
                                }else if (!task.getException().getLocalizedMessage().contains("network error")){
                                    // If sign in fails, handle different error scenarios
                                    dialogHelper.showDialog("Sign up Failed",firebaseDB.failureDialog(task));
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogHelper.showDialog("Sign up Failed",firebaseDB.onFailureDialog(e));
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
                Log.e("Sign up",e.getMessage());
                if(e.getStatusCode()==12500){
                    dialogHelper.showDialog("Sign in Failed", "No google play services installed");
                    hideLoading();
                }
                else if(e.getStatusCode()!=12501){
                    dialogHelper.showDialog("Sign in Failed", firebaseDB.onFailureDialog(e));
                    hideLoading();
                }

            }

        }

    }


    private void isUserHasInfo(Map userData){
        firebaseDB.readData("users", auth.getUid(),"default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
                if(documentSnapshot.getData().isEmpty()){
                    if(signUpMethod.equals("google")){
                        createAccountWithGoogle(userEmail);
                    }
                    hideLoading();
                }else{
                    dialogAction="signin";
                    dialogHelper.signInDialog();
                    dialogHelper.showDialog("Sign up","Account already exists");
                    hideLoading();
                }
            }

            @Override
            public void onStart() {
                Log.d(TAG, "Start getting user info");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"Failed getting user info: "+e);
            }
        });

    }

    private void playLoadingAnim() {

        Handler handler = new Handler(); // write in onCreate function
        handler.post(new Runnable() {
            @Override
            public void run() {
                buttonAnimation.setVisibility(View.VISIBLE);
                buttonAnimation.playAnimation();
                signUpBtn.setText("");

            }
        });


    }
    public void createUserInfo(Map userData){
        firebaseDB.writeUserInfo(userData,"users",auth.getUid(), new FirebaseDatabase.TaskCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                //write users contact number
                Map<String, Object> userContact = new HashMap<>();
                userContact.put("userID", auth.getUid());
                firebaseDB.writeUserInfo(userContact,"contact_numbers",userData.get("contact").toString(), new FirebaseDatabase.TaskCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        signInUser();
                        userDataExist=true;
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        dialogHelper.showDialog("Sign up Failed", errorMessage);
                        hideLoading();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                dialogHelper.showDialog("Sign up Failed", errorMessage);
                hideLoading();
            }
        });
    }

    public void createAccountWithGoogle(String email){
//        passLayout.setVisibility(View.GONE);
//        conPassLayout.setVisibility(View.GONE);
//        this.email.setText(email);
//        emailLayout.setEnabled(false);
//        signUpMethod="google";
//        dialogHelper.showDialog("Register Account","Set up your account information");

    }

    private  void creatAccountWithEmail(){
//        passLayout.setVisibility(View.VISIBLE);
//        conPassLayout.setVisibility(View.VISIBLE);
//        this.email.setText("");
//        emailLayout.setEnabled(true);
//        signUpMethod="email";
    }



    public void signInUser(){
        if(ifHasCameraPermission()){
            Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
            startActivity(intent);
            stopActivity();
        }else{
            dialogHelper.showDialog("Sign up Failed", "You need to allow permission for camera usage as it is required for detection");
            hideLoading();
            cameraPermissionDialog=true;
        }
    }

    private void stopActivity(){
        finish();
    }

    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void hideLoading(){
        buttonAnimation.setVisibility(View.GONE);
        buttonAnimation.pauseAnimation();
        signUpBtn.setText("Sign in");
    }

   public void stopSignInTimeout(){
       timer.schedule(new TimerTask() {
           @Override
           public void run() {
               signInRetries=0;
           }
       }, 10000);
    }

    private void handleEditTextFocusChange(EditText editText, boolean hasFocus) {
        if (hasFocus) {
            switch (editText.getId()) {
                case R.id.ETemailSignup:
                    email.setError(null);
                    break;
                case R.id.ETpassSignup:
                    pass.setError(null);
                    pass.setError("Required*");
                    break;
                case R.id.ETconPassSignup:
                    conPass.setError(null);
                    conPass.setError("Required*");
                    break;

            }
        }
    }

    public boolean ifHasCameraPermission(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            return false;
        }
        return true;
    }


    public void viewCameraPermission(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            System.out.println("ok cam");
            // Check if the CAMERA permission is granted after the user responds to the permission request
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(userDataExist){
                    signInUser();
                }
            } else {
                dialogHelper.showDialog("Sign up Failed", "You need to allow permission for camera usage as it is required for detection");
                hideLoading();
            }
        }
    }
}