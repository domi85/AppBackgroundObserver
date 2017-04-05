package com.domi.appbackgroundobserver;

import android.app.Application;
import android.util.Log;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		AppBackgroundObserver observer = new AppBackgroundObserver(this);
		observer.registerCallback(new AppBackgroundObserver.AppBackgroundObserverCallback() {
			@Override
			public void onAppBackground() {
				Log.d("App", "onAppBackground");
			}

			@Override
			public void onAppForeground() {
				Log.d("App", "onAppForeground");
			}
		});
		observer.start();
	}
}
