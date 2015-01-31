package com.vgi.mafscaling;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

public class FCTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = -1927797105079280969L;
	protected static final JFileChooser fileChooser = new JFileChooser();
	
	public FCTabbedPane(int tabPlacement) {
        super(tabPlacement);
        fileChooser.setCurrentDirectory(new File("."));
    }
}
