package cn.alotus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.openhtmltopdf.extend.SVGDrawer;
import com.openhtmltopdf.java2d.api.DefaultPageProcessor;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import cn.alotus.config.BuilderConfig;
import cn.alotus.config.BuilderConfig.BaseBuilderConfig;
import cn.alotus.core.io.file.FileNameUtil;
import cn.alotus.processor.BufferedImagePageProcessor;

/**
 * HtmlRender
 */
public class HtmlRender {

	private Float pageWidth = 123f;
	private Float pageHeight = 123f;
	private PageSizeUnits units = Java2DRendererBuilder.PageSizeUnits.MM;
	private int imageType = BufferedImage.TYPE_INT_RGB;
	private double scale = 2.0;

	private String fontPath;

	private volatile Boolean loggingEnabled=false;
	 
	public HtmlRender() {
		super();
	}

	public HtmlRender(Float pageWidth, Float pageHeight, PageSizeUnits units) {
		super();
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.units = units;
	}

	public HtmlRender(int imageType) {
		super();
		this.imageType = imageType;
	}

	public HtmlRender(int imageType, double scale) {
		super();
		this.imageType = imageType;
		this.scale = scale;
	}

	public HtmlRender(Float pageWidth, Float pageHeight, PageSizeUnits units, int imageType, double scale) {
		super();
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		this.units = units;
		this.imageType = imageType;
		this.scale = scale;
	}

	/**
	 * toImage
	 * 
	 * @param html   html
	 * @param config config
	 * @return BufferedImage
	 * @throws IOException
	 */
	public BufferedImage toImage(String html, BaseBuilderConfig... config) throws IOException {
		
		XRLog.setLoggingEnabled(loggingEnabled);
		
		Java2DRendererBuilder builder = new Java2DRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(pageWidth, pageHeight, units);
		builder.useEnvironmentFonts(true);
		builder.useFastMode();
		//字体
		WITH_FOOTS.configure(builder);
		//配置
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}
		
