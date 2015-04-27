package com.example.musicrec;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyFragment extends SherlockFragment {


    public NearbyFragment() {
        // Required empty public constructor
    }

    private SupportMapFragment mapFragment = new SupportMapFragment();



    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_nearby, container, false);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map_layout, mapFragment).commit();

        ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<Song> query = ParseQuery.getQuery("Song");
//        query.whereWithinMiles();
        query.whereEqualTo("author", currUser);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Song>() {

            @Override
            public void done(List<Song> songList, ParseException arg1) {

                // This is the easy Part. get a list of songs and show.

//                mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment);

                // Enable the current location "blue dot"
                mapFragment.getMap().setMyLocationEnabled(true);

//              Do for each song.
                ParseGeoPoint p = new ParseGeoPoint( 32.8846092 , -117.242008 );
                MarkerOptions markerOpts =
                        new MarkerOptions().position(new LatLng( p.getLatitude(), p.getLongitude()));

                markerOpts =
                        markerOpts.title("Song1").snippet("Artist?")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                Marker marker = mapFragment.getMap().addMarker(markerOpts);
                mapMarkers.put(songList.get(0).getObjectId(), marker);


            }

        });

        return v;
    }


}
