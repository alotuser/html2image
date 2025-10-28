package cn.alotus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.openhtmltopdf.render.BlockBox;
import com.openhtmltopdf.render.Box;
import com.openhtmltopdf.render.InlineBox;
import com.openhtmltopdf.render.InlineLayoutBox;
import com.openhtmltopdf.render.LineBox;

/**
 * 辅助工具类，处理AsBox相关内容
 */
public class AsBoxUtil {

	/**
	 * 生成指定Box及其所有后代Box的结构化字符串表示，便于调试和分析布局结构。
	 * 
	 * @param root 要生成表示的根Box对象
	 * @return 包含Box层级结构的字符串表示
	 */
	public static String descendantDump(Box root) {
		StringBuilder spaces = new StringBuilder(100);
		IntStream.range(0, 100).forEach(unused -> spaces.append(' '));
		char[] space = new char[100];
		spaces.getChars(0, spaces.length(), space, 0);

		StringBuilder sb = new StringBuilder();
		List<IndentObject> renderObjects = new ArrayList<>();

		findBoxs(root, 0, renderObjects);

		for (IndentObject content : renderObjects) {
			sb.append(space, 0, Math.min(100, content.indent * 4));

			if (content.object instanceof InlineBox) {

				InlineBox box = (InlineBox) content.object;

				// Element el= box.getElement();

				Element el = box.getElement();
				if (null != el) {
					sb.append("<" + el.getTagName() + formatAttrs(el) + ">");
				} else {
					sb.append(content.object.toString() + " => " + Integer.toHexString(System.identityHashCode(content.object)));
				}

			} else if (content.object instanceof BlockBox) {

				BlockBox box = (BlockBox) content.object;

				Element el = box.getElement();
				if (null != el) {
					sb.append("<" + el.getTagName() + formatAttrs(el) + ">");
				} else {
					sb.append(content.object.toString() + " => " + Integer.toHexString(System.identityHashCode(content.object)));
				}

			} else if (content.object instanceof LineBox) {

				LineBox box = (LineBox) content.object;

				Element el = box.getElement();
				if (null != el) {
					sb.append("<" + el.getTagName() + formatAttrs(el) + ">");
				} else {
					sb.append(content.object.toString() + " => " + Integer.toHexString(System.identityHashCode(content.object)));
				}

			} else if (content.object instanceof InlineLayoutBox) {
				InlineLayoutBox box = (InlineLayoutBox) content.object;
				Element el = box.getElement();
				if (null != el) {
					sb.append("<" + el.getTagName() + formatAttrs(el) + ">");
				} else {
					sb.append(content.object.toString() + " => " + Integer.toHexString(System.identityHashCode(content.object)));
				}

			} else {
				sb.append(content.object.toString() + " => " + Integer.toHexString(System.identityHashCode(content.object)));
			}

			sb.append('\n');
		}

		return sb.toString();
	}

	/**
	 * * 递归查找指定Box及其所有后代Box，并将它们按缩进级别存储在输出列表中。
	 * 
	 * @param parent 要查找的父Box对象
	 * @param indent 当前缩进级别
	 * @param out    用于存储结果的输出列表
	 */
	public static void findBoxs(Box parent, int indent, List<IndentObject> out) {
		out.add(new IndentObject(parent, indent));

		indent++;

		for (Box child : parent.getChildren()) {
			findBoxs(child, indent, out);
		}

		if (parent instanceof BlockBox && ((BlockBox) parent).getInlineContent() != null) {
			for (Object child : ((BlockBox) parent).getInlineContent()) {
				if (child instanceof Box) {
					findBoxs((Box) child, indent, out);
				} else {
					out.add(new IndentObject(child, indent));
				}
			}
		}

		if (parent instanceof InlineLayoutBox) {
			for (Object child : ((InlineLayoutBox) parent).getInlineChildren()) {
				if (child instanceof Box) {
					findBoxs((Box) child, indent, out);
				} else {
					out.add(new IndentObject(child, indent));
				}
			}
		}
	}

	private static String formatAttrs(Element el) {
		NamedNodeMap attrs = el.getAttributes();
		if (attrs == null || attrs.getLength() == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node a = attrs.item(i);
			sb.append(" ").append(a.getNodeName()).append("=\"").append(a.getNodeValue()).append("\"");
		}
		return sb.toString();
	}

	private static class IndentObject {
		final Object object;
		final int indent;

		IndentObject(Object obj, int indent) {
			this.object = obj;
			this.indent = indent;
		}
	}

}
