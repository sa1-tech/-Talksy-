package com.talksy.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
	public static String formatTimestamp(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
		return sdf.format(new Date(timestamp));
	}
}
