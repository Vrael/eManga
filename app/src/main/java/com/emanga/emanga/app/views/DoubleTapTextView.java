package com.emanga.emanga.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Ciro on 23/03/2014.
 */
public class DoubleTapTextView extends TextView {
    public static final String TAG = DoubleTapTextView.class.getSimpleName();

    protected GestureDetector mGestureDetector;

    public DoubleTapTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "Double tap on description" );
            if(getLineCount() == 4){
                setMaxLines(Integer.MAX_VALUE);
            } else {
                setLines(4);
            }

            return true;
        }
    }
}
