package com.vgi.mafscaling;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;

public class MafScaling {
    private static final Logger logger = Logger.getLogger(MafScaling.class);
    private static final String Title = "MAF Scaling - v1.2.0";
    private static final String OLTabName = "<html>Open Loop</html>";
    private static final String CLTabName = "<html>Closed Loop</html>";
    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MafScaling window = new MafScaling();
                    window.frame.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MafScaling() {
        Config.load();
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
    	PrimaryOpenLoopFuelingTable pofFuelingTable = new PrimaryOpenLoopFuelingTable();
    	
        frame = new JFrame();
        frame.setTitle(Title);
        frame.setBounds(100, 100, 621, 372);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Config.getWindowSize());
        frame.setLocation(Config.getWindowLocation());
        frame.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
                Config.setWindowSize(frame.getSize());
                Config.setWindowLocation(frame.getLocation());
        		Config.save();
        	}
        });
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        JTabbedPane ol = new OpenLoop(JTabbedPane.LEFT, pofFuelingTable);
        ol.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(ol, OLTabName);

        JTabbedPane cl = new ClosedLoop(JTabbedPane.LEFT, pofFuelingTable);
        cl.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(cl, CLTabName);
    }
}
