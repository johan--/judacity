package com.example.xyzreader.ui;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.remote.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat outputFormat;
    private GregorianCalendar startOfEpoch;

    private Cursor mCursor;
    private Context mContext;
    private Config.Span mSpan;
    private boolean mIsLand;
    private boolean mIsWide;
    private Resources mRes;


    public RecyclerAdapter(Context context, Config.Span sp) {
        mContext = context;
        mSpan = sp;
        mRes =  context.getResources();
        dateFormat = new SimpleDateFormat(mRes.getString(R.string.datetime_pattern));
        outputFormat = new SimpleDateFormat();
        startOfEpoch = new GregorianCalendar(2, 1, 1);

        mIsWide = mRes.getBoolean(R.bool.is_wide);
        mIsLand = mRes.getBoolean(R.bool.is_land);
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((AppCompatActivity) mContext).getLayoutInflater()
                .inflate(R.layout.content_article_item, parent, false);

        if (mIsWide && !mIsLand) {
            view.getLayoutParams().width = mSpan.getWidth();
        }else {
            view.getLayoutParams().height = mSpan.getHeight();
        }
        return new ViewHolder(view);
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e("Error passing date:" + ex.getMessage());
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.fill(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ICallback) mContext).onCallback(view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        if (cursor == null) return;
        mCursor = cursor;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.article_title)
        TextView mItemTitle;
        @Nullable
        @BindView(R.id.article_subtitle)
        TextView mItemSubtitle;
        @Nullable
        @BindView(R.id.article_image)
        ImageView mItemImage;

        @Nullable
        @BindView(R.id.progress_bar_image)
        ProgressBar mProgressBarImage;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void fill(int position) {
            mCursor.moveToPosition(position);

            mItemTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(startOfEpoch.getTime())) {
                mItemSubtitle.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                mItemSubtitle.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }

            mProgressBarImage.setVisibility(View.VISIBLE);

            String imageURL = mCursor.getString(ArticleLoader.Query.THUMB_URL);

            Glide.with(mContext)
                    .load(imageURL)
                     .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mProgressBarImage.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(mItemImage);
            mItemImage.setTransitionName(mRes.getString(R.string.transition_image, getItemId()));
            mItemTitle.setTransitionName(mRes.getString(R.string.transition_title, getItemId()));

        }
    }


}