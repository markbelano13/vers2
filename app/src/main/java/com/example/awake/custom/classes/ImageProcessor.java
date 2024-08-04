package com.example.awake.custom.classes;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;

import com.example.awake.env.ImageUtils;
import com.example.awake.env.Logger;
import com.example.awake.env.Utils;
import com.example.awake.tflite.Classifier;
import com.example.awake.tflite.YoloV5Classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

    //not used class

    private static final Logger LOGGER = new Logger();

    public static final int TF_OD_API_INPUT_SIZE = 160;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;

    private static final String TF_OD_API_MODEL_FILE = "yolov5s.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = true;
    private static Integer sensorOrientation = 90;

    private static Classifier detector;

    private static Matrix frameToCropTransform;
    private static Matrix cropToFrameTransform;

    protected static int previewWidth = 0;
    protected static int previewHeight = 0;

    private Bitmap sourceBitmap;
    public static Bitmap cropBitmap;

    public void initBox(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        System.err.println(Double.parseDouble(configurationInfo.getGlEsVersion()));
        System.err.println(configurationInfo.reqGlEsVersion >= 0x30000);
        System.err.println(String.format("%X", configurationInfo.reqGlEsVersion));

        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        try {
            detector =
                    YoloV5Classifier.create(
                            context.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED,
                            TF_OD_API_INPUT_SIZE);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");

        }
    }

    private static boolean handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        List<String> resultTitles = new ArrayList<>();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {

                resultTitles.add(result.getTitle());
            }
        }
        if (resultTitles.contains("closed") || resultTitles.contains("yawn")) {
            return true;
        }
        return false;

    }

    public interface ImageDetectionCallback {
        void onImageDetected(boolean isTrue, List<Classifier.Recognition> results);
    }

    public static boolean checkImage(Bitmap bitmap, ImageDetectionCallback callback) {

        cropBitmap = Utils.processBitmap(bitmap, TF_OD_API_INPUT_SIZE);

        Handler handler = new Handler();

        new Thread(() -> {
            final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onImageDetected(handleResult(cropBitmap, results), results);
                }
            });
        }).start();

        return false;
    }
}