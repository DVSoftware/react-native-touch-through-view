package com.rome2rio.android.reactnativetouchthroughview;

import com.facebook.react.views.view.ReactViewGroup;
import com.facebook.react.touch.ReactHitSlopView;
import com.facebook.react.bridge.ReactContext;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Rect;
import android.view.ViewGroup;

public class TouchThroughWrapper extends ReactViewGroup implements ReactHitSlopView {
    private boolean lastTouchWasNotValid = false;
    ReactContext context;
    public TouchThroughWrapper(ReactContext context) {
        super(context);
        this.context = context;
        this.setActivityListener(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Activity activity = context.getCurrentActivity();
        ViewGroup viewGroup = this;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int y = (int) event.getY();
            System.out.println("" + y);
            lastTouchWasNotValid = isTouchingTouchThroughView(viewGroup, (int) event.getX(), (int) event.getY());
        }
        return lastTouchWasNotValid;
    }

    // Recursively find out if an absolute x/y position is hitting a child view and stop event
    // propagation if a hit is found.
    private boolean isTouchingTouchThroughView(ViewGroup viewgroup, int x, int y) {
        boolean isTouchingTouchThroughView = false;

        for (int i = 0; i < viewgroup.getChildCount(); i++) {
            View child = viewgroup.getChildAt(i);

            boolean isViewGroup = child instanceof ViewGroup;
            boolean isTouchThroughView = child instanceof TouchThroughView;

            if (isTouchThroughView) {
                int[] location = new int[2];
                int[] thisLocation = new int[2];

                child.getLocationOnScreen(location);
                this.getLocationOnScreen(thisLocation);

                int childX = location[0] - thisLocation[0];
                int childY = location[1] - thisLocation[1];

                Rect bounds = new Rect(childX, childY, childX + child.getWidth(), childY + child.getHeight());

                isTouchingTouchThroughView = bounds.contains(x, y);
            } else if (isViewGroup) {
                isTouchingTouchThroughView = this.isTouchingTouchThroughView((ViewGroup) child, x, y);
            }

            if (isTouchingTouchThroughView) {
                break;
            }
        }

        return isTouchingTouchThroughView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass through touch events to layer behind.
        return false;
    }

    //If the touch was not on the list make the slop rect small so react-native dont use this view as responder
    public Rect getHitSlopRect() {
        if (lastTouchWasNotValid) {
            return new Rect(-1000, -1000, -1000, -1000);
        }
        return new Rect(0, 0, 0, 0);
    }
}
