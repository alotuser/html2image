 # HtmlToImage

一个轻量级的 Java 库，可将标准 HTML 标记转换为图像，并生成对应的客户端图像映射。

📖 新维护项目迁移到[https://github.com/alotuser/openhtmltopdf] ,使用Jsoup更加灵活的选择器方式查找html元素对应位置。

---

## ✨ 功能特性

*   **HTML 转图像**：将 HTML 和 CSS 精准地渲染为图像。
*   **客户端图像映射**：在生成的图像中保留可点击区域，支持原 HTML 中的链接。

## 🚀 使用场景

### 1. 程序化图像合成

*   **场景**：您需要将多张图片与文本动态合成为一张图像。
*   **解决方案**：使用纯 HTML 和 CSS 来布局您的图像和文字，将其构建成一个“网页”，然后通过本库一键转换为图片。这种方式简化了复杂的图形合成编程。

### 2. 确保邮件内容兼容性

*   **场景**：您设计了一封精美的 HTML 营销邮件，但不同邮件客户端（如 Outlook、Gmail）对 HTML/CSS 的支持标准不一，导致最终显示效果错乱。
*   **解决方案**：
    1.  使用本库将您的原始 HTML 内容转换为图片。
    2.  利用**客户端图像映射**功能，为图片中的链接区域添加可点击的映射。
    3.  发送这个由“图片 + 图像映射”构成的新 HTML 邮件，即可在所有客户端中获得一致的视觉体验，并轻松使用各类特殊字体。

### 3. 防范垃圾邮件采集

*   **场景**：您网站上公开的电子邮件地址容易被爬虫和机器人采集，从而导致垃圾邮件泛滥。
*   **解决方案**：使用本库将页面中的电子邮件地址文本直接渲染为图片。这使得自动化工具无法直接读取邮箱信息，从而有效减少垃圾邮件的骚扰。

## 💻 快速开始

### 基本用法

以下示例演示了如何将 HTML 转换为 PNG 图片和 PDF 文档：

```java
// 从文件读取 HTML 内容
String html = HtmlRender.readHtml("D://about.xhtml");

// 初始化渲染器（主类型可使用 BufferedImage.TYPE_INT_ARGB）
HtmlRender htmlRender = new HtmlRender(BufferedImage.TYPE_INT_RGB);

// 添加自定义字体目录
htmlRender.addFontDirectory("D:/myfonts");

// 生成 PNG 图片
htmlRender.toPng(html, "D://test.png");

// 生成 PDF 文档
try (FileOutputStream outputStream = new FileOutputStream("D:/test.pdf")) {
    htmlRender.toPdf(html, outputStream);
}
```

### 安装依赖

在 Maven 项目中添加以下依赖：

```xml
<dependencies>
    <dependency>
        <groupId>com.github.alotuser</groupId>
        <artifactId>html2image</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>
```

## 🔧 高级用法

更多高级使用示例和完整演示项目，请参考：
[https://github.com/alotuser/html2image-demo]
 
