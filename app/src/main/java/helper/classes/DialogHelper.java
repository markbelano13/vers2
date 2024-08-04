package helper.classes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.awake.R;

public class DialogHelper {
    private Dialog dialog;
    private TextView dialogTitle;
    private TextView dialogInfo,dialogOkay, dialogAction;
    private LottieAnimationView dialogLoading;

    public DialogHelper(Context context) {
        instantiate(context);
    }

    public interface DialogClickListener {
        void onOkayClicked();
        void onActionClicked();
    }

    private DialogClickListener dialogClickListener;

    public DialogHelper(Context context, DialogClickListener listener) {
        instantiate(context);
        // Set the listener for the cancel button
        dialogOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onOkayClicked();
                }
                dialog.dismiss();
                normalDialog();
            }
        });

        dialogAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onActionClicked();
                }
                dialog.dismiss();
                normalDialog();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (dialogClickListener != null) {
                    normalDialog();
                }
            }
        });

        this.dialogClickListener = listener;
    }

    public void instantiate(Context context){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.pop_up_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        dialogTitle = dialog.findViewById(R.id.TVTitle);
        dialogInfo = dialog.findViewById(R.id.TVInfo);
        dialogOkay = dialog.findViewById(R.id.TVCancel);
        dialogAction =  dialog.findViewById(R.id.TVAction);
        dialogLoading= dialog.findViewById(R.id.Dialogloading);
    }

    public void showEntrance(){
        dialog.setContentView(R.layout.fullscreen_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    public void resetLayout(){
        dialog.setContentView(R.layout.pop_up_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.dismiss();
    }

    public void showDialog(String title, String info) {
        if(title.contains("Failed")){
            errorTitle();
        }
        dialogTitle.setText(title);
        dialogInfo.setText(info);
        dialog.show();
    }

    public void showLoadingDialog(String title,String info){
        dialogTitle.setText(title);
        dialogLoading.setVisibility(View.VISIBLE);
        dialogInfo.setText(info);
        dialogOkay.setVisibility(View.GONE);
        dialogLoading.playAnimation();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }


    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    public void logoutDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Logout");
    }

    public void signUpDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Sign up");
    }

    public void signInDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Sign in");
    }
    public void customDialog(String action){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText(action);
    }

    public void discardDialog(){
        dialogAction.setVisibility(View.VISIBLE);
        dialogOkay.setText("Cancel");
        dialogAction.setText("Discard");
    }


    public void normalDialog(){
        if(dialogLoading.getVisibility()==View.VISIBLE){
            dialogLoading.setVisibility(View.GONE);
            dialogOkay.setVisibility(View.VISIBLE);
            dialogLoading.pauseAnimation();
        }
        dialogAction.setVisibility(View.GONE);
        dialogOkay.setText("Okay");
        dialogTitle.setTextColor(Color.parseColor("#000000"));
    }

    @SuppressLint("ResourceAsColor")
    public void errorTitle(){
        dialogTitle.setTextColor(Color.parseColor("#FF0000"));
    }
}
