package com.example.xyzreader.ui;


import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.xyzreader.R;

import static android.support.v4.view.ViewCompat.SCROLL_AXIS_VERTICAL;

/**
 * Created by V1 on 03-Jan-18.
 */

public class ArticleDetailScroll extends CoordinatorLayout.Behavior {

    private CountDownTimer mCountDownTimer;
    public static ArticleDetailScroll mInstance;
    private View mChild;
    private boolean mIsActive;
//    private boolean mIsLowScrollY;

    // to inflate from XML this constructor
    public ArticleDetailScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedScroll(
            @NonNull CoordinatorLayout coordinatorLayout,
            @NonNull View child,
            @NonNull View target,
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
//        mIsLowScrollY = coordinatorLayout.findViewById(R.id.nested_scrollview).getScrollY() < 200;

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

//        if(!isActive() && mIsLowScrollY) return;  // выйти если неактивно

        if (!isActive()) {
            child.setAlpha(1.0f);
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }

        if (mCountDownTimer != null) mCountDownTimer.cancel();

//        int timerValue = mIsLowScrollY ? 10 : 2500;  // если активно сократить

        mCountDownTimer = new CountDownTimer(2500, 2500) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                setActive(false);
                if (child == null) return;
//                child.animate().alpha(0).setDuration(500).start();
                child.animate().translationY(mChild.getHeight()).setInterpolator(new LinearInterpolator()).start();
                mCountDownTimer = null;
            }
        };
        mCountDownTimer.start();
        setActive(true);
        mChild = child;
    }

    private synchronized boolean isActive() {
        return mIsActive;
    }

    private synchronized void setActive(boolean isActive) {
        mIsActive = isActive;
    }

    public void setContinue(View child) {

        setTimer(child);

    }


    public static void setContinue() {
        if (mInstance == null) return;
        mInstance.setContinue(mInstance.mChild);
    }

}
