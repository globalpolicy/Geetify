package com.geetify.s0ft.geetify.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.ExternalStorageNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by s0ft on 2/27/2018.
 */

public class AppSettings {

    private static String libraryFileName = "mylibrary.bin";
    public static final int WRITE_EXTERNAL_STORAGE_CODE = 0;

    public static String getAppDataStoragePath(Context context) {
        return context.getFilesDir().getPath() + "/";
    }

    public static File getAppDataStoragePath_File(Context context) {
        return context.getFilesDir();
    }

    public static String getLibraryFile(Context context) {
        return getAppDataStoragePath(context) + libraryFileName;
    }


    public static String getMP3StoragePath() throws CannotCreateFolderOnExternalStorageException {
        File retval = null;
        String saveToFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/GeetifyAudio";
        File saveToFolderPathFile = new File(saveToFolderPath);

        if (!saveToFolderPathFile.exists()) {
            boolean tmp = saveToFolderPathFile.mkdirs();
            if (tmp && saveToFolderPathFile.exists()) {
                retval = saveToFolderPathFile;
            } else {
                throw new CannotCreateFolderOnExternalStorageException("Cannot create Geetify Audio folder");

            }
        } else {
            retval = saveToFolderPathFile;
        }

        return retval.getAbsolutePath() + "/";
    }

    //Thanks to http://www.regexplanet.com/advanced/java/index.html for providing properly formatted Java strings of regex patterns.
    //Lack of literal strings in Java is painful
    public static String[] getDecryptionFunctionNameFilterRegexPatterns() {
        return new String[]{
                "([\"\\'])signature\\1\\s*,\\s*([a-zA-Z0-9$]+)\\(",
                "\\.sig\\|\\|([a-zA-Z0-9$]+)\\(",
                "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*c\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?([a-zA-Z0-9$]+)\\(",
                "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?\\s*([a-zA-Z0-9$]+)\\(",
                "\\bc\\s*&&\\s*d\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*([a-zA-Z0-9$]+)\\("};
    }

    public static String getDecryptionFunctionDefinitionFilterRegexPattern(String functionName) {
        return ("(?x) (?:function\\s+%s|[{;,]\\s*%s\\s*=\\s*function|var\\s+%s\\s*=\\s*function)\\s*" +
                "\\(([^)]*)\\)\\s*" +
                "\\{([^}]+)\\}").replace("%s", Pattern.quote(functionName));
    }

}
