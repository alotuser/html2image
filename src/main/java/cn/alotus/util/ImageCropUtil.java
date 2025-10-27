package cn.alotus.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * 图片截取工具类
 */
public class ImageCropUtil {

	/**
	 * 截取图片并保存
	 */
	public static boolean cropAndSave(String inputPath, String outputPath, int x, int y, int width, int height) {
		try {
			BufferedImage sourceImage = ImageIO.read(new File(inputPath));
			Rectangle rect = new Rectangle(x, y, width, height);

			BufferedImage croppedImage = cropImage(sourceImage, rect);

			String format = getFileFormat(outputPath);
			return ImageIO.write(croppedImage, format, new File(outputPath));

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 安全的图片截取方法
	 */
	public static BufferedImage cropImage(BufferedImage sourceImage, Rectangle rect) {
		// 边界检查
		int x = Math.max(0, rect.x);
		int y = Math.max(0, rect.y);
		int width = Math.min(rect.width, sourceImage.getWidth() - x);
		int height = Math.min(rect.height, sourceImage.getHeight() - y);

		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("截取区域超出图片范围");
		}

		BufferedImage croppedImage = new BufferedImage(width, height, sourceImage.getType());
		Graphics2D g = croppedImage.createGraphics();

		// 设置渲染质量
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(sourceImage, 0, 0, width, height, x, y, x + width, y + height, null);
		g.dispose();

		return croppedImage;
	}
	/**
	 * 获取文件格式
	 * @param filePath
	 * @return
	 */
	private static String getFileFormat(String filePath) {
		String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
		return extension.toLowerCase();
	}

}
