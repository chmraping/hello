package com.example.entity;

import java.io.Serializable;

/**
 * 2013/6/1
 * @author wwj
 * ���ʵ����
 */
public class LrcContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String lrcStr;	//�������
	private int lrcTime;	//��ʵ�ǰʱ��
	public String getLrcStr() {
		return lrcStr;
	}
	public void setLrcStr(String lrcStr) {
		this.lrcStr = lrcStr;
	}
	public int getLrcTime() {
		return lrcTime;
	}
	public void setLrcTime(int lrcTime) {
		this.lrcTime = lrcTime;
	}
}
