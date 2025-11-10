package cn.alotus.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import cn.alotus.core.io.FileUtil;
import cn.alotus.core.text.StrPool;
import cn.alotus.core.util.StrUtil;

/**
 * FontBox 工具类
 * 
 * @author 7g
 *
 */
public class FontBoxUtil {


	/**
	 * 获取字体主名称， 例如：AlibabaPuHuiTi-3-35-Thin
	 * 
	 * @param fontFile 字体文件
	 * @return 字体主名称， 例如 AlibabaPuHuiTi-3-35-Thin
	 * @throws IOException 读取字体文件异常
	 */
	public static String mainName(File fontFile) throws IOException {
		FontInfo  fi= readFontInfo(fontFile);
		if(fi != null) {
			return  StrUtil.replace(fi.getPostScriptName(), StrPool.UNDERLINE, StrPool.DASHED);
		}
	 
		return null;
	}
	
	
	
	
	  /**
     * 读取字体信息的通用方法
     */
    public static FontInfo readFontInfo(File fontFile) throws IOException {
        
        
        if (!fontFile.exists()) {
            throw new IOException("字体文件不存在: " + fontFile.getAbsolutePath());
        }
        
        String fileName = fontFile.getName().toLowerCase();
        
        try {
            // 根据文件扩展名选择适当的解析器
            if (fileName.endsWith(".ttf") ) {
                return readTrueTypeOrOpenType(fontFile);
            } else if (fileName.endsWith(".ttc") || fileName.endsWith(".ttc2")) {
                return readTrueTypeCollection(fontFile);
            } else if (fileName.endsWith(".pfb") || fileName.endsWith(".pfa")) {
                return readType1Font(fontFile);
            } else if (fileName.endsWith(".cff")|| fileName.endsWith(".otf")) {
                return readCFFFont(fontFile);
            } else {
                // 尝试自动检测格式
                return autoDetectFontFormat(fontFile);
            }
        } catch (Exception e) {
            throw new IOException("无法解析字体文件 '" + fileName + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * 读取 TrueType 或 OpenType 字体
     */
    private static FontInfo readTrueTypeOrOpenType(File fontFile) throws IOException {
        TTFParser parser = new TTFParser();
        try (RandomAccessReadBufferedFile randomAccessFile = new RandomAccessReadBufferedFile(fontFile);
             TrueTypeFont font = parser.parse(randomAccessFile)) {
            
        	NamingTable naming = font.getNaming();
            return new FontInfo(
                "TrueType/OpenType",
                naming.getFontFamily(),
                naming.getFontSubFamily(),
                naming.getPostScriptName(),
                naming.getFontFamily() // 使用家族名作为主要名称
            );
        }
    }
    
    /**
     * 读取 TrueType 集合文件
     */
    private static FontInfo readTrueTypeCollection(File fontFile) throws IOException {
        StringBuilder names = new StringBuilder();
        try (TrueTypeCollection ttc = new TrueTypeCollection(fontFile)) {
            ttc.processAllFonts(trueTypeFont -> {
                try (TrueTypeFont font = trueTypeFont) {
                	NamingTable naming = font.getNaming();
                    if (names.length() > 0) names.append(", ");
                    names.append(naming.getFontFamily());
                } catch (IOException e) {
                    System.err.println("处理集合中的字体时出错: " + e.getMessage());
                }
            });
        }
        
        return new FontInfo(
            "TrueType Collection",
            names.toString(),
            "字体集合 - " + names.toString(),
            "Collection",
            names.toString()
        );
    }
    
    /**
     * 读取 Type1 字体 (PFB/PFA)
     */
    private static FontInfo readType1Font(File fontFile) throws IOException {
    	
        try (InputStream randomAccessFile = FileUtil.getInputStream(fontFile)) {
            Type1Font font = Type1Font.createWithPFB(randomAccessFile);
            
            return new FontInfo(
                "Type1",
                font.getFamilyName(),
                font.getFontName(),
                font.getFontName(), // Type1 字体通常用 FontName 作为 PostScript 名称
                font.getFontName()
            );
        }
    }
    
    /**
     * 读取 CFF 字体
     */
    private static FontInfo readCFFFont(File fontFile) throws IOException {
        try (RandomAccessReadBufferedFile randomAccessFile = new RandomAccessReadBufferedFile(fontFile)) {
            CFFParser parser = new CFFParser();
            CFFFont font = parser.parse(randomAccessFile).stream().findFirst().orElse(null)  ;
            if (font==null) {
                throw new IOException("CFF 文件中未找到字体");
            }
            return new FontInfo(
                "CFF",
                font.getName(),
                font.getName(),
                font.getName(),
                font.getName()
            );
        }
    }
    
    /**
     * 自动检测字体格式
     */
    private static FontInfo autoDetectFontFormat(File fontFile) throws IOException {
        // 首先尝试作为 TrueType/OpenType 解析
        try {
            return readTrueTypeOrOpenType(fontFile);
        } catch (Exception e1) {
            // 然后尝试作为 Type1 解析
            try {
                return readType1Font(fontFile);
            } catch (Exception e2) {
                // 最后尝试作为 CFF 解析
                try {
                    return readCFFFont(fontFile);
                } catch (Exception e3) {
                    throw new IOException("无法识别字体格式。支持的类型: TTF, OTF, TTC, PFB, PFA, CFF");
                }
            }
        }
    }
    

    
    
    /**
     * 字体信息容器类
     */
    public static class FontInfo {
        private final String format;
        private final String familyName;
        private final String fullName;
        private final String postScriptName;
        private final String fontName;
        
        public FontInfo(String format, String familyName, String fullName, String postScriptName, String fontName) {
            this.format = format;
            this.familyName = familyName;
            this.fullName = fullName;
            this.postScriptName = postScriptName;
            this.fontName = fontName;
        }
        
        // Getters
        public String getFormat() { return format; }
        public String getFamilyName() { return familyName != null ? familyName : "未知"; }
        public String getFullName() { return fullName != null ? fullName : "未知"; }
        public String getPostScriptName() { return postScriptName != null ? postScriptName : "未知"; }
        public String getFontName() { return fontName != null ? fontName : "未知"; }
        
        @Override
        public String toString() {
            return String.format("格式: %s, 家族: %s, 名称: %s", format, familyName, fontName);
        }
    }
}
