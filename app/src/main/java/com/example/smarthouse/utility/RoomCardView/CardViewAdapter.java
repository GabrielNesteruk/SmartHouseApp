package com.example.smarthouse.utility.RoomCardView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smarthouse.R;

import java.util.ArrayList;

import lombok.AllArgsConstructor;

// przetrzymuje wartosci i przekazuje je do widoku, potrzebne wiec sa wartosci i widok
@AllArgsConstructor
public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {
    private ArrayList<Room> rooms;
    private Context context;
    private OnRoomListener onRoomListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, onRoomListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(rooms.get(position).getName());
        holder.temp.setText(rooms.get(position).getValueName());
        holder.value.setText(rooms.get(position).getValue());

        Glide.with(context)
                .asBitmap()
                .load(rooms.get(position).getImageUrl())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    // przetrzymuje widoki
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView name, temp, value;
        OnRoomListener onRoomListener;

        public ViewHolder(@NonNull View itemView, OnRoomListener onRoomListener) {
            super(itemView);

            image = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.txtName);
            temp = itemView.findViewById(R.id.txtTemp);
            value = itemView.findViewById(R.id.valueTxt);
            this.onRoomListener = onRoomListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRoomListener.onRoomClick(getAdapterPosition());
        }
    }
    public interface OnRoomListener {
        void onRoomClick(int position);
    }
}
