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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import com.example.entity.Constant;
import com.example.entity.Music;
import com.example.service.PlayService;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	// private int MUSICLIST_SUCCESS = 0;
	// private int MUSICLIST_FAILURE = 1;

	private ListView musicListView;// 音乐列表界面
	private SimpleAdapter mAdapter;// 列表界面的适配器
	private List<Music> musicList = new ArrayList<Music>();// 音乐

	private boolean isPlaying = false; // 正在播放
	private boolean isPause = false; // 暂停

	private Button playBtn;
	private Button previousBtn;
	private Button nextBtn;

	private Intent intent = new Intent();
	private int listPosition = 0; // 标识列表位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		musicListView = (ListView) findViewById(R.id.musicListView);
		musicListView.setOnItemClickListener(new MusicListItemClickListener());
		new Thread(runnable).start();

		findViewById();// 找到界面上的控件
		setViewOnclickListener();// 设置监听器

	}

	private void setViewOnclickListener() {
		playBtn.setOnClickListener(this);
	}

	private void findViewById() {
		playBtn = (Button) findViewById(R.id.play);
		previousBtn = (Button) findViewById(R.id.previous);
		nextBtn = (Button) findViewById(R.id.next);
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
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView i = (ImageView) view;
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

	private class MusicListItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			listPosition = position;
			playMusic();

		}

	}

	public void playMusic() {
		if (musicList != null) {
			Music music = musicList.get(listPosition);
			intent.putExtra("songLink", music.getSongLink());
			intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
			intent.setClass(MainActivity.this, PlayService.class);
			startService(intent);
			playBtn.setBackgroundResource(R.drawable.pause_selector);// 改变图标
			isPlaying = true;
			isPause = false;
		}
	}

	public void pauseMusic() {
		intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
		intent.setClass(MainActivity.this, PlayService.class);
		startService(intent);
		playBtn.setBackgroundResource(R.drawable.play_selector);// 改变图标
		isPlaying = false;
		isPause = true;
	}

	public void continueMusic() {
		intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
		intent.setClass(MainActivity.this, PlayService.class);
		startService(intent);
		playBtn.setBackgroundResource(R.drawable.pause_selector);// 改变图标
		isPlaying = true;
		isPause = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play:
			if (isPlaying) {
				pauseMusic();// 暂停音乐
			} else if (isPause) {
				continueMusic();
			} else {
				playMusic();
			}
			break;

		}
	}
}
