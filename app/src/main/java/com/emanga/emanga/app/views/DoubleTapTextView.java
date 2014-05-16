package com.emanga.emanga.app.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Ciro on 23/03/2014.
 */
public class DoubleTapTextView extends TextView {
    public static final String TAG = DoubleTapTextView.class.getSimpleName();

    private int mLines;

    protected GestureDetector mGestureDetector;

    public DoubleTapTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        String lines = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "lines");
        if(lines != null){
            mLines = Integer.valueOf(lines);
        }

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
            if(getLineCount() <= mLines){
                setEllipsize(null);
                setMaxLines(Integer.MAX_VALUE);
            } else {
                setEllipsize(TextUtils.TruncateAt.END);
                setLines(mLines);
            }

            return true;
        }
    }
}
