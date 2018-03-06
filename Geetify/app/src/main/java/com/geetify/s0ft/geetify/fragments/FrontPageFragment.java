package com.geetify.s0ft.geetify.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.geetify.s0ft.geetify.network.HttpGetYoutubeSearch;
import com.geetify.s0ft.geetify.listview.ListviewAdapter;
import com.geetify.s0ft.geetify.MainActivity;
import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.datamodels.YoutubeSong;

import java.util.ArrayList;

public class FrontPageFragment extends Fragment implements HttpGetYoutubeSearch.YoutubeSongsDownloadStatusListener {


    public FrontPageFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_front_page, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getActivity().findViewById(R.id.youtubeSearchResultlist);
        ((MainActivity) (getActivity())).setArrayAdapter(new ListviewAdapter(getActivity(), R.layout.row_item_searchresult, ((MainActivity) (getActivity())).getSongsList()));
        listView.setAdapter(((MainActivity) (getActivity())).getArrayAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View rowItemView, int position, long id) {
                loadSongDownloaderFragment((YoutubeSong) listView.getAdapter().getItem(position));
            }
        });
        listView.setEmptyView(getActivity().findViewById(R.id.empty));
    }

    private void loadSongDownloaderFragment(YoutubeSong youtubeSong) {
        ((MainActivity) getActivity()).loadSongDownloaderFragment(youtubeSong);
    }



    @Override
    public void downloadFinished(ArrayList<YoutubeSong> youtubeSongs) {
        (getActivity().findViewById(R.id.youtubeSearchProgress)).setVisibility(View.GONE);
        ((MainActivity) getActivity()).getSongsList().clear();
        ((MainActivity) getActivity()).getSongsList().addAll(youtubeSongs);
        ((MainActivity) getActivity()).getArrayAdapter().notifyDataSetChanged();
    }

    @Override
    public void progressReporter(int percent) {
        Log.w("YTS", "Youtube videos downloaded : " + percent + "%");
        ((MainActivity) getActivity()).getArrayAdapter().clear();
        (getActivity().findViewById(R.id.youtubeSearchProgress)).setVisibility(View.VISIBLE);
        ((TextView) (getActivity().findViewById(R.id.empty))).setText(percent + "%");
    }


}
