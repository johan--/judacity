package ru.vpcb.bottomactivity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Random;

import ru.vpcb.jokelibrary.JokeLibrary;
import ru.vpcb.jokeoutput.FragmentJoke;

public class MainActivity extends AppCompatActivity  {
    private static final String BUNDLE_JOKE_STRING = "bundle_joke_string";

    //    private final int[] IMAGE_IDS = new int[]{
//            R.drawable.joke_001, R.drawable.joke_002,
//            R.drawable.joke_005, R.drawable.joke_006,
//            R.drawable.joke_010, R.drawable.joke_011,
//            R.drawable.joke_012, R.drawable.joke_014,
//            R.drawable.joke_015, R.drawable.joke_016,
//            R.drawable.joke_017, R.drawable.joke_018,
//            R.drawable.joke_019
//
//    };
//
//    private Random mRnd;
//    private TextView mTextBanner;
//    private TextView mTextJoke;
//    private JokeLibrary mJoke;
//    private ImageView mImageJoke;
//    private ImageView mImageBanner;
    private AdView mAdView;
    private FloatingActionButton mFab;
    private AsyncJoke mAsyncJoke;

    private class AsyncJoke extends AsyncTask<Void, Void, String> {


        public AsyncJoke() {
        }

        @Override
        protected String doInBackground(Void... voids) {
//            try {
//                Thread.sleep(1500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Log.d("AsyncTask", "thread = " + Thread.currentThread().getName());
            return new JokeLibrary().getJoke();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            onComplete(s);
            Log.d("AsyncPost", "thread = " + Thread.currentThread().getName());
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

        MobileAds.initialize(this, getString(R.string.banner_ad_app_id));

        mAdView = findViewById(R.id.adview_banner);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        Log.d("MainActivity", "thread = " + Thread.currentThread().getName());
        mAsyncJoke = new AsyncJoke();
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncJoke().execute();

            }
        });



    }


    public void onComplete(String s) {
        if(s == null || s.isEmpty()) return;

        Fragment fragment = new FragmentJoke();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString(BUNDLE_JOKE_STRING, s);
        fragment.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("fragmentJoke")
                .commit();

    }


}
