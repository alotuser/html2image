# HtmlToImage
这个轻量级 Java 库能够将常规 HTML 代码转换为图像，并支持生成带有 HTML 元素坐标信息的客户端图像映射。

主要功能与应用场景如下：

▌ 程序化图像合成
需求：将多张图片与文字合成为一张图像。
解决方案：使用 HTML、CSS 和图片构建页面，再通过 Html2Image 将其整体转换为图像，轻松实现视觉内容的程序化生成。

▌ 提升邮件内容兼容性
需求：发送带有装饰性 HTML 内容的邮件，却因不同邮件客户端（如 Outlook、Gmail 等）对 HTML 和 CSS 的支持不统一，导致显示效果混乱。
解决方案：通过 Html2Image 将原始 HTML 邮件转换为包含图像及对应链接映射的新 HTML 文件。不仅保持了视觉一致性，还能在邮件中自由使用特殊字体。

▌ 防范垃圾邮件采集
需求：网站页面上公开的团队成员邮箱地址容易被爬虫抓取，导致收到大量垃圾邮件。
解决方案：使用 Html2Image 将邮箱地址转为图片展示，有效防止自动化程序识别和采集，减少垃圾邮件侵扰。


 # 用法demo
``` java
  	String html=HtmlRender.readHtml("D://about.xhtml");
	HtmlRender htmlRender=new HtmlRender(BufferedImage.TYPE_INT_RGB);//main： BufferedImage.TYPE_INT_ARGB
	htmlRender.addFontDirectory("D:/myfonts");
	//生成图片
	htmlRender.toPng(html, "D://test.png");
	//生成pdf文件
	try (FileOutputStream outputStream = new FileOutputStream("D:/test.pdf")) {
		htmlRender.toPdf(html,outputStream);
  	}
```
## 怎么使用？
- 使用方法很简单，和普通的 Maven 一样使用就可以了，如下
``` xml
    <dependencys>
        <dependency>
            <groupId>com.github.alotuser</groupId>
            <artifactId>html2image</artifactId>
            <version>1.1.1</version>
        </dependency>
    </dependencys>
```
# 高级进阶用法-demo
https://github.com/alotuser/html2image-demo
