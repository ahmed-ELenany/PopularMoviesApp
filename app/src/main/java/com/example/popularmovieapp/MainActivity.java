package com.example.popularmovieapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.example.popularmovieapp.calback.ActionCallback;
import com.example.popularmovieapp.entities.Movie;

/**
 * Created by ahmed on 12/12/15.
 */
public class MainActivity extends AppCompatActivity implements ActionCallback {

    static final String TAG = "MainActivity";
    public static FrameLayout frameLayout; // static to be used in MovieFragment to check the device type.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.frame_container);
    }

    @Override
    public void actionCallback(Movie movie) {

        if (frameLayout == null) { // start Activity when app runs on a phone
            Intent intent = new Intent(MainActivity.this, DetailActivity.class)
                    .putExtra(DetailFragment.MOVIE_DATA, movie);
            startActivity(intent);
        } else {           // start Activity when app runs on a tablet
            Bundle arguments = new Bundle();

            arguments.putParcelable(DetailFragment.MOVIE_DATA,
                    movie);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, detailFragment);
            fragmentTransaction.commit();
        }
    }
}