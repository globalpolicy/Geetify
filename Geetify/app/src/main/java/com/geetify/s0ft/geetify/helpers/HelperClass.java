package com.geetify.s0ft.geetify.helpers;

import android.os.Environment;

import com.geetify.s0ft.geetify.R;
import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.FunctionExtractionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by s0ft on 2/8/2018.
 */

public class HelperClass {

    public static String getValidFilename(String inputString) {
        String retval = inputString;
        final String[] reservedChars = {"|", "\\", "?", "*", "<", "\"", ":", ">", "/", "(", ")"};
        for (String reservedChar : reservedChars) {
            retval = retval.replace(reservedChar, "");
        }
        return retval;
    }

    public static String convertMillisecondsToMinutesSeconds(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        int remseconds = seconds - minutes * 60;
        return Integer.toString(minutes) + ":" + Integer.toString(remseconds);
    }

    public static boolean moveFile(File src, File dest) throws IOException {
        boolean success = false;
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dest).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) inChannel.close();
            outChannel.close();
        }
        if (src.delete())
            success = true;
        return success;
    }


    public static String DownloadFromUrl(String URL) throws IOException {
        int chunkSize = 8 * 1024;

        java.net.URL audioUrl = new URL(URL);
        URLConnection urlConnection = audioUrl.openConnection();
        ByteArrayOutputStream downloadByteBuffer = new ByteArrayOutputStream();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0");
        BufferedInputStream downloadInputStream = new BufferedInputStream(urlConnection.getInputStream());

        byte[] buffer = new byte[chunkSize];
        int numBytesRead = 0;
        while (numBytesRead != -1) {
            numBytesRead = downloadInputStream.read(buffer, 0, chunkSize);
            downloadByteBuffer.write(buffer, 0, numBytesRead == -1 ? 0 : numBytesRead);
        }

        return downloadByteBuffer.toString();
    }

    public static void WriteToLog(String message) {
        try {
            String logFilePath = AppSettings.getMP3StoragePath() + "Logs.txt";
            HelperClass.WriteFile(logFilePath, (Calendar.getInstance().getTime().toString() + "\n" + message + "\n\n").getBytes(), true);
        } catch (CannotCreateFolderOnExternalStorageException e) {
            e.printStackTrace();
        }
    }

    public static void WriteFile(String filePath, byte[] contents, boolean append) {
        File file = new File(filePath);
        FileOutputStream fileOutputStream;
        try {
            if (file.createNewFile()) {
                fileOutputStream = new FileOutputStream(file);
            } else {
                if (append) {
                    fileOutputStream = new FileOutputStream(file, true);
                } else {
                    if (file.delete()) {
                        fileOutputStream = new FileOutputStream(file, true);
                    } else {
                        return;
                    }
                }
            }
            fileOutputStream.write(contents);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
