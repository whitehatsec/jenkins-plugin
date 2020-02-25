package hudson.plugins.utils;

import java.io.File;

public class RestrictFileSize {
	public static boolean checkSize(double maxSize, String filePath) {
		File file = new File(filePath);

		// Get length of file in bytes
		long fileSizeInBytes = file.length();
		// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
		double fileSizeInKB = fileSizeInBytes / 1024.0;
		// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
		double maxSizeInMB = maxSize * 1024;// fileSizeInKB / 1024;

		if (fileSizeInKB > maxSizeInMB) {
			return true;
		} else {
			return false;
		}
	}
}
