package com.example.hello;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import com.example.entity.Music;

public class MainActivity extends ActionBarActivity {
	// private int MUSICLIST_SUCCESS = 0;
	// private int MUSICLIST_FAILURE = 1;

	private ListView musicListView;
	private SimpleAdapter mAdapter;
	private List<Music> musicList = new ArrayList<Music>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		musicListView = (ListView) findViewById(R.id.musicListView);
		new Thread(runnable).start();

	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO
			// UI界面的更新等相关操作
			// super.handleMessage(msg);
			// Bundle data = msg.getData();
			// String val = data.getString("value");
			// Log.i("mylog", "请求结果-->" + val);

		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO
			// 在这里进行 http request.网络请求相关操作
			// Message msg = new Message();
			// Bundle data = new Bundle();
			// data.putString("value", "请求结果");
			// msg.setData(data);
			// handler.sendMessage(msg);
			try {
				getMusicList();
				setListAdpter(musicList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void setListAdpter(List<Music> list) {
		List<HashMap<String, Object>> mp3List = new ArrayList<HashMap<String, Object>>();
		for (Music music : list) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("songName", music.getSongName());
			map.put("artistName", music.getArtistName());
			map.put("songPicBig", music.getSongPicBig());
			map.put("songPicBig", getPicBig(music.getSongPicBig()));
			map.put("songLink", music.getSongLink());
			map.put("time", music.getTime());
			mp3List.add(map);
		}

		mAdapter = new SimpleAdapter(this, mp3List,
				R.layout.music_list_item_layout, new String[] { "songPicBig",
						"songName", "artistName" }, new int[] {
						R.id.songPicBig, R.id.songName, R.id.artistName });
		mAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				// TODO Auto-generated method stub
				if(view instanceof ImageView && data instanceof Bitmap){  
			        ImageView i = (ImageView)view;  
			        i.setImageBitmap((Bitmap) data);  
			        return true;  
			    }  
			    return false;  
			}
		});
		musicListView.post(new Runnable() {

			@Override
			public void run() { // 该方法会在UI线程中执行
				musicListView.setAdapter(mAdapter);
			}
		});

	}

	public void getMusicList() throws Exception {
		// 百度音乐搜索地址
		String path = "http://music.baidu.com/data/music/fmlink?songIds=691762,1541626,269605,732067,1610334,1423666,205686,66590612,231125,256845&type=mp3&rate=128&callback=jsonplink1407315626890&_=1407315626183";
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true);
		conn.setRequestMethod("GET");
		conn.connect();
		InputStream is = conn.getInputStream();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		String json = bos.toString();
		int num = json.indexOf("{");
		json = json.substring(num);
		JSONObject jsonObject = new JSONObject(json);
		JSONObject data = (JSONObject) jsonObject.get("data");
		JSONArray array = (JSONArray) data.get("songList");

		for (int i = 0; i < array.length(); i++) {
			JSONObject o = (JSONObject) array.get(i);
			Music music = new Music();
			music.setArtistName(o.getString("artistName"));
			music.setShowLink(o.getString("showLink"));
			music.setSongId(o.getString("songId"));
			music.setSongLink(o.getString("songLink"));
			music.setSongName(o.getString("songName"));
			music.setSongPicBig(o.getString("songPicBig"));
			// music.setSongPicBig(getPicBig(o.getString("songPicBig")));
			music.setTime(o.getString("time"));
			musicList.add(music);
		}

		// JSONArray jsonArray = new JSONArray(json);

	}

	private Bitmap getPicBig(String songPicBig) {
		Bitmap bitmap = null;
		try {
			URL url = new URL(songPicBig);
			bitmap = BitmapFactory.decodeStream(url.openStream());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
