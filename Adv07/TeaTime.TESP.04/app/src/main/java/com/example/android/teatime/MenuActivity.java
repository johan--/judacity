/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.teatime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.teatime.IdlingResource.SimpleIdlingResource;
import com.example.android.teatime.model.Tea;

import java.util.ArrayList;

// TODO (1) Implement ImageDownloader.DelayerCallback
public class MenuActivity extends AppCompatActivity implements ImageDownloader.DelayerCallback {

    Intent mTeaIntent;

    public final static String EXTRA_TEA_NAME = "com.example.android.teatime.EXTRA_TEA_NAME";

    // TODO (2) Add a SimpleIdlingResource variable that will be null in production
    private SimpleIdlingResource mIdlingResource;
    /**
     * TODO (3) Create a method that returns the IdlingResource variable. It will
     * instantiate a new instance of SimpleIdlingResource if the IdlingResource is null.
     * This method will only be called from test.
     */

    @NonNull
    @VisibleForTesting
    public IdlingResource getIdlingResource() {
        if(mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }
    /**
     * TODO (4) Using the method you created, get the IdlingResource variable.
     * Then call downloadImage from ImageDownloader. To ensure there's enough time for IdlingResource
     * to be initialized, remember to call downloadImage in either onStart or onResume.
     * This is because @Before in Espresso Tests is executed after the activity is created in
     * onCreate, so there might not be enough time to register the IdlingResource if the download is
     * done too early.
     */


    // TODO (5) Override onDone so when the thread in ImageDownloader is finished, it returns an
    // ArrayList of Tea objects via the callback.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar menuToolbar = (Toolbar) findViewById(R.id.menu_toolbar);
        setSupportActionBar(menuToolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_title));

// moved to ImageDownloader
//        // Create an ArrayList of teas
//        final ArrayList<Tea> teas = new ArrayList<>();
//        teas.add(new Tea(getString(R.string.black_tea_name), R.drawable.black_tea));
//        teas.add(new Tea(getString(R.string.green_tea_name), R.drawable.green_tea));
//        teas.add(new Tea(getString(R.string.white_tea_name), R.drawable.white_tea));
//        teas.add(new Tea(getString(R.string.oolong_tea_name), R.drawable.oolong_tea));
//        teas.add(new Tea(getString(R.string.honey_lemon_tea_name), R.drawable.honey_lemon_tea));
//        teas.add(new Tea(getString(R.string.chamomile_tea_name), R.drawable.chamomile_tea));

// moved to onDone()
//        // Create a {@link TeaAdapter}, whose data source is a list of {@link Tea}s.
//        // The adapter know how to create grid items for each item in the list.
//        GridView gridview = (GridView) findViewById(R.id.tea_grid_view);
//        TeaMenuAdapter adapter = new TeaMenuAdapter(this, R.layout.grid_item_layout, teas);
//        gridview.setAdapter(adapter);
//
//
//        // Set a click listener on that View
//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//
//                Tea item = (Tea) adapterView.getItemAtPosition(position);
//                // Set the intent to open the {@link OrderActivity}
//                mTeaIntent = new Intent(MenuActivity.this, OrderActivity.class);
//                String teaName = item.getTeaName();
//                mTeaIntent.putExtra(EXTRA_TEA_NAME, teaName);
//                startActivity(mTeaIntent);
//            }
//        });


        getIdlingResource();    // нужно ОБЯЗАТЕЛЬНО ИНАЧЕ ТЕСТ НЕ РАБОТАЕТ get mIdlingResource instance
    }

    @Override
    protected void onStart() {  // use ImageDownloader for download, mIdlingResources for
        super.onStart();
        ImageDownloader.downloadImage(this, MenuActivity.this, mIdlingResource);
    }

    @Override
    public void onDone(ArrayList<Tea> teas) {

        GridView gridview = (GridView) findViewById(R.id.tea_grid_view);
        TeaMenuAdapter adapter = new TeaMenuAdapter(this, R.layout.grid_item_layout, teas);
        gridview.setAdapter(adapter);

        // Set a click listener on that View
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Tea item = (Tea) adapterView.getItemAtPosition(position);
                // Set the intent to open the {@link OrderActivity}
                mTeaIntent = new Intent(MenuActivity.this, OrderActivity.class);
                String teaName = item.getTeaName();
                mTeaIntent.putExtra(EXTRA_TEA_NAME, teaName);
                startActivity(mTeaIntent);
            }
        });


    }
}