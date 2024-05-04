package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** @noinspection BlockingMethodInNonBlockingContext*/
public class HTTPConnector {

    private final SharedPreferences mPreferences;

    public HTTPConnector(SharedPreferences preferences) {
        this.mPreferences = preferences;
    }

    private String getServerAddress() {
        return "http://"+mPreferences.getString("ip_address", "localhost")+":8880/api/";
    }

    public String getData(String endpoint) {
        // Fetch data from the API in the background.
        StringBuilder result = new StringBuilder();
        try {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(getServerAddress() + endpoint);
                //open a URL coonnection
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    result.append((char) data);
                    data = isw.read();
                }

                // return the data to onPostExecute method
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
        return result.toString();
    }

    public Bitmap getImage(String endpoint) {
        // Fetch data from the API in the background.
        Bitmap result;
        try {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(getServerAddress() + endpoint);
                //open a URL coonnection
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();

                result = BitmapFactory.decodeStream(in);

                // return the data to onPostExecute method
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public void postData(String endpoint) {
        StringBuilder result = new StringBuilder();
        try {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(getServerAddress() + endpoint);
                //open a URL coonnection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    result.append((char) data);
                    data = isw.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
