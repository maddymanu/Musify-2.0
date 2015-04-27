package com.example.musicrec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphUser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.List;

/*
 * This class provides the main "frame" for the app.
 * It provides the top 2 fragment-tabs for different views.
 * Trial Comment.
 */

public class Welcome extends SherlockFragmentActivity {

    //Store the top 2 tabs
    ActionBar.Tab Tab1, Tab2, Tab3, Tab4, Tab5;


    //Store the fragment tabs for each view.
    android.support.v4.app.Fragment fragmentTab1 = new FragmentTab1();
    android.support.v4.app.Fragment fragmentTab2 = new FragmentTab2();
    android.support.v4.app.Fragment nearbyFrag = new NearbyFragment();

    //stores the context
    final Context context = this;

    //list to store facebook users for app invitations
    List<GraphUser> friendListForInvites = null;

    SlidingMenu menu;

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        //inflates the layout
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);




        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        //This implements the slidein-sldeout drawer from the left
        View sliderView = inflater.inflate(R.layout.slider_list, null);

        //This stores the list for the options in the sliderlist
        ListView sliderList = (ListView) sliderView
                .findViewById(R.id.optionsListView);

        //Gets the list of friends from the current user for invites
        RequestAsyncTask r = Request.executeMyFriendsRequestAsync(
                ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onCompleted(List<GraphUser> users, Response response) {
                        if (users != null) {

                            //sets the friendlist as the search result
                            friendListForInvites = users;

                        }

                    }

                });



        //this stores the strings for the slider listview
        String[] items2 = new String[]{"Notifications", "Settings", "Send",
                "Logout", "Invite!"};
        //setting the listview for the slider
        ArrayAdapter<String> sliderListViewAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, android.R.id.text1,
                items2);
        sliderList.setAdapter(sliderListViewAdapter);

        //getting the actionbar
        ActionBar actionBar = getSupportActionBar();

        // Hide Actionbar Icon
        actionBar.setDisplayShowHomeEnabled(false);

        // Hide Actionbar Title
        actionBar.setDisplayShowTitleEnabled(true);

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the SlidingMenu Drawer
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setFadeDegree(0.35f);
        menu.setBehindOffset(300);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(sliderView);

        //implements the listener for the slider list items
        sliderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                // Open Different Intents

                // Open Notification Menu
                if (position == 0) {
                    Intent intent = new Intent(Welcome.this, NotificationTray.class);
                    startActivity(intent);
                }
                // Logout
                else if (position == 3) {
                    ParseUser.getCurrentUser();
                    ParseUser.logOut();
                    Intent intent = new Intent(Welcome.this, LoginActivity.class);
                    startActivity(intent);
                }
                // Invite more friends
                else if (position == 4) {
                    inviteFromFacebook(Welcome.this, friendListForInvites);
                }
            }
        });

        // Set Tab Icon and Titles
        Tab1 = actionBar.newTab().setText("Friends");
        // Tab1.setCustomView(View v);
        Tab2 = actionBar.newTab().setText("Own");
        Tab3 = actionBar.newTab().setText("Popping!");
        Tab4 = actionBar.newTab().setText("Nearby!");
        Tab5 = actionBar.newTab().setText("Settings");

        // Set Tab Listeners
        Tab1.setTabListener(new TabListener(fragmentTab2));
        Tab2.setTabListener(new TabListener(fragmentTab1));
        Tab3.setTabListener(new TabListener(fragmentTab1));
        Tab4.setTabListener(new TabListener(nearbyFrag));
        Tab5.setTabListener(new TabListener(fragmentTab1));

        // Add tabs to actionbar
        actionBar.addTab(Tab1);
        actionBar.addTab(Tab2);
        actionBar.addTab(Tab4);
        actionBar.addTab(Tab3);

        actionBar.addTab(Tab5);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            this.menu.toggle();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }






//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getSupportMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    /*
     * This function is used to invite facebook friends to try out this app.
     * TODO - Complete invites
     */
    @SuppressWarnings("deprecation")
    private void inviteFromFacebook(Activity activity, List<GraphUser> list) {

        //if no friend, return
        if (list == null || list.size() == 0)
            return;

        //setting parameters for what to message fb friend
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


}


