package com.crust87.sample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.crust87.exovideoview.widget.ExoVideoPlayerView;
import com.google.android.exoplayer.util.Util;

public class MainActivity extends AppCompatActivity {

    private ExoVideoPlayerView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = (ExoVideoPlayerView) findViewById(R.id.videoView);

        Intent lIntent = new Intent(Intent.ACTION_PICK);
        lIntent.setType("video/*");
        lIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(lIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Uri selectedVideoUri = data.getData();

            mVideoView.setContent(selectedVideoUri);

//
//
//            mVideoView.setVideoURI(selectedVideoUri);
//            mVideoView.start();
        }
    }
}
