package com.geetify.s0ft.geetify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.geetify.s0ft.geetify.helpers.AppSettings;
import com.geetify.s0ft.geetify.helpers.GoogleTokenHelper;
import com.geetify.s0ft.geetify.helpers.HelperClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

public class TokenActivity extends AppCompatActivity {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        if (getIntent().getData() == null) {
            //if not called after redirection from browser
            if(!getIntent().getBooleanExtra("must_reauth",false)){
                if (GoogleTokenHelper.doesTokenExist(this)) {
                    gotoMainActivity();
                }
            }

        }

    }

    private void gotoMainActivity() {
        Intent newActivityIntent = new Intent(this, MainActivity.class);
        newActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newActivityIntent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        intent.setData(null);
        if (dataUri == null) {
            return;
        }

        try {
            String authCode = dataUri.getQueryParameter("code");

            Uri accessTokenUri = Uri.parse("https://www.googleapis.com/oauth2/v4/token");

            HashMap<String, String> postParameters = new HashMap<>();
            postParameters.put("code", authCode);
            postParameters.put("client_id", AppSettings.getClientId());
            postParameters.put("redirect_uri", AppSettings.getRedirectUri());
            postParameters.put("grant_type", "authorization_code");

            Button getAccessTokenBtn = findViewById(R.id.button_get_token);
            getAccessTokenBtn.setEnabled(false);
            ProgressBar progressBar = findViewById(R.id.accesstokenProgressBar);
            progressBar.setVisibility(View.VISIBLE);

            new GetAccessToken().execute(new URL(accessTokenUri.toString()), postParameters, getAccessTokenBtn, progressBar, this);

        } catch (NullPointerException npex) {
            Toast.makeText(this, "Authcode not received!", Toast.LENGTH_SHORT).show();
        } catch (MalformedURLException mfex) {
            HelperClass.WriteToLog("MalFormedURLException while requesting access code.");
            Toast.makeText(this, "Something's wrong with the access code request URL.", Toast.LENGTH_SHORT).show();
        }

    }


    public void onButtonGetTokenClicked(View view) {
        Uri authUri = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth")
                .buildUpon()
                .appendQueryParameter("client_id", AppSettings.getClientId())
                .appendQueryParameter("redirect_uri", AppSettings.getRedirectUri())
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", "https://www.googleapis.com/auth/youtube")
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW, authUri);
        startActivity(intent);
    }

    private static class GetAccessToken extends AsyncTask<Object, Integer, String> {

        WeakReference<Button> getAccessTokenButtonWeakReference;
        WeakReference<ProgressBar> accessTokenRetrievalProgressbarWeakReference;
        WeakReference<Activity> activityWeakReference;
        String errorString = "";

        @Override
        protected String doInBackground(Object... params) {
            HashMap<String, String> postParameters = (HashMap<String, String>) params[1];

            this.getAccessTokenButtonWeakReference = new WeakReference<>((Button) params[2]);

            this.accessTokenRetrievalProgressbarWeakReference = new WeakReference<>((ProgressBar) params[3]);

            this.activityWeakReference = new WeakReference<>((Activity) params[4]);

            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) ((URL) params[0]).openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                String requestBody = "code=" + postParameters.get("code") + "&" +
                        "client_id=" + postParameters.get("client_id") + "&" +
                        "redirect_uri=" + postParameters.get("redirect_uri") + "&" +
                        "grant_type=" + postParameters.get("grant_type");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(requestBody.getBytes());

                if (httpURLConnection.getResponseCode() == 200) {
                    InputStream inputStream = httpURLConnection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String responseContent = "";
                    String line;
                    do {
                        line = bufferedReader.readLine();
                        if (line != null) {
                            responseContent += line + "\n";
                        }
                    } while (line != null);
                    return responseContent;

                } else {
                    this.errorString = "Response code " + httpURLConnection.getResponseCode() + " while requesting access token.";
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                this.errorString = "IOException while requesting access token.";
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Button getAccessTokenButton = getAccessTokenButtonWeakReference.get();
            ProgressBar progressBar = accessTokenRetrievalProgressbarWeakReference.get();
            Activity activity = activityWeakReference.get();

            if (progressBar != null) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            if (getAccessTokenButton != null) {
                getAccessTokenButton.setEnabled(true);
            }

            if (activity != null) {
                if (result == null) {
                    Toast.makeText(activity, this.errorString, Toast.LENGTH_SHORT).show();
                    return;
                }


                try {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    JSONObject resultJson = new JSONObject(result);
                    String accessToken = resultJson.getString("access_token");
                    long validForSeconds = resultJson.getLong("expires_in");
                    String refreshToken = resultJson.getString("refresh_token");

                    long timeStamp = new Date().getTime();
                    editor.putString("access_token", accessToken);
                    editor.putString("refresh_token", refreshToken);
                    editor.putLong("expires_in", validForSeconds);
                    editor.putLong("token_generated_timestamp", timeStamp);
                    editor.apply();
                    ((TokenActivity)activity).gotoMainActivity();
                } catch (JSONException jsex) {
                    Toast.makeText(activity, "Response to access code request not in JSON format!", Toast.LENGTH_SHORT).show();
                    HelperClass.WriteToLog("Response to access code request not in JSON format!\n" + result);
                }
            }

        }
    }
}
