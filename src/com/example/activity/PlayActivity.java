package com.example.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.MusicPageAdapter;
import com.example.entity.Constant;
import com.example.entity.MediaApp;
import com.example.entity.Music;
import com.example.hello.R;
import com.example.service.PlayService;
import com.example.util.MediaUtil;
import com.example.view.LrcView;

public class PlayActivity extends FragmentActivity {
	private MediaApp mediaApp;
	private Button play;
	private Button previous;
	private Button next;
	private TextView musicTitle;// ��������
	private String songLink; // ����·��
	List<Fragment> fragments;
	private ViewPager pager;

	private int listPosition;
	private List<Music> musicList;// �����б�
	private Music music;
	private MusicPageAdapter adapter;
	private Button queueBtn; // �����б�
	private SeekBar music_progressBar; // ��������
	private TextView currentProgress; // ��ǰ�������ĵ�ʱ��
	private TextView finalProgress; // ����ʱ��

	private int currentTime; // ��ǰ��������ʱ��
	private int duration; // ��������
	private int flag; // ���ű�ʶ

	private boolean isPlaying; // ���ڲ���
	private boolean isPause; // ��ͣ
	public static LrcView lrcView; // �Զ�������ͼ

	private MusicImageAlbumFragment musicAlbumFragment = new MusicImageAlbumFragment();
	private LrcFragment lrcFragment = new LrcFragment();
	private PlayerReceiver playerReceiver;
	public static final String UPDATE_ACTION = "com.wwj.action.UPDATE_ACTION"; // ���¶���
	public static final String CTL_ACTION = "com.wwj.action.CTL_ACTION"; // ���ƶ���
	public static final String MUSIC_CURRENT = "com.wwj.action.MUSIC_CURRENT"; // ���ֵ�ǰʱ��ı䶯��
	public static final String MUSIC_DURATION = "com.wwj.action.MUSIC_DURATION";// ���ֲ��ų��ȸı䶯��
	public static final String MUSIC_PLAYING = "com.wwj.action.MUSIC_PLAYING"; // �������ڲ��Ŷ���
	public static final String REPEAT_ACTION = "com.wwj.action.REPEAT_ACTION"; // �����ظ����Ŷ���
	public static final String SHUFFLE_ACTION = "com.wwj.action.SHUFFLE_ACTION";// ����������Ŷ���
	public static final String SHOW_LRC = "com.wwj.action.SHOW_LRC"; // ֪ͨ��ʾ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity_layout);
		mediaApp =(MediaApp) getApplication();
		findViewById();
		setViewOnclickListener();
		getDataFromBundle();

