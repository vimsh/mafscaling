package com.vgi.mafscaling;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

public abstract class FCTabbedPane extends JTabbedPane {
    private static final long serialVersionUID = -1927797105079280969L;
    protected static final JFileChooser fileChooser = new JFileChooser();
    
    public FCTabbedPane(int tabPlacement) {
        super(tabPlacement);
        File logsPath = new File(Config.getLastLogFilesPath());
        if (logsPath.exists() && logsPath.isDirectory())
            fileChooser.setCurrentDirectory(logsPath);
        else
            fileChooser.setCurrentDirectory(new File("."));
        
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        if (fileChooser.getFileFilter() == null)
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        
        setTransferHandler(new TransferHandler() {
            private static final long serialVersionUID = 1L;
            
            @Override 
            public boolean canImport(TransferHandler.TransferSupport support) {
                boolean can = support.isDrop() && (support.getSourceDropActions() & COPY) != 0 && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                if (can)
                    support.setDropAction(COPY);
                return can;
            }
            
            @SuppressWarnings("unchecked")
            @Override 
            public boolean importData(TransferHandler.TransferSupport info) {
                try {
                    List<File> files = (List<File>)info.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (int i = 0; i < files.size(); ++i) {
                        try {
                            String fn = files.get(i).getCanonicalPath();
                            if (!(fn.substring(fn.lastIndexOf(".") + 1).equalsIgnoreCase("csv"))) {
                                JOptionPane.showMessageDialog(null, "Invalid file type - only CSV file are supported: " + fn, "Invalid file", JOptionPane.ERROR_MESSAGE);
                                return false;
                            }
                        }
                        catch( java.io.IOException e ) {}
                    }
                    onDroppedFiles(files);
                    return true;
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error on drag'n'drop: " + e.getMessage(), "Invalid file", JOptionPane.ERROR_MESSAGE);
                }
                return false; 
            }
            
        });
    }
    
    public static String getLogFilesPath() {
        return fileChooser.getCurrentDirectory().getAbsolutePath();
    }
    
    protected abstract void onDroppedFiles(List<File> files);
}
