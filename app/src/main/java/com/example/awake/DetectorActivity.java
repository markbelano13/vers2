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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.example.awake.customview.OverlayView;
import com.example.awake.customview.OverlayView.DrawCallback;
import com.example.awake.env.BorderedText;
import com.example.awake.env.ImageUtils;
import com.example.awake.env.Logger;
import com.example.awake.tflite.Classifier;
import com.example.awake.tflite.DetectorFactory;
import com.example.awake.tracking.MultiBoxTracker;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends com.example.awake.CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1920, 1080);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;



    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    Bitmap newBm;




    private boolean computingDetection = false;


    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    boolean sensorChanged= false;


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        final int modelIndex = modelView.getCheckedItemPosition();
        final String modelString = modelStrings.get(modelIndex);

        try {
            detector = DetectorFactory.getDetector(getAssets(), modelString);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        int cropSize = detector.getInputSize();

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
        sensorChanged=true;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);
        System.out.println("crop size "+cropSize);





        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    protected void updateActiveModel() {
        // Get UI information before delegating to background
        final int modelIndex = modelView.getCheckedItemPosition();
        final int deviceIndex = deviceView.getCheckedItemPosition();
        String threads = threadsTextView.getText().toString().trim();
        final int numThreads = Integer.parseInt(threads);

        if (handler == null) {
            return;
        }

        handler.post(() -> {
            if (modelIndex == currentModel && deviceIndex == currentDevice
                    && numThreads == currentNumThreads) {
                return;
            }
            currentModel = modelIndex;
            currentDevice = deviceIndex;
            currentNumThreads = numThreads;

            // Disable classifier while updating
            if (detector != null) {
                detector.close();
                detector = null;
            }

            // Lookup names of parameters.
            String modelString = modelStrings.get(modelIndex);
            String device = deviceStrings.get(deviceIndex);

            LOGGER.i("Changing model to " + modelString + " device " + device);

            // Try to load model.

            try {
                detector = DetectorFactory.getDetector(getAssets(), modelString);
                // Customize the interpreter to the type of device we want to use.
                if (detector == null) {
                    return;
                }
            }
            catch(IOException e) {
                e.printStackTrace();
                LOGGER.e(e, "Exception in updateActiveModel()");
                Toast toast =
                        Toast.makeText(
                                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
            System.out.println("using "+ device);
            if (device.equals("CPU")) {
                detector.useCPU();
            } else if (device.equals("GPU")) {
                detector.useGpu();
            } else if (device.equals("NNAPI")) {
                detector.useNNAPI();
            }
            detector.setNumThreads(numThreads);



            int cropSize = detector.getInputSize();
            croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            previewWidth, previewHeight,
                            cropSize, cropSize,
                            sensorOrientation, MAINTAIN_ASPECT);

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);
        });
    }

    @Override
    protected void processImage() {


        FrameLayout frameLayout = findViewById(R.id.container);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;


        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }



        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1); // Mirror horizontally
        if (sensorOrientation == 180) {
            matrix.preRotate(180);
        } else if (sensorOrientation == 0){
            matrix.preRotate(-180);
        }

        croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, false);


        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if(sensorChanged){
                                    if (sensorOrientation == 180) {
                                        frameLayout.setPadding((int) (dpWidth*0.175), 0, 0, 0);
                                    } else if (sensorOrientation == 0) {
                                        frameLayout.setPadding((int) (dpWidth*0.2),0, 0, 0);
                                    }else{
                                        frameLayout.setPadding(0, (int) (dpWidth*0.17), 0, 0);
                                    }
                                    frameLayout.setScaleX(1.35f);
                                    frameLayout.setScaleY(1.35f);
                                    sensorChanged=false;
                                    System.out.println("sensor false");
                                }

                            }
                        });

                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        Log.e("CHECK", "run: " + results.size());


                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        newBm = cropToSquare(rgbFrameBitmap);
                        final Canvas canvas = new Canvas(newBm);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                        }

                        detectedBitmapInfo.clearList();

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        List<String> resultTitles = new ArrayList<>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                if(resultTitles.contains("closed") && result.getTitle().equals("open")){
                                    System.out.printf("skippeds");
                                    continue;
                                }
                                setCanvas(canvas,result.getTitle(),location);
