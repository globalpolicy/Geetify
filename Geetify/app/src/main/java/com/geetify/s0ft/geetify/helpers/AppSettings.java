package com.geetify.s0ft.geetify.helpers;

import android.content.Context;
import android.os.Environment;

import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.ExternalStorageNotFoundException;

import java.io.File;

/**
 * Created by s0ft on 2/27/2018.
 */

public class AppSettings {

    private static String libraryFileName = "mylibrary.bin";

    public static String getAppDataStoragePath(Context context) {
        return context.getFilesDir().getPath() + "/";
    }

    public static File getAppDataStoragePath_File(Context context) {
        return context.getFilesDir();
    }

    public static String getLibraryFile(Context context) {
        return getAppDataStoragePath(context) + libraryFileName;
    }


    public static String getMP3StoragePath() throws ExternalStorageNotFoundException, CannotCreateFolderOnExternalStorageException {
        return getMP3StoragePath_File().getAbsolutePath()+"/";
    }

    public static File getMP3StoragePath_File() throws ExternalStorageNotFoundException, CannotCreateFolderOnExternalStorageException {
        File retval;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String externalDirectoryRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
            File geetifyMP3StorageCache = new File(externalDirectoryRoot + "/"+ "GeetifyMP3Cache");
            geetifyMP3StorageCache.mkdirs();
            if (geetifyMP3StorageCache.exists()) {
                retval = geetifyMP3StorageCache;
            }else
                throw new CannotCreateFolderOnExternalStorageException("Cannot create cache folder on external storage.");
        }
        else
            throw new ExternalStorageNotFoundException("External storage not found.");
        return retval;
    }

}
