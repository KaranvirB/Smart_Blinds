package com.example.smartyblinds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.MyViewHolder> {

    private ArrayList<item> itemsList;
    private Context context;

    public recyclerAdapter(ArrayList<item> itemsList, Context context){
        this.itemsList = itemsList;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView titleTxt;
        private ConstraintLayout click_layout;

        public MyViewHolder(final View view){
            super(view);
            titleTxt = view.findViewById(R.id.textview_blind_title);
            click_layout = view.findViewById(R.id.click_layout);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String title = itemsList.get(position).get_title();
        holder.titleTxt.setText(title);

        String serial = itemsList.get(position).get_serial();

        //When user clicks item, they will be sent to a detailed page
        holder.click_layout.setOnClickListener(view -> {
            Intent i = new Intent(context, CurrentBlind.class);
            i.putExtra("title",title);
            i.putExtra("serial",serial);
            context.startActivity(i);
            ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}

