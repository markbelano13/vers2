package com.example.awake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;

public class ForgotPassActivity extends AppCompatActivity {

    Button resetPass;
    TextView email, signIn;
    FirebaseAuth auth;
    DialogHelper dialogHelper;
    FirebaseDatabase db;
    LottieAnimationView buttonAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        auth = FirebaseAuth.getInstance();
        dialogHelper= new DialogHelper(this);
        db= new FirebaseDatabase();

        resetPass= findViewById(R.id.BTNResetPass);
        email= findViewById(R.id.ETusernameForgotPass);
        signIn= findViewById(R.id.TVsignInForgotPass);
        buttonAnimation=findViewById(R.id.BTNloadingForgotPass);

        dialogHelper = new DialogHelper(this, new DialogHelper.DialogClickListener() {
            @Override
            public void onOkayClicked() {
                signIn(email.getText().toString().trim());
            }

            @Override
            public void onActionClicked() {

            }

        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(null);
            }
        });

        String emailIntent=getIntent().getStringExtra("email");
        if(email!=null){
            email.setText(emailIntent);
        }

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAnimation.playAnimation();
                if(!email.getText().toString().trim().isEmpty()){
                    auth.sendPasswordResetEmail(email.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialogHelper.showDialog("Reset Password", "A link has been sent to your email");
                                    buttonAnimation.pauseAnimation();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogHelper.showDialog("Reset Password",db.onFailureDialog(e));
                                    buttonAnimation.pauseAnimation();
                                }
                            });
                }
            }
        });
    }

    public  void  signIn(String email){
        Intent intent = new Intent(getApplicationContext(), Sign_in.class);
        if(email!=null){
            intent.putExtra("email",email);
        }
        startActivity(intent);
        finish();


    }
}