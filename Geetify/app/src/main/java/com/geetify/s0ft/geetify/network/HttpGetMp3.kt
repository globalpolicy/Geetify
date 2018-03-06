package com.geetify.s0ft.geetify.network

import android.net.Uri
import android.os.AsyncTask
import org.json.JSONObject
import java.io.FileOutputStream
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.net.URL

class HttpGetMp3(private val mP3DownloadListener: MP3DownloadListener) : AsyncTask<String, Int, Int>() {

    private val APIUrl = "https://www.yt-mp3-api.com/freedsound.php"

    interface MP3DownloadListener{
        fun progressReporterMP3(typeOfUpdate:Int,statusUpdate:Int)
        fun downloadFinished(mp3TotalBytesDownloaded:Int)
    }

    //returns the size of the MP3
    override fun doInBackground(vararg params: String): Int {
        val videoId = params[0]
        val downloadToMP3FilePath=params[1]
        var bytesWritten=0

        val GETQuery: String = with(Uri.Builder()) {
            appendQueryParameter("v", videoId)
            appendQueryParameter("f", "ogg")
        }.build().encodedQuery
        val finalURL = APIUrl + "?" + GETQuery


        var responseState = ""
        var downloadLink=""
        do {
            try {
                with(URL(finalURL).openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    doInput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("Referer", "https://freedsound.com/?q=" + videoId)
                    connect()

                    val responseString = inputStream.reader().use { it.readText() }
                    val responseJsonObject = JSONObject(responseString)
                    responseState = responseJsonObject.getString("state")
                    if (responseState.equals("processing")) {
                        val percentage = responseJsonObject.getString("percentage")
                        try {
                            publishProgress(0,percentage.toInt())
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    } else if(responseState.equals("ok"))
                    {
                        downloadLink=responseJsonObject.getString("download")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } while (!responseState.equals("ok") && !isCancelled)

        //coming here, either the responseState is "ok" or the task has been cancelled
        try {
            FileOutputStream(downloadToMP3FilePath, false).use {
                val fileoutputstream = it

                if (responseState.equals("ok") && !downloadLink.equals("") && !isCancelled) {
                    with(URL(downloadLink).openConnection() as HttpURLConnection) {
                        requestMethod = "GET"
                        doInput = true
                        connectTimeout = 5000
                        readTimeout = 5000
                        connect()

                        val buffer = ByteArray(1024)
                        inputStream.use {
                            var numBytesRead: Int? = null
                            while (numBytesRead != -1) {
                                numBytesRead = it.read(buffer)
                                if (numBytesRead != -1) {
                                    fileoutputstream.write(buffer, 0, numBytesRead)
                                    bytesWritten += numBytesRead
                                }
                                publishProgress(1, bytesWritten)
                            }
                        }


                    }
                }
            }
        } catch(ex:Exception){
            ex.printStackTrace()
        }
        return bytesWritten
    }

    override fun onProgressUpdate(vararg values: Int?) {
        this.mP3DownloadListener.progressReporterMP3(values[0]!!,values[1]!!)
    }

    override fun onPostExecute(result: Int) {
        this.mP3DownloadListener.downloadFinished(result)
    }
}