package com.example.awake;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awake.custom.classes.ContactsInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import firebase.classes.FirebaseDatabase;
import helper.classes.DialogHelper;

public class ContactsRecycleViewAdapter extends  RecyclerView.Adapter<ContactsRecycleViewAdapter.ContactsInfoViewHolder>{


    ArrayList<ContactsInfo> contactsInfo;
    Context context;
    DialogHelper helper;
    String actionType="";
    FirebaseDatabase firebaseDB;
    FirebaseAuth auth;
    FirebaseFirestore db;

    public ContactsRecycleViewAdapter(ArrayList<ContactsInfo> data, Context context){
        this.contactsInfo=data;
        this.context=context;
        this. helper = new DialogHelper(this.context);
        this.firebaseDB = new FirebaseDatabase();
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ContactsInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_contacts_row,parent,false);
        ContactsRecycleViewAdapter.ContactsInfoViewHolder contactsInfoViewHolder = new ContactsRecycleViewAdapter.ContactsInfoViewHolder(view);

        return contactsInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsInfoViewHolder holder, int position) {
        ContactsInfo contactsInfo = this.contactsInfo.get(position);
        holder.contactName.setText(contactsInfo.getContactName());
        holder.contactNumber.setText(contactsInfo.getContactNumber());
        holder.position = holder.getAdapterPosition();
        holder.info=contactsInfo;

    }

    @Override
    public int getItemCount() {
        return  this.contactsInfo.size();
    }

    public class ContactsInfoViewHolder extends RecyclerView.ViewHolder{

        ImageView contactPhoto;
        TextView contactName;
        TextView contactNumber;
        ImageButton contactCallBtn;
        View rootView;
        int position;
        ContactsInfo info;
        ConstraintLayout contactRowLayout;


        public ContactsInfoViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView=itemView;
            contactPhoto = itemView.findViewById(R.id.contactPhoto);
            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            contactCallBtn= itemView.findViewById(R.id.contactCall);
            contactRowLayout = itemView.findViewById(R.id.contactRowLayout);


            helper = new DialogHelper(context, new DialogHelper.DialogClickListener() {
                @Override
                public void onOkayClicked() {
                    //used to cancel
                }

                @Override
                public void onActionClicked() {
                    if(actionType.equals("Add")){
                        if(CameraActivity.favoritesInfoList.size()<6){
                            Map<String, Object> contactInfo = new HashMap<>();
                            contactInfo.put("contactName",contactName.getText().toString());
                            contactInfo.put("contactNumber",contactNumber.getText().toString());
                            firebaseDB.writeUserInfo(contactInfo, "users/" + auth.getUid() + "/contact_favorites", contactNumber.getText().toString(), new FirebaseDatabase.TaskCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                                    Query query = db.collection("users/"+auth.getUid()+"/contact_favorites");
                                    firebaseDB.getFavoritesList(query, new FirebaseDatabase.ArrayListTaskCallbackContact<Void>() {
                                        @Override
                                        public void onSuccess(ArrayList<ContactsInfo> arrayList) {
                                            CameraActivity.favoritesInfoList= arrayList;
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            Log.e("Get Favorires", errorMessage);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(context, "Failed adding to favorites", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            helper.normalDialog();
                            helper.showDialog("Add to Favorites", "You've reached max favorite limits");
                        }

                    }else{
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+contactNumber.getText().toString()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity((intent));
                    }
                }
            });

            contactCallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionType="call";
                    helper.customDialog("Call anyway");
                    helper.showDialog("Phone Call", "This action will exit the app");
                }
            });

            contactRowLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    actionType="Add";
                    helper.customDialog("Add");
                    helper.showDialog("Add to Favorites", "Add "+contactName.getText().toString().trim()+" to favorites?");

                    return false;
                }
            });






        }
    }



}

