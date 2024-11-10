package com.example.awake;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.awake.custom.classes.DetectionLogsInfo;

import java.util.ArrayList;

import helper.classes.ViewDetectedImageHelper;

public class DetectionLogsRecycleViewAdapter  extends  RecyclerView.Adapter<DetectionLogsRecycleViewAdapter.DetectionLogsViewHolder>{

    ArrayList<DetectionLogsInfo> detectionInfo;
    ViewDetectedImageHelper helper;
    Context context;

    public DetectionLogsRecycleViewAdapter(ArrayList<DetectionLogsInfo> data, Context context){
        this.detectionInfo=data;
        this.context=context;
        helper= new ViewDetectedImageHelper(this.context);
    }

    @NonNull
    @Override
    public DetectionLogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_logs_row,parent,false);
        DetectionLogsViewHolder detectionLogsViewHolder = new DetectionLogsViewHolder(view);

        return detectionLogsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DetectionLogsViewHolder holder, int position) {
        DetectionLogsInfo detectionLogsInfo = detectionInfo.get(position);
        holder.timeStamp.setText(detectionLogsInfo.getTimestamp());
        holder.detectionType.setText(detectionLogsInfo.getDetectionType());
        holder.position = holder.getAdapterPosition();
        holder.info=detectionLogsInfo;

    }

    @Override
    public int getItemCount() {
        return this.detectionInfo.size();
    }

    public class DetectionLogsViewHolder extends RecyclerView.ViewHolder{
        ImageButton viewImageBtn;
        TextView timeStamp;
        TextView detectionType;
        View rootView;
        int position;
        DetectionLogsInfo info;

        public DetectionLogsViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView=itemView;
            viewImageBtn= itemView.findViewById(R.id.viewDetectedImageBtn);
            timeStamp=itemView.findViewById(R.id.detectionTimestamp);
            detectionType=itemView.findViewById(R.id.detectionType);

            helper=new ViewDetectedImageHelper(context, new ViewDetectedImageHelper.DialogClickListener() {
                @Override
                public void onOkayClicked() {
                    helper.dismissDialog();
                }
            });

            viewImageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helper.showDialog(info);
                }
            });



        }
    }


}