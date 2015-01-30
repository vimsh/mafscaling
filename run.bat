@echo off

REM *** Set/Change path to javaw.exe if you have multiple Java installations or if has not been added to the PATH ***
REM set PATH=C:\Program Files\Java\jre17u3\bin;%PATH%

REM

REM *** Windows system skin ***

start javaw -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel -Xms64M -Xmx512M -jar MafScaling.jar

REM

REM *** Nimbus - New Java Swing . REQUIRES JAVA 7 OR GREATER ***

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
