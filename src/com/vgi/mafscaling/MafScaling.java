/*
* Open-Source tuning tools
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.vgi.mafscaling;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

public class MafScaling {
    private static final Logger logger = Logger.getLogger(MafScaling.class);
    private static final String Title = "MAF Scaling - v2.5.2";
    private static final String OLTabName = "<html>Open Loop</html>";
    private static final String CLTabName = "<html>Closed Loop</html>";
    private static final String MRTabName = "<html>MAF Rescale</html>";
    private static final String TRTabName = "<html>Table Rescale</html>";
    private static final String LCTabName = "<html>Load Comp</html>";
    private static final String MITabName = "<html>MAF IAT Comp</html>";
    private static final String VETabName = "<html>MAF VE Calc</html>";
    private static final String VCTabName = "<html>WOT Best VVT</html>";
    private static final String LSTabName = "<html>Log Stats</html>";
    private static final String LVTabName = "<html>Log View</html>";
    private JFrame frame;

    /**
     * Launch the application.
     * @throws UnsupportedLookAndFeelException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws Exception {
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        
        if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
            UIManager.put("Table.gridColor", new Color(214, 217, 223));
            UIManager.put("Table.disabled", false);
            UIManager.put("Table.showGrid", true);
            UIManager.put("Table.intercellSpacing", new Dimension (1, 1));
            UIManager.put("TitledBorder.font", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            UIManager.put("Table.selectionBackground", new Color(115, 164, 209));
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.focusCellBackground", new Color(115, 164, 209));
            UIManager.put("Table.focusCellForeground", Color.WHITE);
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
        MafCompare mafCompare = new MafCompare();

        ImageIcon chartImage = new ImageIcon(getClass().getResource("/chart.jpg"));
        
        frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
                    Config.setWindowSize(frame.getSize());
                    Config.setWindowLocation(frame.getLocation());
                }
                Config.setLastLogFilesPath(FCTabbedPane.getLogFilesPath());
                Config.save();
            }
        });
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        JTabbedPane ol = new OpenLoop(JTabbedPane.LEFT, pofFuelingTable, mafCompare);
        ol.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(ol, OLTabName);

        JTabbedPane cl = new ClosedLoop(JTabbedPane.LEFT, pofFuelingTable, mafCompare);
        cl.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(cl, CLTabName);

        JTabbedPane mr = new MafRescale(JTabbedPane.LEFT);
        mr.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(mr, MRTabName);

        JTabbedPane tr = new TableRescale(JTabbedPane.LEFT);
        tr.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(tr, TRTabName);

        JTabbedPane lc = new LoadComp(JTabbedPane.LEFT);
        lc.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(lc, LCTabName);

        JTabbedPane mi = new MafIatComp(JTabbedPane.LEFT, pofFuelingTable);
        mi.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(mi, MITabName);
        
        JTabbedPane ve = new VECalc(JTabbedPane.LEFT);
        ve.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(ve, VETabName);

        JTabbedPane vc = new VVTCalc(JTabbedPane.LEFT);
        vc.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(vc, VCTabName);
        
        JTabbedPane ls = new LogStats(JTabbedPane.LEFT);
        ls.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(ls, LSTabName);
        
        JTabbedPane lv = new LogView(JTabbedPane.LEFT);
        lv.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(lv, LVTabName);

        frame.pack();
        frame.doLayout();
        frame.setTitle(Title);
        frame.setBounds(100, 100, 621, 372);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Config.getWindowSize());
        frame.setLocation(Config.getWindowLocation());
        frame.setIconImage(chartImage.getImage());
    }
}
