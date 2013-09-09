package com.emanga.views;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *	Is a model where there are an image and a title or text bottom
 *	for ThumbnailAdapter 
 */
public class Thumbnail implements Parcelable {
	public String title;
	public String image;
	
	public Thumbnail(String text, String img) {
		title = text;
		image = img;
	}
	
	public Thumbnail(Parcel in) {
		title = in.readString();
		image = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(image);
	}
	
	public static final Parcelable.Creator<Thumbnail> CREATOR = new Parcelable.Creator<Thumbnail>() {
		public Thumbnail createFromParcel(Parcel in) {
			return new Thumbnail(in);
		}
		public Thumbnail[] newArray(int size) {
            return new Thumbnail[size];
        }
	};
}
