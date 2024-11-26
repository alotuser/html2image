package cn.alotus;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

	
	public static void main(String[] args) throws IOException {

		

		
		
		
		
		
		String html=HtmlRender.readHtml("D://about.xhtml");
		
		HtmlRender htmlRender=new HtmlRender(BufferedImage.TYPE_INT_RGB);
		htmlRender.addFontDirectory("D:/myfonts");
		
		
		 
		//htmlRender.toPng(html, "D://test.png");
		
		 try (FileOutputStream outputStream = new FileOutputStream("D:/test.pdf")) {
			 htmlRender.toPdf(html,outputStream);
         }
	 
		 
		
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
