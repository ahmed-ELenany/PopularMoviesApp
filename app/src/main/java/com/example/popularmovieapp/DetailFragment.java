package com.example.popularmovieapp;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularmovieapp.database.DBHelper;
import com.example.popularmovieapp.database.DataProvider;
import com.example.popularmovieapp.database.DataSource;
import com.example.popularmovieapp.entities.Movie;
import com.example.popularmovieapp.entities.Review;
import com.example.popularmovieapp.entities.Trailer;
import com.example.popularmovieapp.json_data.Reviews_json_Task;
import com.example.popularmovieapp.json_data.Trailers_json_Task;
import com.example.popularmovieapp.utils.Connectivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;

/**
 * Created by ahmed on 12/12/15.
 */

public class DetailFragment extends Fragment implements Trailers_json_Task.TrailersCallback, Reviews_json_Task.ReviewCallback {

    public static final String MOVIE_DATA = "DETAIL_MOVIE";
    public static final String TAG = "Detail Fragment";
    List<Trailer> trailerList;
    private Movie movie;
    private ImageView mImageView;
    private TextView mTitleView, mOverviewView, releaseDateTV;
    private TextView voteAverageView;
    ListView trailersListV;
    TextView reviewText;
    ImageButton favBtn;
    private ShareActionProvider mShareActionProvider;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        Bundle arguments = getArguments();
        if (arguments == null)
            return;

        movie = arguments.getParcelable(DetailFragment.MOVIE_DATA);

        trailersListV = (ListView) rootView.findViewById(R.id.trailersListV);
        reviewText =(TextView)rootView.findViewById(R.id.review);

        mImageView = (ImageView) rootView.findViewById(R.id.moviePoster);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mOverviewView = (TextView) rootView.findViewById(R.id.overview);
        releaseDateTV = (TextView) rootView.findViewById(R.id.date);
        voteAverageView = (TextView) rootView.findViewById(R.id.vote_average);

        favBtn = (ImageButton) rootView.findViewById(R.id.favBtn);
        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFavorite(movie.getId()))
                    fav();
                else {
                    deleteMovie();
                }
            }
        });
        trailersListV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startPlayingIntent(trailerList.get(position));
            }
        });
        if (isFavorite(movie.getId())) {
            favBtn.setImageResource(R.mipmap.ic_remove_favourites);
        }

        if (Connectivity.isConnected(getActivity()))
            new Trailers_json_Task(DetailFragment.this).execute(movie.getId() + "");
        new Reviews_json_Task(DetailFragment.this).execute(movie.getId() + "");
        try {
            String image_url = "http://image.tmdb.org/t/p/w342" + movie.getImage2();
            Picasso.with(getActivity()).load(image_url).into(mImageView);
            mTitleView.setText(movie.getTitle());
            mOverviewView.setText(movie.getOverview());
            String movie_date = movie.getDate();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = DateUtils.formatDateTime(getActivity(),
                    formatter.parse(movie_date).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
            releaseDateTV.setText(date);
            voteAverageView.setText(Integer.toString(movie.getRating()));
        } catch (Exception e) {
            Log.e(TAG, "Error " + e);
        }
    }

    @Override
    public void preExecuteTrailers() {

    }

    @Override
    public void preExecuteReviews() {

    }

    @Override
    public void postExecuteReviews(List<Review> reviewList) {
        try {
           String all_review="";
            Log.d(TAG, "Reviews Count " + reviewList.size());
            for (int i = 0; i < reviewList.size(); i++) {
                 all_review=all_review+reviewList.get(i).getContent()+" ";

            }
            if(reviewList.size()>0)
            reviewText.setText(all_review);

        } catch (Exception e) {
            Log.e(TAG, "Exception , " + e);
        }
    }

    @Override
    public void postExecuteTrailers(List<Trailer> trailerList) {
        try {
            DetailFragment.this.trailerList = trailerList;
            String[] trailers = new String[trailerList.size()];
            Log.d(TAG, "Trailers Count " + trailerList.size());
            for (int i = 0; i < trailerList.size(); i++) {
                trailers[i] = trailerList.get(i).getName();
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, trailers);
            trailersListV.setAdapter(arrayAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Exception , " + e);
        }
    }

    private void startPlayingIntent(Trailer trailer) {
        String url = "https://www.youtube.com/watch?v=" + trailer.getKey();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void fav() {
        ContentValues values = new ContentValues();
        values.put(DataProvider.MOVIE_ID, movie.getId());
        String movieData = movie.getJsonObject();
        values.put(DataProvider.MOVIE_JSONOBJECT,movieData);
        Uri uri = getActivity().getContentResolver().insert(DataProvider.CONTENT_URI, values);
        favBtn.setImageResource(R.mipmap.ic_remove_favourites);

    }


    private void deleteMovie() {
        try {
            getActivity().getContentResolver().delete(DataProvider.CONTENT_URI,DataProvider.MOVIE_ID+ "='" + movie.getId() + "'",null);
            Toast.makeText(getActivity(), "Removed from Favourites", Toast.LENGTH_SHORT).show();
            favBtn.setImageResource(R.mipmap.ic_favourites);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something Error in deleting..", Toast.LENGTH_SHORT).show();
        }


    }
    public boolean isFavorite(int id) {
        try {
            Log.d("id",id+"");
            String URL = "content://com.example.popularmovieapp.database.DataProvider";
            Uri movies = Uri.parse(URL);
            Cursor c = getActivity().managedQuery(movies, null, DataProvider.MOVIE_ID+ "='" + id + "'", null,null );

            c.moveToFirst();       //move cursor to first to start check
            if (!c.isNull(0)) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }
}