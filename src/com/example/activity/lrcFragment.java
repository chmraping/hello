package com.example.activity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.example.entity.LrcContent;
import com.example.hello.R;
import com.example.view.LrcView;

public class LrcFragment extends Fragment {
	public static LrcView lrcView; // 自定义歌词视图
	private View view;
	private List<LrcContent> lrcList; // List集合存放歌词内容对象
	private LrcContent mLrcContent; // 声明一个歌词内容对象
	private String lrcLink;
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // 通知显示歌词
	public static final String SHOW_LRC_FINISHED = "com.wwj.action.SHOW_LRC_FINISHED"; // 通知显示歌词
	Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.music_lyric, null);
		lrcView = (LrcView) view.findViewById(R.id.lrcShowView);
		Intent intent = new Intent();
		view.getContext().sendBroadcast(intent);
		mLrcContent = new LrcContent();
		lrcList = new ArrayList<LrcContent>();
		if (lrcLink != null) {
			showLRC(lrcLink);
		}
		return view;
	}

	/**
	 * 读取歌词
	 * 
	 * @param path
	 * @return
	 */
	public void showLRC(String path) {

		// File f = new File(path.replace(".mp3", ".lrc"));
		this.lrcLink = path;
		if (view != null) {
			new Thread(runnable).start();
		}
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				// 定义一个StringBuilder对象，用来存放歌词内容
				// 创建一个文件输入流对象
				// FileInputStream fis = new FileInputStream(f);
				URL url = new URL(lrcLink);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setConnectTimeout(5 * 1000);
				connection.setDoInput(true);
				connection.connect();
				InputStreamReader isr = new InputStreamReader(
						connection.getInputStream(), "utf-8");

				BufferedReader br = new BufferedReader(isr);
				String s = "";
				while ((s = br.readLine()) != null) {
					// 替换字符
					s = s.replace("[", "");
					s = s.replace("]", "@");

					// 分离“@”字符
					String splitLrcData[] = s.split("@");
					if (splitLrcData.length > 1) {
						mLrcContent.setLrcStr(splitLrcData[1]);

						// 处理歌词取得歌曲的时间
						int lrcTime = time2Str(splitLrcData[0]);

						mLrcContent.setLrcTime(lrcTime);

						// 添加进列表数组
						lrcList.add(mLrcContent);

						// 新创建歌词内容对象
						mLrcContent = new LrcContent();
					}
				}
				// 传回处理后的歌词文件
				lrcView.setmLrcList(lrcList);
				// 切换带动画显示歌词
				lrcView.setAnimation(AnimationUtils.loadAnimation(
						view.getContext(), R.anim.alpha_z));
				Intent intent = new Intent();
				intent.setAction(SHOW_LRC);
				intent.putExtra("lrcList", (Serializable) lrcList);
				view.getContext().sendBroadcast(intent);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				stringBuilder.append("木有歌词文件，赶紧去下载！...");
			} catch (IOException e) {
				e.printStackTrace();
				stringBuilder.append("木有读取到歌词哦！");
			}

		}
	};

	/**
	 * 解析歌词时间 歌词内容格式如下： [00:02.32]陈奕迅 [00:03.43]好久不见 [00:05.22]歌词制作 王涛
	 * 
	 * @param timeStr
	 * @return
	 */
	public int time2Str(String timeStr) {
		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@"); // 将时间分隔成字符串数组

		// 分离出分、秒并转换为整型
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// 计算上一行与下一行的时间转换为毫秒数
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
		return currentTime;
	}

	public List<LrcContent> getLrcList() {
		return lrcList;
	}

	public String getLrcLink() {
		return lrcLink;
	}

	public void setLrcLink(String lrcLink) {
		this.lrcLink = lrcLink;
	}

}
