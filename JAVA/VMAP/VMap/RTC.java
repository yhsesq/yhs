package vmap;
import java.lang.*;
import java.applet.*;

public class RTC extends Thread {
int seconds=0,xyz=0;
RTC (int seconds){
}
public void run(){
while(xyz != 1)
{
seconds++;
try { Thread.sleep(1000); } catch (InterruptedException e){}
}
}

public int retval(){
return seconds;
}

public int rst(int m)
{seconds=m;return m;}

}

