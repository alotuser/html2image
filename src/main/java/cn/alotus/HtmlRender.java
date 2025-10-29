package cn.alotus;

import java.awt.Rectangle;
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
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.text.html.HTML;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;

import com.openhtmltopdf.extend.SVGDrawer;
import com.openhtmltopdf.java2d.api.DefaultPageProcessor;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import cn.alotus.builder.AsRendererBuilder;
import cn.alotus.config.BuilderConfig;
import cn.alotus.config.BuilderConfig.BaseBuilderConfig;
import cn.alotus.config.BuilderConfig.PdfBuilderConfig;
import cn.alotus.core.io.file.FileNameUtil;
import cn.alotus.core.util.StrUtil;
import cn.alotus.processor.BufferedImagePageProcessor;
import cn.alotus.renderer.AsRenderer;

/**
 * HtmlRender
 */
public class HtmlRender {

	private Float pageWidth = 123f;
	private Float pageHeight = 123f;
	private PageSizeUnits units = AsRendererBuilder.PageSizeUnits.MM;
	private int imageType = BufferedImage.TYPE_INT_RGB;
	private double scale = 1.0;
	// private final float x=2.54F*10F/72F;//0.35277778
	private boolean useXp=true;
	private String fontPath;
	private String baseDocumentUri;
	private volatile Boolean loggingEnabled = false;

	private AsRenderer asRenderer;
	public HtmlRender() {
		super();
	}

	public HtmlRender(Float pageWidth, Float pageHeight, PageSizeUnits units) {
		super();
		setPageHeight(pageHeight);
		setPageWidth(pageWidth);

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
		setPageHeight(pageHeight);
		setPageWidth(pageWidth);
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

		AsRendererBuilder builder = new AsRendererBuilder();

		builder.withHtmlContent(html, baseDocumentUri);
		 
		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(getPageWidth(), getPageHeight(), units);
		builder.useEnvironmentFonts(true);
		builder.usePixelDimensions(true);
		builder.useFastMode();
		// 字体
		WITH_FOOTS.configure(builder);
		// 配置
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}

		builder.toSinglePage(bufferedImagePageProcessor);

		asRenderer=builder.runFirstPage();

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

