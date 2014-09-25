package com.example.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.example.entity.LrcContent;

/**
 * 
 * 
 * @author �����ʵ���
 */
public class LrcProcess {
	private List<LrcContent> lrcList; // List���ϴ�Ÿ�����ݶ���
	private LrcContent mLrcContent; // ����һ��������ݶ���
	private String path;
	public static final String SHOW_LRC_FINISHED = "com.wwj.action.SHOW_LRC_FINISHED"; // ֪ͨ��ʾ���

	/**
	 * �޲ι��캯������ʵ��������
	 */
	public LrcProcess() {
		mLrcContent = new LrcContent();
		lrcList = new ArrayList<LrcContent>();
	}

	/**
	 * ��ȡ���
	 * 
	 * @param path
	 * @return
	 */
	public void readLRC(String path) {

		// File f = new File(path.replace(".mp3", ".lrc"));
		this.path = path;
		if (path != null) {
			new Thread(runnable).start();
		}
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				// ����һ��StringBuilder����������Ÿ������
				// ����һ���ļ�����������
				// FileInputStream fis = new FileInputStream(f);
				URL url = new URL(path);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	            connection.setConnectTimeout(5*1000);  
	            connection.setDoInput(true);  
	            connection.connect();  
				InputStreamReader isr = new InputStreamReader(connection.getInputStream(),
						"utf-8");

				BufferedReader br = new BufferedReader(isr);
				String s = "";
				while ((s = br.readLine()) != null) {
					// �滻�ַ�
					s = s.replace("[", "");
					s = s.replace("]", "@");

					// ���롰@���ַ�
					String splitLrcData[] = s.split("@");
					if (splitLrcData.length > 1) {
						mLrcContent.setLrcStr(splitLrcData[1]);

						// ������ȡ�ø�����ʱ��
						int lrcTime = time2Str(splitLrcData[0]);

						mLrcContent.setLrcTime(lrcTime);

						// ��ӽ��б�����
						lrcList.add(mLrcContent);

						// �´���������ݶ���
						mLrcContent = new LrcContent();
					}
				}
				Intent intent = new Intent();
				intent.setAction(SHOW_LRC_FINISHED);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				stringBuilder.append("ľ�и���ļ����Ͻ�ȥ���أ�...");
			} catch (IOException e) {
				e.printStackTrace();
				stringBuilder.append("ľ�ж�ȡ�����Ŷ��");
			}

		}
	};

	/**
	 * �������ʱ�� ������ݸ�ʽ���£� [00:02.32]����Ѹ [00:03.43]�þò��� [00:05.22]������� ����
	 * 
	 * @param timeStr
	 * @return
	 */
	public int time2Str(String timeStr) {
		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@"); // ��ʱ��ָ����ַ�������

		// ������֡��벢ת��Ϊ����
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// ������һ������һ�е�ʱ��ת��Ϊ������
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
		return currentTime;
	}

	public List<LrcContent> getLrcList() {
		return lrcList;
	}
}
