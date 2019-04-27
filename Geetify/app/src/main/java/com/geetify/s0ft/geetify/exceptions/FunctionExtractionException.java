package com.geetify.s0ft.geetify.exceptions;


import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.HelperClass;

import java.util.Calendar;

public class FunctionExtractionException extends Exception {
    public FunctionExtractionException(String message, String scriptText) {
        super(message);
        HelperClass.WriteToLog(message);
        try {
            HelperClass.WriteFile(AppSettings.getMP3StoragePath() + Calendar.getInstance().getTime().toString() + ".js", scriptText.getBytes(), false);
        } catch (CannotCreateFolderOnExternalStorageException e) {
            e.printStackTrace();
        }
    }

    public FunctionExtractionException(String message) {
        super(message);
        HelperClass.WriteToLog(message);

    }
}
