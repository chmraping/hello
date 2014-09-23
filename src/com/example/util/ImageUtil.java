package com.example.util;

import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtil {
	public static Bitmap getPicBig(String url) {
		Bitmap bitmap = null;
		try {
			URL path = new URL(url);
			bitmap = BitmapFactory.decodeStream(path.openStream());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}
}
