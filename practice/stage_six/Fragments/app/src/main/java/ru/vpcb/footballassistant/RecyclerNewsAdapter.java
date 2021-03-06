package ru.vpcb.footballassistant;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.vpcb.footballassistant.data.FDFixture;

import ru.vpcb.footballassistant.glide.SvgSoftwareLayerSetter;
import ru.vpcb.footballassistant.news.NDArticle;
import ru.vpcb.footballassistant.utils.Config;

import static ru.vpcb.footballassistant.utils.Config.DATE_FULL_PATTERN;
import static ru.vpcb.footballassistant.utils.Config.EMPTY_DASH;
import static ru.vpcb.footballassistant.utils.Config.RM_HEAD_VIEW_TYPE;
import static ru.vpcb.footballassistant.utils.Config.RM_ITEM_VIEW_TYPE;

/**
 * RecyclerView Adapter class
 * Used to create and show Item objects of RecyclerView
 */
public class RecyclerNewsAdapter extends RecyclerView.Adapter<RecyclerNewsAdapter.ViewHolder> {
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

    private List<NDArticle> mList;
    private RequestBuilder<PictureDrawable> mRequestBuilder;
    private RequestBuilder<Drawable> mRequestBuilderCommon;
    private SimpleDateFormat mDateFormat;

    /**
     * Constructor of RecyclerAdapter
     *
     * @param context Context of calling activity
     */

    public RecyclerNewsAdapter(Context context, List<NDArticle> list) {
        mContext = context;
        mRes = context.getResources();
        mList = list;

        mIsWide = mRes.getBoolean(R.bool.is_wide);
        mIsLand = mRes.getBoolean(R.bool.is_land);
        mDateFormat = new SimpleDateFormat(DATE_FULL_PATTERN);
        setupRequestBuilder();


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
        return RM_ITEM_VIEW_TYPE;
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
        int layoutId = R.layout.news_recycler_item;

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
    public void swap(List<NDArticle> list) {
        if (list == null) return;
        mList = list;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class of RecyclerView Item
     * Used to hold text and image resources of Item of RecyclerView
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_news_item_poster)
        ImageView mImagePoster;

        @BindView(R.id.text_news_header)
        TextView mTextHead;

        @BindView(R.id.text_news_body)
        TextView mTextBody;

        @BindView(R.id.text_news_item_source)
        TextView mTextSource;

        @BindView(R.id.text_news_item_time)
        TextView mTextTime;

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

            NDArticle article = mList.get(position);
            if (article == null) return;

            String imageURL = article.getUrlToImage();
            loadImage(imageURL, mImagePoster);

//            SpannableString content = new SpannableString(article.getTitle());
//            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//            mTextHead.setText(content);
            mTextHead.setText(article.getTitle());
            mTextBody.setText(article.getDescription());
            mTextSource.setText(article.getAuthor());
            mTextTime.setText(getTimeAgo(article.getPublishedAt()));

        }


    }

    private void loadImage(String imageURL, ImageView imageView) {
        if (imageURL == null || imageURL.isEmpty() || imageView == null) return;

        if (imageURL.toLowerCase().endsWith("svg")) {
            mRequestBuilder.load(imageURL).into(imageView);

        } else {
            mRequestBuilderCommon.load(imageURL).into(imageView);
        }
    }


    private void setupRequestBuilder() {
        mRequestBuilder = Glide.with(mContext)
                .as(PictureDrawable.class)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.fc_logo_loading)
                        .error(R.drawable.fc_logo)
                )
//                .transition(withCrossFade())
                .listener(new SvgSoftwareLayerSetter());

        mRequestBuilderCommon = Glide.with(mContext)
                .as(Drawable.class)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.fc_logo_loading)
                        .error(R.drawable.fc_logo)
                )
//                .transition(withCrossFade())
                .listener(new CommonRequestListener());

    }

    private String getTimeAgo(String s) {
        String time = null;
        try {
            long currentTime = System.currentTimeMillis();
            long newsTime = mDateFormat.parse(s).getTime();
            long delta = TimeUnit.MILLISECONDS.toMinutes(currentTime - newsTime);
            if (delta < 60) {
                time = mContext.getString(R.string.text_news_item_time_min, delta);

            } else {
                delta = delta / 60;
                time = mContext.getString(R.string.text_news_item_time_hour, delta);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (time == null || time.isEmpty())
            time = mContext.getString(R.string.text_news_item_time_empty);
        return time;
    }


    private class CommonRequestListener implements RequestListener<Drawable> {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    }


}