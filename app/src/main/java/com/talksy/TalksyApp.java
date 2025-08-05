package com.talksy;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class TalksyApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		FirebaseApp.initializeApp(this);
	}
}
