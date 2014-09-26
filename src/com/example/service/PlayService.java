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
	private List<Music> musicList = new ArrayList<Music>();// �����б�
	private int listPosition; // ��¼��ǰ���ڲ��ŵ�����
	private int status = 3; // ����״̬��Ĭ��Ϊ˳�򲥷�
	private MyReceiver myReceiver; // �Զ���㲥������
	private int currentTime; // ��ǰ���Ž���
	private int duration; // ���ų���
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); // ��Ÿ���б����
	private int index = 0; // ��ʼ���ֵ

	// ����Ҫ���͵�һЩAction
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // ��ǰ���ֲ���ʱ����¶���
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// �����ֳ��ȸ��¶���
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // ֪ͨ��ʾ���
	public static final String SHOW_LRC_FINISHED = "com.wwj.action.SHOW_LRC_FINISHED"; // ֪ͨ��ʾ���

	/**
	 * handler����������Ϣ�������͹㲥���²���ʱ��
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (mediaPlayer != null) {
					currentTime = mediaPlayer.getCurrentPosition(); // ��ȡ��ǰ���ֲ��ŵ�λ��
					Intent intent = new Intent();
					intent.setAction(MUSIC_CURRENT);
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent); // ��PlayerActivity���͹㲥
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
		 * �������ֲ������ʱ�ļ�����
		 */
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (status == 1) { // ����ѭ��
					mediaPlayer.start();
				} else if (status == 2) { // ȫ��ѭ��
					listPosition++;
					mediaApp.setListPosition(listPosition);
					if (listPosition > musicList.size() - 1) { // ��Ϊ��һ�׵�λ�ü�������
						listPosition = 0;
						mediaApp.setListPosition(listPosition);
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
					sendBroadcast(sendIntent);
					path = musicList.get(listPosition).getSongLink();
					play(0);
				} else if (status == 3) { // ˳�򲥷�
					listPosition++; // ��һ��λ��
					mediaApp.setListPosition(listPosition);
					if (listPosition <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
						path = musicList.get(listPosition).getSongLink();
						play(0);
					} else {
						mediaPlayer.seekTo(0);
						listPosition = 0;
						mediaApp.setListPosition(listPosition);
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("listPosition", listPosition);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
					}
				} else if (status == 4) { // �������
					listPosition = getRandomIndex(musicList.size() - 1);
					mediaApp.setListPosition(listPosition);
					System.out.println("currentIndex ->" + listPosition);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("listPosition", listPosition);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
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

		if (msg == Constant.PlayerMsg.PLAY_MSG) { // ֱ�Ӳ�������
			play(0);
		} else if (msg == Constant.PlayerMsg.PAUSE_MSG) { // ��ͣ
			pause();
		} else if (msg == Constant.PlayerMsg.STOP_MSG) { // ֹͣ
			stop();
		} else if (msg == Constant.PlayerMsg.CONTINUE_MSG) { // ��������
			resume();
		} else if (msg == Constant.PlayerMsg.PREVIOUS_MSG) { // ��һ��
			previous();
		} else if (msg == Constant.PlayerMsg.NEXT_MSG) { // ��һ��
			next();
		} else if (msg == Constant.PlayerMsg.PROGRESS_CHANGE) { // ���ȸ���
			currentTime = intent.getIntExtra("progress", -1);
			play(currentTime);
		} else if (msg == Constant.PlayerMsg.PLAYING_MSG) {
			handler.sendEmptyMessage(1);
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
			handler.sendEmptyMessage(1);
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
		} else {
			stop();
		}
	}

	/**
	 * ��һ��
	 */
	private void previous() {
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("listPosition", listPosition);
		// ���͹㲥������Activity����е�BroadcastReceiver���յ�
		sendBroadcast(sendIntent);
		play(0);
	}

	/**
	 * ��һ��
	 */
	private void next() {
		Intent sendIntent = new Intent(UPDATE_ACTION);
		sendIntent.putExtra("listPosition", listPosition);
		// ���͹㲥������Activity����е�BroadcastReceiver���յ�
		sendBroadcast(sendIntent);
		play(0);
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
				status = 1; // ������״̬��Ϊ1��ʾ������ѭ��
				break;
			case 2:
				status = 2; // ������״̬��Ϊ2��ʾ��ȫ��ѭ��
				break;
			case 3:
				status = 3; // ������״̬��Ϊ3��ʾ��˳�򲥷�
				break;
			case 4:
				status = 4; // ������״̬��Ϊ4��ʾ���������
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
	 * ����ʱ���ȡ�����ʾ������ֵ
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
	 * ��ȡ���λ��
	 * 
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}
}
