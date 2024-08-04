package com.example.awake;



import com.vishnusivadas.advanced_httpurlconnection.PutData;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Database {
    final String ipAddress= "192.168.56.10";
    final String dbLink= "http://"+ipAddress+"/StayAlert/";
    public boolean isConnected=false;
    Thread thread1;
    String result;

    public void checkConnection(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            isConnected();
            if(!isConnected){
                System.out.println("Connection: "+isConnected);
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 1200, TimeUnit.MILLISECONDS);
    }

    public void isConnected(){

        thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                Runnable receiveResponse = new Runnable() {

                    @Override
                    public void run() {
                        PutData putData = new PutData(dbLink+"check_connection.php", "POST", new String[]{"con"}, new String[]{"ok"});
                        if (putData.startPut()) {

                            System.out.println("connecting");
                            if (putData.onComplete()) {

                                if (putData.getResult().equals("Connected")){
                                    isConnected=true;
                                    System.out.println("can connect");
                                }
                                else{
                                    isConnected=false;
                                }
                            }
                        }
                    }
                };

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(receiveResponse);

                try {
                    Object result = future.get(2000, TimeUnit.MILLISECONDS);
//                    System.out.println("Completed  successfully");
                    if (!thread1.isAlive()){
                        thread1.interrupt();
                    }
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                } catch (TimeoutException e) {
                    System.out.println("Timed out. Cancelling the runnable...");
                    future.cancel(true);
                    isConnected=false;
                    if (!thread1.isInterrupted()){
                        thread1.interrupt();
                    }
                }

                executor.shutdown();
            }
        });
        thread1.start();

    }

    private String[] returnArray(String tableName, Map<String, String> mappedData){

        int size = mappedData.size();
        String[] newArray = new String[size];
        int index = 0;
        if (tableName.equals("tableName")){
            newArray= mappedData.keySet().toArray(new String[0]);
        }else{
            newArray= mappedData.values().toArray(new String[0]);
        }

        return newArray;
    }

    private void getResult(String fileName, String method, String[] field, String[] data){
        PutData putData = new PutData(dbLink+fileName, method, field, data); // signup.php to be changed
        if (putData.startPut() && isConnected) {
            System.out.println("executed");
            if (putData.onComplete()) {
                result=putData.getResult();
            }
        }else{
            System.out.println("null");
            result= null;
        }


    }

    public String loginSingupData(String tableName, Map<String, String> mappedData){
        mappedData.put("tableName",tableName);
        String[] field = returnArray("tableName",mappedData);
        String[] data = returnArray(tableName,mappedData);

        getResult((mappedData.size()>3) ? "signup.php":"login.php","POST",field,data);
        if (result == null){
            result="No database connection";
            System.out.println("No database connection");
        }
        return result;

    }

}
