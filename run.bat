@echo off
REM *** Adjust path to javaw.exe as required for your Java system installation ***
REM set PATH=%PATH%;C:\ProgramData\Oracle\Java\javapath
REM
REM *** Windows system ***
start javaw -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM
REM *** New Java Swing ***
REM start javaw -Dswing.defaultlaf=com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM
REM *** Linux Motif ***
REM start javaw -Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM
REM *** Variations of skinned default Java Swing look and feel ***
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.aero.AeroLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.smart.SmartLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.mint.MintLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.fast.FastLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.luna.LunaLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.texture.TextureLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.mcwin.McWinLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.aluminium.AluminiumLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.acryl.AcrylLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.graphite.GraphiteLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.hifi.HiFiLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.noire.NoireLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar
REM start javaw -Dswing.defaultlaf=com.jtattoo.plaf.bernstein.BernsteinLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar