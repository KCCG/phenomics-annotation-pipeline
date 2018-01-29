package au.org.garvan.kccg.annotations.pipeline.engine.annotators.util;

public class TimeUtil {

	public static double start() {
		return System.currentTimeMillis();
	}

	public static String end(double startTime) {
		double endTime = System.currentTimeMillis();
		return formatTime((endTime - startTime) / 1000);
	}

	public static String formatTime(double totalTime) {
		if (totalTime > 60) {
			double mins = totalTime / 60;
			int secs = (int) totalTime % 60;

			if (mins > 60) {
				int hors = (int) (mins / 60);
				mins = mins % 60;
				int m = (int) mins;

				if (secs == 0) {
					if (m == 0) {
						return Integer.toString(hors) + "h";
					} else {
						return Integer.toString(hors) + "h"
								+ Integer.toString(m) + "m";
					}
				} else {
					if (m == 0) {
						return Integer.toString(hors) + "h"
								+ Integer.toString(secs) + "s";
					} else {
						return Integer.toString(hors) + "h"
								+ Integer.toString(m) + "m"
								+ Integer.toString(secs) + "s";
					}
				}
			} else {
				int m = (int) mins;
				if (secs == 0) {
					return Integer.toString(m) + "m";
				} else {
					return Integer.toString(m) + "m" + Integer.toString(secs)
							+ "s";
				}
			}
		} else {
			int t = (int) totalTime;
			return Integer.toString(t) + "s";
		}
	}
}
