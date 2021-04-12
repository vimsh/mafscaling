#!/bin/sh
# *** GTK ***
#java -Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &


# *** Java Default ***
# start javaw -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar

# *** New Java Swing ***
java -Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &

# *** Linux Motif ***
#java -Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &

# *** Variations of skinned default Java Swing look and feel ***
#java -Dswing.defaultlaf=com.jtattoo.plaf.aero.AeroLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.smart.SmartLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.mint.MintLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.fast.FastLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.luna.LunaLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.texture.TextureLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.mcwin.McWinLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.aluminium.AluminiumLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.acryl.AcrylLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.graphite.GraphiteLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.hifi.HiFiLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.noire.NoireLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
#java -Dswing.defaultlaf=com.jtattoo.plaf.bernstein.BernsteinLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar &
