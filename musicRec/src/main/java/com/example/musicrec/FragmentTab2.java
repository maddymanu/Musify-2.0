package com.example.musicrec;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//THIS CLASS SHOULD YOU FB FRIENDS FEED - MOVE THE UPLOAD PART TO WELCOME ACTIVITY

public class FragmentTab2 extends SherlockFragment implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{



    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    private Location currentLocation;
    private Location lastLocation;
    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;

  public static Boolean IS_RUNNING = false;
  Map<String, String> map = new HashMap<String, String>();

  Song currSong;
  int i = 0;

  ListView listview;
  List<GraphUser> friendListForInvites = null;
  CustomArrayAdapter adapter2;
  //private String requestId;

  @SuppressWarnings("deprecation")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.tab_feed, container, false);

    getFacebookIdInBackground();


      // Create a new global location parameters object
      locationRequest = LocationRequest.create();

      // Set the update interval
      locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

      // Use high accuracy
      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

      // Set the interval ceiling to one minute
      locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

      // Create a new location client, using the enclosing class to handle callbacks.
      locationClient = new GoogleApiClient.Builder(getActivity())
              .addApi(LocationServices.API)
              .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
              .addOnConnectionFailedListener(this)
              .build();


    Button invBtn = (Button) rootView.findViewById(R.id.invite_btn);

    RequestAsyncTask r = Request.executeMyFriendsRequestAsync(
        ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {

          @SuppressWarnings("unchecked")
          @Override
          public void onCompleted(List<GraphUser> users, Response response) {
            if (users != null) {

              friendListForInvites = users;
              List<String> friendsList = new ArrayList<String>();

              for (GraphUser user : users) {
                friendsList.add(user.getId());
              }

              @SuppressWarnings("rawtypes")
              final ParseQuery<ParseUser> friendQuery = ParseQuery
                  .getUserQuery();
              friendQuery.whereContainedIn("fbId", friendsList);

              friendQuery.findInBackground(new FindCallback<ParseUser>() {

                public void done(List<ParseUser> friendUsers, ParseException e3) {
                  
                  //getting the list of friends complete here
                  if (friendUsers.size() == 0) {
                    Log.i("Friend", "size0");
                  }

                  ParseQuery<Song> query = ParseQuery.getQuery("Song");
                  query.whereContainedIn("author", friendUsers);
                  query.addDescendingOrder("createdAt");
                  query.findInBackground(new FindCallback<Song>() {

                    @Override
                    public void done(final List<Song> songList, ParseException e) {

                      listview = (ListView) getActivity().getWindow()
                          .getDecorView().findViewById(R.id.listview);

                      adapter2 = new CustomArrayAdapter(getActivity(), songList);
                      listview.setAdapter(adapter2);
                      adapter2.notifyDataSetChanged();

                    }
                  });

                }

              });

            }

          }

        });

    invBtn.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        inviteFromFacebook(getActivity(), friendListForInvites);
      }
    });

    IntentFilter iF = new IntentFilter();
    iF.addAction("com.android.music.metachanged");
    iF.addAction("com.android.music.playstatechanged");
    iF.addAction("com.android.music.musicservicecommand");
    iF.addAction("fm.last.android.metachanged");
    iF.addAction("com.sec.android.app.music.metachanged");
    iF.addAction("com.nullsoft.winamp.metachanged");
    iF.addAction("com.amazon.mp3.metachanged");
    iF.addAction("com.miui.player.metachanged");
    iF.addAction("com.real.IMP.metachanged");
    iF.addAction("com.sonyericsson.music.metachanged");
    iF.addAction("com.rdio.android.metachanged");
    iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
    iF.addAction("com.andrew.apollo.metachanged");
//    iF.addAction("com.spotify.mobile.android.metadatachanged");
//    iF.addAction("com.spotify.music.android.metadatachanged");
    iF.addAction("com.spotify.music.metadatachanged");
      iF.addAction("com.spotify.music.playstatechanged");
      iF.addAction("com.spotify.music.musicservicecommand");
