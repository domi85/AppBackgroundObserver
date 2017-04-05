package com.domi.appbackgroundobserver;

import static android.content.Intent.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

public class AppBackgroundObserver {
	private static final int BACKGROUND = 0;
	private static final int FOREGROUND = 1;

	@IntDef({BACKGROUND, FOREGROUND})
	@Retention(RetentionPolicy.SOURCE)
	public @interface AppState {
	}

	private final AppLifecycleCallback appLifecycleCallback = new AppLifecycleCallback();
	private final AppUiHiddenCallback appUiHiddenCallback = new AppUiHiddenCallback();
	private final AppScreenOffReceiver appScreenOffReceiver = new AppScreenOffReceiver();
	private final AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

	private final Application app;
	private final List<AppBackgroundObserverCallback> appBackgroundObserverCallbackList = new ArrayList<>();

	@AppState
	private int appState = BACKGROUND;

	public AppBackgroundObserver(Application app) {
		this.app = app;
	}

	public void start() {
		app.registerActivityLifecycleCallbacks(appLifecycleCallback);
		app.registerComponentCallbacks(appUiHiddenCallback);
		app.registerReceiver(appScreenOffReceiver, new IntentFilter(ACTION_SCREEN_OFF));
	}

	public void stop() {
		app.unregisterActivityLifecycleCallbacks(appLifecycleCallback);
		app.unregisterComponentCallbacks(appUiHiddenCallback);
		app.unregisterReceiver(appScreenOffReceiver);
	}

	public void registerCallback(@NonNull AppBackgroundObserverCallback callback) {
		appBackgroundObserverCallbackList.add(callback);
	}

	public void unregisterCallback(@NonNull AppBackgroundObserverCallback callback) {
		appBackgroundObserverCallbackList.remove(callback);
	}

	private boolean isAppInForeground() {
		return appState == FOREGROUND;
	}

	private boolean isAppInBackground() {
		return appState == BACKGROUND;
	}

	private void onAppForeground() {
		appState = FOREGROUND;

		invokeOnAppForeground();
	}

	private void onAppBackground() {
		appState = BACKGROUND;

		invokeOnAppBackground();
	}

	private void invokeOnAppForeground() {
		for (AppBackgroundObserverCallback each : appBackgroundObserverCallbackList) {
			each.onAppForeground();
		}
	}

	private void invokeOnAppBackground() {
		for (AppBackgroundObserverCallback each : appBackgroundObserverCallbackList) {
			each.onAppBackground();
		}
	}

	private class AppLifecycleCallback implements Application.ActivityLifecycleCallbacks {
		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		}

		@Override
		public void onActivityStarted(Activity activity) {
			if (isFirstLaunch.compareAndSet(true, false)) {
				onAppForeground();
				return;
			}

			if (isAppInBackground()) {
				onAppForeground();
			}
		}

		@Override
		public void onActivityResumed(Activity activity) {
		}

		@Override
		public void onActivityPaused(Activity activity) {
		}

		@Override
		public void onActivityStopped(Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
		}
	}

	private class AppUiHiddenCallback implements ComponentCallbacks2 {

		@Override
		public void onTrimMemory(int level) {
			if (isUiHidden(level)) {
				onAppBackground();
			}
		}

		private boolean isUiHidden(int level) {
			return level == TRIM_MEMORY_UI_HIDDEN;
		}

		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}

		@Override
		public void onLowMemory() {
		}
	}

	private class AppScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			onAppBackground();
		}
	}

	public interface AppBackgroundObserverCallback {
		void onAppBackground();

		void onAppForeground();
	}
}
