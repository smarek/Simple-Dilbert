package com.mareksebera.simpledilbert.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import androidx.viewpager.widget.ViewPager;

final public class FixedViewPager extends ViewPager {

    public FixedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedViewPager(Context context) {
        super(context);
    }

    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (isEnabled()) {
            try {
                return super.onInterceptTouchEvent(event);
            } catch (final Throwable e) {
                // if you read this: don't worry! just close this class and do
                // something else!
            }
        }
        return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        try {
            return super.onTouchEvent(arg0);
        } catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public boolean performClick() {
        try {
            return super.performClick();
        } catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NotNull MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (final Throwable t) {
            return false;
        }
    }
}
