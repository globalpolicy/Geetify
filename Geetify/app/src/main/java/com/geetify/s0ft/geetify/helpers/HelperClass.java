package com.geetify.s0ft.geetify.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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

}
