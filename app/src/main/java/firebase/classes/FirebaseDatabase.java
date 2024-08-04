package firebase.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.awake.CameraActivity;
import com.example.awake.R;
import com.example.awake.custom.classes.ContactsInfo;
import com.example.awake.custom.classes.DetectionLogsInfo;
import com.example.awake.custom.classes.NotificationInfo;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseDatabase {
    private static final String TAG = "FirebaseDatabase";
    FirebaseAuth mAuth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;
    CollectionReference collection;
    FirebaseFirestore db;
    DocumentReference ref;
    String userID;
    Map<String, Object> userData=new HashMap<>();
    Exception exception =null;
    FirebaseStorage storage;
    private boolean isSyncing = false;
    int unReadCount=0;

    public FirebaseDatabase(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getUid();
        storage= FirebaseStorage.getInstance();

    }




    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DocumentSnapshot documentSnapshot);
        void onStart();
        void onFailure(Exception e);
    }

    public void readData(String collection, String document, String source, final OnGetDataListener listener) {

        Source sourceData;
        if(source.equals("cache")){
            sourceData= Source.CACHE;
        }else if(source.equals("server")){
            sourceData=Source.SERVER;
        }else{
            sourceData=Source.DEFAULT;
        }

        listener.onStart();

        db.collection(collection)
                .document(document)
                .get(sourceData)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        listener.onSuccess(documentSnapshot);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e);
                    }
                });
    }


    public String onFailureDialog(Exception e) {
        if(e==null){
            exception=null;
            return "success";
        }
        System.err.println("error "+e.getMessage());
        return "No connection to the database";

    }

    public String failureDialog(Task task) {
        Exception exception = task.getException();
        if (exception instanceof FirebaseAuthException) {
            // Handle FirebaseAuthException specifically
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            String error = new FirebaseDatabase().getAuthMessage(errorCode);
            if (!error.contains("Fatal")){
                return error;
            }else{
                System.err.println(error);
                return "There is an error with the database";

            }
        } else {
            // Handle generic FirebaseException or other exceptions
            String error = "An unexpected error occurred. Please try again.";
            // Optionally log the exception for debugging
            System.err.println("Unexpected exception: " + exception.getMessage());
            return error;
        }
    }

    public Map<String, Object>  getUserInfo(String source){

        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get((source =="server" || source =="SERVER")?Source.SERVER:Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        userData=document.getData();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return userData;
    }

    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    public void writeUserInfo(Map userData, String collection,String document, TaskCallback<Void> callback) {

        db.collection(collection).document(document).set(userData)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null); // Passing null as there is no specific result for success
                }).addOnFailureListener(e -> {
                    Log.e("WriteUserInfo", "Err " + e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void deleteDocument(String collection,String document, TaskCallback<Void> callback) {
        db.collection(collection).document(document).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess(null); // Passing null as there is no specific result for success
                } else {
                    // Handle errors here
                    Exception e = task.getException();
                    if (e != null) {
                        callback.onFailure(e.getMessage());
                    }
                }
            }
        });
    }
    public void updateUserInfo(Map userData,DocumentReference ref, TaskCallback<Void> callback) {
        ref.update(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null); // Passing null as there is no specific result for success
                    }
                }).addOnFailureListener(e -> {
                    Log.e("UpdateUserInfo", "Error " + e);
                    callback.onFailure(e.getMessage());
                });
    }





    public interface OnInterfaceListener {
        void onInterfaceCheckResult(boolean isTrue,String message);
    }

    public boolean isContactUnique(String contactNumber,OnInterfaceListener listener){
        DocumentReference docRef = db.collection("contact_numbers").document(contactNumber);
        docRef.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        listener.onInterfaceCheckResult(true,null);
                    } else {
                        listener.onInterfaceCheckResult(false,null);
                    }
                } else {
                    Log.d(TAG, "get failed with "+ task.getException().getLocalizedMessage());
                    listener.onInterfaceCheckResult(false,task.getException().getLocalizedMessage());
                }
            }
        });
        return false;
    }

    public interface BitmapTaskCallback<T> {
        void onSuccess(Bitmap bitmap);
        void onFailure(String errorMessage);
    }

    public Bitmap getImageFromServer(String fileName,String folder, BitmapTaskCallback callback) {
        try {
            StorageReference storageRef = storage.getReference().child("users/"+mAuth.getUid()+"/"+folder+"/"+fileName);
            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    System.out.println("success byte");
                    Bitmap bitmap=BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if(bitmap!=null){
                        saveImageToLocal(fileName,bitmap,(fileName.contains("local")?"detections":"userRes"));
                        callback.onSuccess(bitmap);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    callback.onFailure(exception.getLocalizedMessage());

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getImageFromLocal(Context context,String local_path, String fileName) {
        try {
            File imageFile = new File(context.getFilesDir(), local_path + "/" + fileName);
            if (imageFile.exists()) {
//                System.out.println("get image at "+imageFile.getName());
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            } else {
                Log.e("getImageFromLocal", "File does not exist: " + imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] saveImageToLocal(String nameExtension, Bitmap bitmap, String folder){
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String path=mAuth.getUid()+"/"+folder;
        File dir = new File(CameraActivity.context.getFilesDir(), path);
        if(!dir.exists()){
            dir.mkdirs();
        }else{
            System.out.println("Fail local");
        }

        try {
            String fileName="local_"+time +"_"+nameExtension+".jpg";
            if(nameExtension.contains("local_")){
                fileName =nameExtension;
            }else if(nameExtension.contains("profile_pic")){
                fileName="profile_pic.jpg";
            }

            File imageFile = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            compressImage(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fos);

            // Close the FileOutputStream
            fos.flush();
            fos.close();

            return new String[]{path,fileName};
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void saveFileInfoToFirestore(Map fileInfo, String folder) {
        db.collection("users/"+mAuth.getUid()+"/"+folder).document(fileInfo.get("file_name").toString()).set(fileInfo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // trigger Syncing
                        if(folder.equals("image_detection")){
                            syncToServer();
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("UpdateUserInfo", "Error " + e);
                });

    }


    public UploadTask isImageUploaded(String storagePath,String localPath, String fileName, OnInterfaceListener listener){

        Bitmap bitmap= getImageFromLocal(CameraActivity.context, localPath,fileName);
        if(bitmap==null){
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = storage.getReference(storagePath);
        StorageReference imageRef = storageRef.child(fileName);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("uploaded");
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        listener.onInterfaceCheckResult(true,imageRef.getDownloadUrl().toString());
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                        } else {
                            listener.onInterfaceCheckResult(false,task.getException().getLocalizedMessage());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Excep "+exception.getLocalizedMessage());
            }
        });
        return null;
    }

    public void syncToServer(){
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null || isSyncing) {
            return;
        }

        isSyncing = true;
        System.out.println("sync in");
        try {
            String syncPath = "users/" + currentUser.getUid() + "/upload_queue";
            db.collection(syncPath).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot syncFile : querySnapshot.getDocuments()) {
                            String fileName = getValue(syncFile.getData(), "file_name", "");
                            String localPath = getValue(syncFile.getData(), "local_path", "");
                            String storagePath = getValue(syncFile.getData(), "storage_path", "");
                            String firestorePath = getValue(syncFile.getData(), "firestore_path", "");

                            if (!localPath.isEmpty()) {
                                isImageUploaded(storagePath, localPath, fileName, new OnInterfaceListener() {
                                    @Override
                                    public void onInterfaceCheckResult(boolean isTrue, String message) {
                                        if (isTrue) {
                                            String downloadURL = message;
                                            if (downloadURL != null) {
                                                db.document(firestorePath).update(new HashMap<String, Object>() {{
                                                    put("downloadURL", downloadURL);
                                                    put("storageURL", storagePath);
                                                }});

                                                db.document(syncPath + "/" + fileName).delete();
                                            }
                                        } else {
                                            Log.w(TAG, message);
                                        }
                                    }
                                });


                            }
                        }
                    }
                } else {
                    // Handle failures
                    Exception exception = task.getException();
                    System.out.println("Error getting documents: " + exception.getLocalizedMessage());
                }
            });
        } catch (Exception e) {
            System.out.println("Error syncing files: " + e.getMessage());
        }
        isSyncing = false;
    }


    private String getValue(Map<String, Object> data, String key, String defaultValue) {
        return data.containsKey(key) ? data.get(key).toString() : defaultValue;
    }




    public Bitmap compressImage(Bitmap bm){
        int width = bm.getWidth();
        int height = bm.getHeight();

        int maxWidth=480;
        int maxHeight=480;

        if(bm.getHeight()<maxHeight|| bm.getWidth()<maxWidth){
            return bm;
        }
        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }


        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public void checkSync(){
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());

        docRef.get(Source.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    syncToServer();
                } else {
                    Log.d(TAG,"No connection to database");
                }
            } else {
                // Handle failures
                Exception exception = task.getException();
                Log.w(TAG, "Error getting document", exception);
            }
        });
    }



    public interface ArrayListTaskCallback<T> {
        void onSuccess(ArrayList<DetectionLogsInfo> arrayList);
        void onFailure(String errorMessage);
    }

    public ArrayList<DetectionLogsInfo> getDetectionLogsInfo(Query query,ArrayListTaskCallback<Void> callback) {

        ArrayList<DetectionLogsInfo> info = new ArrayList<>();


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                CameraActivity.detectionSnapshot= querySnapshot;
                if (querySnapshot != null) {
                    for (DocumentSnapshot documentFields : querySnapshot.getDocuments()) {
                        String fileName = getValue(documentFields.getData(), "file_name", "");
                        String type = getValue(documentFields.getData(), "detection_name", "");
                        String timestamp = dateFormat(documentFields.getTimestamp("timestamp"));
                        String location = getValue(documentFields.getData(), "location", "to be added");
                        String accuracy = getValue(documentFields.getData(), "accuracy", "");
                        String inference = getValue(documentFields.getData(), "inference", "");
                        String responseTime = getValue(documentFields.getData(), "response_time", "");
                        String localPath = getValue(documentFields.getData(), "local_path", "");
                        String downloadURL = getValue(documentFields.getData(), "downloadURL", "");

                        info.add( new DetectionLogsInfo(type,timestamp,location,accuracy,inference,fileName, localPath, downloadURL,responseTime)); //must be in correct order

                        Timestamp fbTimestamp=  documentFields.getTimestamp("timestamp");

                        if(CameraActivity.lastDetectionTime ==null){
                            CameraActivity.lastDetectionTime =fbTimestamp;
                        }
                        else if(fbTimestamp.compareTo(CameraActivity.lastDetectionTime)>0){
                            CameraActivity.lastDetectionTime =documentFields.getTimestamp("timestamp");
                        }


                    }
                    callback.onSuccess(info);
                }

            } else {
                // Handle failures
                Exception exception = task.getException();
                Log.d(TAG+ "getDetectionLogsIndo","Error getting documents: " + exception.getLocalizedMessage());
                callback.onFailure(exception.getLocalizedMessage());
            }
        });
        return null;
    }

    public interface ArrayListTaskCallbackContact<T> {
        void onSuccess(ArrayList<ContactsInfo> arrayList);
        void onFailure(String errorMessage);
    }

    public ArrayList<ContactsInfo> getFavoritesList(Query query,ArrayListTaskCallbackContact<Void> callback) {
        ArrayList<ContactsInfo> infos = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentSnapshot documentFields : querySnapshot.getDocuments()) {
                        String contactName = getValue(documentFields.getData(), "contactName", "");
                        String contactNumber = getValue(documentFields.getData(), "contactNumber", "");

                        infos.add( new ContactsInfo(contactName,contactNumber,null)); //must be in correct order
                    }
                    callback.onSuccess(infos);
                }

            } else {
                // Handle failures
                Exception exception = task.getException();
                Log.d(TAG+ "getContactsInfo","Error getting documents: " + exception.getLocalizedMessage());
                callback.onFailure(exception.getLocalizedMessage());
            }
        });
        return null;
    }

    public String dateFormat(Timestamp timestamp) {
        String timestampString = null;
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
        timestampString = dateFormat.format(date);
        return timestampString;

    }

    public void checkStatCount(String type){
        readData(type+"_count", mAuth.getUid(), "default", new OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    updateStatCount(documentSnapshot, type);

                }else{
                    Map<String, Object> drowsyCount = new HashMap<>();
                    drowsyCount.put("date", new Date());
                    // Pad the numbers with zeros
                    for (int i = 1; i < 25; i++) {
                        drowsyCount.put("today"+String.format("%02d", i), 0);
                    }

                    for (int i = 1; i < 25; i++) {
                        drowsyCount.put("yesterday"+String.format("%02d", i), 0);
                    }

                    for (int i = 1; i < 31; i++) {
                        drowsyCount.put("day"+String.format("%02d", i), 0);
                    }

                    writeUserInfo(drowsyCount, type+"_count", mAuth.getUid(), new TaskCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(TAG,"Insert success "+type);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.d(TAG,"Insert Failed: "+errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG+ " checkStatCount: ",e.getLocalizedMessage());
            }
        });
    }

    public void incrementCount(String type, double value){
        DocumentReference ref = db.collection(type+"_count").document(mAuth.getUid());

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY)+1;
        String stringHour=String.format("%02d",currentHour);

        Map<String, Object> updates = new HashMap<>();
        updates.put("today"+stringHour, FieldValue.increment(value));
        updates.put("day01", FieldValue.increment(value));

        ref.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG+" Increment","Success");
                } else {
                    Log.d(TAG+" Increment","Failed "+task.getException().getLocalizedMessage());
                }
            }
        });
    }



    public void updateStatCount(DocumentSnapshot documentSnapshot, String type){

        Timestamp timestamp = documentSnapshot.getTimestamp("date");
        LocalDate dateFromFirestore=null;
        LocalDate currentDate=null;
        int daysDifference=0;
        // Convert the timestamp to LocalDate
        Instant instant = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            instant = timestamp.toDate().toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            dateFromFirestore = instant.atZone(zoneId).toLocalDate();
            currentDate = LocalDate.now();
            daysDifference =(int) ChronoUnit.DAYS.between(dateFromFirestore, currentDate);
        }

        if(daysDifference>0){

            Map<String, Object> recentCountMap = new HashMap<>();
            recentCountMap= documentSnapshot.getData();
            Map<String, Object> daysCountMap = new HashMap<>();
            daysCountMap.put("date", new Date());

            //for yesterday
            for (int i = 1; i < 25; i++) {
                daysCountMap.put("today" +String.format("%02d",i), 0);
            }

            if(daysDifference==1){
                for (int i = 1; i < 25; i++) {
                    daysCountMap.put("yesterday" +String.format("%02d",i) , recentCountMap.get("today"+String.format("%02d",i)));
                }
            }else{
                for (int i = 1; i < 25; i++) {
                    daysCountMap.put("yesterday" +String.format("%02d",i), 0);
                }
            }

            //for day
            for(int i=1;i<=daysDifference;i++){
                daysCountMap.put("day" +String.format("%02d",i) ,0);
            }

            for (int i = 1; i < 31; i++) {
                if(daysDifference+i<31){
                    daysCountMap.put("day" + String.format("%02d",daysDifference+i), recentCountMap.get("day"+String.format("%02d",i)));
                }
            }


            DocumentReference ref = db.collection(type+"_count").document(mAuth.getUid());
            updateUserInfo(daysCountMap,ref, new TaskCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG+ "updateStatCount","Success");
                    if(type.equals("yawn")){
                        CameraActivity.chartGenerator.fetchDetectionCounts();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.d(TAG,"Error inserting count");
                }
            });

        } else if(type.equals("yawn")){
            CameraActivity.chartGenerator.fetchDetectionCounts();
        }




    }




    public void updateCheckin(){
        updateUserInfo(new HashMap<String, Object>() {{
            put("last_sign_in", new Date());
        }}, db.collection("users").document(mAuth.getUid()), new FirebaseDatabase.TaskCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.e(TAG,"Success updatingCheckIn ");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG,"Error updatingCheckIn "+errorMessage);
            }
        });
    }

    public void sendNotif(){


    }

    public interface ArrayListTaskCallbackNotif<T> {
        void onSuccess(ArrayList<NotificationInfo> arrayList);
        void onFailure(String errorMessage);
    }

    public void getNotificationsList(){
        Query query = db.collection("users/"+mAuth.getUid()+"/user_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20);
        getNotifList(query, new FirebaseDatabase.ArrayListTaskCallbackNotif<Void>() {
            @Override
            public void onSuccess(ArrayList<NotificationInfo> arrayList) {
                CameraActivity.notificationsInfo=arrayList;
                Log.d(TAG,"get notif list success");
            }

            @Override
            public void onFailure(String errorMessage) {
                System.out.println("get notif list failed "+errorMessage);
            }
        });
    }

    public ArrayList<NotificationInfo> getNotifList(Query query, ArrayListTaskCallbackNotif<Void> callback) {
        ArrayList<NotificationInfo> infos = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentSnapshot documentFields : querySnapshot.getDocuments()) {
                        String title = getValue(documentFields.getData(), "title", "");
                        String message = getValue(documentFields.getData(), "message", "");

                        Timestamp firestoreTimestamp = documentFields.getTimestamp("timestamp");
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault());
                        Date lastSignInDate = firestoreTimestamp.toDate();
                        String notifDate = sdf.format(lastSignInDate);

                        Timestamp detectionTimestamp = documentFields.getTimestamp("detection_date");

                        Date detectionDate = firestoreTimestamp.toDate();
                        SimpleDateFormat sdfNotif = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                        String notifDateShort = sdfNotif.format(detectionDate);

                        ArrayList<Integer> drowsyList= (ArrayList<Integer>)documentFields.get("drowsy_list");
                        ArrayList<Integer> yawnList= (ArrayList<Integer>)documentFields.get("yawn_list");
                        ArrayList<Integer> timeValues= (ArrayList<Integer>)documentFields.get("time_values");
                        boolean wasRead= documentFields.getBoolean("wasRead");
                        String documentName = documentFields.getId();

                        Bitmap icon= BitmapFactory.decodeResource(CameraActivity.context.getResources(),
                                R.drawable.ic_read_message);
                        if(wasRead){
                            icon = BitmapFactory.decodeResource(CameraActivity.context.getResources(),
                                    R.drawable.ic_unread_message);
                        }
                        infos.add( new NotificationInfo(title,notifDate,message,notifDateShort,drowsyList,yawnList,timeValues,wasRead,documentName,icon)); //must be in correct order
                    }
                    callback.onSuccess(infos);
                }

            } else {
                // Handle failures
                Exception exception = task.getException();
                Log.d(TAG+ "getNotificationsInfo","Error getting documents: " + exception.getLocalizedMessage());
                callback.onFailure(exception.getLocalizedMessage());
            }
        });
        return null;
    }

    public void checkNotification(){
        Query query =  db.collection("users/"+user.getUid()+"/user_notifications").whereEqualTo("wasRead", false);

        AggregateQuery unReadCountAQ = query.count();
                unReadCountAQ.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    unReadCount=(int)snapshot.getCount();
                } else {
                    Log.d(TAG, "Count failed: ", task.getException());
                }
            }
        });
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( 640, 720, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }


    public String getAuthMessage(String errorCode){
        String errorMessage="";

        switch (errorCode) {
            case "ERROR_INVALID_CUSTOM_TOKEN":
                errorMessage="Fatal: The custom token format is incorrect. Please check the documentation.";
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                errorMessage="Fatal: The custom token corresponds to a different audience.";
                break;

            case "ERROR_INVALID_CREDENTIAL":
                errorMessage="Account does not exist or wrong email or password";
                break;

            case "ERROR_INVALID_EMAIL":
                errorMessage="Invalid email address";
                break;

            case "ERROR_WRONG_PASSWORD":
                errorMessage="Wrong email or password";
                break;

            case "ERROR_USER_MISMATCH":
                errorMessage="Fatal: The supplied credentials do not correspond to the previously signed in user.";
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                errorMessage="Fatal: This operation is sensitive and requires recent authentication. Log in again before retrying this request.";
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                errorMessage="Email already used in different sign-in method";
//                errorMessage="Fatal: An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.";
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                errorMessage="Account already exists";
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                errorMessage="Credentials already in used by different account";
                break;

            case "ERROR_USER_DISABLED":
                // Handle ERROR_USER_DISABLED
                errorMessage="Account is disabled";
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                errorMessage="Token expires, please sign-in again";
                // Handle ERROR_USER_TOKEN_EXPIRED
                break;

            case "ERROR_USER_NOT_FOUND":
                errorMessage="User not found";
                break;

            case "ERROR_INVALID_USER_TOKEN":
                errorMessage="Invalid user token, please sign-in again";
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                // Handle ERROR_OPERATION_NOT_ALLOWED
                errorMessage="Fatal: This operation is not allowed. You must enable this service in the console.";
                break;

            case "ERROR_WEAK_PASSWORD":
                errorMessage="Weak password";
                break;
            case "ERROR_MISSING_EMAIL":
                errorMessage="Please provide an email";
            default:
                errorMessage="Fatal: Unknown error";
        }


        return errorMessage;
    }
}
