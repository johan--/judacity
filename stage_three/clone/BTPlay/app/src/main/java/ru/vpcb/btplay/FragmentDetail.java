package ru.vpcb.btplay;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import ru.vpcb.btplay.utils.FragmentData;

import static ru.vpcb.btplay.utils.Constants.DETAIL_IS_EXPANDED;
import static ru.vpcb.btplay.utils.Constants.RECIPE_POSITION;
import static ru.vpcb.btplay.utils.Constants.RECIPE_STEP_POSITION;
import static ru.vpcb.btplay.utils.Constants.TAG_DETAIL;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 15-Nov-17
 * Email: vadim.v.voronov@gmail.com
 */

public class FragmentDetail extends Fragment implements IFragmentHelper {


    private RecyclerView mRecyclerView;
    private FragmentDetailAdapter mRecyclerAdapter;
    private int mPosition;
    private boolean mIsWide;
    private IFragmentCallback mFragmentCallback;
    private RecipeItem mRecipeItem;
    private Context mContext;
    private boolean mIsExpanded;

    public FragmentDetail() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mIsExpanded = false;
        if (savedInstanceState != null) {
            mIsExpanded = savedInstanceState.getBoolean(DETAIL_IS_EXPANDED, false);
        }

        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // load mock data


        mRecyclerView = rootView.findViewById(R.id.fc_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        Bundle detailArgs = getArguments();
        mRecipeItem = null;
        try {
            mRecipeItem = new Gson().fromJson(detailArgs.getString(RECIPE_POSITION, null), RecipeItem.class);
        } catch (Exception e) {
            Log.d(TAG_DETAIL, e.getMessage());
        }


//        mRecipeItem = null;
//        if (mFragmentCallback != null) {
//            mRecipeItem = mFragmentCallback.getRecipe(mPosition);
//        }


        mRecyclerView.setLayoutManager(layoutManager);                              // connect to LayoutManager
        mRecyclerView.setHasFixedSize(false);                                       // item size fixed
        mRecyclerAdapter = new FragmentDetailAdapter(mContext, this, mRecipeItem);      //context  and data
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setExpanded(mIsExpanded);

        mIsWide = rootView.findViewById(R.id.fc_p_container) != null;

        if (mIsWide) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentPlayer playerFragment = new FragmentPlayer();
            Bundle playerArgs = new Bundle();
            playerArgs.putInt(RECIPE_STEP_POSITION, mPosition);
            playerFragment.setArguments(playerArgs);
            fragmentManager.beginTransaction()
                    .replace(R.id.fc_p_container, playerFragment)
                    .commit();

        }
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mFragmentCallback = (IFragmentCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        mContext = context;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(DETAIL_IS_EXPANDED, mRecyclerAdapter.isExpanded());
    }

    @Override
    public void onCallback(int position) {
//        Snackbar.make(getView(), "Clicked position: "+position, Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();

        mPosition = position;
        if (mIsWide) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentPlayer playerFragment = new FragmentPlayer();
            Bundle args = new Bundle();
            args.putInt("position", mPosition);
            playerFragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.fc_p_container, playerFragment)
                    .commit();
        } else {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentPlayer playerFragment = new FragmentPlayer();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, playerFragment)
                    .commit();

//            Snackbar.make(getView(), "Clicked Fragment Detail position: " + position + " stack: " +
//                    fragmentManager.getBackStackEntryCount(), Snackbar.LENGTH_SHORT).show();

        }
    }


    @Override
    public List<FragmentDetailItem> getItemList() {
        return null;
    }


    @Override
    public int getSpanHeight() {
        return 0;
    }
}
