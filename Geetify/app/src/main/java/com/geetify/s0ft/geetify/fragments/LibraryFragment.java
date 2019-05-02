package com.geetify.s0ft.geetify.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.geetify.s0ft.geetify.LibrarySongsManager;
import com.geetify.s0ft.geetify.listview.ListviewAdapter4Library;
import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.datamodels.YoutubeSong;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.ExternalStorageNotFoundException;
import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.HelperClass;

import java.io.File;
import java.util.ArrayList;


public class LibraryFragment extends Fragment {

    public interface OnLibrarySongSelectedToPlayListener {
        void onLibrarySongSelected(YoutubeSong youtubeSong);
    }

    private OnLibrarySongSelectedToPlayListener onLibrarySongSelectedToPlayListener;

    private String previousTitle;
    private ArrayList<YoutubeSong> libraryList = new ArrayList<>();
    private ListviewAdapter4Library listviewAdapter;
    private ArrayList<Integer> selectedListviewItemPositions = new ArrayList<>();


    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.searchIcon).setVisible(false);
        previousTitle = (String) getActivity().getTitle();
        getActivity().setTitle("Library");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listviewAdapter = new ListviewAdapter4Library(getActivity(), R.layout.row_item_library, this.libraryList);
        final ListView librarySongsList = getActivity().findViewById(R.id.librarySongsListview);
        librarySongsList.setAdapter(listviewAdapter);
        loadSavedLibrarySongs();


        librarySongsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (b) {
                    librarySongsList.getChildAt(i - librarySongsList.getFirstVisiblePosition()).setBackgroundColor(Color.LTGRAY);
                    selectedListviewItemPositions.add(i);
                } else {
                    librarySongsList.getChildAt(i - librarySongsList.getFirstVisiblePosition()).setBackgroundColor(Color.TRANSPARENT);
                    selectedListviewItemPositions.remove(Integer.valueOf(i));
                }
                actionMode.setTitle(selectedListviewItemPositions.size() + " items selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.setTitle("Select");
                actionMode.getMenuInflater().inflate(R.menu.library_context_menu, menu);
                selectedListviewItemPositions.clear();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.removeSongFromLibraryId:
                        removeSelectedSongsFromLibrary();
                        break;
                    case R.id.share:
                        shareSelectedSongs();
                        break;
                }
                actionMode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                for (Integer selectedListviewItemPos : selectedListviewItemPositions) {
                    librarySongsList.getChildAt(selectedListviewItemPos - librarySongsList.getFirstVisiblePosition()).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        librarySongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                boolean doesSongExist = new LibrarySongsManager(getActivity()).doesSongExist(listviewAdapter.getItem(i).getVideoId());
                if (doesSongExist) {
                    playSong(listviewAdapter.getItem(i));
                }
            }
        });
    }

    private void playSong(YoutubeSong youtubeSong) {
        onLibrarySongSelectedToPlayListener.onLibrarySongSelected(youtubeSong);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onLibrarySongSelectedToPlayListener = (OnLibrarySongSelectedToPlayListener) activity;
        } catch (ClassCastException ccex) {
            throw new ClassCastException(activity.toString() + " must implement OnLibrarySongSelectedToPlayListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().setTitle(previousTitle);
    }

    private void loadSavedLibrarySongs() {
        try {
            libraryList.clear();
            libraryList.addAll(new LibrarySongsManager(getActivity()).retrieveLibrarySongs());
            listviewAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void removeSelectedSongsFromLibrary() {
        ArrayList<YoutubeSong> songsToRemove = new ArrayList<>();

        for (Integer selectedListviewItemPosition : selectedListviewItemPositions) {
            songsToRemove.add(listviewAdapter.getItem(selectedListviewItemPosition));
        }

        Integer noOfRemovedSongs = 0;
        if ((noOfRemovedSongs = new LibrarySongsManager(getActivity()).removeSongsFromLibrary(songsToRemove)) > 0) {
            loadSavedLibrarySongs();
            Toast.makeText(getActivity(), noOfRemovedSongs + " songs removed.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Selected songs could not be removed!", Toast.LENGTH_SHORT).show();
        }

    }

    private void moveSelectedSongsToExternalStorage() {
        ArrayList<YoutubeSong> removeSongsFromLibraryList = new ArrayList<>();
        Integer noOfFilesMoved = 0;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            try {
                String sourceDirectoryPath = AppSettings.getMP3StoragePath();
                File destDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/GeetifyMusic");
                destDirectory.mkdirs();
                if (destDirectory.exists()) {
                    String destDirectoryPath = destDirectory.getAbsolutePath() + "/";

                    for (Integer selectedListviewItemPosition : selectedListviewItemPositions) {
                        try {
                            YoutubeSong thisYoutubeSong = listviewAdapter.getItem(selectedListviewItemPosition);
                            String mp3Filename = HelperClass.getValidFilename(thisYoutubeSong.getTitle()) + ".webm";
                            File mp3FileSrc = new File(sourceDirectoryPath + mp3Filename);
                            File mp3FileDest = new File(destDirectoryPath + mp3Filename);
                            if (HelperClass.moveFile(mp3FileSrc, mp3FileDest)) {
                                noOfFilesMoved++;
                                removeSongsFromLibraryList.add(thisYoutubeSong);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (noOfFilesMoved.equals(new LibrarySongsManager(getActivity()).removeSongsFromLibrary(removeSongsFromLibraryList))) {
                        Toast.makeText(getActivity(), noOfFilesMoved + " songs moved to external storage.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), noOfFilesMoved + " songs moved to external storage but failed to remove from library!", Toast.LENGTH_LONG).show();
                    }
                    if (noOfFilesMoved > 0)
                        loadSavedLibrarySongs();
                }
            } catch (CannotCreateFolderOnExternalStorageException ccfoesex) {
                ccfoesex.printStackTrace();
            }
        }
    }

    private void shareSelectedSongs() {

        try {
            ArrayList<Uri> audioUris = new ArrayList<Uri>();
            for (Integer selectedListviewItemPosition : selectedListviewItemPositions) {
                String mp3Filename = HelperClass.getValidFilename(listviewAdapter.getItem(selectedListviewItemPosition).getTitle()) + ".webm";
                Uri mp3ContentUri = FileProvider.getUriForFile(getActivity(), "com.geetify.s0ft.geetify.fileprovider", new File(AppSettings.getMP3StoragePath(), mp3Filename));
                audioUris.add(mp3ContentUri);
            }

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
            shareIntent.setType("audio/*");
            startActivity(Intent.createChooser(shareIntent, "Share songs to.."));
        } catch (CannotCreateFolderOnExternalStorageException ccfoesex) {
            ccfoesex.printStackTrace();
        } catch (IllegalArgumentException iaex) {
            iaex.printStackTrace();
        }
    }

}
