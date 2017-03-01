package com.dailystudio.app;

import android.app.Application;
import android.content.Context;

import com.dailystudio.GlobalContextWrapper;
import com.dailystudio.development.Logger;
import com.dailystudio.BuildConfig;

public class DevBricksApplication extends Application {

	private DevBricksApplicationAgent mDevBricksAgent;

	@Override
	public void onCreate() {
		super.onCreate();

		mDevBricksAgent = new DevBricksApplicationAgent(this);
		mDevBricksAgent.onCreate();
	}
	
	@Override
	public void onTerminate() {
		if (mDevBricksAgent != null) {
			mDevBricksAgent.onTerminate();
		}

		super.onTerminate();
	}

	protected boolean isDebugBuild() {
		if (mDevBricksAgent != null) {
			return mDevBricksAgent.isDebugBuild();
		}

		return false;
	}

}
