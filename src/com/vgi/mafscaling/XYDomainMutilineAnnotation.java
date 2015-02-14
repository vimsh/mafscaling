package com.vgi.mafscaling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.annotations.AbstractAnnotation;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

public class XYDomainMutilineAnnotation extends AbstractAnnotation implements XYAnnotation {
	private static final long serialVersionUID = -6654378602438420832L;
	
	public class AxisAnnotationEntity extends ChartEntity {
		private static final long serialVersionUID = -6152531745073523000L;
		private int rendererIndex;
		private AbstractAnnotation annotation;

		public AxisAnnotationEntity(Shape hotspot, int rendererIndex, AbstractAnnotation annotation) {
			super(hotspot, "", "");
			this.rendererIndex = rendererIndex;
			this.annotation = annotation;
		}
		
		public int getRendererIndex() {
			return this.rendererIndex;
		}

		public AbstractAnnotation getAnnotation() {
			return this.annotation;
		}

		public void setRendererIndex(int index) {
			this.rendererIndex = index;
		}

		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof AxisAnnotationEntity))
				return false;
			AxisAnnotationEntity that = (AxisAnnotationEntity) obj;
			if (this.rendererIndex != that.rendererIndex)
				return false;
			return true;
		}
	}
	
	private HashMap<Paint, String> labels = new HashMap<Paint, String>();
    private Stroke stroke = new BasicStroke(1.0f);
    private Paint paint = Color.WHITE;
    private Font labelFont = new Font("Verdana", Font.BOLD, 11);
    private RectangleAnchor labelAnchor = RectangleAnchor.TOP;
    private TextAnchor labelTextAnchor;
    private double value;

    public double getValue() {
    	return this.value;
    }
    
    public void setValue(double value) {
        this.value = value;
        fireAnnotationChanged();
    }

    public void setLabelAnchor(RectangleAnchor labelAnchor) {
        this.labelAnchor = labelAnchor;
    }

    public void setLabelTextAnchor(TextAnchor labelTextAnchor) {
        this.labelTextAnchor = labelTextAnchor;
    }
    
    public void addLabel(String label, Paint color, boolean notify) {
    	labels.put(color, label);
    	if (notify)
    		fireAnnotationChanged();
    }
    
    public void removeLabel(Paint paint2) {
    	labels.remove(paint2);
		fireAnnotationChanged();
    }
    
    public void clearLabels(boolean notify) {
    	labels.clear();
    	if (notify)
    		fireAnnotationChanged();
    }
    
    public int count() {
    	return labels.size();
    }
	
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
    	if (labels.size() == 0)
    		return;
        if (info == null)
            return;
        EntityCollection entities = info.getOwner().getEntityCollection();
        if (entities == null)
            return;
		int index = plot.getDomainAxisIndex(domainAxis);
		if (index < 0)
			return;
		RectangleEdge axisEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(index), plot.getOrientation());
		double value = domainAxis.valueToJava2D(this.value, dataArea, axisEdge);
		Line2D line = null;
        if (axisEdge.equals(RectangleEdge.LEFT))
        	line = new Line2D.Double(dataArea.getMinX(), value, dataArea.getMaxX(), value);
        else if (axisEdge.equals(RectangleEdge.RIGHT))
        	line = new Line2D.Double(dataArea.getMaxX(), value, dataArea.getMinX(), value);
        else if (axisEdge.equals(RectangleEdge.TOP))
        	line = new Line2D.Double(value, dataArea.getMinY(), value, dataArea.getMaxY());
        else if (axisEdge.equals(RectangleEdge.BOTTOM))
        	line = new Line2D.Double(value, dataArea.getMaxY(), value, dataArea.getMinY());
		if (line == null)
			return;
		g2.setPaint(paint);
		g2.setStroke(stroke);
		g2.draw(line);
		drawLabels(g2, dataArea, line.getBounds2D(), axisEdge);
        AxisAnnotationEntity entity = new AxisAnnotationEntity(ShapeUtilities.createLineRegion(line, 6), rendererIndex, this);
        entities.add(entity);
    }

    protected void drawLabels(Graphics2D g2, Rectangle2D dataArea, Rectangle2D markerArea, RectangleEdge axisEdge) {
        g2.setFont(labelFont);
        int offset = 0;
    	Point2D coordinates;
        for (Map.Entry<Paint, String> entry : labels.entrySet()) {
        	RectangleInsets labelOffset = new RectangleInsets(2 + offset, 5, 2, 5);
        	offset += 20;
            if (RectangleEdge.isLeftOrRight(axisEdge))
            	coordinates = RectangleAnchor.coordinates(labelOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.CONTRACT, LengthAdjustmentType.EXPAND), labelAnchor);
            else
            	coordinates = RectangleAnchor.coordinates(labelOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.EXPAND, LengthAdjustmentType.CONTRACT), labelAnchor);
            g2.setPaint(entry.getKey());
            TextUtilities.drawRotatedString(entry.getValue(), g2, (float) coordinates.getX(), (float) coordinates.getY(), labelTextAnchor, 0, TextAnchor.TOP_CENTER);
        }
    }
}
