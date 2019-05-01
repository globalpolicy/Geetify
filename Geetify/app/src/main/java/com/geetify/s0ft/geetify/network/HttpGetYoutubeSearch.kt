package com.geetify.s0ft.geetify.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.geetify.s0ft.geetify.datamodels.YoutubeSong
import com.geetify.s0ft.geetify.helpers.AppSettings
import com.geetify.s0ft.geetify.helpers.GoogleTokenHelper
import com.geetify.s0ft.geetify.helpers.StringUnescaper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class HttpGetYoutubeSearch(private val activityContext: WeakReference<Context>, private val youtubeSongsDownloadListener: YoutubeSongsDownloadStatusListener, private val numOfVidsToSearch: String) : AsyncTask<String, Int, ArrayList<YoutubeSong>>() {

    private val apiURLString = "https://www.googleapis.com/youtube/v3/search"
    private var tokenError = false

    interface YoutubeSongsDownloadStatusListener {
        fun progressReporter(percent: Int)
        fun downloadFinished(youtubeSongs: ArrayList<YoutubeSong>)
        fun apiTokenInvalidError()//used to signal revoked access
    }

    override fun doInBackground(vararg params: String): ArrayList<YoutubeSong> {
        if (activityContext.get() != null) {
            val youtubeSongs = retrieveYoutubeSnippets(params)//retrieves a list of relevant YoutubeSong objects
            return youtubeSongs
        } else {
            return ArrayList()
        }
    }


    fun retrieveYoutubeSnippets(params: Array<out String>): ArrayList<YoutubeSong> {
        if (GoogleTokenHelper.hasTokenExpired(activityContext.get())) {
            //if token has expired, request a new token using the refresh token
            if (!refreshAccessToken()) {
                //access token couldn't be refreshed. ask the user to re-do the OAuth2 permissions
                tokenError = true
                return ArrayList()
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityContext.get())
        val accessToken = sharedPreferences.getString("access_token", "")
        if (accessToken.equals("")) {
            tokenError = true
            return ArrayList()
        }

        val keyWord: String = params[0]
        val retval = ArrayList<YoutubeSong>()

        val GETQuery: String = with(Uri.Builder()) {
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
                setRequestProperty("Authorization", "Bearer ${accessToken}")
                val responseCode = responseCode
                if (responseCode == 401) {
                    //authorization has been revoked by the user
                    tokenError = true
                    return ArrayList()
                }

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
                    retval.add(YoutubeSong(StringUnescaper.unescapeHtml3(title), description, videoId, hqThumbnailUrl, publishedAt, hqThumbnailBitmap))
                    publishProgress(i, responseItemsJsonArray.length())
                }

            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return retval
    }

    fun refreshAccessToken(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activityContext.get())
        val refreshToken=sharedPreferences.getString("refresh_token","")
        if(refreshToken.equals("")){
            return false
        }

        with(URL("https://www.googleapis.com/oauth2/v4/token").openConnection() as HttpURLConnection){
            doOutput=true
            requestMethod="POST"
            val postContent="refresh_token=${refreshToken}&client_id=${AppSettings.getClientId()}&grant_type=refresh_token"
            outputStream.write(postContent.toByteArray())

            if(responseCode!=200){
                //authorization has been revoked
                return false
            }

            val responseContent=inputStream.bufferedReader().readText()
            try{
                val responseJson=JSONObject(responseContent)
                val accessToken=responseJson.getString("access_token")
                val expiresIn=responseJson.getLong("expires_in")

                val preferenceEditor=sharedPreferences.edit()
                preferenceEditor.putString("access_token",accessToken)
                preferenceEditor.putLong("expires_in",expiresIn)
                preferenceEditor.apply()
                return true
            }catch(jsex:JSONException){
                return false
            }
        }
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
        if (!tokenError) {
            this.youtubeSongsDownloadListener.downloadFinished(result)
        } else {
            this.youtubeSongsDownloadListener.apiTokenInvalidError()
        }

    }

    override fun onProgressUpdate(vararg values: Int?) {
        this.youtubeSongsDownloadListener.progressReporter((values[0]!! + 1) * 100 / (values[1]!!))
    }
}