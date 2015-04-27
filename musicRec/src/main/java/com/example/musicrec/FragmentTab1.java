package com.example.musicrec;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

//THIS CLASS SHOULD YOU FB FRIENDS FEED - MOVE THE UPLOAD PART TO WELCOME ACTIVITY

public class FragmentTab1 extends SherlockFragment {

//  public static Boolean IS_RUNNING = false;
//  Map<String, String> map = new HashMap<String, String>();



  ListView listview;
  CustomArrayAdapter adapter2;

  @SuppressWarnings("deprecation")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View rootView = inflater.inflate(R.layout.tab_feed, container, false);
    
    ParseUser currUser = ParseUser.getCurrentUser();
    ParseQuery<Song> query = ParseQuery.getQuery("Song");
    //query.whereWithinMiles() -- Use this for nearby locations.
    query.whereEqualTo("author", currUser);
    query.addDescendingOrder("createdAt");
    query.findInBackground(new FindCallback<Song>() {

      @Override
      public void done(List<Song> songList, ParseException arg1) {
        listview = (ListView) rootView.findViewById(R.id.listview);
        adapter2 = new CustomArrayAdapter(getActivity() , songList);
        listview.setAdapter(adapter2);
      }
      
    });
    
    

    return rootView;
  }

}