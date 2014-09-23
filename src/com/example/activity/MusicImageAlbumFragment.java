package com.example.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.hello.R;
import com.example.util.ImageUtil;

public class MusicImageAlbumFragment extends Fragment {
	private ImageView imageView;
	private Bitmap bitmap;
	private String url;

	public void updateAlbum() {
		if (url != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					bitmap = ImageUtil.getPicBig(url);
					imageView.post(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});
				}
			}).start();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.music_image, null);
		// imageView =(ImageView) inflater.inflate(R.id.music_image_album,
		// null);
		imageView = (ImageView) view.findViewById(R.id.music_image_album);
		imageView.setBackgroundResource(R.drawable.loading);
		// updateAlbum(url);

		return view;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
