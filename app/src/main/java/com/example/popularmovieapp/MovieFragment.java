package com.example.popularmovieapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovieapp.calback.ActionCallback;
import com.example.popularmovieapp.database.DataProvider;
import com.example.popularmovieapp.database.DataSource;
import com.example.popularmovieapp.entities.Movie;
import com.example.popularmovieapp.json_data.Movies_json_Task;
import com.example.popularmovieapp.utils.Connectivity;
import com.example.popularmovieapp.utils.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 12/12/15.
 */

public class MovieFragment extends Fragment implements Movies_json_Task.Callback {

    private String SORT_SETTING_KEY = "sort_setting";
    private final String FAVOURITES = "fav";
    private final String POPULARITY_DESC = "popular";
    private final String RATING_DESC = "top_rated";
    private final String MOVIES_KEY = "movies";
    private GridView mGridView;
    private TextView progressTV;
    private ProgressBar progressBar;
    private LinearLayout progressLayout;
    private MovieGridAdapter mMovieGridAdapter;
    private String sortBy = POPULARITY_DESC;
    private ArrayList<Movie> moviesList = null;
    private Settings settings;
    Movies_json_Task moviesJsonTask;

    String TAG = "MovieFragment";

    ActionCallback actionCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        settings = new Settings(getActivity());
        Log.d(TAG, "Create");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "On Create View");
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        actionCallback = (ActionCallback) getActivity();
        mGridView = (GridView) view.findViewById(R.id.gridview_movies);

        progressLayout = (LinearLayout) view.findViewById(R.id.progress);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressTV = (TextView) view.findViewById(R.id.progressTV);

        try {
            checkInstanceState(savedInstanceState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private void initAdapter() {
        mMovieGridAdapter = new MovieGridAdapter(getActivity());
        mGridView.setAdapter(mMovieGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovieGridAdapter.getItem(position);
                if (actionCallback != null)
                    actionCallback.actionCallback(movie);
            }
        });
    }

    private void checkInstanceState(Bundle savedInstanceState) throws JSONException {
        try{  if (savedInstanceState != null) {
            Log.d(TAG, "Instance was saved..");
            progressLayout.setVisibility(View.GONE);

            if (savedInstanceState.containsKey(MOVIES_KEY)) {
                moviesList = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
                for (Movie movie : moviesList) {
                    mMovieGridAdapter.add(movie);
                }
            } else {
                initAdapter();
                Log.d(TAG, "Update First Time..");
                updateMovies(sortBy);
            }
        } else {
            initAdapter();
            Log.d(TAG, "Update First Time..");
            sortBy = settings.getSortBy();
            if (!sortBy.equalsIgnoreCase(FAVOURITES))
                updateMovies(sortBy);
            else {
                loadFavourites();
            }
        }
    }catch (Exception e){
            initAdapter();
            Log.d(TAG, "Update First Time..");
            sortBy = settings.getSortBy();
            if (!sortBy.equalsIgnoreCase(FAVOURITES))
                updateMovies(sortBy);
            else {
                loadFavourites();
            }

        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        try {
            checkInstanceState(savedInstanceState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (moviesList != null) {
            outState.putParcelableArrayList(MOVIES_KEY, moviesList);
        }
        Log.d(TAG, "On Save Instance..");
        super.onSaveInstanceState(outState);
    }

    private void updateMovies(String sort_by) {
        Log.d(TAG, "Loading Movies in order to " + sort_by);
        if (Connectivity.isConnected(getActivity())) {
            moviesJsonTask = new Movies_json_Task(MovieFragment.this);
            moviesJsonTask.set_Sort(sortBy);
            moviesJsonTask.execute(sortBy);
        } else {
            progressBar.setVisibility(View.GONE);
            progressTV.setText("Check Internet Connection");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);
        MenuItem action_sort_by_popularity = menu.findItem(R.id.action_sort_by_popularity);
        MenuItem action_sort_by_rating = menu.findItem(R.id.action_sort_by_rating);
        MenuItem action_show_favourites = menu.findItem(R.id.action_show_favourites);
        sortBy = settings.getSortBy();
        if (sortBy.equalsIgnoreCase(RATING_DESC))
            action_sort_by_rating.setChecked(true);
        else if (sortBy.equalsIgnoreCase(POPULARITY_DESC))
            action_sort_by_popularity.setChecked(true);
        else
            action_show_favourites.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (moviesJsonTask != null)
            moviesJsonTask.cancel(true);
        if (item.isChecked())
            return true;
        item.setChecked(true);
        switch (id) {
            case R.id.action_sort_by_popularity:
                sortBy = POPULARITY_DESC;
                updateMovies(sortBy);
                settings.setSortBy(sortBy);
                return true;
            case R.id.action_sort_by_rating:
                sortBy = RATING_DESC;
                settings.setSortBy(sortBy);
                updateMovies(sortBy);
                return true;
            case R.id.action_show_favourites:
                sortBy = FAVOURITES;
                settings.setSortBy(sortBy);
                try {
                    loadFavourites();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadFavourites() throws JSONException {
        // Retrieve student records
        String URL = "content://com.example.popularmovieapp.database.DataProvider";
        Uri movies = Uri.parse(URL);
        Cursor c = getActivity().managedQuery(movies, null, null, null, DataProvider.MOVIE_JSONOBJECT);
        List<Movie> movieList = new ArrayList<>();
        if (c.moveToFirst()) {
            do{
                String movieObject = c.getString(2);
                JSONObject jsonObject = new JSONObject(movieObject);
                Movie movie = new Movie(jsonObject);
                movieList.add(movie);
            } while (c.moveToNext());
        }

        DataSource dataSource = new DataSource(getActivity());
        dataSource.open();
        moviesList = (ArrayList<Movie>) movieList;

        Log.d(TAG, "Favourites Size " + moviesList.size());
        if (moviesList == null || moviesList.isEmpty()) {
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            progressTV.setText("No Favourites..");
            mGridView.setVisibility(View.GONE);
        } else {
            progressLayout.setVisibility(View.GONE);
            mMovieGridAdapter.clear();
            for (Movie movie : moviesList)
                mMovieGridAdapter.add(movie);
            mMovieGridAdapter.notifyDataSetChanged();
        }
        dataSource.close();
    }

    @Override
    public void preExecute() {
        mGridView.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressTV.setVisibility(View.VISIBLE);
        progressTV.setText("Loading wait...");
    }

    @Override
    public void postExecute(List<Movie> movieList) {
        if (mMovieGridAdapter != null) {
            mMovieGridAdapter.clear();
            if (movieList != null)
                for (Movie movie : movieList) {
                    mMovieGridAdapter.add(movie);
                }
        }
        mGridView.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        moviesList = new ArrayList<>();
        if (movieList != null)
            moviesList.addAll(movieList);
        if (MainActivity.frameLayout != null)
            if (actionCallback != null && !moviesList.isEmpty())
                actionCallback.actionCallback(moviesList.get(0));
    }

}