package cn.alotus.util;

/**
 * 文件工具类
 */
public class FilesUtil {

	public static final char SLASH = '/';
	public static final char BACKSLASH = '\\';
	public static boolean isFileSeparator(char c) {
		return SLASH == c || BACKSLASH == c;
	}
	/**
	 * 返回文件名<br>
	 * <pre>
	 * "d:/test/aaa" 返回 "aaa"
	 * "/test/aaa.jpg" 返回 "aaa.jpg"
	 * </pre>
	 *
	 * @param filePath 文件
	 * @return 文件名
	 * @since 4.1.13
	 */
	public static String getName(String filePath) {
		if (null == filePath) {
			return null;
		}
		int len = filePath.length();
		if (0 == len) {
			return filePath;
		}
		if (isFileSeparator(filePath.charAt(len - 1))) {
			// 以分隔符结尾的去掉结尾分隔符
			len--;
		}

		int begin = 0;
		char c;
		for (int i = len - 1; i > -1; i--) {
			c = filePath.charAt(i);
			if (isFileSeparator(c)) {
				// 查找最后一个路径分隔符（/或者\）
				begin = i + 1;
				break;
			}
		}

		return filePath.substring(begin, len);
	}
	
}
