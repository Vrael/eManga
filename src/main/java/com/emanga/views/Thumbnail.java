package com.emanga.views;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *	Is a model where there are an image and a title or text bottom
 *	for ThumbnailAdapter 
 */
public class Thumbnail implements Parcelable {
	public String title = null;
	public String image = null;
	public String date = null;
	public String number = null;
	public String url = null;
	
	public Thumbnail(){}
	
	public Thumbnail(String text, String img, String time, String nChapter, String link) {
		title = text;
		image = img;
		date = time;
		number = nChapter;
		url = link;
	}
	
	public Thumbnail(Parcel in) {
		title = in.readString();
		image = in.readString();
		date = in.readString();
		number = in.readString();
		url = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(image);
		dest.writeString(date);
		dest.writeString(number);
		dest.writeString(url);
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
