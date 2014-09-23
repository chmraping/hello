package com.example.entity;

import java.io.Serializable;

public class Music implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String songId;
	private String songName;
	private String artistName;
	private String songPicBig;
	private String songPicRadio;
	private String time;
	private String songLink;
	private String showLink;
	private String lrcLink;
	private String size;
	public String getSongId() {
		return songId;
	}
	public void setSongId(String songId) {
		this.songId = songId;
	}
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public String getSongPicBig() {
		return songPicBig;
	}
	public void setSongPicBig(String songPicBig) {
		this.songPicBig = songPicBig;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getSongLink() {
		return songLink;
	}
	public void setSongLink(String songLink) {
		this.songLink = songLink;
	}
	public String getShowLink() {
		return showLink;
	}
	public void setShowLink(String showLink) {
		this.showLink = showLink;
	}
	public String getLrcLink() {
		return lrcLink;
	}
	public void setLrcLink(String lrcLink) {
		this.lrcLink = lrcLink;
	}
	public void setSongPicRadio(String songPicRadio) {
		this.songPicRadio = songPicRadio;
		
	}
	public String getSongPicRadio() {
		return songPicRadio;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}

	
	
}
