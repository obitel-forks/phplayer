package com.az.pplayer.Views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.az.pplayer.Data.DataHolder;
import com.az.pplayer.Data.ExoPlayerVideoHandler;
import com.az.pplayer.Data.VideoLinkHolder;
import com.az.pplayer.DataSource.VideoLinksSource;
import com.az.pplayer.MainActivity;
import com.az.pplayer.Models.VideoItem;
import com.az.pplayer.Models.VideoUrl;
import com.az.pplayer.R;
import com.az.pplayer.Storage.UserStorage;
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
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

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
    private String mVideoUrl = "";
    public static final String INTENT_EXTRA_VIDEO_URL = "VIDEO_URL";

    private SimpleExoPlayerView mSimpleExoPlayerView;
    private ImageButton mIbFullScreen;
    private  ImageButton mMenuButton;
    DrawerLayout mDrawerLayout;

    RecyclerView recyclerView;
    SwipyRefreshLayout mSwipyRefreshLayout;

    private boolean shouldDestroyVideo = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Intent intent = getIntent();

        mVideoUrl= intent.getStringExtra("url");
        LoadVideo();
    }

    void LoadVideo(){
        String videoUrl = VideoLinkHolder.GetDefaultUrl(mVideoUrl);
        if (videoUrl==null)
            LoadSite(mVideoUrl);
        else
            initializePlayer(videoUrl);
    }

    void LoadSite(final String _url){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String defaultUrl ="";

                try {
                    Document doc = Jsoup.connect("https://pornhub.com"+_url).get();
                    Element script = doc.select("#player").select("script").first();

                    String rawHtml = script.html().substring(0,6553);
                    String[] urlParts = rawHtml.substring(rawHtml.indexOf("videoUrl")).split("videoUrl");
                    List<VideoUrl> urls = new ArrayList<>();

                    for (String urlPart : urlParts)
                    {
                        try {
                            String url = urlPart.substring(0, urlPart.indexOf(","))
                                    .replace("\":\"","").replace("\"}","").replace("\\","").replace("]","");
                            if (url.length() > 2) {
                                VideoUrl _url =new VideoUrl(url,
                                        urlPart.substring(urlPart.indexOf("quality")+10,urlPart.indexOf("quality")+14).replace("\"","")
                                );
                                if (_url.Quality =="480")
                                    defaultUrl = _url.Link;
                                if (defaultUrl.length()==0)
                                    defaultUrl = _url.Link;
                                urls.add(_url);

                            }
                        } catch (Exception ex)
                        {

                        }

                    }
                    VideoLinkHolder.Save(mVideoUrl,urls);
                    DataHolder.Save(mVideoUrl,VideoLinksSource.ParseLinks(doc));

                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                    final String finalDefaultUrl = VideoLinkHolder.GetDefaultUrl(mVideoUrl);
                if (finalDefaultUrl.length()>0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //imageView = (ImageView) findViewById(R.id.imageView);
                            initializePlayer(finalDefaultUrl);
                            //mSwipyRefreshLayout.setRefreshing(false);
                            //imageView = (ImageView) findViewById(R.id.imageView);
                            ShowVideos(mVideoUrl);
                        }
                    });
                }
            }
        }).start();
    }


    void ShowVideos(String catUrl){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), UserStorage.Get().getColumns());
        recyclerView.setLayoutManager(gridLayoutManager);


        VideoDataAdapter dataAdapter = new VideoDataAdapter(getApplicationContext(),
                DataHolder.Get(catUrl).CurrentVideo(),
                UserStorage.Get().getFontSize());
        recyclerView.setAdapter(dataAdapter);
    }


    private void initializePlayer(final String mVideoUrl) {
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
        mMenuButton = findViewById(R.id.menu_btn);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.video_drawer_layout);
                drawer.openDrawer(GravityCompat.END);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadVideo();
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

    // Get the x and y position after the button is draw on screen
// (It's important to note that we can't get the position in the onCreate(),
// because at that stage most probably the view isn't drawn yet, so it will return (0, 0))


    // The method that displays the popup.

}
