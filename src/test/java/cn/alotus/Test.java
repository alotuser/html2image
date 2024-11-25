package cn.alotus;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Test {

	
	public static void main(String[] args) throws IOException {

		String about=HtmlRender.readHtml("D://about.xhtml");
		
		HtmlRender htmlRender=new HtmlRender(BufferedImage.TYPE_INT_ARGB);
		htmlRender.addFontDirectory("D:/myfonts");
		
		htmlRender.toPng(about, "D://test.png");
		
		
		
		
		
//		String html = FileUtil.readUtf8String("D:\\about.xhtml");
//
//		renderSamplePNG(html, "D:\\about.png");

	}

}
