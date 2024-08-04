
package helper.classes;

        import android.app.Dialog;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.awake.CameraActivity;
        import com.example.awake.custom.classes.DetectionLogsInfo;
        import com.example.awake.R;

        import firebase.classes.FirebaseDatabase;

public class ViewDetectedImageHelper {
    Dialog dialog;
    TextView type, timestamp, accuracy, location, inference, title, responseTime;
    LinearLayout responseTimeLL;
    private ImageButton closeBtn;
    private DetectionLogsInfo info;
    ImageView detectionImage;
    FirebaseDatabase firebaseDB;
    Context context;

    public ViewDetectedImageHelper(Context context) {
        this.context=CameraActivity.context;
        instantiate(CameraActivity.context);
    }


    public interface DialogClickListener {
        void onOkayClicked();
    }

    private DialogClickListener dialogClickListener;

    public ViewDetectedImageHelper(Context context, DialogClickListener listener) {

        this.context=context;
        instantiate(context);
        // Set the listener for the cancel button
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogClickListener != null) {
                    dialogClickListener.onOkayClicked();
                }
                dialog.dismiss();
                dismissDialog();
            }
        });


        this.dialogClickListener = listener;



    }
    public void instantiate(Context context){
        firebaseDB= new FirebaseDatabase();
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.fragment_view_detection_image);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        closeBtn=dialog.findViewById(R.id.detectionViewCloseBtn);
        type = dialog.findViewById(R.id.detectionLogsType);
        timestamp= dialog.findViewById(R.id.detectionLogsDate);
        accuracy= dialog.findViewById(R.id.detectionLogsAccuracy);
        location= dialog.findViewById(R.id.detectionLogsLocation);
        inference= dialog.findViewById(R.id.detectionLogsInference);
        title=dialog.findViewById(R.id.detectionLogsTitle);
        detectionImage=dialog.findViewById(R.id.detectionLogsImage);
        responseTime = dialog.findViewById(R.id.detectionLogsResponseTime);
        responseTimeLL= dialog.findViewById(R.id.responseTimeLL);
    }

    public void showDialog(DetectionLogsInfo info){
        type.setText(info.getDetectionType());
        timestamp.setText(info.getTimestamp());
        location.setText(info.getLocation());
        accuracy.setText(info.getAccuracy()+"%");
        inference.setText(info.getInference()+"ms");
        title.setText(info.getTitle());
        detectionImage.setImageResource(R.drawable.blank_detection);
        if(info.getDetectionType().equals("Drowsy")){
            responseTimeLL.setVisibility(View.VISIBLE);
            responseTime.setText(String.format("%.2f",Double.parseDouble(info.getResponseTime())) +"s");
        }else{
            responseTimeLL.setVisibility(View.GONE);
        }

        Bitmap bitmap = firebaseDB.getImageFromLocal(context, info.getLocalPath(),info.getTitle());
        if(bitmap!=null){
            detectionImage.setImageBitmap(bitmap);

        }else{
            Toast.makeText(context, "Loading Image", Toast.LENGTH_SHORT).show();
            firebaseDB.getImageFromServer(info.getTitle(),"detection_images", new FirebaseDatabase.BitmapTaskCallback() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    detectionImage.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.w("ViewDetectionHelper", errorMessage);
                }
            });
        }


        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}