package com.example.popularmovieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.popularmovieapp.entities.Movie;
import com.squareup.picasso.Picasso;

/**
 * Created by ahmed on 12/12/15.
 */
public class MovieGridAdapter extends ArrayAdapter<Movie> {

    public MovieGridAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ImageView img;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movie_item, parent, false);
           img =(ImageView) view.findViewById(R.id.movieImgV); //image that customise in grid
            view.setTag(img);
        }

        final Movie movie = getItem(position);
        String image_url = "http://image.tmdb.org/t/p/w185" + movie.getImage(); // setup image url

        img = (ImageView) view.getTag();

        Picasso.with(getContext()) //use picasso library to load image from web
                .load(image_url)
                .into(img); // load image from url into ImageView

        return view;
    }

}