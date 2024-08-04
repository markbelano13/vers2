package com.example.awake.custom.classes;

import android.os.AsyncTask;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SMSManager {
    public  static final String ip= "192.168.254.138";

    private static final String SEND_SMS_URL = "http://"+ip+"/StatyAlertApi/sendSMS.php";

    public static void sendSMS(String phoneNumber, String message) {
        new SendSmsTask().execute(phoneNumber, message);
    }

    private static class SendSmsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String phoneNumber = params[0];
            String message = params[1];

            try {
                URL url = new URL(SEND_SMS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "phoneNumber=" + phoneNumber + "&message=" + message;
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Request successful
                    // Handle response if needed
                } else {
                    // Request failed
                    // Handle error
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
