package com.example.activity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import com.example.entity.Constant;
import com.example.entity.MediaApp;
import com.example.entity.Music;
import com.example.hello.R;
import com.example.service.PlayService;

public class MainActivity extends Activity implements OnClickListener {
	private RelativeLayout play_layout;
	private ListView musicListView;// 音乐列表界面
	private SimpleAdapter mAdapter;// 列表界面的适配器
	private List<Music> musicList ;// 音乐列表
	private MediaApp mediaApp;
	private boolean isPlaying = false; // 正在播放
	private boolean isPause = false; // 暂停

	private Button playBtn;
	private Button refreshBtn;
	private Thread thread;
	// private Button previousBtn;
	// private Button nextBtn;

	private Intent intent = new Intent();
	private int listPosition = 0; // 标识列表位置
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mediaApp =(MediaApp) getApplication();
		musicListView = (ListView) findViewById(R.id.musicListView);
		musicListView.setOnItemClickListener(new MusicListItemClickListener());
		play_layout = (RelativeLayout) findViewById(R.id.play_layout);
		play_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						PlayActivity.class);
				Bundle bundle = new Bundle();
//				bundle.putSerializable("musicList", (Serializable) musicList);
				bundle.putInt("listPosition", listPosition);
				bundle.putBoolean("isPlaying", isPlaying);
				bundle.putBoolean("isPause", isPause);
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});
		thread =new Thread(runnable);
		thread.start();
		

		findViewById();// 找到界面上的控件
		setViewOnclickListener();// 设置监听器

	}

	private void setViewOnclickListener() {
		playBtn.setOnClickListener(this);
		refreshBtn.setOnClickListener(this);
	}

	private void findViewById() {
		playBtn = (Button) findViewById(R.id.play);
		refreshBtn = (Button) findViewById(R.id.refresh);
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
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
			map.put("songPicBig", getPicBig(music.getSongPicBig()));
			map.put("songLink", music.getSongLink());
			map.put("time", music.getTime());
			map.put("lrcLink", music.getLrcLink());
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

	@SuppressLint("UseValueOf")
	public void getMusicList() throws Exception {
		// 百度音乐搜索地址
		String channelPath = "http://fm.baidu.com/dev/api/?tn=playlist&id=public_yuzhong_huayu&special=flash&prepend=&format=json";
		String musicPath = "http://music.baidu.com/data/music/fmlink?songIds=";

		String channelJson = getJson(channelPath);// 获取该频道返回的musiclist
		JSONObject channelJsonObject = new JSONObject(channelJson);
		JSONArray array = (JSONArray) channelJsonObject.get("list");
		//不重复的随机数
		HashSet<Integer> set = new HashSet<Integer>();
		while(set.size()<10){
			set.add(new Integer(new Random().nextInt(100)));
		}
//		for (int i = 0; i < 10; i++) {
//			JSONObject o = (JSONObject) array.get(i);
//			musicPath = musicPath + o.get("id") + ",";
//		}
		for(int i :set){
			JSONObject o = (JSONObject) array.get(i);
			musicPath = musicPath + o.get("id") + ",";
		}
		musicPath = musicPath.substring(0, musicPath.length() - 1);
		// 处理musiclist
		String musicJson = getJson(musicPath);
		JSONObject musicJsonObject = new JSONObject(musicJson);
		JSONObject data = (JSONObject) musicJsonObject.get("data");
		array = (JSONArray) data.get("songList");
		musicList = new ArrayList<Music>();
	
		for (int i = 0; i < 10; i++) {
			JSONObject o = (JSONObject) array.get(i);
			Music music = new Music();
			music.setArtistName(o.getString("artistName"));
			music.setShowLink(o.getString("showLink"));
			music.setSongId(o.getString("songId"));
			music.setSongLink(o.getString("songLink"));
			music.setSongName(o.getString("songName"));
			music.setSongPicBig(o.getString("songPicBig"));
			music.setSongPicRadio(o.getString("songPicRadio"));
			music.setSize(o.getString("size"));
			music.setTime(o.getString("time"));
			
			music.setLrcLink("http://musicdata.baidu.com"+o.getString("lrcLink"));
			musicList.add(music);
		}
		mediaApp.setMusicList(musicList);
		
		// JSONArray jsonArray = new JSONArray(json);

	}

	public String getJson(String path) throws Exception {
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
		// int num = json.indexOf("{");
		// if (num != 0) {
		// json = json.substring(num);
		// }
		return json;
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
			mediaApp.setListPosition(position);
			playMusic();

		}

	}

	public void playMusic() {
		if (musicList != null) {
			Music music = musicList.get(listPosition);
			intent.putExtra("songLink", music.getSongLink());
			intent.putExtra("listPosition", listPosition);
			intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
			intent.putExtra("musicList", (Serializable)musicList);
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
		case R.id.refresh:
			thread =new Thread(runnable);
			thread.start();
		}
	}



	/**
	 * 按返回键弹出对话框确定退出
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("退出")
					.setMessage("您确定要退出？")
					.setNegativeButton("取消", null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
									Intent intent = new Intent(
											MainActivity.this,
											PlayService.class);
									// unregisterReceiver(homeReceiver);
									stopService(intent); // 停止后台服务
								}
							}).show();

		}
		return super.onKeyDown(keyCode, event);
	}

}
