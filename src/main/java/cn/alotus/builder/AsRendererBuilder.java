package cn.alotus.builder;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

import com.openhtmltopdf.java2d.api.FSPageProcessor;
import com.openhtmltopdf.layout.Layer;
import com.openhtmltopdf.outputdevice.helper.BaseDocument;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.PageDimensions;
import com.openhtmltopdf.outputdevice.helper.UnicodeImplementation;

import cn.alotus.renderer.AsRenderer;

public class AsRendererBuilder extends BaseRendererBuilder<AsRendererBuilder, cn.alotus.builder.AsRendererBuilder.AsRendererBuilderState> {
	public AsRendererBuilder() {
		super(new AsRendererBuilderState());
	}

	/**
	 * Compulsory method. The layout graphics are used to measure text and should be from an image or device with the same characteristics as the output graphicsw provided by the page processor.
	 *
	 * @param g2d
	 * @return this for method chaining
	 */
	public AsRendererBuilder useLayoutGraphics(Graphics2D g2d) {
		state._layoutGraphics = g2d;
		return this;
	}

	/**
	 * Whether to use fonts available in the environment. Enabling environment fonts may mean different text rendering behavior across different environments. The default is not to use environment fonts.
	 */
	public AsRendererBuilder useEnvironmentFonts(boolean useEnvironmentFonts) {
		state._useEnvironmentFonts = useEnvironmentFonts;
		return this;
	}
	/**
	 * Pixel Dimensions is the size parameter of an exponential character image in two-dimensional space, usually represented in two dimensions: length and width, with units of pixels (px). For example, the pixel dimension of a photo may be labeled as "1920 × 1080", indicating that it contains 1920 pixels in the length direction and 1080 pixels in the width direction.
	 * @param usePixelDimensions
	 * @return
	 */
	public AsRendererBuilder usePixelDimensions(boolean usePixelDimensions) {
		state._usePixelDimensions = usePixelDimensions;
		return this;
	}
	
	
	/**
	 * Render everything to a single page. I.e. only one big page is genereated, no pagebreak will be done. The page is only as height as needed.
	 */
	public AsRendererBuilder toSinglePage(FSPageProcessor pageProcessor) {
		state._pagingMode = Layer.PAGED_MODE_SCREEN;
		state._pageProcessor = pageProcessor;
		return this;
	}

	/**
	 * Output the document in paged format. The user can use the DefaultPageProcessor or use its source as a reference to code their own page processor for advanced usage.
	 *
	 * @param pageProcessor
	 * @return this for method chaining
	 */
	public AsRendererBuilder toPageProcessor(FSPageProcessor pageProcessor) {
		state._pagingMode = Layer.PAGED_MODE_PRINT;
		state._pageProcessor = pageProcessor;
		return this;
	}

	/**
	 * <code>useLayoutGraphics</code> and <code>toPageProcessor</code> MUST have been called. Also a document MUST have been set with one of the with* methods. This will build the renderer and output each page of the document to the specified page processor.
	 */
	public AsRenderer runPaged() throws IOException {
		try (Closeable d = this.applyDiagnosticConsumer(); AsRenderer renderer = this.buildAsRenderer(d)) {
			renderer.layout();
			if (state._pagingMode == Layer.PAGED_MODE_PRINT)
				renderer.writePages();
			else
				renderer.writeSinglePage();
			
			return renderer;
		}
	}

	/**
	 * <code>useLayoutGraphics</code> and <code>toPageProcessor</code> MUST have been called. Also a document MUST have been set with one of the with* methods. This will build the renderer and output the first page of the document to the specified page processor.
	 */
	public AsRenderer runFirstPage() throws IOException {
		try (Closeable d = this.applyDiagnosticConsumer(); AsRenderer renderer = this.buildAsRenderer(d)) {
			renderer.layout();
			if (state._pagingMode == Layer.PAGED_MODE_PRINT)
				renderer.writePage(0);
			else
				renderer.writeSinglePage();
			
			return renderer;
		}
	}

	public AsRenderer buildAsRenderer() {
		return buildAsRenderer(this.applyDiagnosticConsumer());
	}

	public AsRenderer buildAsRenderer(Closeable diagnosticConsumer) {

		UnicodeImplementation unicode = new UnicodeImplementation(state._reorderer, state._splitter, state._lineBreaker, state._unicodeToLowerTransformer, state._unicodeToUpperTransformer, state._unicodeToTitleTransformer, state._textDirection, state._charBreaker);

		PageDimensions pageSize = new PageDimensions(state._pageWidth, state._pageHeight, state._isPageSizeInches);

		BaseDocument doc = new BaseDocument(state._baseUri, state._html, state._document, state._file, state._uri);

		/*
		 * If no layout graphics is provied, just use a sane default
		 */
		if (state._layoutGraphics == null) {
			BufferedImage bf = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
			state._layoutGraphics = bf.createGraphics();
		}

		return new AsRenderer(doc, unicode, pageSize, state, diagnosticConsumer);
	}

	/**
	 * This class is an internal implementation detail. 
	 */
	public static class AsRendererBuilderState extends BaseRendererBuilder.BaseRendererBuilderState {
		 
		public AsRendererBuilderState() {
		}

		public Graphics2D _layoutGraphics;
		public FSPageProcessor _pageProcessor;
		public boolean _useEnvironmentFonts = false;
		public boolean _usePixelDimensions  = false;
	}

}