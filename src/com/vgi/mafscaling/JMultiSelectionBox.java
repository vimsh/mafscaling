package com.vgi.mafscaling;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

public class JMultiSelectionBox extends JButton {
	private static final long serialVersionUID = -6780160237517176970L;
	private static int widthCorrection = 2;
	private static int heighCorrection = 2;
	JComboBox<String> combo = new JComboBox<String>();
	JPanel menu = new JPanel();
	JDialog menuFrame = null;
	JScrollPane scroll = null;
	WindowFocusListener windowFocusListener = null;
	MouseListener mouseListener = null;
	ActionListener actionListener = null;
	JButton button;
	boolean mouseOver = false;
	
    public JMultiSelectionBox() {
        this(null, null);
    }

    public JMultiSelectionBox(Icon icon) {
        this(null, icon);
    }

    public JMultiSelectionBox(String text) {
        this(text, null);
    }

    public JMultiSelectionBox(String text, Icon icon) {
    	super(text, icon);
    	if (UIManager.getLookAndFeel().getClass().getName().contains("Motif"))
    		heighCorrection += 4;
    	windowFocusListener = new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent arg0) { }
            @Override
            public void windowLostFocus(WindowEvent arg0) {
            	button.setText(getSelectedItemsString());
            	if (!mouseOver) {
	            	menuFrame.dispose();
	            	menuFrame = null;
            	}
            }
    	};
    	mouseListener = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) { }
			@Override
			public void mousePressed(MouseEvent e) { }
			@Override
			public void mouseReleased(MouseEvent e) { }
			@Override
			public void mouseEntered(MouseEvent e) { mouseOver = true; }
			@Override
			public void mouseExited(MouseEvent e) { mouseOver = false; }    		
    	};
    	button = this;
    	button.addMouseListener(mouseListener);
    	menu.setBackground(Color.WHITE);
    	menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        setAction(new AbstractAction(text) {
			private static final long serialVersionUID = -3085698135352698778L;
			@Override
            public void actionPerformed(ActionEvent e) {
				if (menuFrame != null) {
	            	menuFrame.dispose();
	            	menuFrame = null;
	            	return;
				}
				if (menu.getComponentCount() == 0)
					return;
				menuFrame = new JDialog();
		    	menuFrame.addWindowFocusListener(windowFocusListener);
		    	scroll = new JScrollPane(menu);
		    	scroll.setBorder(new LineBorder(Color.BLACK, 1));
		    	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		    	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    	menuFrame.getContentPane().add(scroll);
		    	menuFrame.setUndecorated(true);
		    	menuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    	menuFrame.setResizable(false);
		        Point location = button.getLocationOnScreen();
		        location.y += button.getHeight();
		        menuFrame.setLocation(location);
				menuFrame.setPreferredSize(new Dimension(button.getWidth(), 150));
		    	menuFrame.pack();
		    	menuFrame.setVisible(true);
		        menuFrame.setResizable(false);
		        menuFrame.requestFocus();
            }
        });
        actionListener = new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		button.setText(getSelectedItemsString());
        	}
        };
        setMargin(new Insets(0, 2, 0, 2));
        UIDefaults def = new UIDefaults();
        def.put("Button.contentMargins", new Insets(0, 3, 0, 3));
        putClientProperty("Nimbus.Overrides", def);
        
        setIcon(new ImageIcon(getClass().getResource("/down.gif")));
        setIconTextGap(3);
        
        setVerticalTextPosition(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setHorizontalTextPosition(SwingConstants.LEFT);
    }
    
    public void addItem(String item) {
    	JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(item);
    	menuItem.addActionListener(actionListener);
    	menuItem.setOpaque(false);
        menu.add(menuItem);
    }
    
    public void removeItem(String item) {
    	for (int i = 0; i < menu.getComponentCount(); ++i) {
    		JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) menu.getComponent(i);
    		if (menuItem.getText().equals(item)) {
    			menu.remove(i);
    			break;
    		}
    	}
    }
    
    public void removeAllItems() {
    	menu.removeAll();
    }
    
    public List<String> getSelectedItems() {
    	ArrayList<String> list = new ArrayList<String>();
    	for (int i = 0; i < menu.getComponentCount(); ++i) {
    		JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) menu.getComponent(i);
    		if (menuItem.isSelected())
    			list.add(menuItem.getText());
    	}
    	if (list.size() > 0)
    		return list;
    	return null;
    }
    
    public String getSelectedItemsString() {
    	String s = " ";
    	for (int i = 0; i < menu.getComponentCount(); ++i) {
    		JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) menu.getComponent(i);
    		if (menuItem.isSelected()) {
    			if (s.trim().isEmpty())
    				s += menuItem.getText();
    			else
    				s += (" + " + menuItem.getText());
    		}
    	}
    	return s;
    }
    

    public void setPrototypeDisplayValue(String val) {
    	combo.setPrototypeDisplayValue(val);
    }
    
    @Override
    public Dimension getPreferredSize() {
    	Dimension d = combo.getPreferredSize();
    	d.width += widthCorrection;
    	d.height += heighCorrection;
		return d;
    }
    
    @Override
    public Dimension getMinimumSize() {
    	Dimension d = combo.getMinimumSize();
    	d.width += widthCorrection;
    	d.height += heighCorrection;
		return d;
    }
    
    @Override
    public Dimension getMaximumSize() {
    	Dimension d = combo.getMaximumSize();
    	d.width += widthCorrection;
    	d.height += heighCorrection;
		return d;
    }
}