package com.vgi.mafscaling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.apache.jorphan.gui.MenuScroller;

public class JMultiSelectionBox extends JButton {
	private static final long serialVersionUID = -6780160237517176970L;
	JPopupMenu menu = new JPopupMenu();
	ActionListener actionListener;
	JButton button;
	
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
    	button = this;
    	MenuScroller.setScrollerFor(menu);
        setAction(new AbstractAction(text) {
			private static final long serialVersionUID = -3085698135352698778L;
			@Override
            public void actionPerformed(ActionEvent e) {
                menu.show(button, 0, button.getHeight());
            }
        });
        actionListener = new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		button.setText(getSelectedItemsString());
        	}
        };
    }
    
    public void addItem(String item) {
    	JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(item);
    	menuItem.addActionListener(actionListener);
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
}