		fragments = new ArrayList<Fragment>();
		fragments.add(musicAlbumFragment);
		fragments.add(lrcFragment);
		adapter = new MusicPageAdapter(getSupportFragmentManager(), fragments);
		pager.setAdapter(adapter);
		registerReceiver();
		initView();

	}

	private void initView() {
		musicTitle.setText(music.getSongName());
		musicAlbumFragment.setUrl(music.getSongPicRadio());
		lrcFragment.setLrcLink(music.getLrcLink());
		if (isPlaying) {
			play.setBackgroundResource(R.drawable.pause);
		}
		finalProgress.setText(MediaUtil.formatSecond(Integer.parseInt(music.getTime())));
	}

	private void findViewById() {
		pager = (ViewPager) findViewById(R.id.pager);
		play = (Button) findViewById(R.id.play);
		previous = (Button) findViewById(R.id.previous);
		next = (Button) findViewById(R.id.next);
		musicTitle = (TextView) findViewById(R.id.musicTitle);
		music_progressBar = (SeekBar) findViewById(R.id.audioTrack);
		currentProgress = (TextView) findViewById(R.id.current_progress);
		finalProgress = (TextView) findViewById(R.id.final_progress);
	}

	private void setViewOnclickListener() {
		ViewOnclickListener viewOnclickListener = new ViewOnclickListener();
		play.setOnClickListener(viewOnclickListener);
		previous.setOnClickListener(viewOnclickListener);
		next.setOnClickListener(viewOnclickListener);
//		music_progressBar.setOnSeekBarChangeListener(new SeekBarChangeListener());   ���߻���û���
	}

	private void registerReceiver() {
		// �����ע��㲥������
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_ACTION);
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		registerReceiver(playerReceiver, filter);
	}

	/**
	 * ��Bundle�л�ȡ����HomeActivity�д�����������
	 */
	private void getDataFromBundle() {
		Intent intent = getIntent();

		listPosition = mediaApp.getListPosition();

		isPlaying = intent.getBooleanExtra("isPlaying", false);
		isPause = intent.getBooleanExtra("isPause", false);
		// musicList = (List<Music>) intent.getSerializableExtra("musicList");
		mediaApp = (MediaApp) getApplication();
		musicList = mediaApp.getMusicList();
		music = musicList.get(listPosition);
	

	}

	/**
	 * �ؼ�����¼�
	 * 
	 * @author wwj
	 * 
	 */
	private class ViewOnclickListener implements OnClickListener {
		Intent intent = new Intent();

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play:
				if (isPlaying) {
					play.setBackgroundResource(R.drawable.play_selector);
					intent.setClass(PlayActivity.this, PlayService.class);
					intent.putExtra("MSG", Constant.PlayerMsg.PAUSE_MSG);
					startService(intent);
					isPlaying = false;
					isPause = true;
				} else if (isPause) {
					play.setBackgroundResource(R.drawable.pause_selector);
					intent.setClass(PlayActivity.this, PlayService.class);
					intent.putExtra("MSG", Constant.PlayerMsg.CONTINUE_MSG);
					startService(intent);
					isPause = false;
					isPlaying = true;
				}else{
					play.setBackgroundResource(R.drawable.pause_selector);
					intent.setClass(PlayActivity.this, PlayService.class);
					intent.putExtra("MSG", Constant.PlayerMsg.PLAY_MSG);
					startService(intent);
					isPause = false;
					isPlaying = true;
				}
				break;
			case R.id.previous: // ��һ�׸���
				previous_music();
				break;
			case R.id.next: // ��һ�׸���
				next_music();
				break;
			}
		}

		/**
		 * ��һ��
		 */
		public void previous_music() {
			listPosition = listPosition - 1;
			mediaApp.setListPosition(listPosition);
			if (listPosition >= 0) {
				play.setBackgroundResource(R.drawable.play_selector);
				music = musicList.get(listPosition); // ��һ��MP3

				Intent intent = new Intent(PlayActivity.this, PlayService.class);
				// intent.setAction("com.wwj.media.MUSIC_SERVICE");
				intent.putExtra("songLink", music.getSongLink());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("MSG", Constant.PlayerMsg.PREVIOUS_MSG);
				startService(intent);

			} else {
				listPosition = 0;
				Toast.makeText(PlayActivity.this, "û����һ����", Toast.LENGTH_SHORT)
						.show();
			}
		}

		/**
		 * ��һ��
		 */
		public void next_music() {
			listPosition = listPosition + 1;
			mediaApp.setListPosition(listPosition);
			if (listPosition <= musicList.size() - 1) {
				play.setBackgroundResource(R.drawable.play_selector);
				music = musicList.get(listPosition);
				Intent intent = new Intent(PlayActivity.this, PlayService.class);
				// intent.setAction("com.wwj.media.MUSIC_SERVICE");
				intent.putExtra("songLink", music.getSongLink());
				intent.putExtra("listPosition", listPosition);
				intent.putExtra("MSG", Constant.PlayerMsg.NEXT_MSG);
				startService(intent);

			} else {
				listPosition = musicList.size() - 1;
				mediaApp.setListPosition(listPosition);
				Toast.makeText(PlayActivity.this, "û����һ����", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	/**
	 * �������մ�service�������Ĺ㲥���ڲ���
	 * 
	 * 
	 */
	public class PlayerReceiver extends BroadcastReceiver {

		Music music = musicList.get(listPosition);

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				currentTime = intent.getIntExtra("currentTime", -1);
				// currentProgress.setText(MediaUtil.formatTime(currentTime));
				 currentProgress.setText(MediaUtil.formatTime(currentTime));
				duration = Integer.parseInt(music.getTime()) * 1000;
				long pos = music_progressBar.getMax() * currentTime / duration;
				music_progressBar.setProgress((int) pos);
			} else if (action.equals(MUSIC_DURATION)) {
				duration = Integer.parseInt(music.getTime()) * 1000;
				music_progressBar.setMax(duration);
				finalProgress.setText(duration);
			} else if (action.equals(UPDATE_ACTION)) {
				// ��ȡIntent�е�current��Ϣ��current����ǰ���ڲ��ŵĸ���
				listPosition = mediaApp.getListPosition();
				songLink = musicList.get(listPosition).getSongLink();
				music = musicList.get(listPosition);
				duration = Integer.parseInt(music.getTime()) * 1000;
				if (listPosition >= 0) {
					musicTitle.setText(music.getSongName());
					// musicArtist.setText(musicList.get(listPosition).getArtistName());
					play.setBackgroundResource(R.drawable.pause);
				}
				if (listPosition == 0) {
					finalProgress.setText(duration);
					play.setBackgroundResource(R.drawable.pause_selector);
					isPause = true;
				}
				musicAlbumFragment.setUrl(music.getSongPicRadio());
				musicAlbumFragment.updateAlbum();
				lrcFragment.showLRC(music.getLrcLink());
			}
		}
	}

	/**
	 * ʵ�ּ���Seekbar����
	 * 
	 * @author wwj
	 * 
	 */
	private class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			switch (seekBar.getId()) {
			case R.id.audioTrack:
				if (fromUser) {
					audioTrackChange(progress); // �û����ƽ��ȵĸı�
				}
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	}

	/**
	 * ���Ž��ȸı�
	 * 
	 * @param progress
	 */
	public void audioTrackChange(int progress) {
		Intent intent = new Intent(PlayActivity.this, PlayService.class);
		intent.putExtra("listPosition", listPosition);
		intent.putExtra("MSG", Constant.PlayerMsg.PROGRESS_CHANGE);
		intent.putExtra("progress", progress);
		startService(intent);
	}
}
