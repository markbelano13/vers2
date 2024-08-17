/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.awake;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.awake.custom.classes.BitmapInfo;
import com.example.awake.custom.classes.ChartGenerator;
import com.example.awake.custom.classes.ContactsInfo;
import com.example.awake.custom.classes.CustomizedExceptionHandler;
import com.example.awake.custom.classes.DetectionLogsInfo;
import com.example.awake.custom.classes.NotificationBuilder;
import com.example.awake.custom.classes.NotificationInfo;
import com.example.awake.custom.classes.SMSManager;
import com.example.awake.env.ImageUtils;
import com.example.awake.env.Logger;
import com.example.awake.env.Utils;
import com.example.awake.tflite.Classifier;
import com.example.awake.tflite.YoloV5Classifier;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
//        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {


  BottomNavigationView bottomNav;
  public static int bottomNavIndex=0;
  private static final Logger LOGGER = new Logger();
  private static final String TAG = "CameraActivity";
  private static final int PERMISSIONS_REQUEST = 1;
  public static String PACKAGE_NAME;
  public static Context context;
  public Date date;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  private static final String ASSET_PATH = "";
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private boolean debug = false;
  protected Handler handler;
  private Handler scanHandler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  protected int defaultModelIndex = 0;
  protected int defaultDeviceIndex = 1;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;
  protected ArrayList<String> modelStrings = new ArrayList<String>();

  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;

  protected TextView frameValueTextView, cropValueTextView, inferenceTimeTextView, msTV;
  protected ImageView bottomSheetArrowImageView;
  private ImageView plusImageView, minusImageView;
  protected ListView deviceView;
  protected TextView threadsTextView;
  protected ListView modelView;
  /** Current indices of device and model. */
  int currentDevice = -1;
  int currentModel = -1;
  int currentNumThreads = -1;
  FrameLayout frameLayout;
  ImageButton backBtn;
  boolean rearCam=false;

  public static int elevation=0;
  public static String eyeStatus="";
  public static String mouthStatus="";
  public Bitmap cropCopyBitmap = null;
  public Bitmap copyBitmap = null;
  BitmapInfo detectedBitmapInfo;
  public long timestamp = 0;
  public long lastProcessingTimeMs;
  public float confidenceLevel=0;
  public YoloV5Classifier detector;

  public static boolean isOfflineMode=false;


  private int currentIndex = 0;
  private int currentIndexYawn  = 0;
  private int closeCount = 0;
  private int yawnCount = 0;
  private Runnable runnableCode;
  private Runnable mouthRunnable;
  boolean ring=false;
  Ringtone ringtone;
  boolean  appStopped=false;
  Handler schedHandler;
  Runnable connectivityCheckRunnable;
  String statusDriver=" ACTIVE ";
  String statusDriverMouth=" ACTIVE ";
  FirebaseUser user;
  FirebaseFirestore db;
  FirebaseDatabase firebaseDB;
  FirebaseStorage storage = FirebaseStorage.getInstance();
  public static Map<String, Object> userInfo = new HashMap<>();
  DialogHelper dialogHelper;

  ArrayList<String> deviceStrings = new ArrayList<String>();

  public static ArrayList<DetectionLogsInfo> detectionLogsInfo = new ArrayList<>();
  public static ArrayList<NotificationInfo> notificationsInfo = new ArrayList<>();
  public static Query query;
  public static int drowsyCount=0, yawnCountRes=0;
  public static double averageResponse=0.0;
  double currentResponseTime =0;
  public static Timestamp lastDetectionTime =null;
  public static NotificationBuilder notificationBuilder;
  public static QuerySnapshot detectionSnapshot=null;

  long lastYawnReportTime = System.currentTimeMillis();
  long lastDrowsyReportTime = System.currentTimeMillis();

  public static Map<String, Object> weatherMap = new HashMap<>();

  Bitmap detectedBitmap;
  String detectedEye, detectMS;
  private long startResponseTime = 0L;
  public static Date lastDetectionOnOpen= new Date();


  public static DocumentSnapshot drowsyCountDocument;
  public static DocumentSnapshot yawnCountDocument;
  public static DocumentSnapshot averageDrowsyCountDocument;

  public static androidx.fragment.app.Fragment lastFragment;
  public static ArrayList<ContactsInfo> contactInfoList = new ArrayList<>();
  public static ArrayList<ContactsInfo> favoritesInfoList = new ArrayList<>();
  public static ChartGenerator chartGenerator;
  public Location location;
  public  Geocoder geocoder;
  public  List<Address> addresses;
  public static String[] addressGlobal = new String[]{"NA","NA"};
  public String[] lastCoordinate =new String[]{"0","0"};
  Vibrator vibrator;
  boolean vibrating=false;
  private boolean toastVisible =false;
  public static boolean canAlarmGlobal =false;
  public static boolean canAlarm=false;
  public List<Date> alarmList= new ArrayList<>(Arrays.asList(new Date[3]));

  FloatingActionButton viewDetection;
  BottomAppBar bottomAppBar;



  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    PACKAGE_NAME = getApplicationContext().getPackageName();
    context=getApplicationContext();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      View decorView = getWindow().getDecorView();
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }
    firebaseDB= new FirebaseDatabase();
    db = FirebaseFirestore.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();

    getUserInfo();




    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler(
            "/mnt/sdcard/"));

    notificationBuilder = new NotificationBuilder();
    dialogHelper= new DialogHelper(this);
    setContentView(R.layout.tfe_od_activity_camera);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    dialogHelper.showLoadingDialog("Fetching Content","Getting things ready...");

    frameLayout = findViewById(R.id.container);
    scanHandler = new Handler();
    Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    // Create a Ringtone object from the URI
    ringtone = RingtoneManager.getRingtone(CameraActivity.this, defaultRingtoneUri);


    backBtn= findViewById(R.id.backBtn);
    geocoder = new Geocoder(this, Locale.getDefault());

    bottomNav = findViewById(R.id.bottomNavigationView);
    bottomNav.setBackground(null);
    viewDetection = findViewById(R.id.viewDetection);
    bottomAppBar = findViewById(R.id.bottomAppBar);





    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    long[] pattern = {0, 1000, 1000};
    String[] values = new String[20];
    detectedBitmapInfo= new BitmapInfo();
    


    runnableCode = new Runnable() {
      @Override
      public void run() {

        String eyeValue = eyeStatus;

        if ("closed".equals(eyeValue) && closeCount<20) {
          closeCount++;
        }else if (closeCount>0){
          closeCount--;
        }
        values[currentIndex] = eyeValue;
        currentIndex = (currentIndex + 1) % 20; // Wrap around to the beginning of the array
        double closePercentage = (closeCount / 20.0) * 100.0;

        if(canAlarm){
          if(closePercentage>70 && !ringtone.isPlaying() && !appStopped ){
            toastAMessage("Drowsiness Alert! Your safety is at risk");

            // Play the default ringtone
            if(detectedEye==null && values[values.length-1]!=null && detectedBitmapInfo.getResultTitles().contains("closed")){
              ringtone.play();
              detectedBitmap= detectedBitmapInfo.getBitmap();
              detectedEye=eyeStatus;
              detectMS=lastProcessingTimeMs+"";
              startResponseTime= System.currentTimeMillis();
              statusDriver=" DROWSY ";
            }
          }

          else if(closePercentage<60){
            if(detectedEye!=null && values[values.length-1]!=null){
              System.out.println("insert");
              currentResponseTime = (double) (System.currentTimeMillis() - (double)startResponseTime - 1500.0)/1000.0;
              if(currentResponseTime<0){
                currentResponseTime=0.3;
              }
              startResponseTime = 0L;

              if(lastDrowsyReportTime>3000){
                saveDetectedImage(detectedBitmap,detectedEye, detectMS+"",currentResponseTime);
                lastDrowsyReportTime=System.currentTimeMillis();
                checkAlertForSMS(new Date());

              }
              detectedEye=null;

              statusDriver=" ACTIVE ";
              //start timer for alert response
            }
            ringtone.stop();
          }
        }

//        msTV.setText(statusDriver+" "+(int)closePercentage);

        scanHandler.postDelayed(this, 100); // Schedule the task to run again after 100 milliseconds
      }
    };
    scanHandler.post(runnableCode);

    //FOR MOUTH

    String[] valuesYawn = new String[20];



    mouthRunnable = new Runnable() {
      @Override
      public void run() {


        String mouthValue = mouthStatus;
        if ("yawn".equals(mouthValue) && yawnCount<20) {
          yawnCount++;
        }else if (yawnCount>0){
          yawnCount--;
        }
        valuesYawn[currentIndexYawn] = mouthValue;
        currentIndexYawn = (currentIndexYawn + 1) % 20; // Wrap around to the beginning of the array when past 20 it will be back to 1
        double yawnPercentage = (yawnCount / 20.0) * 100.0;  //convert to perncet base on stringlength

        if(canAlarm){
          if(yawnPercentage>70 && !ringtone.isPlaying() && !appStopped ){
            toastAMessage("Heads up! A yawn can be a sign of fatigue. Consider getting some fresh air");


            if(statusDriverMouth.contains("ACTIVE") && valuesYawn[valuesYawn.length-1]!=null && lastYawnReportTime>3000){
              if(detectedBitmapInfo.getResultTitles().contains("yawn")){
                saveDetectedImage(detectedBitmapInfo.getBitmap(),mouthValue,lastProcessingTimeMs+"",0);
                lastYawnReportTime=System.currentTimeMillis();
              }

            }
            statusDriverMouth=" YAWNING ";

          }
          else if(yawnPercentage<=70){
            statusDriverMouth=" ACTIVE ";

          }
        }

        if(HomeFrag.statusDriverTV!=null){
          StatsFrag.userState.setText((statusDriver.contains("ACTIVE"))?statusDriverMouth:statusDriver);


          if(HomeFrag.statusDriverTV.getText().toString().contains("ACTIVE")){
            if(vibrating){
              vibrating=false;
              toastVisible=false;
              vibrator.cancel();
              dialogHelper.dismissDialog();
              dialogHelper.normalDialog();
            }
          }else{
            if(!vibrating){
              vibrating=true;
              vibrator.vibrate(pattern, 0);
            }
          }
        }
        scanHandler.postDelayed(this, 100); // Schedule the task to run again after 100 milliseconds
      }
    };
    scanHandler.post(mouthRunnable);


    bottomNav.setOnItemSelectedListener(item -> {
      switch (item.getItemId()) {
        case R.id.map:
          if(bottomNavIndex!=1){
            bottomNavIndex=1;
            addFragment(new HomeFrag());
            Toast.makeText(CameraActivity.this, "Map", Toast.LENGTH_SHORT).show();
          }
          break;
        case R.id.phone:
          if(bottomNavIndex!=2){
            bottomNavIndex=2;
            addFragment(new PhoneFrag());
            Toast.makeText(CameraActivity.this, "Phone", Toast.LENGTH_SHORT).show();
          }
          break;
        case R.id.detect:

          break;
        case R.id.stats:
          if(bottomNavIndex!=4){
            bottomNavIndex=4;
            addFragment(new StatsFrag());
            Toast.makeText(CameraActivity.this, "Stats", Toast.LENGTH_SHORT).show();
          }
          break;
        case R.id.profile:
          if(bottomNavIndex!=5){
            bottomNavIndex=5;
            addFragment(new ProfileFrag());
            Toast.makeText(CameraActivity.this, "Profile", Toast.LENGTH_SHORT).show();
          }
          break;
      }
      return true;
    });

    viewDetection.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        System.out.println("see scan");
        elevation= 30;
        changeFrameLayoutElevation();
        canAlarm=true;
      }
    });


    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        elevation=0;
        canAlarm= canAlarmGlobal;
        vibrator.cancel();
        ringtone.stop();
        statusDriver=" ACTIVE ";
        statusDriverMouth=" ACTIVE ";
        changeFrameLayoutElevation();
        
      }
    });

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }

    threadsTextView = findViewById(R.id.threads);
    currentNumThreads = Integer.parseInt(threadsTextView.getText().toString().trim());
    plusImageView = findViewById(R.id.plus);
    minusImageView = findViewById(R.id.minus);
    deviceView = findViewById(R.id.device_list);
    deviceStrings.add("CPU");
    deviceStrings.add("GPU");
    deviceStrings.add("NNAPI");
    deviceView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter<String> deviceAdapter =
            new ArrayAdapter<>(
                    CameraActivity.this , R.layout.deviceview_row, R.id.deviceview_row_text, deviceStrings);
    deviceView.setAdapter(deviceAdapter);
    deviceView.setItemChecked(defaultDeviceIndex, true);
    currentDevice = defaultDeviceIndex;
    deviceView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateActiveModel();
              }
            });

    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    gestureLayout = findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
    modelView = findViewById((R.id.model_list));

    modelStrings = getModelStrings(getAssets(), ASSET_PATH);
    modelView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter<String> modelAdapter =
            new ArrayAdapter<>(
                    CameraActivity.this , R.layout.listview_row, R.id.listview_row_text, modelStrings);
    modelView.setAdapter(modelAdapter);
    modelView.setItemChecked(defaultModelIndex, true);
    currentModel = defaultModelIndex;
    modelView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateActiveModel();
              }
            });

    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
              gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
              gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            //                int width = bottomSheetLayout.getMeasuredWidth();
            int height = gestureLayout.getMeasuredHeight();

            sheetBehavior.setPeekHeight(height);
          }
        });
    sheetBehavior.setHideable(false);

    sheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                }
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                }
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
                break;
              case BottomSheetBehavior.STATE_SETTLING:
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

    frameValueTextView = findViewById(R.id.frame_info);
    cropValueTextView = findViewById(R.id.crop_info);
    inferenceTimeTextView = findViewById(R.id.inference_info);
    msTV = findViewById(R.id.msTV);


    plusImageView.setOnClickListener(this);
    minusImageView.setOnClickListener(this);

    Handler delayedExecutionHandler = new Handler();
    delayedExecutionHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        minusImageView.performClick();
        updateActiveModel();
        firebaseDB.checkSync();
      }
    }, 6000);

    Handler loadingHandler = new Handler();
    loadingHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        dialogHelper.dismissDialog();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        canAlarmGlobal = true;
        canAlarm= canAlarmGlobal;
      }
    }, 8000);

    refreshDefaultQuery();

    updateDetectionLogs(null,"");

    getContactList();
    getContactFavoritesList();

  }

  public interface ImageDetectionCallback {
    void onImageDetected(boolean isTrue, List<Classifier.Recognition> results);
  }

  public boolean checkImage(Bitmap bitmap, ImageDetectionCallback callback){
    int size=160;
    Bitmap cropBitmap = Utils.processBitmap(bitmap, size);

    List<String> resultTitles = new ArrayList<>();

    Handler handler = new Handler();

    new Thread(() -> {
      final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
      handler.post(new Runnable() {
        @Override
        public void run() {
          for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= 0.5f) {
              resultTitles.add(result.getTitle());
            }
          }
          if(resultTitles.contains("closed") || resultTitles.contains("yawn")){
            callback.onImageDetected(true, results);
          }
        }
      });
    }).start();

    return false;
  }



  private void refreshDefaultQuery() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 0);

    // Set the time to 12:00 AM
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startDate= calendar.getTime();

    query= db.collection("users/"+user.getUid()+"/image_detection")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", new Date())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10);



  }

  ////////////////////////////////////////////



  public void getUserInfo(){
    firebaseDB.readData("users", user.getUid(),"default", new FirebaseDatabase.OnGetDataListener() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
        userInfo = documentSnapshot.getData();
        bottomNav.setSelectedItemId(R.id.map);
        bottomNav.setSelectedItemId(R.id.stats);
//        bottomNavigation.clearCount(1);

        chartGenerator = new ChartGenerator();
        chartGenerator.setRange("yesterday");


        //moves count if past n days
        firebaseDB.checkStatCount("drowsy");
        firebaseDB.checkStatCount("yawn");
        firebaseDB.checkStatCount("average_response");



      }

      @Override
      public void onStart() {
        Log.d(TAG, "Start getting user info");
      }

      @Override
      public void onFailure(Exception e) {
        Log.d(TAG,"Failed getting user info: "+e);
        Toast.makeText(CameraActivity.this, "Unable to load user information", Toast.LENGTH_SHORT).show();
        bottomNav.setSelectedItemId(R.id.stats);
        addFragment(new StatsFrag());
//        bottomNavigation.clearCount(1);
      }
    });

  }

  @SuppressLint("MissingPermission")
  public void getCurrentAddress(){

    Handler addressHandler= new Handler();
    addressHandler.post(new Runnable() {
      @Override
      public void run() {
        //sige na siya e call nga function to check if address kay na chage
        try {
          if (location != null && (!lastCoordinate[0].equals(String.valueOf(location.getLatitude())) || !lastCoordinate[1].equals(String.valueOf(location.getLongitude())))) {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            lastCoordinate = new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())};
          }else{
            return;
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        String address="NA",city="NA" ,country="NA",state="NA",postalCode, knownName;

        if(addresses!=null){
          address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
          city = addresses.get(0).getLocality();
          state = addresses.get(0).getAdminArea();
          country = addresses.get(0).getCountryName();
          postalCode = addresses.get(0).getPostalCode();
          knownName = addresses.get(0).getFeatureName();

          addressGlobal = new String[]{city,state,address};

          System.out.printf("address "+postalCode);
          System.out.printf("country "+String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude())+" sdf");
        }else{
          if(location!=null){
            addressGlobal = new String[]{String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude())};
          }
        }

        if(!city.equals("NA") && city!=weatherMap.get("city")){
          HomeFrag.getWeatherDetails(city,country);
        }
      }
    });



  }



  public void saveDetectedImage(Bitmap bitmap,String detectionType, String ms, double responseTime){
    if(detectionType.equals("open")||detectionType.equals("no_yawn")){
      return;
    }

    String result[] =firebaseDB.saveImageToLocal(""+timestamp,bitmap,"detections");

    if(result!=null){
      Map<String, Object> imageInfo = new HashMap<>();
      imageInfo.put("detection_name", (detectionType.equals("yawn")?"Yawn":"Drowsy"));
      imageInfo.put("timestamp", date);
      imageInfo.put("location",(addressGlobal.length==3)? addressGlobal[2]: addressGlobal[0]+", "+ addressGlobal[1]);
      imageInfo.put("file_name",result[1]);
      imageInfo.put("local_path",result[0]);
      imageInfo.put("firestore_path","users/"+user.getUid()+"/image_detection/"+result[1]);
      imageInfo.put("storage_path","users/"+user.getUid()+"/detection_images");
      imageInfo.put("inference",ms);
      imageInfo.put("accuracy",(int) ((Math.round(confidenceLevel * 100.0f) / 100.0f)*100));
      imageInfo.put("response_time",responseTime);
      lastDetectionOnOpen =new Date();

      firebaseDB.saveFileInfoToFirestore(imageInfo, "upload_queue");
      firebaseDB.saveFileInfoToFirestore(imageInfo, "image_detection");


      updateDetectionLogs(query, detectionType.equals("yawn")?"Yawn":"Drowsy");
      firebaseDB.incrementCount(detectionType.equals("closed")?"drowsy":detectionType,1);
      firebaseDB.incrementCount("average_response",responseTime);


    }
  }

  public void checkAlertForSMS(Date date){
      alarmList.add(0,date);
      int rangeMinutes=10;

    System.out.println("listss "+alarmList.get(0)+" "+alarmList.get(1)+" "+alarmList.get(2));

    if(alarmList.get(2)!=null){
      long diffInMillis = Math.abs(alarmList.get(0).getTime() - alarmList.get(2).getTime());
      long diffInMinutes = diffInMillis / (60 * 1000);
      if(diffInMinutes <= rangeMinutes){
        if(!favoritesInfoList.isEmpty()){
          for (ContactsInfo contact : favoritesInfoList) {
            SMSManager.sendSMS(convertPhoneNumber(contact.getContactNumber()), "Good "+HomeFrag.getTimeOfDay()+"\n"+userInfo.get("first_name").toString()
                    + " " + userInfo.get("middle_name") + ". " + userInfo.get("last_name") + " " + userInfo.get("suffix") +" is experiencing continuous drowsiness" +
                    " for the last "+rangeMinutes+" minutes. Consider taking action for the safety of "+userInfo.get("first_name").toString());
            System.out.println("sent Sms "+convertPhoneNumber(contact.getContactNumber()));
          }
        }
      };

    }
  }

  public String convertPhoneNumber(String phoneNumber) {
    int index = phoneNumber.indexOf('9');
    if (index != -1) {
      String rightPart = phoneNumber.substring(index);
      return "63" + rightPart;
    }
    return phoneNumber;
  }



  public void setDetectionCountOnLocal(int count){
    Map<String, Object> countInfo = new HashMap<>();
    countInfo.put("today_count", count);
    firebaseDB.writeUserInfo(countInfo, "users/" + user.getUid() + "/user_res/", "detection_records", new FirebaseDatabase.TaskCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        Log.d(TAG+" getCount","Set  successful");
      }

      @Override
      public void onFailure(String errorMessage) {
        Log.d(TAG+" getCount","Set  fail "+errorMessage);
      }
    });
  }

  public void updateDetectionLogs(Query query, String detectionType){
    if(query!=null){
      this.query=query;
    }
    refreshDefaultQuery();

    detectionLogsInfo = firebaseDB.getDetectionLogsInfo(this.query, new FirebaseDatabase.ArrayListTaskCallback<Void>() {
      @Override
      public void onSuccess(ArrayList<DetectionLogsInfo> arrayList) {
        detectionLogsInfo =arrayList;

        if(chartGenerator!=null){
          chartGenerator.fetchDetectionCounts();
        }
//        if(detectionType.equals("Yawn")){
//        }else if(detectionType.equals("Drowsy")){
//          averageResponse= ((averageResponse*drowsyCount)+currentResponseTime)/(drowsyCount+1);
//        }
        StatsFrag statsFrag = (StatsFrag)getSupportFragmentManager().findFragmentByTag("StatsFrag");
        if(statsFrag!=null&& statsFrag.isVisible()){
          StatsFrag fragment = (StatsFrag) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            fragment.displayDetectionLogs();
            fragment.updateDetectionRecords();
        }
      }

      @Override
      public void onFailure(String errorMessage) {
        System.out.println("error "+errorMessage);
      }
    });


  }

  public interface TaskCallback<T> {
    void onSuccess(T result);
    void onFailure(String errorMessage);
  }

  public static Calendar getCurrentDate(){
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 0);

    // Set the time to 12:00 AM
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar;
  }

  public static void getDetectionRecordsCount(String range, TaskCallback callback){

    int limit=24;
    if(range.equals("day3")){
      limit=3;
    }else if(range.equals("day7")) {
      limit = 7;
    }else if(range.equals("day")) {
      limit = 30;
    }

    if(drowsyCountDocument==null || yawnCountDocument==null || averageDrowsyCountDocument==null){
      return;
    }


    double drowsySum=0;
    for (int i = 1; i < 80; i++) {
      String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
      if(drowsyCountDocument.contains(field) && i<= limit){
        int value= (int) Double.parseDouble(drowsyCountDocument.getData().get(field).toString()) ;
        if(value>0){
          drowsySum += value;
        }
      }else{
        break;
      }
    }
    drowsyCount=(int)drowsySum;

    double yawnSum=0;
    for (int i = 1; i < 80; i++) {
      String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
      if(yawnCountDocument.contains(field) && i<= limit){
        int value= (int) Double.parseDouble(yawnCountDocument.getData().get(field).toString()) ;
        if(value>0){
          yawnSum += value;
        }
      }else{
        break;
      }
    }
    yawnCountRes=(int)yawnSum;

    double averageSum=0;
    for (int i = 1; i < 80; i++) {
      String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
      if(averageDrowsyCountDocument.contains(field) && i<= limit){
        double value= Double.parseDouble(averageDrowsyCountDocument.getData().get(field).toString()) ;
        if(value>0){
          averageSum += value;
        }
      }else{
        break;
      }
    }
    averageResponse=Double.isNaN(averageSum/drowsyCount)?0:averageSum/drowsyCount;


    callback.onSuccess(true);
  }


  public void getContactList(){
    Uri uri = ContactsContract.Contacts.CONTENT_URI;
    String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"ASC";
    Cursor cursor = getContentResolver().query(
            uri, null,null,null,sort
    );

    if (cursor.getCount()>0){
      while (cursor.moveToNext()){
        @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?";

        Cursor cursor1= getContentResolver().query(
                uriPhone, null, selection, new String[]{id},null
        );

        if (cursor1.moveToNext()){
          @SuppressLint("Range") String number = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

          contactInfoList.add(new ContactsInfo(name, number, null));

          cursor1.close();
        }
      }
      cursor.close();
    }
  }

  public void getContactFavoritesList(){
    Query query = db.collection("users/"+user.getUid()+"/contact_favorites");
    firebaseDB.getFavoritesList(query, new FirebaseDatabase.ArrayListTaskCallbackContact<Void>() {
      @Override
      public void onSuccess(ArrayList<ContactsInfo> arrayList) {
        favoritesInfoList= arrayList;
      }

      @Override
      public void onFailure(String errorMessage) {
        Log.e(TAG, errorMessage);
      }
    });
  }

  public void toastAMessage(String message){
    if(!toastVisible){
      toastVisible=true;
      dialogHelper.normalDialog();
      dialogHelper.showDialog("Alert",message);
//      Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
  }


  @Override
  public void onBackPressed() {
    if(backBtn.getVisibility()==View.VISIBLE){
      elevation=0;
      backBtn.setVisibility(View.GONE);
      bottomAppBar.setVisibility(View.VISIBLE);
      viewDetection.setVisibility(View.VISIBLE);
      msTV.setElevation(elevation);
      frameLayout.setElevation(elevation);
      canAlarm= canAlarmGlobal;
      vibrator.cancel();
      ringtone.stop();
      statusDriver=" ACTIVE ";
      statusDriverMouth=" ACTIVE ";
    }

  }
  public void stopActivity(){
    if (ringtone.isPlaying()) {
      System.out.println("is playinh");
    }
    ringtone.stop();
    appStopped=true;
    scanHandler.removeCallbacks(runnableCode);
    finish();
  }



  public void replaceFragment(androidx.fragment.app.Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.frame_layout,fragment,fragment.getClass().getSimpleName());
    transaction.commit();
  }

  public void addFragment(androidx.fragment.app.Fragment fragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (!fragmentManager.isDestroyed()) {
      int backStackEntryCount = fragmentManager.getBackStackEntryCount();


      if((fragment instanceof HomeFrag && backStackEntryCount>1)){
        removeFragment();
      }else{


        for (int i = 0; i < backStackEntryCount - 1; i++) {
          fragmentManager.popBackStack();
        }


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_layout, fragment, fragment.getClass().getSimpleName());
        transaction.addToBackStack(null); // This allows the user to press the back button to return to the previous fragment
        transaction.commit();
      }

//      setWrapper(fragment);

    }




  }


  public void removeFragment() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    }
  }

  public void setWrapper(androidx.fragment.app.Fragment fragment) {
    if (HomeFrag.wrapper != null) {
      if(fragment instanceof HomeFrag){
        HomeFrag.wrapper.setVisibility(View.GONE);
      }else{
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
              HomeFrag.wrapper.setVisibility(View.VISIBLE);
          }
        }, 1000); // Delay for 1 second (1000 milliseconds)
      }

    }
  }

  public void changeFrameLayoutElevation() {
    int newElevation= elevation;
    if (frameLayout != null) {
      frameLayout.setElevation(newElevation);
      if(newElevation>0){
        bottomAppBar.setVisibility(View.GONE);
        viewDetection.setVisibility(View.GONE);
        backBtn.setElevation(newElevation);
        backBtn.setVisibility(View.VISIBLE);
        msTV.setElevation(newElevation);
      }
      else{
        bottomAppBar.setVisibility(View.VISIBLE);
        viewDetection.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.GONE);
        msTV.setElevation(newElevation);
      }
    }
  }




  protected ArrayList<String> getModelStrings(AssetManager mgr, String path){
    ArrayList<String> res = new ArrayList<String>();
    try {
      String[] files = mgr.list(path);
      for (String file : files) {
        String[] splits = file.split("\\.");
        if (splits[splits.length - 1].equals("tflite")) {
          res.add(file);
        }
      }

    }
    catch (IOException e){
      System.err.println("getModelStrings: " + e.getMessage());
    }
    return res;
  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /** Callback for android.hardware.Camera API */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);
    ringtone.stop();
    vibrator.cancel();
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
    notificationBuilder.setNotifSent(false);
    notificationBuilder.resetCountBuilder();
    vibrator.cancel();
    ringtone.stop();
    scanHandler.removeCallbacks(runnableCode);
    scanHandler.removeCallbacks(mouthRunnable);

  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // Check if this is the front-facing camera
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          final StreamConfigurationMap map =
                  characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          if (map != null) {
            // Fallback to camera1 API for internal cameras that don't have full support.
            // This should help with legacy situations where using the camera2 API causes
            // distorted or otherwise broken previews.
//            useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
            useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                    || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
            LOGGER.i("Camera API lv2?: %s", useCamera2API);
            return cameraId;
          }
        }
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }


  protected void setFragment() {

//    String cameraId = chooseCamera();

    String cameraId = rearCam ? "0":chooseCamera();

//    String cameraId = rearCam ? chooseCamera():"0";
    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();


  }


  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }


//  @Override
//  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//    setUseNNAPI(isChecked);
//    if (isChecked) apiSwitchCompat.setText("NNAPI");
//    else apiSwitchCompat.setText("TFLITE");
//  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.plus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads >= 9) return;
      numThreads++;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    } else if (v.getId() == R.id.minus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads == 1) {
        return;
      }
      numThreads--;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    }
  }

  protected void showFrameInfo(String frameInfo) {
    frameValueTextView.setText(frameInfo);
  }

  protected void showCropInfo(String cropInfo) {
    cropValueTextView.setText(cropInfo);
  }

  protected void showInference(String inferenceTime) {
    inferenceTimeTextView.setText(inferenceTime);
    msTV.setText(inferenceTime);

  }

  protected abstract void updateActiveModel();
  protected abstract void processImage();


  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void setNumThreads(int numThreads);

  protected abstract void setUseNNAPI(boolean isChecked);
}
