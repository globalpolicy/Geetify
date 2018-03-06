package com.geetify.s0ft.geetify.datamodels;

import com.geetify.s0ft.geetify.baseclasses.Song;

import java.io.Serializable;

/**
 * Created by s0ft on 2/8/2018.
 */

public class LibrarySong extends Song implements Serializable{

    private String hqThumbnailFilename;


    public LibrarySong(String title, String description, String videoId, String hqThumbnailUrl, String hqThumbnailFilename, String publishedDate) {
        super(title,description,videoId,hqThumbnailUrl,publishedDate);
        setHqThumbnailFilename(hqThumbnailFilename);
    }

    public String getHqThumbnailFilename() {
        return hqThumbnailFilename;
    }

    public void setHqThumbnailFilename(String hqThumbnailFilename) {
        this.hqThumbnailFilename = hqThumbnailFilename;
    }

}