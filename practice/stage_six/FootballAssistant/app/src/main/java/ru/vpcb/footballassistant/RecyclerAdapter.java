package ru.vpcb.footballassistant;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vpcb.footballassistant.data.FDFixture;
import ru.vpcb.footballassistant.utils.Config;

import static ru.vpcb.footballassistant.utils.Config.RM_HEAD_VIEW_TYPE;
import static ru.vpcb.footballassistant.utils.Config.RM_ITEM_VIEW_TYPE;

/**
 * RecyclerView Adapter class
 * Used to create and show Item objects of RecyclerView
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    /**
     * Cursor object source of data
     */
    private Cursor mCursor;
    /**
     * Context  context of calling activity
     */
    private Context mContext;
    /**
     * Span object used for RecyclerView as storage of display item parameters
     */
    private Config.Span mSpan;
    /**
     * Boolean is true for landscape layout
     */
    private boolean mIsLand;
    /**
     * Boolean is true for tablet with sw800dp
     */
    private boolean mIsWide;
    /**
     * Resources of activity
     */
    private Resources mRes;

    private List<FDFixture> mList;
    private DateFormat mDateFormat;

    /**
     * Constructor of RecyclerAdapter
     *
     * @param context Context of calling activity
     * @param sp      Span  object used for RecyclerView as storage of display item parameters
     */
    public RecyclerAdapter(Context context, Config.Span sp, List<FDFixture> list) {
        mContext = context;
        mSpan = sp;
        mRes = context.getResources();
        mList = list;

        mIsWide = mRes.getBoolean(R.bool.is_wide);
        mIsLand = mRes.getBoolean(R.bool.is_land);
        mDateFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    }

    /**
     * Returns itemID by position
     *
     * @param position in position of item
     * @return int itemID
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns item viewType by position
     *
     * @param position in position of item
     * @return int item viewType
     */

    @Override
    public int getItemViewType(int position) {
// test  every 3rd is header
//        return (position % 3 == 0) ? RM_HEAD_VIEW_TYPE : RM_ITEM_VIEW_TYPE;
        return  RM_ITEM_VIEW_TYPE;
    }

    /**
     * Creates ViewHolder of Item of RecyclerView
     * Sets width or height of item according to span and size of RecyclerView Container
     *
     * @param parent   ViewGroup parent of item
     * @param viewType int type of View of Item, unused in this application
     * @return ViewHolder of Item of RecyclerView
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        if (viewType == RM_ITEM_VIEW_TYPE) {
            layoutId = R.layout.recycler_match_item;
        } else {
            layoutId = R.layout.recycler_match_head;
        }
        View view = ((AppCompatActivity) mContext).getLayoutInflater()
                .inflate(layoutId, parent, false);

//        view.getLayoutParams().height = mSpan.getHeight();
        return new ViewHolder(view);
    }


    /**
     * Fills ViewHolder Item with image and text from data source.
     * Sets onClickListener which calls  ICallback.onComplete(view, int) method in calling activity.
     *
     * @param holder   ViewHolder object which is filled
     * @param position int position of item in Cursor data source
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fill(position);
        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ICallback) mContext).onComplete(view, pos);
            }
        });

    }

    /**
     * Returns number of Items of Cursor mCursor data source
     *
     * @return int number of Items of Cursor mCursor data source
     */
    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    /**
     * Replaces mList with new List<FDFixture> object and
     * calls notifyDataSetChanged() method.
     *
     * @param list List<FDFixture> parameter.
     */
    public void swap(List<FDFixture> list) {
        if (list == null) return;
        mList = list;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class of RecyclerView Item
     * Used to hold text and image resources of Item of RecyclerView
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.text_tm_item_home)
        TextView mTextTeamHome;
        @Nullable
        @BindView(R.id.text_tm_item_away)
        TextView mTextTeamAway;
        @Nullable
        @BindView(R.id.text_tm_item_time)
        TextView mTextTime;
        @Nullable
        @BindView(R.id.text_rm_head_league)
        TextView mTextLeague;
        @Nullable
        @BindView(R.id.image_rm_item_home)
        ImageView mImageHome;
        @Nullable
        @BindView(R.id.image_rm_item_away)
        ImageView mImageAway;
        @Nullable
        @BindView(R.id.image_rm_head_league)
        ImageView mImageLeague;

        /**
         * Constructor
         * Binds all views with the ButterKnife object.
         *
         * @param view View of parent
         */
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        /**
         * Fills TextViews with the data from source data object
         * Loads image with the Glide loader to ImageViews.
         *
         * @param position int position of item in RecyclerView
         */
        private void fill(int position) {
            if (mList == null) return;

            FDFixture fixture = mList.get(position);

            if (getItemViewType() == RM_HEAD_VIEW_TYPE) {
                if (position == 0) {
                    mTextLeague.setText(mContext.getString(R.string.text_test_rm_item_favorites));
                    mImageLeague.setImageResource(R.drawable.ic_star);
                } else {
                    mTextLeague.setText(mContext.getString(R.string.text_test_rm_item_league2));
                    mImageLeague.setImageResource(R.drawable.icon_ball);
                }
            }

            if (getItemViewType() == RM_ITEM_VIEW_TYPE) {
                mTextTeamHome.setText(fixture.getHomeTeamName());
                mTextTeamAway.setText(fixture.getAwayTeamName());
                mTextTime.setText(fixture.getMatchTime());
            }
//            String imageURL = mCursor.getString(ArticleLoader.Query.THUMB_URL);
//
//            Glide.with(mContext)
//                    .load(imageURL)
//                    .listener(new RequestListener<Drawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                            mProgressBarImage.setVisibility(View.INVISIBLE);
//                            return false;
//                        }
//                    })
//                    .into(mItemImage);
//            mItemImage.setTransitionName(mRes.getString(R.string.transition_image, getItemId()));
//            mItemTitle.setTransitionName(mRes.getString(R.string.transition_title, getItemId()));
        }


    }


}