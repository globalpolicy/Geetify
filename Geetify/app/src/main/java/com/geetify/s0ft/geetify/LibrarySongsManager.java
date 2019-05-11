package com.geetify.s0ft.geetify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geetify.s0ft.geetify.datamodels.LibrarySong;
import com.geetify.s0ft.geetify.datamodels.YoutubeSong;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.NoSongFoundException;
import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.HelperClass;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by s0ft on 2/8/2018.
 */

public class LibrarySongsManager {

    private Context context;
    private String savePath;

    public LibrarySongsManager(Context context) throws CannotCreateFolderOnExternalStorageException {
        this.context = context;
        this.savePath = AppSettings.getLibraryFile();
    }

    public boolean addLibrarySong(YoutubeSong youtubeSong) {
        boolean retval = false;
        try {
            ArrayList<YoutubeSong> youtubeSongs = retrieveLibrarySongs();
            youtubeSongs.remove(youtubeSong);
            youtubeSongs.add(youtubeSong);

            ArrayList<LibrarySong> librarySongsList = new ArrayList<>();
            for (YoutubeSong eachYoutubeSong : youtubeSongs) {
                eachYoutubeSong.getHqThumbnailBitmap().compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(AppSettings.getAppDataStoragePath() + eachYoutubeSong.getVideoId() + ".png", false));
                librarySongsList.add(new LibrarySong(eachYoutubeSong.getTitle(), eachYoutubeSong.getDescription(), eachYoutubeSong.getVideoId(), eachYoutubeSong.getHqThumbnailUrl(), eachYoutubeSong.getVideoId(), eachYoutubeSong.getPublishedDate()));
            }
            LibrarySongs librarySongs = new LibrarySongs(librarySongsList);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(this.savePath));
            objectOutputStream.writeObject(librarySongs);
            objectOutputStream.flush();
            objectOutputStream.close();

            retval = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retval;
    }

    public ArrayList<YoutubeSong> retrieveLibrarySongs() throws IOException, ClassNotFoundException,CannotCreateFolderOnExternalStorageException {
        ArrayList<YoutubeSong> retval = new ArrayList<>();
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(this.savePath));
            LibrarySongs librarySongs = (LibrarySongs) objectInputStream.readObject();
            for (LibrarySong librarySong : librarySongs.getListOfLibrarySongs()) {
                retval.add(new YoutubeSong(librarySong.getTitle(), librarySong.getDescription(), librarySong.getVideoId(), librarySong.getHqThumbnailUrl(), librarySong.getPublishedDate(), BitmapFactory.decodeFile(AppSettings.getAppDataStoragePath() + librarySong.getHqThumbnailFilename() + ".png")));
            }
            objectInputStream.close();
        } catch (FileNotFoundException fnfex) {
            //if file not found, just returns empty arraylist
        }

        return retval;
    }

    private String getFilepathFromVideoid(String videoId) {
        String retval = "";

        try {
            for (YoutubeSong youtubeSong : retrieveLibrarySongs()) {
                if (youtubeSong.getVideoId().equals(videoId)) {
                    String songFilename = HelperClass.getValidFilename(youtubeSong.getTitle());
                    if ((new File(AppSettings.getMP3StoragePath() + songFilename + ".webm").exists())) {
                        retval = AppSettings.getMP3StoragePath() + songFilename + ".webm";
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return retval;
    }

    public boolean doesSongExist(String videoId) {
        return !getFilepathFromVideoid(videoId).equals("");
    }

    YoutubeSong getYoutubeSongFromVideoid(String videoId) {
        YoutubeSong retval = null;

        try {
            for (YoutubeSong youtubeSong : retrieveLibrarySongs()) {
                if (youtubeSong.getVideoId().equals(videoId))
                    retval = youtubeSong;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return retval;
    }

    void cleanupNonLibraryMP3AndPNG() {

        try {
            final ArrayList<String> MP3ToNotDelete = new ArrayList<>();
            for (YoutubeSong youtubeSong : retrieveLibrarySongs()) {
                MP3ToNotDelete.add(new File(getFilepathFromVideoid(youtubeSong.getVideoId())).getName());
            }
            File[] mp3Files =new File(AppSettings.getMP3StoragePath()).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".webm") && !MP3ToNotDelete.contains(file.getName());
                }
            });
            for (File mp3file : mp3Files) {
                mp3file.delete();
            }

            final ArrayList<String> PNGToNotDelete = new ArrayList<>();
            for (YoutubeSong youtubeSong : retrieveLibrarySongs()) {
                PNGToNotDelete.add(youtubeSong.getVideoId() + ".png");
            }
            File[] pngFiles = AppSettings.getAppDataStoragePath_File(context).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".png") && !PNGToNotDelete.contains(file.getName());
                }
            });
            for (File pngfile : pngFiles) {
                pngfile.delete();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Integer removeSongsFromLibrary(ArrayList<YoutubeSong> songsToRemove) {
        Integer retval = 0;

        try {
            ArrayList<LibrarySong> newLibrarySongs = new ArrayList<>();
            ArrayList<YoutubeSong> existinglibrarySongs = retrieveLibrarySongs();

            for (YoutubeSong existingYoutubeSong : existinglibrarySongs) {
                if (!songsToRemove.contains(existingYoutubeSong)) {
                    newLibrarySongs.add(new LibrarySong(existingYoutubeSong.getTitle(), existingYoutubeSong.getDescription(), existingYoutubeSong.getVideoId(), existingYoutubeSong.getHqThumbnailUrl(), existingYoutubeSong.getVideoId(), existingYoutubeSong.getPublishedDate()));
                } else {
                    retval++;
                }
            }

            LibrarySongs librarySongs = new LibrarySongs(newLibrarySongs);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(this.savePath));
            objectOutputStream.writeObject(librarySongs);
            objectOutputStream.flush();
            objectOutputStream.close();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return retval;

    }

    public YoutubeSong getNextSongFromLibrary(YoutubeSong currentSong) throws NoSongFoundException{
        try{
            ArrayList<YoutubeSong> existingSongs=new ArrayList<>(this.retrieveLibrarySongs());
            for(YoutubeSong youtubeSong:existingSongs){
                if(youtubeSong.equals(currentSong))
                    return existingSongs.get(existingSongs.indexOf(youtubeSong)+1);
            }
        }
        catch (Exception ex){
            throw new NoSongFoundException("No next song found.");
        }
        throw new NoSongFoundException("No next song found.");
    }

    public YoutubeSong getPreviousSongFromLibrary(YoutubeSong currentSong) throws NoSongFoundException{
        try{
            ArrayList<YoutubeSong> existingSongs=new ArrayList<>(this.retrieveLibrarySongs());
            for(YoutubeSong youtubeSong:existingSongs){
                if(youtubeSong.equals(currentSong))
                    return existingSongs.get(existingSongs.indexOf(youtubeSong)-1);
            }
        }
        catch (Exception ex){
            throw new NoSongFoundException("No previous song found.");
        }
        throw new NoSongFoundException("No previous song found.");
    }

    private static class LibrarySongs implements Serializable {
        LibrarySongs(ArrayList<LibrarySong> listOfLibrarySongs) {
            setListOfLibrarySongs(listOfLibrarySongs);
        }

        ArrayList<LibrarySong> getListOfLibrarySongs() {
            return listOfLibrarySongs;
        }

        public void setListOfLibrarySongs(ArrayList<LibrarySong> listOfLibrarySongs) {
            this.listOfLibrarySongs = listOfLibrarySongs;
        }

        private ArrayList<LibrarySong> listOfLibrarySongs = new ArrayList<>();
    }


}
