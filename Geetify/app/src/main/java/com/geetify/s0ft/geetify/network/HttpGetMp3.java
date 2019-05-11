package com.geetify.s0ft.geetify.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.geetify.s0ft.geetify.exceptions.CannotCreateFolderOnExternalStorageException;
import com.geetify.s0ft.geetify.exceptions.FunctionExtractionException;
import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.HelperClass;
import com.geetify.s0ft.geetify.helpers.SignatureDecrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

public class HttpGetMp3 extends AsyncTask<String, String, Boolean> {


    public interface MP3DownloadListener {
        void onProgressUpdate(String progressMessage);
        void onDownloadError(String error);
        void onDownloadFinished();
    }

    private final WeakReference<Context> weakContext;

    private MP3DownloadListener mp3DownloadListener;

    public HttpGetMp3(MP3DownloadListener mp3DownloadListener, Context context) {
        this.mp3DownloadListener = mp3DownloadListener;
        this.weakContext = new WeakReference<>(context);
    }

    private String ERROR_MESSAGE="";

    @Override
    protected Boolean doInBackground(String... params) {
        String videoId = params[0];
        String youtubeURL = "https://www.youtube.com/watch?v=" + videoId;


        try {
            publishProgress("Downloading video webpage...");
            URL url = new URL(youtubeURL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int byte_ = inputStream.read();
            while (byte_ != -1) {
                byteBuffer.write(byte_);
                byte_ = inputStream.read();
            }
            String webpageContents = byteBuffer.toString();

            String ytPlayerConfigString = webpageContents.substring(webpageContents.indexOf("ytplayer.config = ") + 18, webpageContents.indexOf("ytplayer.load") - 1);
            JSONObject ytPlayerConfigJSON = new JSONObject(ytPlayerConfigString);
            String ytArgsString = ytPlayerConfigJSON.getString("args");
            JSONObject ytArgsJSON = new JSONObject(ytArgsString);
            String videoTitle = ytArgsJSON.getString("title");
            String player_responseString = ytArgsJSON.getString("player_response");
            JSONObject player_responseJSON = new JSONObject(player_responseString);
            String streamingDataString = player_responseJSON.getString("streamingData");
            JSONObject streamingDataJSON = new JSONObject(streamingDataString);
            JSONArray adaptiveFormatsJSON = new JSONArray();
            try {
                String adaptiveFormatsString = streamingDataJSON.getString("adaptiveFormats");
                adaptiveFormatsJSON = new JSONArray(adaptiveFormatsString);
            } catch (JSONException jsex) {
                //if key "adaptiveFormats" doesn't exist
                String adaptiveFormatsString = ytArgsJSON.getString("adaptive_fmts");
                String[] tokens = adaptiveFormatsString.split(",");
                for (String token : tokens) {
                    JSONObject adaptiveFormat = new JSONObject();
                    String[] parameters = token.split("&");
                    for (String parameter : parameters) {
                        String[] tmp = parameter.split("=");
                        String key = tmp[0];
                        String value = tmp.length == 2 ? tmp[1] : "";
                        switch (key) {
                            case "type":
                                adaptiveFormat.put("mimeType", URLDecoder.decode(value, "UTF-8"));
                                break;
                            case "clen":
                                adaptiveFormat.put("contentLength", value);
                                break;
                            case "url":
                                adaptiveFormat.put("url", URLDecoder.decode(value, "UTF-8"));
                                break;
                            case "s":
                                adaptiveFormat.put("encryptedSig", value);
                                break;
                        }
                    }
                    adaptiveFormatsJSON.put(adaptiveFormat);
                }
            }


            Double shortestContentLength = Double.POSITIVE_INFINITY;
            Integer smallestWebmURLIndex = -1;
            for (int i = 0; i < adaptiveFormatsJSON.length(); i++) {
                JSONObject adaptiveFormatJSON = adaptiveFormatsJSON.getJSONObject(i);
                String mimeType = adaptiveFormatJSON.getString("mimeType");
                Double contentLength = adaptiveFormatJSON.getDouble("contentLength");
                if (mimeType.contains("audio/webm")) {
                    if (contentLength < shortestContentLength) {
                        shortestContentLength = contentLength;
                        smallestWebmURLIndex = i;
                    }
                }
            }

            if(smallestWebmURLIndex==-1){
                this.ERROR_MESSAGE="smallestWebmURLIndex is still -1. Could not get the download URL.";
                return false;
            }

            JSONObject smallestWebmURLJSON = adaptiveFormatsJSON.getJSONObject(smallestWebmURLIndex);
            String smallestWebmURL = smallestWebmURLJSON.getString("url");
            if (smallestWebmURLJSON.has("encryptedSig")) {
                try{
                    String decryptedSignature = SignatureDecrypter.DecryptSignature(AppSettings.getDecryptionFunctionNameFilterRegexPatterns(), smallestWebmURLJSON.getString("encryptedSig"),ytPlayerConfigJSON);
                    smallestWebmURL+="&signature="+decryptedSignature;
                }
                catch (FunctionExtractionException feex){
                    this.ERROR_MESSAGE=feex.getMessage();
                    return false;
                }
            }

            Log.w("YTS", smallestWebmURL);
            publishProgress("Beginning audio download...");

            URL audioUrl = new URL(smallestWebmURL);
            URLConnection audioUrlConnection = audioUrl.openConnection();
            Integer downloadedLength = 0;
            Integer chunksize = 512 * 1024;
            ByteArrayOutputStream audioByteBuffer = new ByteArrayOutputStream();

            while (downloadedLength < shortestContentLength) {
                audioUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
                audioUrlConnection.setRequestProperty("Range", "bytes=" + (downloadedLength) + "-" + (downloadedLength + chunksize - 1));

                BufferedInputStream audioInputStream = new BufferedInputStream(audioUrlConnection.getInputStream());

                int returnedBytesLength = Integer.parseInt(audioUrlConnection.getHeaderField("Content-Length"));


                byte[] buffer = new byte[returnedBytesLength];
                int numBytesRead = 0;
                while (numBytesRead < returnedBytesLength) {
                    numBytesRead += audioInputStream.read(buffer, numBytesRead, returnedBytesLength - numBytesRead);
                }


                audioByteBuffer.write(buffer);
                downloadedLength += returnedBytesLength;

                audioUrlConnection = audioUrl.openConnection();
                Double progressPercentage=   downloadedLength / shortestContentLength * 100;
                publishProgress("Downloading audio : "+String.format("%.2f", progressPercentage) + "%");
            }


            File saveAudioFile = new File(AppSettings.getMP3StoragePath() + HelperClass.getValidFilename(videoTitle) + ".webm");
            FileOutputStream fileOutputStream = new FileOutputStream(saveAudioFile);
            audioByteBuffer.writeTo(fileOutputStream);
            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            this.ERROR_MESSAGE=e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            this.ERROR_MESSAGE=e.getMessage();
        } catch (JSONException e) {
            e.printStackTrace();
            this.ERROR_MESSAGE=e.getMessage();
        } catch (CannotCreateFolderOnExternalStorageException e) {
            e.printStackTrace();
            this.ERROR_MESSAGE=e.getMessage();
        }


        return false;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        this.mp3DownloadListener.onProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean downloadSuccess) {
        if(downloadSuccess){
            this.mp3DownloadListener.onDownloadFinished();
        }else{
            this.mp3DownloadListener.onDownloadError(ERROR_MESSAGE);
        }

    }
}
