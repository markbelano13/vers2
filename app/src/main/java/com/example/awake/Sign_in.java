package com.example.awake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android. os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;

public class Sign_in extends AppCompatActivity {
    private static final String TAG = "Sign_up";
    Dialog dialog;
    TextView dialogOkay, dialogTitle, dialogInfo;
    Button signInBtn;
    FirebaseFirestore db;
    LottieAnimationView buttonAnimation;
    boolean offlineMode=false;
    Handler handler;
    Runnable connectivityCheckRunnable;
    FirebaseAuth mAuth;
    FirebaseUser user;
    TextView creatAccTV;
    GoogleSignInClient googleSignInClient;
    CollectionReference collection;
    EditText username, password;
    GoogleSignInAccount account;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData;
    boolean cameraPermissionDialog=false;
    boolean userDataExist=false;
    private DialogHelper splashDialogHelper;
    private DialogHelper dialogHelper;
    ConstraintLayout signInLayout;


    @Override
    public void onStart() {
        super.onStart();

        if(checkAndRequestPermissions()){
            signInUser();
        }



        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseDB = new FirebaseDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        splashDialogHelper=new DialogHelper(this);
        splashDialogHelper.showEntrance();

        if(user != null){


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getUserInfo();
                }
            }, 2000);

        }
        else{
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    splashDialogHelper.resetLayout();
                }
            }, 2000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,options);


        signInLayout= findViewById(R.id.signInLayout);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                signInLayout.setVisibility(View.VISIBLE);
            }
        }, 2000);

        creatAccTV = (TextView)findViewById(R.id.TVcreateAcc);
        signInBtn = ( Button)findViewById(R.id.BTNsignin);
        buttonAnimation=findViewById(R.id.BTNloading);
        username = findViewById(R.id.ETusername);
        password = findViewById(R.id.ETpass);

        String email=getIntent().getStringExtra("email");
        if(email!=null){
            username.setText(email);
        }
        dialogHelper = new DialogHelper(Sign_in.this);

        dialogHelper = new DialogHelper(Sign_in.this, new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                if(cameraPermissionDialog){
                    viewCameraPermission();
                    cameraPermissionDialog=false;
                }
            }

            @Override
            public void onActionClicked() {
                System.out.println("kj");
            }

        });



        creatAccTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser("");
            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    username.setError(null);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    password.setError(null);
                }
            }
        });

        username.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(username, hasFocus));
        password.setOnFocusChangeListener((v, hasFocus) -> handleEditTextFocusChange(password, hasFocus));
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.setError(null);
            }
        });


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String email = username.getText().toString().trim();
                String pass = password.getText().toString().trim();


                if(username.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()){
                    username.setError(password.getText().toString().trim().isEmpty()?"Required*":null);
                    password.setError(password.getText().toString().trim().isEmpty()?"Required*":null);
                }
                else if(pass.length()<6){
                    if(pass.length()<6){
                        password.setError("Password must be atleast 6 characters long*");
                    }
                }else if(!username.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
                    playLoadingAnim();
                    mAuth.signInWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(Sign_in.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        signInUser();
                                    } else if (task.getException().getLocalizedMessage().contains("network error")){
                                        // If sign in fails, handle different error scenarios
                                        dialogHelper.showDialog("Sign in Failed", "No connection to the database");
                                        hideLoading();
                                    }else{
                                        dialogHelper.showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                        hideLoading();
                                    }
                                }
                            });

                }




            }
        });



    }

    private void getUserInfo(){
        firebaseDB.readData("users", user.getUid(),"default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
                userData = documentSnapshot.getData();
                if(userData==null || userData.isEmpty()) {
                    Log.w(TAG,"No data exist");
                    signUpUser(user.getEmail());
                }else{
                    if(ifHasCameraPermission() && ifHasStoragePermission()){
                        signInUser();
                    }
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
                signInBtn.setText("");
            }
        });
    }

    private void signUpUser(String email) {
        Intent intent = new Intent(getApplicationContext(), Sign_up.class);
        intent.putExtra("email",email);
        startActivity(intent);
        stopActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                account = task.getResult(ApiException.class);

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    isUserInfoExists();
                                    playLoadingAnim();
                                }else if (!task.getException().getLocalizedMessage().contains("network error")){
                                    // If sign in fails, handle different error scenarios
                                    dialogHelper.showDialog("Sign in Failed", firebaseDB.failureDialog(task));
                                    hideLoading();

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogHelper.showDialog("Sign in Failed", firebaseDB.onFailureDialog(e));
                                hideLoading();
                            }
                        });

            } catch (ApiException e) {
                e.printStackTrace();
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

    public void isUserInfoExists(){
        DocumentReference docIdRef = db.collection("users").document(mAuth.getUid());
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        signInUser();
                        userDataExist=true;
                    } else {
                        System.out.println("doesnt exists");
                        signUpUser(account.getEmail());
                    }
                }
            }
        });
    }




    public void signInUser(){
        user = mAuth.getCurrentUser();
        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
        if(user!=null){
            startActivity(intent);
            stopActivity();

        }
        else{
            dialogHelper.showDialog("Login Failed", "You need to allow permission for camera usage as it is required for detection");
            hideLoading();
            cameraPermissionDialog=true;
        }


    }



    public void showDialog(String title, String info){
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void hideLoading(){
        buttonAnimation.setVisibility(View.GONE);
        buttonAnimation.pauseAnimation();
        signInBtn.setText("Sign in");
    }
    public boolean ifHasStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_DENIED) {
                return false;
            }

        }else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                return false;
            }

        }
        return true;
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


    public void viewStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_MEDIA_IMAGES},110);
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},110);
        }



    }


    private void stopActivity(){
        finish();
    }

    private void handleEditTextFocusChange(EditText editText, boolean hasFocus) {
        if (hasFocus) {
            switch (editText.getId()) {
                case R.id.ETusername:
                    username.setError(null);
                    break;
                case R.id.ETpass:
                    password.setError(null);
                    break;

            }
        }
    }

    private boolean checkAndRequestPermissions() {
        int permissionReadMediaImages = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int callpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        int readContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int locationpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionReadMediaImages != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
        }
        if (readContactsPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (callpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 3);
            return false;
        }
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 3) {


            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {


                    if (permissions[i].equals(Manifest.permission.CAMERA)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "camera granted");

                        }
                    } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "storage granted");

                        }
                    } else if (permissions[i].equals(Manifest.permission.CALL_PHONE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "call granted");

                        }
                    } else if (permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "sms granted");

                        }
                    } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("msg", "location granted");

                        }
                    }


                }

            }


        }
    }





}