//                                copyBitmap=newBm;


                                cropToFrameTransform.mapRect(location);
                                resultTitles.add(result.getTitle());
                                result.setLocation(location);
                                result.getConfidence();
                                mappedRecognitions.add(result);
                                confidenceLevel= result.getConfidence();

                            }
                        }

                        detectedBitmapInfo.setBitmap(newBm);
                        detectedBitmapInfo.setList(resultTitles);



//                        RectF myRectF = new RectF(80, 90, 80, 90);
//                        setCanvas(canvas,"sdd",myRectF);
//                        dialogHelper.showTestImage(copyBitmap);


                        eyeStatus=resultTitles.contains("open")||resultTitles.contains("closed")?resultTitles.contains("open") ? "open" : "closed":"";
                        mouthStatus=resultTitles.contains("yawn")||resultTitles.contains("no_yawn")?resultTitles.contains("yawn") ? "yawn" : "no_yawn":"";
                        System.out.println("mouth: "+mouthStatus+"\neyes: "+eyeStatus);

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;

                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        showFrameInfo(previewWidth + "x" + previewHeight);
                                        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                                        showInference(lastProcessingTimeMs + "ms");
                                    }
                                });

                    }



                });

    }

    public void setCanvas(Canvas canvas, String label, RectF location){
        int color=1;
        switch (label){
            case "open":
                color=Color.GREEN;
                break;
            case "closed":
                color=Color.BLUE;
                break;
            case "yawn":
                color=Color.YELLOW;
                break;
            case "no_yawn":
                color=Color.RED;
                break;
            default:
                color=Color.MAGENTA;
                break;
        }

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        paint.setTextSize(55.0f);
        paint.setStrokeWidth(4.0f);

        paint.setTypeface(ResourcesCompat.getFont(context,R.font.nunito_regular));


        int scale=1080/160;

        canvas.drawText(label, location.left*(scale), (label.equals("yawn")||label.equals("no_yawn"))?location.bottom*(scale)+100:location.top*(scale)+50,paint);
        canvas.drawRect(location.left*(scale)+70,location.top*(scale)+70,location.right*(scale)+70,location.bottom*(scale)+(label.equals("yawn")?80:70),paint);

        date=new Date();
        String time = firebaseDB.dateFormat(Timestamp.now())+" " +(TimeZone.getDefault().getID().equals("Asia/Manila")?"PHT":TimeZone.getDefault().getID());
        Paint timePaint = new Paint();
        timePaint.setTextSize(60.0f);
        timePaint.setColor(Color.WHITE);


        Paint outlinePaint = new Paint();

        outlinePaint.setAntiAlias(true);
        //  paint.setARGB(150, 0, 0, 0); // alpha, r, g, b (Black, semi see-through)
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setTextSize(60.0f);
        // paint2.setColor(0x00000000);
        outlinePaint.setStyle(Paint.Style.FILL);
        canvas.drawText(time,53,83,outlinePaint);
        canvas.drawText(time,50,80,timePaint);

    }

    public Bitmap cropToSquare(Bitmap srcBmp){
        Bitmap dstBmp;
        Matrix matrix = new Matrix();
        int orientation = getResources().getConfiguration().orientation;
        System.out.println("orient "+orientation);
        if(orientation==2){
            matrix.postRotate(180);
        }else{
            matrix.postRotate(270);
        }

        matrix.preScale(1 ,-1);
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight(),matrix,true
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth(),matrix,true
            );
        }
        return dstBmp;
    }



    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }


}
