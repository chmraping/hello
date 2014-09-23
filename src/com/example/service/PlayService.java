package com.example.service;

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

import com.example.entity.Constant;
import com.example.entity.Music;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class PlayService extends Service {
	private MediaPlayer mediaPlayer;
	private String path;

	private int msg;
	boolean isAlive;
	boolean isPause;
	
	private int currentTime; // 当前播放进度
	private int time; // 播放长度
	private int status = 3; // 播放状态，默认为顺序播放
	private List<Music> musicList; // 存放musice对象的集合
	private int currentMusic = 0; // 记录当前正在播放的音乐
	
	ServiceReceiver receiver;//消息接收器

	// 服务要发送的一些Action
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // 更新动作
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // 控制动作
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // 当前音乐播放时间更新动作
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// 新音乐长度更新动作
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // 通知显示歌词

	/**
	 * handler用来接收消息，来发送广播更新播放时间
	 */
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
					currentMusic++;
					if(currentMusic > musicList.size() - 1) {	//变为第一首的位置继续播放
						currentMusic = 0;
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", currentMusic);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					path = musicList.get(currentMusic).getSongLink();
					play(0);
				} else if (status == 3) { // 顺序播放
					currentMusic++;	//下一首位置
					if (currentMusic <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", currentMusic);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
						path = musicList.get(currentMusic).getSongLink();
						play(0);
					}else {
						mediaPlayer.seekTo(0);
						currentMusic = 0;
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", currentMusic);
						// 发送广播，将被Activity组件中的BroadcastReceiver接收到
						sendBroadcast(sendIntent);
					}
				} else if(status == 4) {	//随机播放
					currentMusic = getRandomIndex(musicList.size() - 1);
					System.out.println("currentIndex ->" + currentMusic);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", currentMusic);
					// 发送广播，将被Activity组件中的BroadcastReceiver接收到
					sendBroadcast(sendIntent);
					path = musicList.get(currentMusic).getSongLink();
					play(0);
				}
			}
		});
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		path = intent.getStringExtra("songLink");
		currentMusic = intent.getIntExtra("listPosition", -1);
		msg = intent.getIntExtra("MSG", 0);
		musicList = (List<Music>) intent.getSerializableExtra("musicList");
		
		if (msg == Constant.PlayerMsg.PLAY_MSG) { 
			play(0);
		} else if (msg == Constant.PlayerMsg.PAUSE_MSG) {
			pause();
		} else if (msg == Constant.PlayerMsg.STOP_MSG) {
			stop();
		} else if (msg == Constant.PlayerMsg.CONTINUE_MSG) {
			resume();
		}else if (msg == Constant.PlayerMsg.NEXT_MSG) {
			next();
		}else if (msg == Constant.PlayerMsg.PREVIOUS_MSG) {
			previous();
		}
		receiver = new ServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		registerReceiver(receiver, filter);
		
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public boolean stopService(Intent name) {
		unregisterReceiver(receiver);
		return super.stopService(name);
	}
	
	private void previous() {
		Intent preIntent = new Intent(UPDATE_ACTION);
		preIntent.putExtra("current", currentMusic);
		sendBroadcast(preIntent);
		play(0);
		
	}

	private void next() {
		Intent nextIntent = new Intent();
		nextIntent.setAction(UPDATE_ACTION);
		nextIntent.putExtra("current", currentMusic);
		sendBroadcast(nextIntent);
		play(0);
	}

	/**
	 * 获取随机位置
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
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
//			if (mediaPlayer.isPlaying()) {
//				stop();
//			}
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
	public class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			

			}
		}
		
	

}