		AsRendererBuilder builder = new AsRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(getPageWidth(), getPageHeight(), units);
		builder.useFastMode();
		// 字体
		WITH_FOOTS.configure(builder);
		// 配置
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}
		builder.toPageProcessor(bufferedImagePageProcessor);
		asRenderer=builder.runPaged();

		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages();

	}

	/**
	 * toPdf OutputStream outputStream = new ByteArrayOutputStream(4096)
	 * 
	 * @param html         html
	 * @param outputStream outputStream
	 * @param config       config
	 * @throws IOException
	 */
	public void toPdf(String html, OutputStream outputStream, PdfBuilderConfig... config) throws IOException {

		toPdf((builder) -> {
			builder.withHtmlContent(html, "");
			// builder.useDefaultPageSize(pageWidth, pageHeight, units);
			builder.toStream(outputStream);
		}, (builder) -> {
			// 配置
			for (PdfBuilderConfig baseBuilderConfig : config) {
				baseBuilderConfig.configure(builder);
			}
		});

	}

	/**
	 * toPdf
	 * 
	 * @param config config
	 * @throws IOException
	 */
	public void toPdf(PdfBuilderConfig... config) throws IOException {

		XRLog.setLoggingEnabled(loggingEnabled);

		PdfRendererBuilder builder = new PdfRendererBuilder();

		// pdf
		BuilderConfig.WITH_PDF.configure(builder);
		// 字体
		WITH_FOOTS.configure(builder);
		// 配置
		for (PdfBuilderConfig builderConfig : config) {
			builderConfig.configure(builder);
		}

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
	 * fonts eg： .otf .ttf
	 */
	public final BaseBuilderConfig WITH_FOOTS = (builder) -> {
		if (null != fontPath) {
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

	
	
	/**
	 * Find elements by ID and return their content area rectangles.
	 * 
	 * @param id The ID of the element to find.
	 * @return A map of elements to their content area rectangles.
	 */
	public Map<Element, Rectangle> findById(String id) {
		
		
		if (asRenderer == null) {
			throw new IllegalStateException("Please call toImage or toImages method first to initialize the renderer.");
		}
		
		return asRenderer.findElementRectangle(e -> {
			return StrUtil.equals(id, e.getAttribute(HTML.Attribute.ID.toString()));
		});
	}

	/**
	 * Find elements by name and return their content area rectangles.
	 * 
	 * @param name The name of the element to find.
	 * @return A map of elements to their content area rectangles.
	 */
	public Map<Element, Rectangle> findByName(String name) {
		if (asRenderer == null) {
			throw new IllegalStateException("Please call toImage or toImages method first to initialize the renderer.");
		}
		return asRenderer.findElementRectangle(e -> {
			return StrUtil.equals(name, e.getAttribute(HTML.Attribute.NAME.toString()));
		});
	}

	/**
	 * Find elements by CSS class and return their content area rectangles.
	 * 
	 * @param cssClass The CSS class of the element to find.
	 * @return A map of elements to their content area rectangles.
	 */
	public Map<Element, Rectangle> findByClass(String cssClass) {
		if (asRenderer == null) {
			throw new IllegalStateException("Please call toImage or toImages method first to initialize the renderer.");
		}
		return asRenderer.findElementRectangle(e -> {
			return StrUtil.equals(cssClass, e.getAttribute(HTML.Attribute.CLASS.toString()));
		});
	}

	/**
	 * Find elements by tag name and return their content area rectangles.
	 * 
	 * @param tagName The tag name of the element to find.
	 * @return A map of elements to their content area rectangles.
	 */
	public Map<Element, Rectangle> findByTagName(String tagName) {
		if (asRenderer == null) {
			throw new IllegalStateException("Please call toImage or toImages method first to initialize the renderer.");
		}
		return asRenderer.findElementRectangle(e -> {
			return StrUtil.equals(tagName, e.getTagName());
		});
	}

	/**
	 * Find elements by arbitrary attribute selector and return their content area rectangles.
	 * 
	 * @param name  The attribute name.
	 * @param value The attribute value.
	 * @return A map of elements to their content area rectangles.
	 */
	public Map<Element, Rectangle> findBySelector(String name, String value) {
		if (asRenderer == null) {
			throw new IllegalStateException("Please call toImage or toImages method first to initialize the renderer.");
		}
		return asRenderer.findElementRectangle(e -> {
			return StrUtil.equals(value, e.getAttribute(name));
		});
	}
	
	/**
	 * pageWidth
	 * @return pageWidth
	 */
	public Float getPageWidth() {
		return pageWidth;
	}

	/**
	 * pageWidth
	 * 
	 * @param pageWidth
	 */
	public void setPageWidth(Float pageWidth) {
		this.pageWidth = pageWidth;
	}

	/**
	 * pageHeight
	 * 
	 * @return pageHeight
	 */
	public Float getPageHeight() {
		return pageHeight;
	}

	/**
	 * pageHeight
	 * 
	 * @param pageHeight
	 */
	public void setPageHeight(Float pageHeight) {
		this.pageHeight = pageHeight;
	}

	/**
	 * units
	 * 
	 * @return units
	 */
	public PageSizeUnits getUnits() {
		return units;
	}

	/**
	 * units
	 * 
	 * @param units
	 */
	public void setUnits(PageSizeUnits units) {
		this.units = units;
	}

	/**
	 * imageType
	 * 
	 * @return imageType
	 */
	public int getImageType() {
		return imageType;
	}

	/**
	 * imageType
	 * 
	 * @param imageType
	 */
	public void setImageType(int imageType) {
		this.imageType = imageType;
	}

	/**
	 * scale
	 * 
	 * @return scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * scale
	 * 
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	/**
	 * fontPath
	 * 
	 * @return fontPath
	 */
	public String getFontPath() {
		return fontPath;
	}

	/**
	 * fontPath
	 * 
	 * @param fontPath
	 */
	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}

	/**
	 * addFontDirectory
	 * 
	 * @param fontPath
	 */
	public void addFontDirectory(String fontPath) {
		this.fontPath = fontPath;
	}

	/**
	 * loggingEnabled
	 * 
	 * @return loggingEnabled
	 */
	public Boolean getLoggingEnabled() {
		return loggingEnabled;
	}

	/**
	 * loggingEnabled
	 * 
	 * @param loggingEnabled
	 */
	public void setLoggingEnabled(Boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
	/**
	 * Pixel Dimensions is the size parameter of an exponential character image in two-dimensional space, usually represented in two dimensions: length and width, with units of pixels (px). For example, the pixel dimension of a photo may be labeled as "1920 × 1080", indicating that it contains 1920 pixels in the length direction and 1080 pixels in the width direction.
	 * @return useXp
	 */
	public boolean isUseXp() {
		return useXp;
	}
	/**
	 * Pixel Dimensions is the size parameter of an exponential character image in two-dimensional space, usually represented in two dimensions: length and width, with units of pixels (px). For example, the pixel dimension of a photo may be labeled as "1920 × 1080", indicating that it contains 1920 pixels in the length direction and 1080 pixels in the width direction.
	 * @param useXp
	 */
	public void setUseXp(boolean useXp) {
		this.useXp = useXp;
	}
	/**
	 * baseDocumentUri the base document URI to resolve future relative resources (e.g. images)
	 * @return
	 */
	public String getBaseDocumentUri() {
		return baseDocumentUri;
	}
	/**
	 * baseDocumentUri the base document URI to resolve future relative resources (e.g. images)
	 * @param baseDocumentUri
	 */
	public void setBaseDocumentUri(String baseDocumentUri) {
		this.baseDocumentUri = baseDocumentUri;
	}

	/**
	 * getAsRenderer
	 * 
	 * @return AsRenderer
	 */
	public AsRenderer getAsRenderer() {
		return asRenderer;
	}
	
	
	/**
	 * create
	 * 
	 * @return HtmlRender
	 */
	public static HtmlRender create() {
		return new HtmlRender();
	}

	/**
	 * create
	 * 
	 * @param pageWidth  pageWidth
	 * @param pageHeight pageHeight
	 * @param units      units
	 * @return HtmlRender
	 */
	public static HtmlRender create(Float pageWidth, Float pageHeight, PageSizeUnits units) {
		return new HtmlRender(pageWidth, pageHeight, units);
	}

	/**
	 * create
	 * 
	 * @param imageType imageType
	 * @return HtmlRender
	 */
	public static HtmlRender create(int imageType) {
		return new HtmlRender(imageType);
	}

	/**
	 * create
	 * 
	 * @param imageType imageType
	 * @param scale     scale
	 * @return HtmlRender
	 */
	public static HtmlRender create(int imageType, double scale) {
		return new HtmlRender(imageType, scale);
	}

	/**
	 * create
	 * 
	 * @param pageWidth  pageWidth
	 * @param pageHeight pageHeight
	 * @param units      units
	 * @param imageType  imageType
	 * @param scale      scale
	 * @return HtmlRender
	 */
	public static HtmlRender create(Float pageWidth, Float pageHeight, PageSizeUnits units, int imageType, double scale) {
		return new HtmlRender(pageWidth, pageHeight, units, imageType, scale);
	}
	
 
	
	
	@SuppressWarnings("unused")
	@Deprecated
	private BufferedImage runRendererSingle(String html, final String filename) throws IOException {

		AsRendererBuilder builder = new AsRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 2.0);

		builder.useDefaultPageSize(650, 700, AsRendererBuilder.PageSizeUnits.MM);
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
		AsRendererBuilder builder = new AsRendererBuilder();
		builder.withHtmlContent(html, null);
		builder.useFastMode();
		builder.testMode(true);

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 1.0);

		builder.toPageProcessor(bufferedImagePageProcessor);

		// BuilderConfig.J2D_WITH_FONT.configure(builder);

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

			AsRendererBuilder builder = new AsRendererBuilder();
			builder.useSVGDrawer(svg);
			builder.useMathMLDrawer(mathMl);

			builder.withHtmlContent(html, "");

			BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_ARGB, 2.0);

			builder.useDefaultPageSize(150, 130, AsRendererBuilder.PageSizeUnits.MM);

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
