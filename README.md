# HtmlToImage
这个简单的Java库将普通HTML标记转换为图像，并提供使用HTML元素的客户端图像映射。

它能做什么：使用案例
程序化合成图像——使用案例：您需要从其他图像和文本中合成图像。

解决方案 - 使用纯 HTML、CSS 和图像创建一个网页，并使用 Html2Image 将其转换为图像。

提高垃圾邮件防范能力 - 使用场景：您需要发送带有装饰性 HTML 的电子邮件。

你使用CSS、图像和链接来构建HTML。

可惜的是，你的客户打开Outlook、Gmail或其他邮件客户端的收件箱，却发现你的邮件被弄得乱七八糟（仅仅看起来像在浏览器中呈现的同一HTML页面）。

这是因为mail和其他客户端仅支持HTML的部分功能，并且它们的实现和漏洞各不相同。

解决方案 - 使用 Html2Image 将原始邮件的 HTML 转换为仅包含图像（.）和客户端图像映射（）的新 HTML，以包含原始 HTML 中的链接。

这还可以让你在邮件中轻松使用独特的字体。

防止垃圾邮件——使用场景：您的网站有一个联系人列表及其电子邮件地址。

这些电子邮件地址以明文形式显示，使得机器人和蜘蛛能够收集这些地址并向您的团队发送有关伟哥的广告垃圾邮件。

解决方案 - 使用 Html2Image 将这些电子邮件地址转换为图像。


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
# html2image-demo
https://github.com/alotuser/html2image-demo
