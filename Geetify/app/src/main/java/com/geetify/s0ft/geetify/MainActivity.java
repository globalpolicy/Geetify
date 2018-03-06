package com.geetify.s0ft.geetify;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.geetify.s0ft.geetify.baseclasses.BaseActivity;
import com.geetify.s0ft.geetify.datamodels.YoutubeSong;
import com.geetify.s0ft.geetify.fragments.FrontPageFragment;
import com.geetify.s0ft.geetify.fragments.LibraryFragment;
import com.geetify.s0ft.geetify.fragments.PreferencesFragment;
import com.geetify.s0ft.geetify.fragments.SlidingPanelBottomToolbarFragment;
import com.geetify.s0ft.geetify.fragments.SongDownloaderFragment;
import com.geetify.s0ft.geetify.listview.ListviewAdapter;
import com.geetify.s0ft.geetify.network.HttpGetYoutubeSearch;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, LibraryFragment.OnLibrarySongSelectedToPlayListener, SongDownloaderFragment.OnPlayDownloadedSongListener {

    private SlidingPanelBottomToolbarFragment slidingPanelBottomToolbarFragment = new SlidingPanelBottomToolbarFragment();
    private SongDownloaderFragment songDownloaderFragment = new SongDownloaderFragment();

    private ArrayList<YoutubeSong> songsList = new ArrayList<>();
    private FrontPageFragment frontPageFragment;
    private ActionBarDrawerToggle hamburger;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public void setArrayAdapter(ListviewAdapter arrayAdapter) {
        this.arrayAdapter = arrayAdapter;
    }

    private ListviewAdapter arrayAdapter;

    public ArrayList<YoutubeSong> getSongsList() {
        return songsList;
    }

    public ListviewAdapter getArrayAdapter() {
        return arrayAdapter;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clearJunk();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        frontPageFragment = new FrontPageFragment();
        fragmentTransaction.add(R.id.fragmentContainerId, frontPageFragment);
        fragmentTransaction.commit();

        drawerLayout = findViewById(R.id.drawerlayout);
        hamburger = new ActionBarDrawerToggle(this, drawerLayout, R.string.hamburger_open, R.string.hamburger_close);
        drawerLayout.addDrawerListener(hamburger);
        hamburger.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);


        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    private void clearJunk() {
        final LibrarySongsManager librarySongsManager = new LibrarySongsManager(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                librarySongsManager.cleanupNonLibraryMP3AndPNG();
            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchIcon).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);

        return true;

    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.w("YTS", query);
            getArrayAdapter().clear();
            findViewById(R.id.youtubeSearchProgress).setVisibility(View.VISIBLE);
            ((TextView) (findViewById(R.id.empty))).setText("");
            new HttpGetYoutubeSearch(frontPageFragment, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_numOfVids", "5")).execute(query);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Boolean addToBackStackFlag=!(fragmentManager.findFragmentById(R.id.fragmentContainerId) instanceof SongDownloaderFragment);

        switch (item.getItemId()) {
            case R.id.navbar_menu_settings:
                if (!(fragmentManager.findFragmentById(R.id.fragmentContainerId) instanceof PreferencesFragment)) {
                    fragmentTransaction.replace(R.id.fragmentContainerId, new PreferencesFragment());
                    if(addToBackStackFlag) fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                }
                break;
            case R.id.navbar_menu_library:
                if (!(fragmentManager.findFragmentById(R.id.fragmentContainerId) instanceof LibraryFragment)) {
                    fragmentTransaction.replace(R.id.fragmentContainerId, new LibraryFragment());
                    if(addToBackStackFlag) fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                }
                break;
            case R.id.navbar_menu_about:
                if(!(fragmentManager.findFragmentById(R.id.fragmentContainerId) instanceof AboutFragment)){
                    fragmentTransaction.replace(R.id.fragmentContainerId,new AboutFragment());
                    if(addToBackStackFlag) fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                }
                break;
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (hamburger.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLibrarySongSelected(YoutubeSong youtubeSong) {
        setcurrentSong(youtubeSong);
        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        FragmentManager fragmentManager = getFragmentManager();

        if (!(fragmentManager.findFragmentById(R.id.songFragmentContainerId) instanceof SlidingPanelBottomToolbarFragment)) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.songFragmentContainerId, slidingPanelBottomToolbarFragment);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).addPanelSlideListener(slidingPanelBottomToolbarFragment);
        slidingPanelBottomToolbarFragment.playSongInCollapsedState(currentSong);
    }

    @Override
    public void onDownloadedMP3PlayRequested() {
        //slidingPanelBottomToolbarFragment=new SlidingPanelBottomToolbarFragment();
        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        FragmentManager fragmentManager = getFragmentManager();
        if (!(fragmentManager.findFragmentById(R.id.songFragmentContainerId) instanceof SlidingPanelBottomToolbarFragment)) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.songFragmentContainerId, slidingPanelBottomToolbarFragment);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).addPanelSlideListener(slidingPanelBottomToolbarFragment);
        slidingPanelBottomToolbarFragment.playSongInCollapsedState(currentSong);
    }

    public void loadSongDownloaderFragment(YoutubeSong youtubeSong) {
        setcurrentSong(youtubeSong);
        FragmentManager fragmentManager = getFragmentManager();

        if (!(fragmentManager.findFragmentById(R.id.fragmentContainerId) instanceof SongDownloaderFragment)) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainerId, songDownloaderFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }
        ((SlidingUpPanelLayout) findViewById(R.id.slidingLayout)).addPanelSlideListener(slidingPanelBottomToolbarFragment);
    }

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout slidingUpPanelLayout=findViewById(R.id.slidingLayout);
        if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else
            super.onBackPressed();

    }

    public void setcurrentSong(YoutubeSong youtubeSong) {
        currentSong = youtubeSong;
    }

    public YoutubeSong getcurrentSong() {
        return currentSong;
    }

}
