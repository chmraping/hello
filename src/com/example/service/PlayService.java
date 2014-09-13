package com.example.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;

import com.example.entity.Constant;

@SuppressLint("NewApi")
public class PlayService extends Service {
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private String path;
	private int msg;
	boolean isAlive;
	boolean isPause;

	// private Thread thread;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		path = intent.getStringExtra("songLink");
		msg = intent.getIntExtra("MSG", 0);
		if(mediaPlayer.isPlaying()){
			System.out.println(1);
		}
		if (msg == Constant.PlayerMsg.PLAY_MSG) { // 异步缓冲
			play(0);
		} else if (msg == Constant.PlayerMsg.PAUSE_MSG) {
			pause();
		} else if (msg == Constant.PlayerMsg.STOP_MSG) {
			stop();
		} else if (msg == Constant.PlayerMsg.CONTINUE_MSG) {
			resume();
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
		}else{
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
	// /** Called when MediaPlayer is ready */
	// public void onPrepared(MediaPlayer player) {
	// player.start();
	// }

}
