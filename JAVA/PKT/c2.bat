erase ..\pktupload\pktgui.jar
erase ..\pktupload\pktsrc.jar
erase pkt.log
erase *.class
deltree /Y com
deltree /Y mindbright
deltree /Y netscape
deltree /Y net
deltree /Y org
deltree /Y thinlet
deltree /Y META-INF
deltree /Y compile
mkdir compile
cd compile
erase pktgui.jar
erase pkt.log
erase *.class
c:\java\bin\jar xvf ../../pktupload/pkt.jar 
IF ERRORLEVEL 1 GOTO END
deltree /Y META-INF
erase net\sf\pkt\PKTCLI.class
copy ..\*.gif net\sf\pkt
copy ..\*.xml net\sf\pkt
copy ..\*.jav .
copy ..\*.MF .
copy ..\License .
erase pktcli.java
copy pktcli-gui.jav PKTCLI.java
c:\java\bin\javac -O -g:none -target 1.1 -d . *.java
IF ERRORLEVEL 1 GOTO END
erase *.java
c:\java\bin\jar cvmf MANIFEST.MF pktgui.jar *
CALL ..\..\pkt\cx.bat
move pktgui.jar ..\..\pktupload
cd ..
deltree /Y compile
erase *.class
c:\java\bin\jar cvf pktsrc.jar *
CALL ..\pkt\cx.bat
move pktsrc.jar ..\pktupload
rem c:\java\bin\java -jar pktgui.jar
rem c:\java\bin\java -cp pktgui.jar net.sf.pkt.PKTXUL
:END
