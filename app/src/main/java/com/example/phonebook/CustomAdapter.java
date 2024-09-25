package com.example.phonebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;

    private ArrayList<String> contactsName, numbersCount;

    private ArrayList<Integer> contacts_ids;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    CustomAdapter(Context context, ArrayList<Integer> contactsIds, ArrayList<String> contactsName, ArrayList<String> numbersCount, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.contacts_ids = contactsIds;
        this.contactsName = contactsName;
        this.numbersCount = numbersCount;
        this.listener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {
        holder.name.setText(contactsName.get(position));
        holder.count.setText(numbersCount.get(position));
    }

    @Override
    public int getItemCount() {
        return contactsName.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, count;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            count = itemView.findViewById(R.id.countNumbers);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}

