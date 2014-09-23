package com.example.activity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.example.entity.Music;
import com.example.hello.R;
import com.example.service.PlayService;

public class MainActivity extends Activity implements OnClickListener {
	private RelativeLayout play_layout;
	private ListView musicListView;// �����б����
	private SimpleAdapter mAdapter;// �б�����������
	private List<Music> musicList = new ArrayList<Music>();// �����б�

	private boolean isPlaying = false; // ���ڲ���
	private boolean isPause = false; // ��ͣ

	private Button playBtn;
	// private Button previousBtn;
	// private Button nextBtn;

	private Intent intent = new Intent();
	private int listPosition = 0; // ��ʶ�б�λ��
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		musicListView = (ListView) findViewById(R.id.musicListView);
		musicListView.setOnItemClickListener(new MusicListItemClickListener());
		play_layout = (RelativeLayout) findViewById(R.id.play_layout);
		play_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						PlayActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("musicList", (Serializable) musicList);
				bundle.putInt("listPosition", listPosition);
				bundle.putBoolean("isPlaying", isPlaying);
				bundle.putBoolean("isPause", isPause);
				intent.putExtras(bundle);
//				Music music = musicList.get(listPosition);
//				intent.putExtra("listPosition", listPosition);
//				intent.putExtra("songLink", music.getSongLink());
//				intent.putExtra("songPicRadio", music.getSongPicRadio());
//				intent.putExtra("songName", music.getSongName());
//				intent.putExtra("artistName", music.getArtistName());
				startActivity(intent);

			}
		});
		new Thread(runnable).start();

		findViewById();// �ҵ������ϵĿؼ�
		setViewOnclickListener();// ���ü�����

	}

	private void setViewOnclickListener() {
		playBtn.setOnClickListener(this);
	}

	private void findViewById() {
		playBtn = (Button) findViewById(R.id.play);
	}


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
			public void run() { // �÷�������UI�߳���ִ��
				musicListView.setAdapter(mAdapter);
			}
		});

	}

	public void getMusicList() throws Exception {
		// �ٶ�����������ַ
		String channelPath = "http://fm.baidu.com/dev/api/?tn=playlist&id=public_yuzhong_huayu&special=flash&prepend=&format=json";
		String musicPath = "http://music.baidu.com/data/music/fmlink?songIds=";

		String channelJson = getJson(channelPath);// ��ȡ��Ƶ�����ص�musiclist
		JSONObject channelJsonObject = new JSONObject(channelJson);
		JSONArray array = (JSONArray) channelJsonObject.get("list");
		for (int i = 0; i < 10; i++) {
			JSONObject o = (JSONObject) array.get(i);
			musicPath = musicPath + o.get("id") + ",";
		}
		musicPath = musicPath.substring(0, musicPath.length() - 1);
		// ����musiclist
		String musicJson = getJson(musicPath);
		JSONObject musicJsonObject = new JSONObject(musicJson);
		JSONObject data = (JSONObject) musicJsonObject.get("data");
		array = (JSONArray) data.get("songList");
		
		for (int i = 0; i < array.length(); i++) {
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
//			 music.setSongPicBig(getPicBig(o.getString("songPicBig")));
			music.setTime(o.getString("time"));
			music.setLrcLink(o.getString("lrcLink"));
			musicList.add(music);
		}

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
			playMusic();

		}

	}

	public void playMusic() {
		if (musicList != null) {
			Music music = musicList.get(listPosition);
			intent.putExtra("songLink", music.getSongLink());
			intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
			intent.putExtra("musicList", (Serializable)musicList);
			intent.setClass(MainActivity.this, PlayService.class);
			startService(intent);
			playBtn.setBackgroundResource(R.drawable.pause_selector);// �ı�ͼ��
			isPlaying = true;
			isPause = false;

		}
	}

	public void pauseMusic() {
		intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
		intent.setClass(MainActivity.this, PlayService.class);
		startService(intent);
		playBtn.setBackgroundResource(R.drawable.play_selector);// �ı�ͼ��
		isPlaying = false;
		isPause = true;
	}

	public void continueMusic() {
		intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
		intent.setClass(MainActivity.this, PlayService.class);
		startService(intent);
		playBtn.setBackgroundResource(R.drawable.pause_selector);// �ı�ͼ��
		isPlaying = true;
		isPause = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play:
			if (isPlaying) {
				pauseMusic();// ��ͣ����
			} else if (isPause) {
				continueMusic();
			} else {
				playMusic();
			}
			break;

		}
	}

	/**
	 * �����ؼ������Ի���ȷ���˳�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("�˳�")
					.setMessage("��ȷ��Ҫ�˳���")
					.setNegativeButton("ȡ��", null)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
									Intent intent = new Intent(
											MainActivity.this,
											PlayService.class);
									// unregisterReceiver(homeReceiver);
									stopService(intent); // ֹͣ��̨����
								}
							}).show();

		}
		return super.onKeyDown(keyCode, event);
	}

}
