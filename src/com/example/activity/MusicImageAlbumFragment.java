package com.example.activity;

import java.net.URL;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hello.R;


public class MusicImageAlbumFragment extends Fragment {
	private ImageView imageView;
	private Bitmap bitmap;
	private String url;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				imageView.setImageBitmap(bitmap);
			}
		};
	};

	public void updateAlbum() {
		if (url != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					URL path;
					try {
						path = new URL(url);
						bitmap = BitmapFactory.decodeStream(path.openStream());
					} catch (Exception e) {
						e.printStackTrace();
					}
					handler.sendEmptyMessage(1);
				}
			}).start();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.music_image, null);
		imageView = (ImageView) view.findViewById(R.id.music_image_album);
//		imageView.setBackgroundResource(R.drawable.loading);

		return view;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
