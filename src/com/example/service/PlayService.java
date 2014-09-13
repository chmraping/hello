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
		if (msg == Constant.PlayerMsg.PLAY_MSG) { // �첽����
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
	 * �ָ���������
	 * 
	 */
	private void resume() {
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	}

	/**
	 * ��������
	 * 
	 * @param position
	 */
	private void play(int position) {
		try {
			if (mediaPlayer.isPlaying()) {
				stop();
			}
			mediaPlayer.reset();// �Ѹ�������ָ�����ʼ״̬
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(path);
			// mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.prepareAsync(); // �첽����
			mediaPlayer.setOnPreparedListener(new PreparedListener(position));// ע��һ��������
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ͣ����
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
	 * ֹͣ����
	 */
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			// try {
			// mediaPlayer.prepareAsync();//
			// �ڵ���stop�������Ҫ�ٴ�ͨ��start���в���,��Ҫ֮ǰ����prepare����
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
	 * ʵ��һ��OnPrepareLister�ӿ�,������׼���õ�ʱ��ʼ����
	 * 
	 */
	private final class PreparedListener implements OnPreparedListener {
		private int positon;

		public PreparedListener(int positon) {
			this.positon = positon;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start(); // ��ʼ����
			if (positon > 0) { // ������ֲ��Ǵ�ͷ����
				mediaPlayer.seekTo(positon);
			}
		}
	}
	// /** Called when MediaPlayer is ready */
	// public void onPrepared(MediaPlayer player) {
	// player.start();
	// }

}
