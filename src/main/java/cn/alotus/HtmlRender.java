
package cn.alotus;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
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
 * <pre>
 * HtmlRender is a convenience facade around OpenHTMLToPDF's building and rendering facilities. 
 * It provides simple APIs to convert HTML content into images (single or multi-page), PDF streams, and PNG files. 
 * The class exposes configuration points for page size, units, image type, scaling, font directories and base document URI.
 *
 * Usage summary: 
 * - configure the instance (page size, units, fonts, baseDocumentUri, etc.) 
 * - call toImage / toImages / toPdf / toPng to render content 
 * - after rendering, use getAsRenderer() and findBy* methods to locate element rectangles
 *
 * Threading and lifecycle: 
 *- HtmlRender holds an internal AsRenderer instance after rendering; findBy* methods require that rendering has been executed first (toImage or toImages).
 *
 * Note: 
 * - This class aims to keep rendering details encapsulated and expose common flows used in the project.
 *</pre>
 * @author alotus
 * @version 1.0
 * @date 2024-05-15
 */
public class HtmlRender {

	// Page width in the configured units (default value provided).
	private Float pageWidth = 123f;

	// Page height in the configured units (default value provided).
	private Float pageHeight = 123f;

	// Units for page size measurements (MM, PT, IN, etc.).
	private PageSizeUnits units = AsRendererBuilder.PageSizeUnits.MM;

	// BufferedImage type used for produced images (e.g. BufferedImage.TYPE_INT_RGB).
	private int imageType = BufferedImage.TYPE_INT_RGB;

	// Global scale factor applied when rendering images.
	private double scale = 1.0;

	// Whether to use pixel dimensions for sizing (true = use px).
	private boolean usePx = true;

	// Optional directory or path that contains font files (.ttf, .otf).
	// When set, fonts from this path will be registered with the builder.
	private String fontPath;

	// Base document URI used to resolve relative resources (images, CSS, etc.).
	private String baseDocumentUri;

	// Controls logging for OpenHTMLToPDF internals.
	private volatile Boolean loggingEnabled = false;

	// Internal renderer instance produced by builder after a render call.
	private AsRenderer asRenderer;

	public HtmlRender() {
		super();
	}

	/**
	 * Construct with explicit page width and height (units set separately).
	 *
	 * @param pageWidth  page width in configured units
	 * @param pageHeight page height in configured units
	 */
	public HtmlRender(Float pageWidth, Float pageHeight) {
		super();
		setPageHeight(pageHeight);
		setPageWidth(pageWidth);
	}

	/**
	 * Construct with explicit page size and units.
	 *
	 * @param pageWidth  page width
	 * @param pageHeight page height
	 * @param units      units for page size
	 */
	public HtmlRender(Float pageWidth, Float pageHeight, PageSizeUnits units) {
		super();
		setPageHeight(pageHeight);
		setPageWidth(pageWidth);

		this.units = units;
	}

	/**
	 * Construct specifying image buffer type.
	 *
	 * @param imageType {@link BufferedImage imageType}
	 */
	public HtmlRender(int imageType) {
		super();
		this.imageType = imageType;
	}

	/**
	 * Construct specifying image buffer type and a scale factor.
	 *
	 * @param imageType {@link BufferedImage imageType}
	 * @param scale     rendering scale multiplier
	 */
	public HtmlRender(int imageType, double scale) {
		super();
		this.imageType = imageType;
		this.scale = scale;
	}

	/**
	 * Full constructor allowing page size, units, image type and scale to be set.
	 *
	 * @param pageWidth  page width
	 * @param pageHeight page height
	 * @param units      units for page size
	 * @param imageType  {@link BufferedImage imageType}
	 * @param scale      rendering scale multiplier
	 */
	public HtmlRender(Float pageWidth, Float pageHeight, PageSizeUnits units, int imageType, double scale) {
		super();
		setPageHeight(pageHeight);
		setPageWidth(pageWidth);
		this.units = units;
		this.imageType = imageType;
		this.scale = scale;
	}

	/**
	 * Render provided HTML into a single BufferedImage.
	 *
	 * This method sets up a builder, applies configured fonts and any supplied builder configurations, and renders the first page as an image.
	 *
	 * @param html   the HTML content to render
	 * @param config optional configuration callbacks applied to the builder
	 * @return BufferedImage containing the first page render
	 * @throws IOException when IO or rendering resources fail
	 */
	public BufferedImage toImage(String html, BaseBuilderConfig... config) throws IOException {

		XRLog.setLoggingEnabled(loggingEnabled);

		AsRendererBuilder builder = new AsRendererBuilder();

		builder.withHtmlContent(html, baseDocumentUri);

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(getPageWidth(), getPageHeight(), units);
		builder.useEnvironmentFonts(true);
		builder.usePixelDimensions(usePx);
		builder.useFastMode();
		// register fonts if provided
		WITH_FOOTS.configure(builder);
		// apply external configurations
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}

