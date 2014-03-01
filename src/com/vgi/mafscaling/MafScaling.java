package com.vgi.mafscaling;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;

public class MafScaling {
    private static final Logger logger = Logger.getLogger(MafScaling.class);
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
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("MAF Scaling - Beta 7");
        frame.setBounds(100, 100, 621, 372);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        JTabbedPane badNoodlePane = new BadNoodle(JTabbedPane.LEFT);
        badNoodlePane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(badNoodlePane, "<html>Open Loop by BadNoodle</html>");

        JTabbedPane mickeyd2005 = new Mickeyd2005(JTabbedPane.LEFT);
        mickeyd2005.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(mickeyd2005, "<html>Closed Loop by Mickeyd2005</html>");
    }
}
