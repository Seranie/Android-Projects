package com.example.mind_care.home.reminders.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mind_care.R;
import com.example.mind_care.home.reminders.model.RemindersGroupItem;

import java.util.List;

public class RemindersGroupAdapter extends RecyclerView.Adapter<RemindersGroupAdapter.GroupItemViewHolder> {
    //TODO incomplete adapter, takes in a list of group items
    private List<RemindersGroupItem> remindersGroupItems;

    public RemindersGroupAdapter(List<RemindersGroupItem> remindersGroupItems){
        this.remindersGroupItems = remindersGroupItems;
    }

    @NonNull
    @Override
    public GroupItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminders_group_item, parent, false);
        return new GroupItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupItemViewHolder holder, int position) {
        RemindersGroupItem groupItem = remindersGroupItems.get(position);
        holder.groupItemImage.setImageResource(groupItem.getImageSource());
        holder.groupItemName.setText(groupItem.getNameSource());
    }

    @Override
    public int getItemCount() {
        return remindersGroupItems.size();
    }

    class GroupItemViewHolder extends RecyclerView.ViewHolder{
        ImageView groupItemImage;
        TextView groupItemName;

        public GroupItemViewHolder(@NonNull View itemView) {
            super(itemView);
            groupItemImage = itemView.findViewById(R.id.group_item_image);
            groupItemName = itemView.findViewById(R.id.group_item_name);
        }
    }
}