package com.example.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MusicPageAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragments;

	//构造方法,传入fragments
	public MusicPageAdapter(FragmentManager fm,List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
		System.out.println("fragments");
	}
	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}
	@Override
	public int getCount() {
		return fragments.size();
	}





}
