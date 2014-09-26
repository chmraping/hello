package com.example.entity;

import java.util.List;

import android.app.Application;

public class MediaApp extends Application{
	private int listPosition =0;
	private List<Music> musicList;
	
	
	public int getListPosition() {
		return listPosition;
	}
	public void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}
	public List<Music> getMusicList() {
		return musicList;
	}
	public void setMusicList(List<Music> musicList) {
		this.musicList = musicList;
	}
	
}