//    iF.addAction("com.spotify.music.MainActivity.metadatachanged");


    getActivity().registerReceiver(mReceiver, iF);
    return rootView;
  }

  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      String action = intent.getAction();
      String cmd = intent.getStringExtra("command");

      String artist = intent.getStringExtra("artist");
      String album = intent.getStringExtra("album");
      String track = intent.getStringExtra("track");

      if (map.get(track) != null) {

      } else {
        Log.d("mIntentReonReceive", action + "/" + cmd);
        Log.d("Music", artist + ":" + album + ":" + track);

        if(currentLocation != null) {
            Log.d("Cur locations:", currentLocation.toString());
        }


          // everytime theres an update, push it to Parse track.
        currSong = new Song();
        currSong.setAuthor(ParseUser.getCurrentUser());

        if (track != null) {
          currSong.setTitle(track);
        } else {
          currSong.setTitle("unknown");
        }

        if (artist != null) {
          currSong.setArtist(artist);
        } else {
          currSong.setArtist("unknown");
        }

        // CHANGE THIS TO ACTUALLY UPLOAD A SONG
        if(track != null && artist != null) {

            //get location here.
            currSong.saveInBackground();
        }


      }

      map.put(track, artist);

    }
  };


    /*
  * In response to a request to start updates, send a request to Location Services
  */
    private void startPeriodicUpdates() {
        Log.d("START PUp" , "reaching Start perioid");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            Log.d("GET Location:" , "Reaching get locs.");
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }


    /*
  * Called by Location Services when the request to connect the client finishes successfully. At
  * this point, you can request the current location or start periodic updates
  */
    public void onConnected(Bundle bundle) {
        if (MainApplication.APPDEBUG) {
            Log.d("Connocation services", MainApplication.APPTAG);
        }
        Log.d("MAIN" , "REACHING ON CONNECTED");
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    /*
     * Called by Location Services if the connection to the location client drops because of an error.
     */
    public void onDisconnected() {
        if (MainApplication.APPDEBUG) {
            Log.d("Dis location services", MainApplication.APPTAG);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(MainApplication.APPTAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                if (MainApplication.APPDEBUG) {
                    // Thrown if Google Play services canceled the original PendingIntent
                    Log.d(MainApplication.APPTAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }


    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();

        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        locationClient.connect();
    }

    /*
   * Report location updates to the UI.
   */
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d("Cur locations:", currentLocation.toString());
//        if (!hasSetUpInitialLocation) {
//            // Zoom to the current location.
//            updateZoom(myLatLng);
//            hasSetUpInitialLocation = true;
//        }
        // Update map radius indicator
//        updateCircle(myLatLng);
//        doMapQuery();
//        doListQuery();
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    /*
   * Show a dialog returned by Google Play services for the connection error code
   */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), MainApplication.APPTAG);
        }
    }

  @SuppressWarnings("deprecation")
  private static void getFacebookIdInBackground() {
    Request.executeMeRequestAsync(ParseFacebookUtils.getSession(),
        new Request.GraphUserCallback() {
          @Override
          public void onCompleted(GraphUser user, Response response) {
            if (user != null) {
              ParseUser.getCurrentUser().put("fbId", user.getId());
              ParseUser.getCurrentUser().put("name", user.getName());

//                Update User location here.
//             ParseUser.getCurrentUser().put("location", user.getName());
              ParseUser.getCurrentUser().saveInBackground();

            }
          }
        });
  }

  private void sendRequestDialog() {
    Bundle params = new Bundle();
    params.putString("message", "Learn how to make your Android apps social");

    WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
        getActivity(), Session.getActiveSession(), params))
        .setOnCompleteListener(new OnCompleteListener() {

          @Override
          public void onComplete(Bundle values, FacebookException error) {
            if (error != null) {
              if (error instanceof FacebookOperationCanceledException) {
                Toast.makeText(getActivity().getApplicationContext(),
                    "Request cancelled",

                    Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(getActivity().getApplicationContext(),
                    "Network Error", Toast.LENGTH_SHORT).show();
              }
            } else {
              final String requestId = values.getString("request");
              if (requestId != null) {
                Toast.makeText(getActivity().getApplicationContext(),
                    "Request sent", Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(getActivity().getApplicationContext(),
                    "Request cancelled", Toast.LENGTH_SHORT).show();
              }
            }
          }

        }).build();
    requestsDialog.show();
  }

  @SuppressWarnings("deprecation")
  private void inviteFromFacebook(Activity activity, List<GraphUser> list) {

    if (list == null || list.size() == 0)
      return;

    Bundle parameters = new Bundle();

    parameters.putString("message", "Use my app!");

    Facebook mFacebook = new Facebook("830750263621357");
    // Show dialog for invitation
    mFacebook.dialog(activity, "apprequests", parameters,
        new Facebook.DialogListener() {
          @Override
          public void onComplete(Bundle values) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onCancel() {
            // TODO Auto-generated method stub

          }

          @Override
          public void onFacebookError(FacebookError e) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onError(DialogError e) {
            // TODO Auto-generated method stub

          }
        });

  }


    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), "");
            }
            return false;
        }
    }

}


