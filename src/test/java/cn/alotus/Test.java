package cn.alotus;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import cn.alotus.util.ImageCropUtil;

public class Test {

	
	public static void main(String[] args) throws IOException {


		String html=HtmlRender.readHtml("D://1.html");
		
		HtmlRender htmlRender=HtmlRender.create(BufferedImage.TYPE_INT_RGB);
		htmlRender.addFontDirectory("D:/myfonts");
		htmlRender.setPageWidth(400f);
		htmlRender.setPageHeight(300f);
		htmlRender.setScale(1f);
		
		
		//htmlRender.toImage(html, BuilderConfig.WITH_CUSTOM);
		
//		htmlRender.toImage(html, builder->{
//			 builder.useFont(new File("myfont"), "myfont");
//		});
//		
		
		 
		htmlRender.toPng(html, "D://1.png");
		
		Map<Element, Rectangle>   mers= htmlRender.findByClass("original-price");
//		//[x=7,y=321,width=359,height=20]
//		
		System.out.println(mers);
		
		Rectangle  f =mers.values().stream().findFirst().get();
		
		
		
		BufferedImage original = ImageIO.read(new File("D:\\1.png"));

		Rectangle rect = new Rectangle(f.x, f.y, f.width, f.height);

		BufferedImage cropped = ImageCropUtil.cropImage(original, rect);
		ImageIO.write(cropped, "png", new File("D:\\1-cropped.png"));
		
		
		
		
		
//		 try (FileOutputStream outputStream = new FileOutputStream("D:/test.pdf")) {
//			 htmlRender.toPdf(html,outputStream);
//         }
	 
		 
		
//		String html = FileUtil.readUtf8String("D:\\about.xhtml");
//
//		renderSamplePNG(html, "D:\\about.png");
		
		
		
		
//        // PERF: Should only be called once, as each font must be parsed for font family name.
//        List<CSSFont> fonts = AutoFont.findFontsInDirectory("D:/myfonts");
//
//        // Use this in your template for the font-family property.
//        String fontFamily = AutoFont.toCSSEscapedFontFamily(fonts);
//
//        // Add fonts to builder.
//        //AutoFont.toBuilder(builder, fonts);
//
//		 System.out.println(fontFamily);
		 
		 

	}

}