		builder.toSinglePage(bufferedImagePageProcessor);

		asRenderer = builder.runFirstPage();

		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages().get(0);

	}

	/**
	 * Render provided HTML into a list of BufferedImages (one per page).
	 *
	 * @param html   the HTML content to render
	 * @param config optional configuration callbacks applied to the builder
	 * @return List of BufferedImage, one entry per generated page
	 * @throws IOException when IO or rendering resources fail
	 */
	public List<BufferedImage> toImages(String html, BaseBuilderConfig... config) throws IOException {

		XRLog.setLoggingEnabled(loggingEnabled);

		AsRendererBuilder builder = new AsRendererBuilder();

		builder.withHtmlContent(html, baseDocumentUri);

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(getPageWidth(), getPageHeight(), units);
		builder.useFastMode();
		// register fonts if provided
		WITH_FOOTS.configure(builder);
		// apply external configurations
		for (BaseBuilderConfig baseBuilderConfig : config) {
			baseBuilderConfig.configure(builder);
		}
		builder.toPageProcessor(bufferedImagePageProcessor);
		asRenderer = builder.runPaged();

		/*
		 * Render Paged Image(s)
		 */
		return bufferedImagePageProcessor.getPageImages();

	}

	/**
	 * Convert HTML to PDF and write to provided OutputStream.
	 *
	 * This convenience method delegates to a builder-based flow. The provided PdfBuilderConfig instances are applied before running the builder.
	 *
	 * @param html         html content to render
	 * @param outputStream output stream to receive PDF bytes
	 * @param config       optional pdf builder configuration callbacks
	 * @throws IOException on IO errors
	 */
	public void toPdf(String html, OutputStream outputStream, PdfBuilderConfig... config) throws IOException {

		toPdf((builder) -> {
			builder.withHtmlContent(html, baseDocumentUri);
			// builder.useDefaultPageSize(pageWidth, pageHeight, units);
			builder.toStream(outputStream);
		}, (builder) -> {
			// apply provided pdf configs
			for (PdfBuilderConfig baseBuilderConfig : config) {
				baseBuilderConfig.configure(builder);
			}
		});

	}

	/**
	 * Render to PDF using PdfRendererBuilder and any provided PdfBuilderConfig.
	 *
	 * This method configures PDF-specific settings (PDF/A, fonts, etc.) and runs the builder synchronously.
	 *
	 * @param config optional pdf builder configuration callbacks
	 * @throws IOException on IO errors during rendering
	 */
	public void toPdf(PdfBuilderConfig... config) throws IOException {

		XRLog.setLoggingEnabled(loggingEnabled);

		PdfRendererBuilder builder = new PdfRendererBuilder();

		// configure pdf capabilities
		BuilderConfig.WITH_PDF.configure(builder);
		// register fonts if provided
		WITH_FOOTS.configure(builder);
		// apply external configurations
		for (PdfBuilderConfig builderConfig : config) {
			builderConfig.configure(builder);
		}

		builder.run();

	}

	/**
	 * Write rendered HTML as PNG file to disk (single page).
	 *
	 * @param html    html content
	 * @param outPath destination file path
	 * @throws IOException on IO errors
	 */
	public void toPng(String html, String outPath) throws IOException {

		BufferedImage image = toPng(html);

		ImageIO.write(image, "PNG", new File(outPath));

	}

	/**
	 * Render HTML to a single BufferedImage (PNG-ready).
	 *
	 * @param html html content
	 * @return BufferedImage the rendered image of the first page
	 * @throws IOException on IO errors
	 */
	public BufferedImage toPng(String html) throws IOException {

		BufferedImage image = toImage(html, BuilderConfig.WITH_BASE);

		return image;
	}

	/**
	 * Font registration helper: if fontPath is a directory, register all .ttf and .otf files with the renderer builder. This allows using those fonts via CSS @font-family in HTML.
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
	 * Requires that a previous render has initialized the internal AsRenderer.
	 *
	 * @param id The ID of the element to find.
	 * @return A map of DOM Element to its content Rectangle
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
	 * Find elements by name attribute and return their content area rectangles.
	 *
	 * @param name The value of the name attribute to match.
	 * @return Map of Element to Rectangle for matched elements
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
	 * Find elements by CSS class (class attribute) and return their rectangles.
	 *
	 * @param cssClass CSS class string to match
	 * @return Map of Element to Rectangle for matched elements
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
	 * Find elements by tag name and return their rectangles.
	 *
	 * Matches element tag names (case-sensitive as provided by DOM).
	 *
	 * @param tagName element tag name to match
	 * @return Map of Element to Rectangle for matched elements
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
	 * Find elements by arbitrary attribute name/value pair and return their rectangles.
	 *
	 * @param name  attribute name
	 * @param value attribute value to match
	 * @return Map of Element to Rectangle for matched elements
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
	 * pageWidth getter
	 *
	 * @return configured page width
	 */
	public Float getPageWidth() {
		return pageWidth;
	}

	/**
	 * pageWidth setter
	 *
	 * @param pageWidth page width value in configured units
	 */
	public void setPageWidth(Float pageWidth) {
		this.pageWidth = pageWidth;
	}

	/**
	 * pageWidth setter
	 *
	 * @param pageWidth page width value in configured units
	 * @return  HtmlRender
	 */
	public HtmlRender pageWidth(Float pageWidth) {
		this.pageWidth = pageWidth;
		return this;
	}
	
	/**
	 * pageHeight getter
	 *
	 * @return configured page height
	 */
	public Float getPageHeight() {
		return pageHeight;
	}

	/**
	 * pageHeight setter
	 *
	 * @param pageHeight page height value in configured units
	 */
	public void setPageHeight(Float pageHeight) {
		this.pageHeight = pageHeight;
	}
	/**
	 * pageHeight setter
	 *
	 * @param pageHeight page height value in configured units
	 * @return HtmlRender
	 */
	public HtmlRender pageHeight(Float pageHeight) {
		this.pageHeight = pageHeight;
		return this;
	}

	/**
	 * units getter
	 *
	 * @return units used for page size
	 */
	public PageSizeUnits getUnits() {
		return units;
	}

	/**
	 * units setter
	 *
	 * @param units units to use for page size
	 */
	public void setUnits(PageSizeUnits units) {
		this.units = units;
	}

	/**
	 * units setter
	 *
	 * @param units units to use for page size
	 * @return HtmlRender
	 */
	public HtmlRender units(PageSizeUnits units) {
		this.units = units;
		return this;
	}
	
	/**
	 * imageType getter
	 *
	 * @return configured BufferedImage type
	 */
	public int getImageType() {
		return imageType;
	}

	/**
	 * imageType setter
	 *
	 * @param imageType BufferedImage type constant
	 */
	public void setImageType(int imageType) {
		this.imageType = imageType;
	}
	/**
	 * imageType setter
	 *
	 * @param imageType BufferedImage type constant
	 * @return HtmlRender
	 */
	public HtmlRender imageType(int imageType) {
		this.imageType = imageType;
		return this;
	}

	/**
	 * scale getter
	 *
	 * @return rendering scale factor
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * scale setter
	 *
	 * @param scale rendering scale multiplier
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}
	/**
	 * scale setter
	 *
	 * @param scale rendering scale multiplier
	 * @return HtmlRender
	 */
	public HtmlRender scale(double scale) {
		this.scale = scale;
		return this;
	}

	/**
	 * fontPath getter
	 *
	 * @return font directory path
	 */
	public String getFontPath() {
		return fontPath;
	}

	/**
	 * fontPath  eg: java>AlibabaPuHuiTi.ttf use html> font-family: AlibabaPuHuiTi;
	 *
	 * @param fontPath directory that contains font files (.ttf, .otf)
	 */
	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}
	/**
	 * fontPath  eg: java>AlibabaPuHuiTi.ttf use html> font-family: AlibabaPuHuiTi;
	 *
	 * @param fontPath directory that contains font files (.ttf, .otf)
	 */
	public HtmlRender fontPath(String fontPath) {
		this.fontPath = fontPath;
		return this;
	}

	/**
	 * Convenience alias for setFontPath: register a directory of fonts. eg: java>AlibabaPuHuiTi.ttf use html> font-family: AlibabaPuHuiTi;
	 *
	 * @see setFontPath()
	 * @param fontPath path to font directory
	 * @return  HtmlRender
	 */
	public HtmlRender addFontDirectory(String fontPath) {
		this.fontPath = fontPath;
		return this;
	}

	/**
	 * loggingEnabled getter
	 *
	 * @return whether OpenHTMLToPDF logging is enabled
	 */
	public Boolean getLoggingEnabled() {
		return loggingEnabled;
	}

	/**
	 * loggingEnabled setter
	 *
	 * @param loggingEnabled enable or disable internal logging
	 */
	public void setLoggingEnabled(Boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	/**
	 * loggingEnabled setter
	 *
	 * @param loggingEnabled enable or disable internal logging
	 * @return 
	 */
	public HtmlRender enabledLogging(Boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
		return this;
	}
	/**
	 * Configure use of pixel units (px) for layout calculations.
	 *Pixel Dimensions is the size parameter of an exponential character image in two-dimensional space, usually represented in two dimensions: length and width, with units of pixels (px). For example, the pixel dimension of a photo may be labeled as "1920 × 1080", indicating that it contains 1920 pixels in the length direction and 1080 pixels in the width direction.
	 * @param useXp true to use pixel dimensions
	 * @return  HtmlRender
	 */
	public HtmlRender usePx(boolean usePx) {
		this.usePx = usePx;
		return this;
	}

	/**
	 * Shortcut to enable pixel units. Pixel Dimensions is the size parameter of an exponential character image in two-dimensional space, usually represented in two dimensions: length and width, with units of pixels (px). For example, the pixel dimension of a photo may be labeled as "1920 × 1080", indicating that it contains 1920 pixels in the length direction and 1080 pixels in the width direction.
	 * @return HtmlRender
	 */
	public HtmlRender usePx() {
		this.usePx = true;
		return this;
	}

	/**
	 * baseDocumentUri getter
	 *
	 * @return base document URI used to resolve relative resources
	 */
	public String getBaseDocumentUri() {
		return baseDocumentUri;
	}

	/**
	 * baseDocumentUri setter
	 *
	 * @param baseDocumentUri the base document URI to resolve future relative resources (e.g. images)
	 * @return 
	 */
	public HtmlRender baseDocumentUri(String baseDocumentUri) {
		this.baseDocumentUri = baseDocumentUri;
		return this;
	}

	/**
	 * Expose the underlying AsRenderer instance produced by the last rendering operation.
	 *
	 * @return AsRenderer or null if no rendering has occurred yet
	 */
	public AsRenderer getAsRenderer() {
		return asRenderer;
	}

	///-----------------------------------------///
	/// Static factory helpers for convenience. ///
	///-----------------------------------------///
	
	/**
	 * Create HtmlRender with default settings.
	 *
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create() {
		return new HtmlRender();
	}

	/**
	 * Create HtmlRender with specified page size.
	 *
	 * @param pageWidth  page width
	 * @param pageHeight page height
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create(Float pageWidth, Float pageHeight) {
		return new HtmlRender(pageWidth, pageHeight);
	}

	/**
	 * Create HtmlRender with specified page size and units.
	 *
	 * @param pageWidth  page width
	 * @param pageHeight page height
	 * @param units      units for page size
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create(Float pageWidth, Float pageHeight, PageSizeUnits units) {
		return new HtmlRender(pageWidth, pageHeight, units);
	}

	/**
	 * Create HtmlRender with specified image type.
	 *
	 * @param imageType {@link BufferedImage imageType}
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create(int imageType) {
		return new HtmlRender(imageType);
	}

	/**
	 * Create HtmlRender with specified image type and scale.
	 *
	 * @param imageType {@link BufferedImage imageType}
	 * @param scale     rendering scale multiplier
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create(int imageType, double scale) {
		return new HtmlRender(imageType, scale);
	}

	/**
	 * Create HtmlRender with full configuration.
	 *
	 * @param pageWidth  page width
	 * @param pageHeight page height
	 * @param units      units for page size
	 * @param imageType  {@link BufferedImage imageType}
	 * @param scale      rendering scale multiplier
	 * @return new HtmlRender instance
	 */
	public static HtmlRender create(Float pageWidth, Float pageHeight, PageSizeUnits units, int imageType, double scale) {
		return new HtmlRender(pageWidth, pageHeight, units, imageType, scale);
	}
	/**
	 * Read HTML content from an absolute file path.
	 *
	 * @param absResPath absolute path to the HTML resource
	 * @return HTML content as a UTF-8 string
	 * @throws IOException on IO errors
	 */
	public static String readHtml(String absResPath) throws IOException {

		try (InputStream htmlIs = new FileInputStream(absResPath)) {
			byte[] htmlBytes = IOUtils.toByteArray(htmlIs);
			return new String(htmlBytes, StandardCharsets.UTF_8);
		}

	}

	
	///-----------------------------------------///
	/// @Deprecated. @SuppressWarnings("unused")///
	///-----------------------------------------///
//	@SuppressWarnings("unused")
//	@Deprecated
//	private BufferedImage runRendererSingle(String html, final String filename) throws IOException {
//
//		AsRendererBuilder builder = new AsRendererBuilder();
//
//		builder.withHtmlContent(html, baseDocumentUri);
//
//		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 2.0);
//
//		builder.useDefaultPageSize(650, 700, AsRendererBuilder.PageSizeUnits.MM);
//		builder.useEnvironmentFonts(true);
//		// 开发模式下开启可以打印信息
//		builder.useFastMode();
//		builder.testMode(true);
//
//		String FONT_PATH = "D:\\myfonts";
//		builder.useFont(new File(FONT_PATH + "/zitijiaaizaoziyikong.ttf"), "bzff");
//
//		builder.toSinglePage(bufferedImagePageProcessor);
//
//		builder.runFirstPage();
//
//		/*
//		 * Render Single Page Image
//		 */
//		return bufferedImagePageProcessor.getPageImages().get(0);
//
//		// ImageIO.write(image, "PNG", new File(filename));
//
//		/*
//		 * Render Multipage Image Files
//		 */
//		// builder.toPageProcessor(new DefaultPageProcessor(zeroBasedPageNumber -> new FileOutputStream(filename.replace(".png", "_" + zeroBasedPageNumber + ".png")), BufferedImage.TYPE_INT_ARGB, "PNG")).runPaged();
//
//	}
//
//	@Deprecated
//	@SuppressWarnings("unused")
//	private List<BufferedImage> runRendererPaged(String resourcePath, String html) {
//		AsRendererBuilder builder = new AsRendererBuilder();
//		builder.withHtmlContent(html, baseDocumentUri);
//		builder.useFastMode();
//		builder.testMode(true);
//
//		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_RGB, 1.0);
//
//		builder.toPageProcessor(bufferedImagePageProcessor);
//
//		// BuilderConfig.J2D_WITH_FONT.configure(builder);
//
//		try {
//			builder.runPaged();
//		} catch (Exception e) {
//			System.err.println("Failed to render resource (" + resourcePath + ")");
//			e.printStackTrace();
//			return null;
//		}
//
//		return bufferedImagePageProcessor.getPageImages();
//	}
//
//	@Deprecated
//	@SuppressWarnings("unused")
//	private void renderSamplePNG(String html, final String filename) throws IOException {
//		try (SVGDrawer svg = new BatikSVGDrawer(); SVGDrawer mathMl = new MathMLDrawer()) {
//
//			AsRendererBuilder builder = new AsRendererBuilder();
//			builder.useSVGDrawer(svg);
//			builder.useMathMLDrawer(mathMl);
//
//			builder.withHtmlContent(html, baseDocumentUri);
//
//			BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(BufferedImage.TYPE_INT_ARGB, 2.0);
//
//			builder.useDefaultPageSize(150, 130, AsRendererBuilder.PageSizeUnits.MM);
//
//			builder.useEnvironmentFonts(true);
//			// 开发模式下开启可以打印信息
//			builder.useFastMode();
//			builder.testMode(true);
//
//			String FONT_PATH = "D:\\myfonts";
//			builder.useFont(new File(FONT_PATH + "/zitijiaaizaoziyikong.ttf"), "bzff");
//
//			/*
//			 * Render Single Page Image
//			 */
//			builder.toSinglePage(bufferedImagePageProcessor).runFirstPage();
//			BufferedImage image = bufferedImagePageProcessor.getPageImages().get(0);
//
//			ImageIO.write(image, "PNG", new File(filename));
//
//			/*
//			 * Render Multipage Image Files
//			 */
//			builder.toPageProcessor(new DefaultPageProcessor(zeroBasedPageNumber -> new FileOutputStream(filename.replace(".png", "_" + zeroBasedPageNumber + ".png")), BufferedImage.TYPE_INT_ARGB, "PNG")).runPaged();
//
//		}
//	}
//
//	@SuppressWarnings("unused")
//	private void renderPDF(String html, PdfAConformance pdfaConformance, OutputStream outputStream) throws IOException {
//		try (SVGDrawer svg = new BatikSVGDrawer(); SVGDrawer mathMl = new MathMLDrawer()) {
//
//			PdfRendererBuilder builder = new PdfRendererBuilder();
//			builder.useSVGDrawer(svg);
//			builder.useMathMLDrawer(mathMl);
//			builder.addDOMMutator(LaTeXDOMMutator.INSTANCE);
//			builder.usePdfAConformance(pdfaConformance);
//			builder.withHtmlContent(html, baseDocumentUri);
//			builder.toStream(outputStream);
//			builder.run();
//		}
//	}
}
