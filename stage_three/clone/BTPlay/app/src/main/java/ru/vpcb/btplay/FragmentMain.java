package ru.vpcb.btplay;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.vpcb.btplay.network.LoaderDb;
import ru.vpcb.btplay.network.LoaderUri;
import ru.vpcb.btplay.utils.NetworkData;
import ru.vpcb.btplay.utils.FragmentData;
import ru.vpcb.btplay.utils.RecipeData;

import static ru.vpcb.btplay.utils.Constants.BUNDLE_LOADER_STRING_ID;
import static ru.vpcb.btplay.utils.Constants.LOADER_RECIPES_DB_ID;
import static ru.vpcb.btplay.utils.Constants.LOADER_RECIPES_ID;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 15-Nov-17
 * Email: vadim.v.voronov@gmail.com
 */

public class FragmentMain extends Fragment implements IFragmentHelper,
        LoaderUri.ICallbackUri, LoaderDb.ICallbackDb {

    private List<String> mList;
    private RecyclerView mRecyclerView;
    private FragmentMainAdapter mRecyclerAdapter;

    private LoaderUri mLoader;
    private LoaderDb mLoaderDb;

    // test!!!
    private Toast mToast;


    public FragmentMain() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mLoader = new LoaderUri(getContext(), this);
        mLoaderDb = new LoaderDb(getContext(), this);

        if (NetworkData.isOnline(getContext())) {
            getLoaderManager().initLoader(LOADER_RECIPES_ID, new Bundle(), mLoader); // empty bundle FFU
        } else {
            showError();
        }

        getLoaderManager().initLoader(LOADER_RECIPES_DB_ID, null, mLoaderDb); // empty bundle FFU

        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        final View rootView = inflater.inflate(R.layout.fragment_main_recycler, container, false);
        mList = FragmentData.loadMockCards();                               // load mock data

        mRecyclerView = rootView.findViewById(R.id.fc_recycler);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        setDisplayMetrics();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), mSpan);
        mRecyclerView.setLayoutManager(layoutManager);                          // connect to LayoutManager
        mRecyclerView.setHasFixedSize(true);                                    // item size fixed

        mRecyclerAdapter = new FragmentMainAdapter(rootView.getContext(), this);     //context  and data
        mRecyclerView.setAdapter(mRecyclerAdapter);

        return rootView;
    }

    private static final int LOW_WIDTH_PORTRAIT = 400;   //ok
    private static final int LOW_WIDTH_LANDSCAPE = 550;  //ok
    private static final int LOW_SCALE_PORTRAIT = 250;   //ok
    private static final int LOW_SCALE_LANDSCAPE = 200;  // ok

    private static final int MIDDLE_WIDTH_PORTRAIT = 600;
    private static final int MIDDLE_WIDTH_LANDSCAPE = 850;
    private static final int MIDDLE_SCALE_PORTRAIT = 200;
    private static final int MIDDLE_SCALE_LANDSCAPE = 250;

    private static final int HIGH_WIDTH_PORTRAIT = 800;
    private static final int HIGH_WIDTH_LANDSCAPE = 1200;
    private static final int HIGH_SCALE_PORTRAIT = 300;
    private static final int HIGH_SCALE_LANDSCAPE = 350;

    private static final int MAX_SPAN = 6;
    private static final int MIN_SPAN = 1;
    private static final int MIN_HEIGHT = 200;
    private static final double SCALE_RATIO_PORTRAIT = 1.8;  // low_ok
    private static final double SCALE_RATIO_LANDSCSAPE = 2.3; // low_ok

    private int mSpan;
    private int mSpanHeight;

    private void setDisplayMetrics() {
        DisplayMetrics dp = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dp);
        boolean isLand = dp.widthPixels > dp.heightPixels;
        int width = (int)(dp.widthPixels/dp.density);

        if(!isLand) {
            mSpan = 1;
            if(width > HIGH_WIDTH_PORTRAIT) {
                mSpanHeight = (int)(HIGH_WIDTH_PORTRAIT/SCALE_RATIO_PORTRAIT);
                mSpan = (int)(HIGH_WIDTH_PORTRAIT/HIGH_SCALE_PORTRAIT);
            }else if (width > MIDDLE_WIDTH_PORTRAIT) {
                mSpanHeight = (int) (MIDDLE_WIDTH_PORTRAIT / SCALE_RATIO_PORTRAIT);
                mSpan = (int)(MIDDLE_WIDTH_PORTRAIT/MIDDLE_SCALE_PORTRAIT);
            }else {
                mSpanHeight = (int) (LOW_WIDTH_PORTRAIT / SCALE_RATIO_PORTRAIT);
                mSpan = (int)(LOW_WIDTH_PORTRAIT/LOW_SCALE_PORTRAIT);
            }
        }else {
            if(width > HIGH_WIDTH_LANDSCAPE) {
                mSpanHeight = (int)(HIGH_WIDTH_LANDSCAPE/SCALE_RATIO_LANDSCSAPE);
                mSpan = (int)(HIGH_WIDTH_LANDSCAPE/HIGH_SCALE_LANDSCAPE);
            }else if (width > MIDDLE_WIDTH_LANDSCAPE) {
                mSpanHeight = (int) (MIDDLE_WIDTH_LANDSCAPE / SCALE_RATIO_LANDSCSAPE);
                mSpan = (int)(MIDDLE_WIDTH_LANDSCAPE/MIDDLE_SCALE_LANDSCAPE);
            }else {
                mSpanHeight = (int) (LOW_WIDTH_LANDSCAPE / SCALE_RATIO_LANDSCSAPE);
                mSpan = (int)(LOW_WIDTH_LANDSCAPE/LOW_SCALE_LANDSCAPE);
            }
        }

        if(mSpan < MIN_SPAN) mSpan = MIN_SPAN;
        if(mSpan >MAX_SPAN)mSpan = MAX_SPAN;
        if(mSpanHeight < MIN_HEIGHT) mSpanHeight = MIN_HEIGHT;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


