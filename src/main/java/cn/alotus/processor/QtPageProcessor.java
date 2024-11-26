package cn.alotus.processor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.openhtmltopdf.java2d.api.FSPage;
import com.openhtmltopdf.java2d.api.FSPageOutputStreamSupplier;
import com.openhtmltopdf.java2d.api.FSPageProcessor;
/**
 * PageProcessor to render everything to buffered images
 */
public class QtPageProcessor implements FSPageProcessor {

	private final FSPageOutputStreamSupplier _osFactory;

	private final int _imageType;
	private final double _scale;
	private final String _imageFormat;

	private List<QtPage> _pages = new ArrayList<>();
	
	
	private class QtPage implements FSPage {
		private final BufferedImage _img;
		private final Graphics2D _g2d;
		private final int _pgNo;
		private final FSPageOutputStreamSupplier _osf;
		private final String _imgFrmt;

		public QtPage(int pgNo, int w, int h, FSPageOutputStreamSupplier osFactory, int imageType, String imageFormat) {
			_img = new BufferedImage(w, h, imageType);
			_g2d = _img.createGraphics();

			if (_img.getColorModel().hasAlpha()) {
				/* We need to clear with white transparent */
				_g2d.setBackground(new Color(255, 255, 255, 0));
				_g2d.clearRect(0, 0, (int) _img.getWidth(), (int) _img.getHeight());
			} else {
				_g2d.setColor(Color.WHITE);
				_g2d.fillRect(0, 0, (int) _img.getWidth(), (int) _img.getHeight());
			}

			_pgNo = pgNo;
			_osf = osFactory;
			_imgFrmt = imageFormat;

			/*
			 * Apply the scale on the bitmap
			 */
			_g2d.scale(_scale, _scale);

		}

		@Override
		public Graphics2D getGraphics() {
			return _g2d;
		}

		public void save() {
			OutputStream os = null;
			try {
				os = _osf.supply(_pgNo);
				ImageIO.write(_img, _imgFrmt, os);
			} catch (IOException e) {
				throw new RuntimeException("Couldn't write page image to output stream", e);
			} finally {
				if (os != null)
					try {
						os.close();
					} catch (IOException e) {
					}
			}
		}
	}

	/**
	 * Creates a page processor which saves each page as an image.
	 * 
	 * @param osFactory   must supply an output stream for each page. The os
	 *                    will be closed by the page processor.
	 * @param imageType   must be a constant from the BufferedImage class.
	 * @param imageFormat must be a format such as png or jpeg
	 */
	public QtPageProcessor(FSPageOutputStreamSupplier osFactory, int imageType, String imageFormat) {
		_osFactory = osFactory;
		_imageType = imageType;
		this._scale = 1;
		_imageFormat = imageFormat;
	}

	public QtPageProcessor(FSPageOutputStreamSupplier _osFactory, int _imageType, double _scale, String _imageFormat) {
		this._osFactory = _osFactory;
		this._imageType = _imageType;
		this._scale = _scale;
		this._imageFormat = _imageFormat;
	}

	/**
	 * Create a graphics device that can be supplied to useLayoutGraphics.
	 * The caller is responsible for calling dispose on the returned device.
	 * 
	 * @return Graphics2D
	 */
	public Graphics2D createLayoutGraphics() {
		BufferedImage bf = new BufferedImage(1, 1, _imageType);
		return bf.createGraphics();
	}

	@Override
	public FSPage createPage(int zeroBasedPageNumber, int width, int height) {
		QtPage qtPage = new QtPage(zeroBasedPageNumber, (int) (width * _scale), (int) (height * _scale), _osFactory, _imageType, _imageFormat);
		_pages.add(qtPage);
		return qtPage;
	}

	@Override
	public void finishPage(FSPage pg) {
		QtPage page = (QtPage) pg;
		page.getGraphics().dispose();
		page.save();
	}
	
	public List<BufferedImage> getPageImages() {
		List<BufferedImage> images = new ArrayList<>();
		for (QtPage page : _pages) {
			images.add(page._img);
		}
		return images;
	}
}
