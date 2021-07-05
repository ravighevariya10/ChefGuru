package com.example.splashscreen;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class myAdapter extends RecyclerView.Adapter<myAdapter.myViewHolder> {



    ArrayList<Uri> uri;

    public myAdapter(ArrayList<Uri> uri) {
        this.uri=uri;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview,parent,false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        holder.imageView.setImageURI(uri.get(position));

    }

    @Override
    public int getItemCount() {
        return uri.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image1);
        }

    }

}