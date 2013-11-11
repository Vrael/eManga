package com.emanga.utils;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class Image {
	public static Bitmap getCroppedBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	            bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
	            bitmap.getWidth() / 2, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
	    //return _bmp;
	    return output;
	}
}
