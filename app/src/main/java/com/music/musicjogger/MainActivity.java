package com.music.musicjogger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.music.musicjogger.Utils.Constants;
import com.music.musicjogger.Utils.SessionManager;
import com.music.musicjogger.adapters.AudioAdapter;
import com.music.musicjogger.models.ModelAudio;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<ModelAudio> data;
    RecyclerView recyclerview;
    ProgressBar progressBar;
    public AudioAdapter adapter;
    private Handler mHandler;
    //media player
    MediaPlayer mp;
    private boolean isPLAYING = false;

    //controlls
    ImageView btnNext,btnPrev,btnPlayPause;
    TextView tvTotalAudioTime;
    SeekBar seekBar;
    RelativeLayout rlBottomSheet;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<>();
        getViews();


        // Mediaplayer
        mp = new MediaPlayer();
//        //getting list of audios
        listAllFiles();
        //
        mHandler = new Handler();

        //media player action buttons
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPLAYING = true;
                int pos =  SessionManager.getIntPref(Constants.AUDIO_POSITION,MainActivity.this);
                Log.d(Constants.TAG, "position in session: " + pos);
                Log.d(Constants.TAG, "data size: "+String.valueOf(data.size()-1));
                Log.d(Constants.TAG, "position next: " + pos);
                boolean setToStart = false;
                if (pos == data.size()-1){
                    pos = 0;
                }else {
                    pos = pos+1;
                }
                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setPlaying(false);
                }
                data.get(pos).setPlaying(true);
                SessionManager.putIntPref(Constants.AUDIO_POSITION, pos,MainActivity.this);
                PlayAudio(data.get(pos).getUrl());
                adapter.notifyDataSetChanged();
                btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            }
        });
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPLAYING = true;
                int pos =  SessionManager.getIntPref(Constants.AUDIO_POSITION,MainActivity.this);
                Log.d(Constants.TAG, "position in session: " + pos);

                if (pos == 0){
                    pos = data.size()-1;

                }else {
                    pos = pos-1;
                }
                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setPlaying(false);
                }
                data.get(pos).setPlaying(true);
                PlayAudio(data.get(pos).getUrl());
                SessionManager.putIntPref(Constants.AUDIO_POSITION,pos,MainActivity.this);
                adapter.notifyDataSetChanged();
                btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            }
        });

        //play pause button
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayPauseAudio(MainActivity.this);
            }
        });

        //setting seekbar value
//Make sure you update Seekbar on UI thread

        MainActivity.this.runOnUiThread(runnable= new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if(mp != null){
                    seekBar.setMax(mp.getDuration()/1000);
                    // where mFileDuration is mMediaPlayer.getDuration();
                    int mCurrentPosition = mp.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    int durationMs = mp.getDuration();
                    //timer work
                    String timeTotal = getTimeString(durationMs);
                    String timeCurrent = getTimeString(mp.getCurrentPosition());
                    tvTotalAudioTime.setText(timeCurrent + "/" + timeTotal);
                    if (timeTotal.equals(timeCurrent)){
                        btnPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        });
//seekbar change listner
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //  mp.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int totalDuration = mp.getDuration();
                int currentPosition = seekBar.getProgress();
                Log.d(Constants.TAG, "totalDuration: " + totalDuration + "\n currentPosition: " + currentPosition);
                // forward or backward to certain seconds
                mp.seekTo(currentPosition*1000);

            }
        });
    }

    private void setRecyclerView() {
        adapter = new AudioAdapter(MainActivity.this, data);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.VERTICAL, false);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setAdapter(adapter);
    }

    private void getViews() {
        recyclerview = findViewById(R.id.recyclerview);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        tvTotalAudioTime = findViewById(R.id.tvTotalAudioTime);
        seekBar = findViewById(R.id.seekBar);
        rlBottomSheet = findViewById(R.id.rlBottomSheet);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mHandler != null && runnable != null) {
            mHandler.removeCallbacks(runnable);
        }
        SessionManager.clearsession(MainActivity.this);
        try {
            if (mp != null) {
                if (mp.isPlaying()) {
                    mp.pause();
                    mp.release();
                    finish();
                } else {
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    @Override
    protected void onResume() {
        super.onResume();
        // SessionManager.clearsession(MainActivity.this);
//        if(mp.isPlaying()) {
//            if (mp != null) {
//                mp.stop();
//                mp.release();
//            }
//        }
    }

    public void listAllFiles() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // [START storage_list_all]
        StorageReference listRef = storage.getReference().child("audios");
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.
                }
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ModelAudio modelAudio = new ModelAudio();
                            modelAudio.setTitle(item.getName());
                            modelAudio.setUrl(uri.toString());
                            data.add(modelAudio);
                        }
                    });
                }
                SessionManager.putIntPref(Constants.AUDIO_POSITION,0,MainActivity.this);
                setRecyclerView();
                //adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                rlBottomSheet.setVisibility(View.VISIBLE);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        Toast.makeText(MainActivity.this, "Playlist not loaded, try again!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                });
        // [END storage_list_all]
    }



    public void PlayAudio(String url){

        // check for already playing
        if(mp!=null && mp.isPlaying()){
                mp.stop();
                mp.release();
                mp = new MediaPlayer();
                try {
                    mp.reset();
                    mp.setDataSource(url);
                    mp.prepare();
                    mp.start();
                    btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    //  btnPlayPause.setText("Play");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }else{
            // Resume song
            if(mp!=null){
                mp.stop();
                mp.release();
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(url);
                    mp.prepare();
                    mp.start();
                    btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void PlayPauseAudio(Context context) {
        // check for already playing
        if (isPLAYING){
            try {
                if (mp != null) {
                    if (mp.isPlaying()) {
                        mp.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    } else {
                        mp.setLooping(false);
                        btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.d(Constants.TAG, "onCompletion");
//                                mp.stop();
//                                mp.release();
                                btnPlayPause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                             //   btnNext.callOnClick();
                            }
                        });
                        mp.start();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            isPLAYING = true;
            int pos = SessionManager.getIntPref(Constants.AUDIO_POSITION, MainActivity.this);
            PlayAudio(data.get(pos).getUrl());
            data.get(pos).setPlaying(true);
            btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            adapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("DefaultLocale")
    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        if (hours>0){
            buf
                    .append(String.format("%02d", hours))
                    .append(":")
                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));
        }else {
            buf
                    .append(String.format("%02d", minutes))
                    .append(":")
                    .append(String.format("%02d", seconds));

        }

        return buf.toString();
    }
}