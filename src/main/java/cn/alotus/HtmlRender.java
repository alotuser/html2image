package cn.alotus;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import cn.alotus.config.BuilderConfig;
import cn.alotus.config.BuilderConfig.BaseBuilderConfig;
import cn.alotus.processor.BufferedImagePageProcessor;



public class HtmlRender {

	private Float pageWidth=123f;
	private Float pageHeight=123f;
	private PageSizeUnits units=Java2DRendererBuilder.PageSizeUnits.MM;
	private int imageType=BufferedImage.TYPE_INT_RGB;
	private double scale=2.0;
	
	private String fontPath;
	
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


	public BufferedImage toImage(String html,BaseBuilderConfig... config) throws IOException{
		Java2DRendererBuilder builder = new Java2DRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(pageWidth, pageHeight, units);
		builder.useEnvironmentFonts(true);
		builder.useFastMode();
		
		//
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
	
	
	public List<BufferedImage> toImages(String html,BaseBuilderConfig... config) throws IOException{
		Java2DRendererBuilder builder = new Java2DRendererBuilder();

		builder.withHtmlContent(html, "");

		BufferedImagePageProcessor bufferedImagePageProcessor = new BufferedImagePageProcessor(imageType, scale);

		builder.useDefaultPageSize(pageWidth, pageHeight, units);
		builder.useEnvironmentFonts(true);
		builder.useFastMode();
		
		builder.toPageProcessor(bufferedImagePageProcessor);
		builder.runPaged();
	
		/*
		 * Render Single Page Image
		 */
		return bufferedImagePageProcessor.getPageImages();

	}
	
	
	
	public void toPng(String html,String outPath) throws IOException {
		
		BufferedImage  image= toImage(html, BuilderConfig.WITH_BASE);
		
		
		ImageIO.write(image, "PNG", new File(outPath));
		
	}
	
	
	
	


 

	private static BufferedImage runRendererSingle(String html, final String filename) throws IOException {

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

	private static List<BufferedImage> runRendererPaged(String resourcePath, String html) {
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

	private static void renderSamplePNG(String html, final String filename) throws IOException {
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

	private static void renderPDF(String html, PdfAConformance pdfaConformance, OutputStream outputStream) throws IOException {
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
