package com.music.musicjogger.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.music.musicjogger.MainActivity;
import com.music.musicjogger.R;
import com.music.musicjogger.Utils.Constants;
import com.music.musicjogger.Utils.SessionManager;
import com.music.musicjogger.models.ModelAudio;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.MyViewHolder> {
    Context context;
    private List<ModelAudio> data;
    public AudioAdapter(Context context, ArrayList<ModelAudio> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public AudioAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_audio, parent, false);
        return new AudioAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioAdapter.MyViewHolder holder, final int position) {
        final ModelAudio model = data.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.rlTrack.setTag(position);
        if (model.isPlaying()){
            holder.rlTrack.setBackgroundColor(context.getResources().getColor(R.color.green));
        }else {
              holder.rlTrack.setBackgroundColor(context.getResources().getColor(R.color.black));
        }
        holder.rlTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<data.size(); i++){
                    data.get(i).setPlaying(false);
                }
                data.get(position).setPlaying(true);
                SessionManager.putIntPref(Constants.AUDIO_POSITION,position,context);
                ((MainActivity)context).PlayAudio(model.getUrl());
                ((MainActivity)context).adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        RelativeLayout rlTrack;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            rlTrack = itemView.findViewById(R.id.rlTrack);
        }
    }


}
