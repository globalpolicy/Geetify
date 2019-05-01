package com.geetify.s0ft.geetify.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

public class GoogleTokenHelper {
    /*
    *   Once you have detected that the user has revoked the permission you can ask the user the grant permission again.

        To detect that the grant has been revoked: Provided that you had authorization before,

        Making an API call using a revoked access_token will result in a response with status code 401. Like this

        {
         "error": {
          "errors": [
           {
            "domain": "global",
            "reason": "authError",
            "message": "Invalid Credentials",
            "locationType": "header",
            "location": "Authorization"
           }
          ],
          "code": 401,
          "message": "Invalid Credentials"
         }
        }

        Attempting to refresh a token after the revocation will result in a response with a 400 status code and an invalid_grant message. Just as specified in the RFC 6749, Section 5.2

        invalid_grant The provided authorization grant (e.g., authorization code, resource owner credentials) or refresh token is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client.

        Here is an example of such response:

        {
        "error" : "invalid_grant"
        }
    * */

    public static boolean doesTokenExist(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString("access_token", "");
        if (accessToken == "") {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasTokenExpired(Context context) {
        //check if access token has expired
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long currentTimeStamp = new Date().getTime();
        long accessTokenValidTill = sharedPreferences.getLong("token_generated_timestamp", 0) + sharedPreferences.getLong("expires_in", 0) * 1000;
        if (currentTimeStamp > accessTokenValidTill) {
            //access token has expired
            return true;
        }
        else{
            return false;
        }
    }
}
