package com.example.service;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;

import com.example.activity.LrcFragment;
import com.example.entity.Constant;
import com.example.entity.LrcContent;
import com.example.entity.MediaApp;
import com.example.entity.Music;

@SuppressLint("NewApi")
public class PlayService extends Service {
	private MediaPlayer mediaPlayer;
	private String path;
	private int msg;
	boolean isAlive;
	boolean isPause;
	private MediaApp mediaApp;
	private List<Music> musicList = new ArrayList<Music>();// 音乐列表
	private int listPosition; // 记录当前正在播放的音乐
	private int status = 3; // 播放状态，默认为顺序播放
	private MyReceiver myReceiver; // 自定义广播接收器
	private int currentTime; // 当前播放进度
	private int duration; // 播放长度
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); // 存放歌词列表对象
	private int index = 0; // 歌词检索值

	// 服务要发送的一些Action
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // 当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// 新音乐长度更新动作
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // 通知显示歌词
	public static final String SHOW_LRC_FINISHED = "com.wwj.action.SHOW_LRC_FINISHED"; // 通知显示歌词

	/**
	 * handler用来接收消息，来发送广播更新播放时间
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (mediaPlayer != null) {
					currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
					Intent intent = new Intent();
					intent.setAction(MUSIC_CURRENT);
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent); // 给PlayerActivity发送广播
					handler.sendEmptyMessageDelayed(1, 1000);
				}
			}
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mediaApp =(MediaApp) getApplication();
		mediaPlayer = new MediaPlayer();
		/**
		 * 设置音乐播放完成时的监听器
		 */
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (status == 1) { // 单曲循环
					mediaPlayer.start();
				} else if (status == 2) { // 全部循环
					listPosition++;
					mediaApp.setListPosition(listPosition);
					if (listPosition > musicList.size() - 1) { // 变为第一首的位置继续播放
						listPosition = 0;
						mediaApp.setListPosition(listPosition);
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getSongLink();
					play(0);
				} else if (status == 3) { // 顺序播放
					listPosition++; // 下一首位置
					mediaApp.setListPosition(listPosition);
					if (listPosition <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
						path = musicList.get(listPosition).getSongLink();
						play(0);
					} else {
						mediaPlayer.seekTo(0);
						listPosition = 0;
						mediaApp.setListPosition(listPosition);
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
					}
				} else if (status == 4) { // 随机播放
					listPosition = getRandomIndex(musicList.size() - 1);
					mediaApp.setListPosition(listPosition);
					System.out.println("currentIndex ->" + listPosition);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getSongLink();
					play(0);
				}
			}
		});

		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		filter.addAction(SHOW_LRC);
		filter.addAction(SHOW_LRC_FINISHED);
		registerReceiver(myReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		msg = intent.getIntExtra("MSG", 0);
		listPosition = mediaApp.getListPosition();
		musicList = mediaApp.getMusicList();
		path = intent.getStringExtra("songLink");

		if (msg == Constant.PlayerMsg.PLAY_MSG) { // 直接播放音乐
			play(0);
		} else if (msg == Constant.PlayerMsg.PAUSE_MSG) { // 暂停
			pause();
		} else if (msg == Constant.PlayerMsg.STOP_MSG) { // 停止
			stop();
		} else if (msg == Constant.PlayerMsg.CONTINUE_MSG) { // 继续播放
			resume();
		} else if (msg == Constant.PlayerMsg.PREVIOUS_MSG) { // 上一首
			previous();
		} else if (msg == Constant.PlayerMsg.NEXT_MSG) { // 下一首
			next();
		} else if (msg == Constant.PlayerMsg.PROGRESS_CHANGE) { // 进度更新
			currentTime = intent.getIntExtra("progress", -1);
			play(currentTime);
		} else if (msg == Constant.PlayerMsg.PLAYING_MSG) {
			handler.sendEmptyMessage(1);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 恢复播放音乐
	 * 
	 */
	private void resume() {
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	}

	/**
	 * 播放音乐
	 * 
	 * @param position
	 */
	private void play(int position) {
		try {
			if (mediaPlayer.isPlaying()) {
				stop();
			}
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(path);
			// mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.prepareAsync(); // 异步缓冲
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));// 注册一个监听器
			handler.sendEmptyMessage(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停音乐
	 */
	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPause = true;
		} else {
			stop();
		}
	}

	/**
	 * 上一首
	 */
	private void previous() {
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("listPosition", listPosition);
		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
		sendBroadcast(sendIntent);
		play(0);
	}

	/**
	 * 下一首
	 */
	private void next() {
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("listPosition", listPosition);
		// 发送广播，将被Activity组件中的BroadcastReceiver接收到
		sendBroadcast(sendIntent);
		play(0);
	}

	/**
	 * 停止音乐
	 */
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			// try {
			// mediaPlayer.prepareAsync();//
			// 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}

	/**
	 * 
	 * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
	 * 
	 */
	private final class PreparedListener implements OnPreparedListener {
		private int positon;

		public PreparedListener(int positon) {
			this.positon = positon;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start(); // 开始播放
			if (positon > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(positon);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public class MyReceiver extends BroadcastReceiver {

		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			int control = intent.getIntExtra("control", -1);
			switch (control) {
			case 1:
				status = 1; // 将播放状态置为1表示：单曲循环
				break;
			case 2:
				status = 2; // 将播放状态置为2表示：全部循环
				break;
			case 3:
				status = 3; // 将播放状态置为3表示：顺序播放
				break;
			case 4:
				status = 4; // 将播放状态置为4表示：随机播放
				break;
			}

			String action = intent.getAction();
			if (action.equals(SHOW_LRC)) {
//				listPosition = intent.getIntExtra("listPosition", -1);
				listPosition = mediaApp.getListPosition();
				lrcList = (List<LrcContent>) intent
						.getSerializableExtra("lrcList");
				handler.post(mRunnable);
			}
		}
	}

	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			// PlayActivity.lrcView.setIndex(lrcIndex());
			// PlayActivity.lrcView.invalidate();
			LrcFragment.lrcView.setIndex(lrcIndex());
			LrcFragment.lrcView.invalidate();
			handler.postDelayed(mRunnable, 100);
		}
	};

	/**
	 * 根据时间获取歌词显示的索引值
	 * 
	 * @return
	 */
	public int lrcIndex() {
		if (mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			duration = mediaPlayer.getDuration();
		}
		if (currentTime < duration) {
			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
						index = i;
					}
					if (currentTime > lrcList.get(i).getLrcTime()
							&& currentTime < lrcList.get(i + 1).getLrcTime()) {
						index = i;
					}
				}
				if (i == lrcList.size() - 1
						&& currentTime > lrcList.get(i).getLrcTime()) {
					index = i;
				}
			}
		}
		return index;
	}

	/**
	 * 获取随机位置
	 * 
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}
}
