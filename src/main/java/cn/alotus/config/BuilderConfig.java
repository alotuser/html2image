package cn.alotus.config;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.bidi.support.ICUBreakers;
import com.openhtmltopdf.extend.FSTextBreaker;
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder;
import com.openhtmltopdf.latexsupport.LaTeXDOMMutator;
import com.openhtmltopdf.mathmlsupport.MathMLDrawer;
import com.openhtmltopdf.objects.StandardObjectDrawerFactory;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import com.openhtmltopdf.render.DefaultObjectDrawerFactory;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;

import cn.alotus.drawer.CustomObjectDrawerBinaryTree;
/**
 * BuilderConfig.WITH_BASE
 */
public class BuilderConfig {

	@FunctionalInterface
	public interface BaseBuilderConfig {
		public void configure(BaseRendererBuilder<?, ?> builder);
	}

	@FunctionalInterface
	public interface PdfBuilderConfig {
		public void configure(PdfRendererBuilder builder);
	}

	@FunctionalInterface
	public interface Java2DBuilderConfig {
		public void configure(Java2DRendererBuilder builder);
	}

	public static final BaseBuilderConfig WITH_DEFAULT = (builder) -> {
	};

	public static final BaseBuilderConfig WITH_BASE = (builder) -> {

		builder.addDOMMutator(LaTeXDOMMutator.INSTANCE);
		builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
		builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
		builder.defaultTextDirection(BaseRendererBuilder.TextDirection.LTR);

	};
	/**
	 * 开发模式下开启可以打印信息
	 */
	public static final BaseBuilderConfig WITH_TEST = (builder) -> {
		builder.testMode(true);
	};
	public static final BaseBuilderConfig WITH_CUSTOM = (builder) -> {

		DefaultObjectDrawerFactory objectDrawerFactory = new StandardObjectDrawerFactory();
		objectDrawerFactory.registerDrawer("custom/binary-tree", new CustomObjectDrawerBinaryTree());
		builder.useObjectDrawerFactory(objectDrawerFactory);
		builder.useUnicodeLineBreaker(new SimpleTextBreaker());

	};




	public static final Java2DBuilderConfig J2D_WITH_FONT = (builder) -> {
		builder.useFont(new File("target/test/visual-tests/Karla-Bold.ttf"), "TestFont");
		builder.useUnicodeLineBreaker(new SimpleTextBreaker());
	};

	
	public static final PdfBuilderConfig WITH_PDF = (builder) -> {
		
		builder.usePdfAConformance(PdfAConformance.NONE);
		builder.useSVGDrawer(new BatikSVGDrawer());
		builder.useMathMLDrawer(new MathMLDrawer());
		builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
		builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
		builder.useObjectDrawerFactory(buildObjectDrawerFactory());
		
		builder.defaultTextDirection(BaseRendererBuilder.TextDirection.LTR);

		builder.addDOMMutator(LaTeXDOMMutator.INSTANCE);
		
		
	};
	
	
	
	public static final PdfBuilderConfig WITH_FONT = (builder) -> {
		builder.useFont(new File("target/test/visual-tests/Karla-Bold.ttf"), "TestFont");
		builder.useUnicodeLineBreaker(new SimpleTextBreaker());
	};
	
	
	public static final PdfBuilderConfig WITH_EXTRA_FONT = (builder) -> {
		WITH_FONT.configure(builder);
		builder.useFont(new File("target/test/visual-tests/SourceSansPro-Regular.ttf"), "ExtraFont");
	};

	public static final PdfBuilderConfig WITH_ARABIC = (builder) -> {
		WITH_FONT.configure(builder);
		builder.useFont(new File("target/test/visual-tests/NotoNaskhArabic-Regular.ttf"), "arabic");
		builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
		builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
		builder.useUnicodeLineBreaker(new ICUBreakers.ICULineBreaker(Locale.US)); // Overrides WITH_FONT
		builder.defaultTextDirection(TextDirection.LTR);
	};

	public static final PdfBuilderConfig WITH_COLLAPSED_LINE_BREAKER = (builder) -> {
		WITH_FONT.configure(builder);
		builder.useUnicodeLineBreaker(new CollapsedSpaceTextBreaker());
	};

	/**
	 * Configures the builder to use SVG drawer but not font.
	 */
	public static final PdfBuilderConfig WITH_SVG = (builder) -> builder.useSVGDrawer(new BatikSVGDrawer());
	/**
	 * Configures the builder to use MATHML drawer but not font.
	 */
	public static final PdfBuilderConfig WITH_MATHML = (builder) -> builder.useMathMLDrawer(new MathMLDrawer());

	/**
	 * A simple line breaker so that our tests are not reliant on the external Java API.
	 */
	public static class SimpleTextBreaker implements FSTextBreaker {
		private String text;
		private int position;

		@Override
		public int next() {
			int ret = text.indexOf(' ', this.position);
			this.position = ret + 1;
			return ret;
		}

		@Override
		public void setText(String newText) {
			this.text = newText;
			this.position = 0;
		}
	}

	/**
	 * A simple line breaker that produces similar results to the JRE standard line breaker.
	 * So we can test line breaking/justification with conditions more like real world.
	 */
	public static class CollapsedSpaceTextBreaker implements FSTextBreaker {
		private final static Pattern SPACES = Pattern.compile("[\\s\u00AD]");
		private Matcher matcher;

		@Override
		public int next() {
			if (!matcher.find()) {
				return -1;
			}

			return matcher.end();
		}

		@Override
		public void setText(String newText) {
			this.matcher = SPACES.matcher(newText);
		}
	}
	/**
	 * 
	 * @return
	 */
	public static DefaultObjectDrawerFactory buildObjectDrawerFactory() {
		DefaultObjectDrawerFactory objectDrawerFactory = new StandardObjectDrawerFactory();
		objectDrawerFactory.registerDrawer("custom/binary-tree", new CustomObjectDrawerBinaryTree());
		return objectDrawerFactory;
	}

}