//    @Override
//    public void onResume() {
//        super.onResume();
//        getLoaderManager().restartLoader(LOADER_RECIPES_ID, new Bundle(), mLoader); // empty bundle FFU
//    }

    @Override
    public void onCallback(int position) {
//        Toast.makeText(getContext(),"Clicked position: "+position,Toast.LENGTH_SHORT).show();


        FragmentDetail detailFragment = new FragmentDetail();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();

        Snackbar.make(getView(), "Clicked position: " + position + " stack: " +
                fragmentManager.getBackStackEntryCount(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public List<String> getList() {
        return new ArrayList<>(mList);
    }

    @Override
    public List<FragmentDetailItem> getItemList() {
        return null;
    }

    @Override
    public RecyclerView getRecycler() {
        return null;
    }

    @Override
    public int getSpan() {
        return mSpan;
    }

    @Override
    public int getSpanHeight() {
        return mSpanHeight;
    }


    @Override
    public void showProgress() {
        showToast("Main Progress");
    }

    @Override
    public void onComplete(Bundle data) {
        String s = null;
        if (data != null) {
            s = data.getString(BUNDLE_LOADER_STRING_ID);
        }
        final List<RecipeItem> listRecipeItem = RecipeItem.getRecipeList(s);

        showToast("Main Complete");

        new AsyncTask<Void, Void,Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return RecipeData.bulkInsert(getActivity().getContentResolver(),
                        getLoaderManager(),listRecipeItem, mLoaderDb);
            }
        }.execute();
    }

    @Override
    public void showError() {
        showToast("Main Error");
    }

    @Override
    public void onComplete(Cursor cursor) {
        if (cursor == null || mRecyclerAdapter == null) {   // нет адаптера выходим
            showToast("Main No Database");
            return;
        }
        mRecyclerAdapter.swapCursor(cursor);
        showToast("Main Database Complete");

    }

    @Override
    public void onReset() {

    }

    // test!!!
    private void showToast(final String s) {

        if (mToast != null) {
            mToast.cancel();
        }
        Snackbar.make(getView(),s, BaseTransientBottomBar.LENGTH_SHORT).show();
//        mToast = Toast.makeText(getContext(), s, Toast.LENGTH_SHORT);
//        mToast.show();

    }
}
