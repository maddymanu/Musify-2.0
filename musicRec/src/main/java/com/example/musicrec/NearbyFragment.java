package com.example.musicrec;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import radams.gracenote.webapi.GracenoteException;
import radams.gracenote.webapi.GracenoteMetadata;
import radams.gracenote.webapi.GracenoteWebAPI;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyFragment extends SherlockFragment {


    public NearbyFragment() {
        // Required empty public constructor
    }

    private SupportMapFragment mapFragment = new SupportMapFragment();


    GracenoteWebAPI api;
    private static String clientID = "9932800"; // Put your clientID here.
    private static String clientTag = "6944FB99E90C816EE1F6338E5F16035E";

    Bitmap bm;


    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_nearby, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            api = new GracenoteWebAPI(clientID, clientTag);
        } catch (GracenoteException e) {
            Log.d("Grace" , "Exc");
            e.printStackTrace();
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map_layout, mapFragment).commit();

        ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<Song> query = ParseQuery.getQuery("Song");
//        query.whereWithinMiles();
//        query.whereEqualTo("author", currUser);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Song>() {

            @Override
            public void done(List<Song> songList, ParseException arg1) {

                // This is the easy Part. get a list of songs and show.
                Log.d("Nearby" , "Size=" + songList.size());

//                mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment);

                // Enable the current location "blue dot"
                mapFragment.getMap().setMyLocationEnabled(true);

                for (Song s : songList) {

//                    Do for each song.
                    if (s == null || s.getLocation() == null) {
                        //Log.d("Nearby" , "leaving bcoz1");
                        continue;
                    }else {
                        Double lat = s.getLocation().getLatitude();
                        Double longitude = s.getLocation().getLongitude();

                        if(lat == null || longitude == null) {
                            //Log.d("Nearby" , "leaving bcoz2");
                            continue;
                        }else {
                            GracenoteMetadata results = null;

                            String title = s.getTitle().toString();
                            String artist = s.getArtist().toString();

                            //Log.d("SONG" , title + " " + artist);
//                            if(api != null && title != null && artist !=null){
//                                results = api.searchTrack("", "", "All of Me");
//                            }else {
//                                continue;
//                            }

                            try {
                                results = api.searchTrack(artist, "", title);
                            }catch (NullPointerException e) {
                                e.printStackTrace();
                            }

//                            if(results == null) {
//                                continue;
//                            }

                            String url = null;
                            try {
//                    Log.d("Albums", results.getAlbums().get(0).get("album_coverart").toString());
                                url = results.getAlbums().get(0).get("album_coverart").toString();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            if (url == null) {
                                try {
//                        Log.d("Albums", results.getAlbums().get(0).get("artist_image_url").toString());
                                    url = results.getAlbums().get(0).get("album_coverart").toString();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }

                            try {

                                //downloading the image from the url.
                                if (url != null) {
                                    InputStream is = new URL(url).openStream();

                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;

                                    BitmapFactory.decodeStream(is, null, options);

                                    options.inSampleSize = calculateInSampleSize(options, 100, 100);
                                    options.inJustDecodeBounds = false;
                                    is.close();

                                    is = new URL(url).openStream();
                                    bm = BitmapFactory.decodeStream(is, null, options);

                                    int width = bm.getWidth();
                                    int height = bm.getHeight();
                                    int newWidth = 100;
                                    int newHeight = 100;

                                    float scaleWidth = ((float) newWidth) / width;
                                    float scaleHeight = ((float) newHeight) / height;

                                    Matrix matrix = new Matrix();
                                    matrix.postScale(scaleWidth, scaleHeight);

                                    //creating a bitmap and rounding off corners.
                                    bm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
                                    bm = getRoundedCornerBitmap(bm, 12f);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            ParseGeoPoint p = new ParseGeoPoint(lat, longitude);
                            MarkerOptions markerOpts =
                                    new MarkerOptions().position(new LatLng(p.getLatitude(), p.getLongitude()));

                            markerOpts =
                                    markerOpts.title(s.getTitle()).snippet(s.getArtist())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            try {
                                markerOpts = markerOpts.icon(BitmapDescriptorFactory.fromBitmap(bm));
                                Marker marker = mapFragment.getMap().addMarker(markerOpts);

                                mapMarkers.put(songList.get(0).getObjectId(), marker);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                }
//


            }

        });

        return v;
    }


    /*
     * function to round off corners of a bitmap.
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float rnd) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = rnd;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    //calculates the size of the input image.
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


}
