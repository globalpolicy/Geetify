package com.geetify.s0ft.geetify.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import com.geetify.s0ft.geetify.datamodels.YoutubeSong
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HttpGetYoutubeSearch(private val youtubeSongsDownloadListener: YoutubeSongsDownloadStatusListener, private val numOfVidsToSearch: String) : AsyncTask<String, Int, ArrayList<YoutubeSong>>() {

    private val apiURLString = "https://www.googleapis.com/youtube/v3/search"
    private val apiKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

    interface YoutubeSongsDownloadStatusListener {
        fun progressReporter(percent: Int)
        fun downloadFinished(youtubeSongs: ArrayList<YoutubeSong>)
    }

    override fun doInBackground(vararg params: String): ArrayList<YoutubeSong> {
        val youtubeSongs = retrieveYoutubeSnippets(params)//retrieves a list of relevant YoutubeSong objects
        return youtubeSongs
    }

    fun retrieveYoutubeSnippets(params: Array<out String>): ArrayList<YoutubeSong> {
        val keyWord: String = params[0]
        val retval = ArrayList<YoutubeSong>()

        val GETQuery: String = with(Uri.Builder()) {
            appendQueryParameter("key", apiKey)
            appendQueryParameter("part", "snippet")
            appendQueryParameter("type", "video")
            appendQueryParameter("q", keyWord)
            appendQueryParameter("maxResults", (if (numOfVidsToSearch.toInt() > 50) "50" else if (numOfVidsToSearch.toInt() < 1) "1" else numOfVidsToSearch))

        }.build().encodedQuery
        val finalURL = apiURLString + "?" + GETQuery

        try {
            with(URL(finalURL).openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                doInput = true
                connectTimeout = 5000
                readTimeout = 5000
                connect()

                val responseString = inputStream.reader().use { it.readText() }
                val responseJsonObject = JSONObject(responseString)
                val responseItemsJsonArray = JSONArray(responseJsonObject.getJSONArray("items").toString())
                for (i in 0 until (responseItemsJsonArray.length())) {
                    val responseItemJsonObject = responseItemsJsonArray.getJSONObject(i)
                    val videoId = responseItemJsonObject.getJSONObject("id").getString("videoId")
                    val publishedAt = responseItemJsonObject.getJSONObject("snippet").getString("publishedAt")
                    val title = responseItemJsonObject.getJSONObject("snippet").getString("title")
                    val description = responseItemJsonObject.getJSONObject("snippet").getString("description")
                    val hqThumbnailUrl = responseItemJsonObject.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("high").getString("url")
                    val hqThumbnailBitmap = downloadBitmap(hqThumbnailUrl)
                    retval.add(YoutubeSong(title, description, videoId, hqThumbnailUrl, publishedAt, hqThumbnailBitmap))
                    publishProgress(i, responseItemsJsonArray.length())
                }

            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return retval
    }

    fun downloadBitmap(thumbnailUrl: String): Bitmap {
        var retval: Bitmap? = null

        with(URL(thumbnailUrl).openConnection() as HttpURLConnection) {
            doInput = true
            connectTimeout = 5000
            readTimeout = 5000
            connect()
            retval = BitmapFactory.decodeStream(inputStream)
        }

        return retval!!
    }

    override fun onPostExecute(result: ArrayList<YoutubeSong>) {
        this.youtubeSongsDownloadListener.downloadFinished(result)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        this.youtubeSongsDownloadListener.progressReporter((values[0]!! + 1) * 100 / (values[1]!!))
    }
}