package com.example.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.entity.Music;

public class MediaUtil {
	
	private List<Music> musicList;
	
	public static String formateTime(int currentTime) {
		String minutes = currentTime / 60 + "";
		String second = currentTime % 60 + "";
		return minutes + ":" + second;
	}
	public List<Music> getMusic(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					getMusicList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return musicList;
	}
	
	private void  getMusicList() throws Exception {
		// 百度音乐搜索地址
		String channelPath = "http://fm.baidu.com/dev/api/?tn=playlist&id=public_yuzhong_huayu&special=flash&prepend=&format=json";
		String musicPath = "http://music.baidu.com/data/music/fmlink?songIds=";

		String channelJson = getJson(channelPath);// 获取该频道返回的musiclist
		JSONObject channelJsonObject = new JSONObject(channelJson);
		JSONArray array = (JSONArray) channelJsonObject.get("list");
		for (int i = 0; i < 10; i++) {
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
			music.setTime(o.getString("time"));
			music.setLrcLink(o.getString("lrcLink"));
			musicList.add(music);
		}

	}

	private String getJson(String path) throws Exception {
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
		return json;
	}
}
