package com.geetify.s0ft.geetify.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.geetify.s0ft.geetify.LibrarySongsManager;
import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.datamodels.YoutubeSong;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.ExternalStorageNotFoundException;
import com.geetify.s0ft.geetify.exceptions.NoSongFoundException;
import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.HelperClass;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;

/**
 * Created by s0ft on 2/17/2018.
 */

public class SlidingPanelBottomToolbarFragment extends Fragment implements View.OnClickListener, SlidingUpPanelLayout.PanelSlideListener, MediaPlayer.OnCompletionListener {


    private YoutubeSong currentSong = null;

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private SeekBar seekBar;
    private Runnable updateSeekbarRunnable;


    public SlidingPanelBottomToolbarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sliding_panel_toolbar_bottom_fragment, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.currentSong != null) {
            setupBottomToolbarViews();
        }
    }

    public void playSongInCollapsedState(YoutubeSong youtubeSong) {
        this.currentSong = youtubeSong;
        setupBottomToolbarViews();
        ((ImageButton) (getView().findViewById(R.id.bottomToolbarSongPlayPause))).setImageResource(R.drawable.ic_pause_black_24dp);
        playCurrentSong();
    }

    public void playSongInExpandedState(YoutubeSong youtubeSong) {
        this.currentSong = youtubeSong;
        setupExpandedToolbarViews();
        ((ImageButton) (getView().findViewById(R.id.slidingPanelpauseplayicon))).setImageResource(R.drawable.ic_pause_black_24dp);
        playCurrentSong();
    }


    private void setupBottomToolbarViews() {
        loadCollapsedLayout();
        ((ImageView) getView().findViewById(R.id.bottomToolbarSongImage)).setImageBitmap(currentSong.getHqThumbnailBitmap());
        ((TextView) getView().findViewById(R.id.bottomToolbarSongTitle)).setText(currentSong.getTitle());
        getView().findViewById(R.id.bottomToolbarSongTitle).setSelected(true);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                ((ImageButton) (getView().findViewById(R.id.bottomToolbarSongPlayPause))).setImageResource(R.drawable.ic_pause_black_24dp);
            else
                ((ImageButton) (getView().findViewById(R.id.bottomToolbarSongPlayPause))).setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        getView().findViewById(R.id.bottomToolbarSongPlayPause).setOnClickListener(this);
    }

    private void setupExpandedToolbarViews() {
        ((ImageView) getView().findViewById(R.id.slidingPanelSongImage)).setImageBitmap(currentSong.getHqThumbnailBitmap());
        ((TextView) getView().findViewById(R.id.slidingPanelSongTitle)).setText(currentSong.getTitle());
        getView().findViewById(R.id.slidingPanelSongTitle).setSelected(true);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                ((ImageButton) (getView().findViewById(R.id.slidingPanelpauseplayicon))).setImageResource(R.drawable.ic_pause_black_24dp);
            else
                ((ImageButton) (getView().findViewById(R.id.slidingPanelpauseplayicon))).setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
        getView().findViewById(R.id.slidingPanelpauseplayicon).setOnClickListener(this);
        getView().findViewById(R.id.slidingPanelskipToNext).setOnClickListener(this);
        getView().findViewById(R.id.slidingPanelskipToPrevious).setOnClickListener(this);
        setupSeekbar();
    }

    private void playCurrentSong() {
        try {
            if (mediaPlayer != null)
                mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(AppSettings.getMP3StoragePath() + HelperClass.getValidFilename(currentSong.getTitle()) + ".ogg");
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            setupSeekbar();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (CannotCreateFolderOnExternalStorageException ccfoesex) {
            ccfoesex.printStackTrace();
        } catch (ExternalStorageNotFoundException esnfex) {
            esnfex.printStackTrace();
        }
    }


    private void setupSeekbar() {
        try {
            seekBar = getView().findViewById(R.id.slidingPanelseekbar);
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            seekBar.setProgress(0);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b && mediaPlayer != null) {
                        mediaPlayer.seekTo(i*1000 );
                    }
                }


                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            setupHandlerUpdateCallback();
        } catch (NullPointerException npex) {
            npex.printStackTrace();
        }
    }

    private void setupHandlerUpdateCallback() {
        handler = new Handler();
        updateSeekbarRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                        ((TextView) (getView().findViewById(R.id.slidingPanelSongCurrentPosition))).setText(HelperClass.convertMillisecondsToMinutesSeconds(mediaPlayer.getCurrentPosition()) + "/" + HelperClass.convertMillisecondsToMinutesSeconds(mediaPlayer.getDuration()));
                    }
                    handler.postDelayed(updateSeekbarRunnable, 1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
        seekBar.removeCallbacks(updateSeekbarRunnable);
        handler.post(updateSeekbarRunnable);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.bottomToolbarSongPlayPause || viewId == R.id.slidingPanelpauseplayicon) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    ((ImageView) view).setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaPlayer.pause();
                } else {
                    ((ImageView) view).setImageResource(R.drawable.ic_pause_black_24dp);
                    mediaPlayer.start();//just resumes the paused music
                }
            }
        } else if (viewId == R.id.slidingPanelskipToNext) {
            if (mediaPlayer != null) {
                try {
                    playSongInExpandedState(new LibrarySongsManager(getActivity()).getNextSongFromLibrary(this.currentSong));
                } catch (NoSongFoundException e) {
                    Log.w("YTS", "No next song found!");
                }
            }
        } else if (viewId == R.id.slidingPanelskipToPrevious) {
            if (mediaPlayer != null) {
                try {
                    playSongInExpandedState(new LibrarySongsManager(getActivity()).getPreviousSongFromLibrary(this.currentSong));
                } catch (NoSongFoundException e) {
                    Log.w("YTS", "No previous song found!");
                }
            }
        }
    }


    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if (getActivity() != null) {
            if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                changeViewsToExpanded();
            } else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                changeViewsToCollapsed();
            }
        }
    }

    private void changeViewsToExpanded() {
        if (loadExpandedLayout())
            setupExpandedToolbarViews();
    }

    private void changeViewsToCollapsed() {
        if (loadCollapsedLayout())
            setupBottomToolbarViews();
    }

    private boolean loadExpandedLayout() {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = layoutInflater.inflate(R.layout.sliding_panel_toolbar_fragment, null);
        ViewGroup rootView = (ViewGroup) getView();
        if (rootView != null) {
            rootView.removeAllViews();
            rootView.addView(newView);
            return true;
        }
        return false;
    }

    private boolean loadCollapsedLayout() {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = layoutInflater.inflate(R.layout.sliding_panel_toolbar_bottom_fragment, null);
        ViewGroup rootView = (ViewGroup) getView();
        if (rootView != null) {
            rootView.removeAllViews();
            rootView.addView(newView);
            return true;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_autoNextSong", false)) {
            switch (((SlidingUpPanelLayout) getActivity().findViewById(R.id.slidingLayout)).getPanelState()) {
                case COLLAPSED:
                    try {
                        playSongInCollapsedState(new LibrarySongsManager(getActivity()).getNextSongFromLibrary(currentSong));
                    } catch (NoSongFoundException e) {
                        Log.w("YTS", e.getMessage());
                    }
                    break;
                case EXPANDED:
                    try {
                        playSongInExpandedState(new LibrarySongsManager(getActivity()).getNextSongFromLibrary(currentSong));
                    } catch (NoSongFoundException e) {
                        Log.w("YTS", e.getMessage());
                    }
                    break;
            }
        }else{
            playCurrentSong();
            this.mediaPlayer.pause();
            SlidingUpPanelLayout.PanelState panelState=((SlidingUpPanelLayout) getActivity().findViewById(R.id.slidingLayout)).getPanelState();
            View view=null;
            if(panelState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)){
                view=getView().findViewById(R.id.bottomToolbarSongPlayPause);
            }else if(panelState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
                view=getView().findViewById(R.id.slidingPanelpauseplayicon);
            }
            if(view!=null){
                ((ImageView) view).setImageResource(R.drawable.ic_play_arrow_black_24dp);
            }

        }
    }


}