package com.example.popularmovieapp.json_data;

import android.os.AsyncTask;
import android.util.Log;

import com.example.popularmovieapp.entities.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by amedM on 12/15/2015.
 */
public class Movies_json_Task extends AsyncTask<String, Void, List<Movie>> {
    Callback callback;
    private final String TAG = "Movies_json_Task";
    private String sort_type;

    public interface Callback {
        public void preExecute();

        public void postExecute(List<Movie> movieList);
    }

    public Movies_json_Task(Callback callback) {
        this.callback = callback;
    }

    private final String LOG_TAG = Movies_json_Task.class.getSimpleName();
    public void set_Sort(String sort_typ){
        this.sort_type=sort_typ;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (this.callback != null) {
            callback.preExecute();
        }

    }

    @Override
    protected List<Movie> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        try {
            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?sort_by=";
            final String api_key="c11f6d1d1d3ee47740a208568be1bdc9";
            final String full_url=BASE_URL+sort_type+"&api_key="+api_key;
            URL url = new URL(full_url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect(); //check connection to network
            }
            if (reader != null) {
                try {
                    reader.close();   //close read
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        try {
            return getMoviesDataFromJson(jsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private List<Movie> getMoviesDataFromJson(String jsonStr) throws JSONException {
        if (jsonStr.isEmpty() || jsonStr == null)                     //if json is empty will return null and stop operation
            return null;
        JSONObject movieJson = new JSONObject(jsonStr);
        JSONArray movieArray = movieJson.getJSONArray("results");
        List<Movie> results = new ArrayList<>();
        if (movieArray != null && !jsonStr.isEmpty())
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                Movie movieModel = new Movie(movie);
                results.add(movieModel);
            }
        return results;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        // call back to return movies data to MovieFragment
        if (callback != null) {
            callback.postExecute(movies);
        }
    }
}