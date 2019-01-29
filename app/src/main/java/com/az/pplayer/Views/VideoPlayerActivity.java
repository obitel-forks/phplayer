package com.az.pplayer.Views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import com.az.pplayer.Data.DataHolder;
import com.az.pplayer.Data.ExoPlayerVideoHandler;
import com.az.pplayer.Models.VideoItem;
import com.az.pplayer.Models.VideoUrl;
import com.az.pplayer.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoPlayerActivity extends AppCompatActivity {
    private String mVideoUrl = "http://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_480p_surround-fix.avi";
    public static final String INTENT_EXTRA_VIDEO_URL = "VIDEO_URL";

    private SimpleExoPlayerView mSimpleExoPlayerView;
    private ImageButton mIbFullScreen;

    private boolean shouldDestroyVideo = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Intent intent = getIntent();
        mVideoUrl = intent.getStringExtra(INTENT_EXTRA_VIDEO_URL);
        initializePlayer();
//        Fragment fragment = new VideoPlayerFragment();
//
//        Bundle bundle = new Bundle();
//        bundle.putString(VideoPlayerFragment.BUNDLE_KEY_VIDEO_URL, mVideoUrl);
//        fragment.setArguments(bundle);
//
//        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();

    }


    private void initializePlayer() {
        mSimpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        mIbFullScreen = (ImageButton) findViewById(R.id.exo_fullscreen_btn);

        ExoPlayerVideoHandler.getInstance().prepareExoPlayerForUri(getApplicationContext(), Uri.parse(mVideoUrl), mSimpleExoPlayerView, findViewById(R.id.pBarBuffer), findViewById(R.id.container_play_pause));
        ExoPlayerVideoHandler.getInstance().goToForeground();
        mIbFullScreen = findViewById(R.id.exo_fullscreen_btn);
        mIbFullScreen.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shouldDestroyVideo = false;
                        Intent intent = new Intent(VideoPlayerActivity.this, SmallPlayerActivity.class);
                        intent.putExtra(VideoPlayerActivity.INTENT_EXTRA_VIDEO_URL, mVideoUrl);
                        startActivity(intent);

                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initializePlayer();
    }

    @Override
    public void onBackPressed() {
        shouldDestroyVideo = false;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ExoPlayerVideoHandler.getInstance().goToBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shouldDestroyVideo) {
            ExoPlayerVideoHandler.getInstance().releaseVideoPlayer();
        }
    }
}
