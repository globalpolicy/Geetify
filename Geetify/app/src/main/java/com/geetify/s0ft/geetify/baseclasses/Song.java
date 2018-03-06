package com.geetify.s0ft.geetify.baseclasses;

import java.io.Serializable;

/**
 * Created by s0ft on 2/8/2018.
 */

public abstract class Song implements Serializable{

    private String title;
    private String description;
    private String videoId;
    private String hqThumbnailUrl;
    private String publishedDate;

    public Song(String title, String description, String videoId, String hqThumbnailUrl, String publishedDate) {
        this.title = title;
        this.description = description;
        this.videoId = videoId;
        this.hqThumbnailUrl = hqThumbnailUrl;
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getHqThumbnailUrl() {
        return hqThumbnailUrl;
    }

    public void setHqThumbnailUrl(String hqThumbnailUrl) {
        this.hqThumbnailUrl = hqThumbnailUrl;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(getClass()) && getVideoId().equals(((Song) obj).getVideoId());
    }
}
