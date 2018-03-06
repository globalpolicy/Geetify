package com.geetify.s0ft.geetify.datamodels;

import android.graphics.Bitmap;

import com.geetify.s0ft.geetify.baseclasses.Song;

/**
 * Created by s0ft on 2/4/2018.
 */

public class YoutubeSong extends Song {

    private Bitmap hqThumbnailBitmap;

    public Bitmap getHqThumbnailBitmap() {
        return hqThumbnailBitmap;
    }

    private void setHqThumbnailBitmap(Bitmap hqThumbnailBitmap) {
        this.hqThumbnailBitmap = hqThumbnailBitmap;
    }

    public YoutubeSong(String title,String description,String videoId,String hqThumbnailUrl,String publishedDate, Bitmap hqThumbnailBitmap){
        super(title,description,videoId,hqThumbnailUrl,publishedDate);
        setHqThumbnailBitmap(hqThumbnailBitmap);
    }


}
