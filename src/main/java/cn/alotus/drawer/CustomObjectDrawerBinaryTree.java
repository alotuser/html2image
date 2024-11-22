package cn.alotus.drawer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.w3c.dom.Element;

import com.openhtmltopdf.extend.FSObjectDrawer;
import com.openhtmltopdf.extend.OutputDevice;
import com.openhtmltopdf.extend.OutputDeviceGraphicsDrawer;
import com.openhtmltopdf.render.RenderingContext;

public class CustomObjectDrawerBinaryTree implements FSObjectDrawer {
	int fanout;
	int angle;

	@Override
	public Map<Shape, String> drawObject(Element e, double x, double y, final double width, final double height, OutputDevice outputDevice, RenderingContext ctx, final int dotsPerPixel) {
		final int depth = Integer.parseInt(e.getAttribute("data-depth"));
		fanout = Integer.parseInt(e.getAttribute("data-fanout"));
		angle = Integer.parseInt(e.getAttribute("data-angle"));

		outputDevice.drawWithGraphics((float) x, (float) y, (float) width / dotsPerPixel, (float) height / dotsPerPixel, new OutputDeviceGraphicsDrawer() {
			@Override
			public void render(Graphics2D graphics2D) {
				double realWidth = width / dotsPerPixel;
				double realHeight = height / dotsPerPixel;
				double titleBottomHeight = 10;

				renderTree(graphics2D, realWidth / 2f, realHeight - titleBottomHeight, realHeight / depth, -90, depth);

				/*
				 * Now draw some text using different fonts to exercise all different font
				 * mappings
				 */
				Font font = Font.decode("Times New Roman").deriveFont(10f);
				if (depth == 10)
					font = Font.decode("Arial"); // Does not get mapped
				if (angle == 35)
					font = Font.decode("Courier"); // Would get mapped to Courier
				if (depth == 6)
					font = Font.decode("Dialog"); // Gets mapped to Helvetica
				graphics2D.setFont(font);
				String txt = "FanOut " + fanout + " Angle " + angle + " Depth " + depth;
				Rectangle2D textBounds = font.getStringBounds(txt, graphics2D.getFontRenderContext());
				graphics2D.setPaint(new Color(16, 133, 30));
				GradientPaint gp = new GradientPaint(10.0f, 25.0f, Color.blue, (float) textBounds.getWidth(), (float) textBounds.getHeight(), Color.red);
				if (angle == 35)
					graphics2D.setPaint(gp);
				graphics2D.drawString(txt, (int) ((realWidth - textBounds.getWidth()) / 2), (int) (realHeight - titleBottomHeight));
			}
		});
		return null;
	}

	private void renderTree(Graphics2D gfx, double x, double y, double len, double angleDeg, int depth) {
		double rad = angleDeg * Math.PI / 180f;
		double xTarget = x + Math.cos(rad) * len;
		double yTarget = y + Math.sin(rad) * len;
		gfx.setStroke(new BasicStroke(2f));
		gfx.setColor(new Color(255 / depth, 128, 128));
		gfx.draw(new Line2D.Double(x, y, xTarget, yTarget));

		if (depth > 1) {
			double childAngle = angleDeg - (((fanout - 1) * angle) / 2f);
			for (int i = 0; i < fanout; i++) {
				renderTree(gfx, xTarget, yTarget, len * 0.95, childAngle, depth - 1);
				childAngle += angle;
			}
		}
	}
}