		builder.toSinglePage(bufferedImagePageProcessor);
		builder.runFirstPage();

		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages().get(0);

	}

	/**
	 * toImages
	 * 
	 * @param html   html
	 * @param config config
	 * @return List<BufferedImage>
	 * @throws IOException
	 */
	public List<BufferedImage> toImages(String html, BaseBuilderConfig... config) throws IOException {
		
		XRLog.setLoggingEnabled(loggingEnabled);
		
		Java2DRendererBuilder builder = new Java2DRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(pageWidth, pageHeight, units);
		builder.useEnvironmentFonts(true);
		builder.useFastMode();
		//字体
		WITH_FOOTS.configure(builder);
		//配置
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}
		builder.toPageProcessor(bufferedImagePageProcessor);
		builder.runPaged();

		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages();

	}

	/**
	 * toPdf
	 * 		OutputStream outputStream = new ByteArrayOutputStream(4096)
	 * @param html   html
	 * @param config config
	 * @return BufferedImage
	 * @throws IOException
	 */
	public void toPdf(String html,OutputStream outputStream, BaseBuilderConfig... config) throws IOException {
		
		XRLog.setLoggingEnabled(loggingEnabled);

		PdfRendererBuilder builder = new PdfRendererBuilder();

		builder.withHtmlContent(html, "");
		builder.useDefaultPageSize(pageWidth, pageHeight, units);
		builder.useFastMode();

		// pdf
		BuilderConfig.WITH_PDF.configure(builder);
		// 字体
		WITH_FOOTS.configure(builder);
		// 配置
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}

		builder.toStream(outputStream);
		builder.run();

	}

	
	/**
	 * toPng
	 * 
	 * @param html    html
	 * @param outPath outPath
	 * @throws IOException
	 */
	public void toPng(String html, String outPath) throws IOException {

		BufferedImage image = toPng(html);

		ImageIO.write(image, "PNG", new File(outPath));

	}
	
	/**
	 * toPng
	 * 
	 * @param html html
	 * @return BufferedImage
	 * @throws IOException
	 */
	public BufferedImage toPng(String html) throws IOException {

		BufferedImage image = toImage(html, BuilderConfig.WITH_BASE);
		
		return image;
	}
	
	
	/**
	 * fonts eg： .otf  .ttf
	 */
	public final BaseBuilderConfig WITH_FOOTS = (builder) -> {
		if(null!=fontPath) {
			File f = new File(fontPath);
			if (f.isDirectory()) {
				File[] files = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						String lower = name.toLowerCase();
						return lower.endsWith(".otf") || lower.endsWith(".ttf");
					}
				});
				for (int i = 0; i < files.length; i++) {
					builder.useFont(files[i], FileNameUtil.mainName(files[i]));
				}
			}
		}

	};

	
	
	public Float getPageWidth() {
		return pageWidth;
	}

	public void setPageWidth(Float pageWidth) {
		this.pageWidth = pageWidth;
	}

	public Float getPageHeight() {
		return pageHeight;
	}

	public void setPageHeight(Float pageHeight) {
		this.pageHeight = pageHeight;
	}

	public PageSizeUnits getUnits() {
		return units;
	}

	public void setUnits(PageSizeUnits units) {
		this.units = units;
	}

	public int getImageType() {
		return imageType;
	}

	public void setImageType(int imageType) {
		this.imageType = imageType;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public String getFontPath() {
		return fontPath;
	}

	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}
	
	public void addFontDirectory(String fontPath) {
		this.fontPath = fontPath;
	}
	
	
	public Boolean getLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(Boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	
	@SuppressWarnings("unused")
	@Deprecated
	private BufferedImage runRendererSingle(String html, final String filename) throws IOException {

		Java2DRendererBuilder builder = new Java2DRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 2.0);

		builder.useDefaultPageSize(650, 700, Java2DRendererBuilder.PageSizeUnits.MM);
		builder.useEnvironmentFonts(true);
		// 开发模式下开启可以打印信息
		builder.useFastMode();
		builder.testMode(true);

		String FONT_PATH = "D:\\myfonts";
		builder.useFont(new File(FONT_PATH + "/zitijiaaizaoziyikong.ttf"), "bzff");

		builder.toSinglePage(bufferedImagePageProcessor);

		builder.runFirstPage();

		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages().get(0);

		// ImageIO.write(image, "PNG", new File(filename));

		/*
		 * Render Multipage Image Files
		 */
		// builder.toPageProcessor(new DefaultPageProcessor(zeroBasedPageNumber -> new FileOutputStream(filename.replace(".png", "_" + zeroBasedPageNumber + ".png")), BufferedImage.TYPE_INT_ARGB, "PNG")).runPaged();

	}
	
	@Deprecated
	@SuppressWarnings("unused")
	private List<BufferedImage> runRendererPaged(String resourcePath, String html) {
		Java2DRendererBuilder builder = new Java2DRendererBuilder();
		builder.withHtmlContent(html, null);
		builder.useFastMode();
		builder.testMode(true);

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 1.0);

		builder.toPageProcessor(bufferedImagePageProcessor);

		BuilderConfig.J2D_WITH_FONT.configure(builder);

		try {
			builder.runPaged();
		} catch (Exception e) {
			System.err.println("Failed to render resource (" + resourcePath + ")");
			e.printStackTrace();
			return null;
		}

		return bufferedImagePageProcessor.getPageImages();
	}
	
	@Deprecated
	@SuppressWarnings("unused")
	private void renderSamplePNG(String html, final String filename) throws IOException {
		try (SVGDrawer svg = new BatikSVGDrawer(); SVGDrawer mathMl = new MathMLDrawer()) {

			Java2DRendererBuilder builder = new Java2DRendererBuilder();
			builder.useSVGDrawer(svg);
			builder.useMathMLDrawer(mathMl);

			builder.withHtmlContent(html, "");

			BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_ARGB, 2.0);

			builder.useDefaultPageSize(150, 130, Java2DRendererBuilder.PageSizeUnits.MM);

			builder.useEnvironmentFonts(true);
			// 开发模式下开启可以打印信息
			builder.useFastMode();
			builder.testMode(true);

			String FONT_PATH = "D:\\myfonts";
			builder.useFont(new File(FONT_PATH + "/zitijiaaizaoziyikong.ttf"), "bzff");

			/*
			 * Render Single Page Image
			 */
			builder.toSinglePage(bufferedImagePageProcessor).runFirstPage();
			BufferedImage image = bufferedImagePageProcessor.getPageImages().get(0);

			ImageIO.write(image, "PNG", new File(filename));

			/*
			 * Render Multipage Image Files
			 */
			builder.toPageProcessor(new DefaultPageProcessor(zeroBasedPageNumber -> new FileOutputStream(filename.replace(".png", "_" + zeroBasedPageNumber + ".png")), BufferedImage.TYPE_INT_ARGB, "PNG")).runPaged();

		}
	}

	@SuppressWarnings("unused")
	private void renderPDF(String html, PdfAConformance pdfaConformance, OutputStream outputStream) throws IOException {
		try (SVGDrawer svg = new BatikSVGDrawer(); SVGDrawer mathMl = new MathMLDrawer()) {

			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useSVGDrawer(svg);
			builder.useMathMLDrawer(mathMl);
			builder.addDOMMutator(LaTeXDOMMutator.INSTANCE);
			builder.usePdfAConformance(pdfaConformance);
			builder.withHtmlContent(html, "");
			builder.toStream(outputStream);
			builder.run();
		}
	}

	public static String readHtml(String absResPath) throws IOException {

		try (InputStream htmlIs = new FileInputStream(absResPath)) {
			byte[] htmlBytes = IOUtils.toByteArray(htmlIs);
			return new String(htmlBytes, StandardCharsets.UTF_8);
		}

	}

}
