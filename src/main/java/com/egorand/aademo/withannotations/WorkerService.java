package com.egorand.aademo.withannotations;

import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.UiThread;

@EService
public class WorkerService extends Service {

	private Messenger clientMessenger;
	private Messenger localMessenger = new Messenger(new IncomingHandler(this));

	private boolean isStopped = false;

	@Override
	public void onCreate() {
		super.onCreate();
		doWork();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return localMessenger.getBinder();
	}

	@Background
	void doWork() {
		int timer = 0;
		while (timer <= 100 && !isStopped) {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				Log.e(Constants.DEBUG_TAG, e.getLocalizedMessage(), e);
			}
			publishProgress(++timer);
		}
		finishWork();
	}

	@UiThread
	void publishProgress(int value) {
		Message msg = Message.obtain(null, Constants.MSG_PROGRESS_UPDATE);
		Bundle data = new Bundle();
		data.putInt(Constants.ARGUMENT_PROGRESS, value);
		msg.setData(data);
		try {
			if (clientMessenger != null) {
				clientMessenger.send(msg);
			}
		} catch (RemoteException e) {
			Log.e(Constants.DEBUG_TAG, e.getLocalizedMessage(), e);
		}
	}

	@UiThread
	void finishWork() {
		stopSelf();
	}

	private static class IncomingHandler extends Handler {

		private final WorkerService service;

		public IncomingHandler(WorkerService service) {
			this.service = service;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Constants.MSG_REGISTER_CLIENT: {
					service.clientMessenger = msg.replyTo;
				}
					break;
				case Constants.MSG_UNREGISTER_CLIENT: {
					service.isStopped = true;
					service.clientMessenger = null;
				}
					break;
				default: {
					super.handleMessage(msg);
				}
			}
		}
	}
}
