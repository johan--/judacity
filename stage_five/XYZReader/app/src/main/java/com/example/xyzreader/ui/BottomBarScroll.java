package com.example.xyzreader.ui;


import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.xyzreader.R;
import com.example.xyzreader.remote.Config;

import static android.support.v4.view.ViewCompat.SCROLL_AXIS_VERTICAL;
import static com.example.xyzreader.remote.Config.BOTTOM_BAR_DELAY_HIDE;
import static com.example.xyzreader.remote.Config.BOTTOM_BAR_FAST_HIDE;
import static com.example.xyzreader.remote.Config.BOTTOM_BAR_SCROLLY_THRESHOLD;
import static com.example.xyzreader.remote.Config.BOTTOM_BAR_SCROLL_DY_THRESHOLD;

/**
 * Exercise for course : Android Developer Nanodegree
 * Created: Vadim Voronov
 * Date: 03-Jan-18
 * Email: vadim.v.voronov@gmail.com
 */

public class BottomBarScroll extends CoordinatorLayout.Behavior {

    private CountDownTimer mCountDownTimer;
    public static BottomBarScroll mInstance;
    private View mChild;
    private boolean mIsActive;
    private boolean mIsLowScrollTextY;
    private boolean mIsLand;
    private boolean mIsWide;

    // to inflate from XML this constructor
    public BottomBarScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        mIsLand = res.getBoolean(R.bool.is_land);
        mIsWide = res.getBoolean(R.bool.is_wide);
    }

    @Override
    public void onNestedScroll(
            @NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child,
            @NonNull View target,
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        mIsLowScrollTextY = coordinatorLayout.findViewById(R.id.nested_scrollview).getScrollY() < BOTTOM_BAR_SCROLLY_THRESHOLD ;

        if(Config.isInstructiveLocked()) {
            return;
        }
        if(dyConsumed < Math.abs(BOTTOM_BAR_SCROLL_DY_THRESHOLD)){
            return;
        }
        setContinue(child);
        mInstance = this;
    }


    @Override
    public boolean onStartNestedScroll(
            @NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child,
            @NonNull View directTargetChild,
            @NonNull View target, int axes, int type) {
//        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);

        return axes == SCROLL_AXIS_VERTICAL;
    }

    private void setTimer(final View child) {
        if (child == null) return;

        if (!mIsActive && mIsLowScrollTextY) return;  // выйти если неактивно

        if (!mIsActive) {
            child.setAlpha(1.0f);
            if (!mIsLand && !mIsWide) {
                child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            } else {
                child.animate().translationX(0).setInterpolator(new LinearInterpolator()).start();
            }
        }

        if (mCountDownTimer != null) mCountDownTimer.cancel();

        int timerValue = mIsLowScrollTextY ? BOTTOM_BAR_FAST_HIDE : BOTTOM_BAR_DELAY_HIDE;  // если активно сократить

        mCountDownTimer = new CountDownTimer(timerValue, timerValue) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                mIsActive = false;
                if (child == null) return;
//                child.animate().alpha(0).setDuration(500).start();
                if (!mIsLand && !mIsWide) {
                    child.animate().translationY(mChild.getHeight()).setInterpolator(new LinearInterpolator()).start();
                } else {
                    child.animate().translationX(-mChild.getWidth()).setInterpolator(new LinearInterpolator()).start();
                }

                mCountDownTimer = null;
            }
        };
        mCountDownTimer.start();
        mIsActive = true;
        mChild = child;
    }



    public void setContinue(View child) {

        setTimer(child);

    }


    public static void setContinue() {
        if (mInstance == null) return;
        mInstance.setContinue(mInstance.mChild);
    }

}
