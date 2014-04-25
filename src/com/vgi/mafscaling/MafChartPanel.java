package com.vgi.mafscaling;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MafChartPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final Logger logger = Logger.getLogger(MafChartPanel.class);
    ChartPanel chartPanel = null;
	IMafChartHolder chartHolder = null;
    private XYItemEntity xyItemEntity = null;
    private boolean AllowPointMove = true;
    private boolean IsMovable = false;
    private double initialMovePointY = 0;

	public MafChartPanel(JFreeChart chart, IMafChartHolder holder) {
		chartPanel = new ChartPanel(chart, true, true, true, true, true);
		chartHolder = holder;
		chartPanel.addMouseMotionListener(this);
		chartPanel.addMouseListener(this);
		chartPanel.addMouseWheelListener(this);
		chartPanel.setAutoscrolls(true);
		chartPanel.setMouseZoomable(false);
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

    public void movePoint(MouseEvent event) {
        try {
            if (IsMovable) {
                int itemIndex = xyItemEntity.getItem();
                int seriesIndex = xyItemEntity.getSeriesIndex();
                if (seriesIndex != 0)
                    return;
                XYSeries series = ((XYSeriesCollection)xyItemEntity.getDataset()).getSeries(seriesIndex);
                XYPlot plot = chartPanel.getChart().getXYPlot();
                Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                Point2D p = chartPanel.translateScreenToJava2D(event.getPoint());
                double finalMovePointY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
                double difference = finalMovePointY - initialMovePointY;
                if (series.getY(itemIndex).doubleValue() + difference > plot.getRangeAxis().getRange().getLength() ||
                    series.getY(itemIndex).doubleValue() + difference < 0.0)
                    initialMovePointY = finalMovePointY;
                series.updateByIndex(itemIndex, series.getY(itemIndex).doubleValue() + difference);
                chartHolder.onMovePoint(itemIndex, series.getY(itemIndex).doubleValue());
                chartPanel.getChart().fireChartChanged();
                chartPanel.updateUI();
                initialMovePointY = finalMovePointY;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    private void zoomChartAxis(ChartPanel chart, boolean increase) {
        int width = chart.getMaximumDrawWidth() - chart.getMinimumDrawWidth();
        int height = chart.getMaximumDrawHeight() - chart.getMinimumDrawWidth();        
        if (increase)
           chart.zoomInBoth(width/2, height/2);
        else
           chart.zoomOutBoth(width/2, height/2);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL)
            return;
        if (e.getWheelRotation() < 0)
        	zoomChartAxis(chartPanel, true);
        else
        	zoomChartAxis(chartPanel, false);
    }
    
    public void mouseDragged(MouseEvent e) {
        if (AllowPointMove)
            movePoint(e);
    }

    public void mouseExited(MouseEvent e) {
        IsMovable = false;
        initialMovePointY = 0;
        chartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void mousePressed(MouseEvent e) {
        Insets insets = chartPanel.getInsets();
        int x = (int) ((e.getX() - insets.left) / chartPanel.getScaleX());
        int y = (int) ((e.getY() - insets.top) / chartPanel.getScaleY());
        ChartEntity entity = chartPanel.getChartRenderingInfo().getEntityCollection().getEntity(x,  y);
        if (entity == null || !(entity instanceof XYItemEntity))
            return;
        IsMovable = true;
        chartPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        xyItemEntity = (XYItemEntity)entity;
        XYPlot plot = chartPanel.getChart().getXYPlot();
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
        Point2D p = chartPanel.translateScreenToJava2D(e.getPoint());
        initialMovePointY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
    }

    public void mouseReleased(MouseEvent arg0) {
        IsMovable = false;
        initialMovePointY = 0;
        chartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseMoved(MouseEvent arg0) {
    }

}
