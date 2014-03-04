package com.github.hrobertson.headtrackudp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SenderThread extends Thread {
	
	private Handler mHandler;
	
	@Override
	public void run() {	
    Looper.prepare();
    mHandler = new Handler();
    Looper.loop();
	}
	
	public void requestStop() {
		// using the handler, post a Runnable that will quit()
		// the Looper attached to our DownloadThread
		// obviously, all previously queued tasks will be executed
		// before the loop gets the quit Runnable
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// This is guaranteed to run on the DownloadThread
				// so we can use myLooper() to get its looper
				Log.i("HeadTrack", "SenderThread loop quitting by request");
				
				Looper.myLooper().quit();
			}
		});
	}
	
	public void enquePacket(UDPSender sender) {
		mHandler.post(sender);
	}
	
}
