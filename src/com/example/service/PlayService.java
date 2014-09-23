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
	
	private int currentTime; // ��ǰ���Ž���
	private int time; // ���ų���
	private int status = 3; // ����״̬��Ĭ��Ϊ˳�򲥷�
	private List<Music> musicList; // ���musice����ļ���
	private int currentMusic = 0; // ��¼��ǰ���ڲ��ŵ�����
	
	ServiceReceiver receiver;//��Ϣ������

	// ����Ҫ���͵�һЩAction
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // ��ǰ���ֲ���ʱ����¶���
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// �����ֳ��ȸ��¶���
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // ֪ͨ��ʾ���

	/**
	 * handler����������Ϣ�������͹㲥���²���ʱ��
	 */
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
					currentMusic++;
					if(currentMusic > musicList.size() - 1) {	//��Ϊ��һ�׵�λ�ü�������
						currentMusic = 0;
					}
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", currentMusic);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
					sendBroadcast(sendIntent);
					path = musicList.get(currentMusic).getSongLink();
					play(0);
				} else if (status == 3) { // ˳�򲥷�
					currentMusic++;	//��һ��λ��
					if (currentMusic <= musicList.size() - 1) {
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", currentMusic);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
						path = musicList.get(currentMusic).getSongLink();
						play(0);
					}else {
						mediaPlayer.seekTo(0);
						currentMusic = 0;
						Intent sendIntent = new Intent(UPDATE_ACTION);
						sendIntent.putExtra("current", currentMusic);
						// ���͹㲥������Activity����е�BroadcastReceiver���յ�
						sendBroadcast(sendIntent);
					}
				} else if(status == 4) {	//�������
					currentMusic = getRandomIndex(musicList.size() - 1);
					System.out.println("currentIndex ->" + currentMusic);
					Intent sendIntent = new Intent(UPDATE_ACTION);
					sendIntent.putExtra("current", currentMusic);
					// ���͹㲥������Activity����е�BroadcastReceiver���յ�
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
	 * ��ȡ���λ��
	 * @param end
	 * @return
	 */
	protected int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
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
//			if (mediaPlayer.isPlaying()) {
//				stop();
//			}
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
	public class ServiceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			

			}
		}
		
	

}
