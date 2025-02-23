#include <stdio.h>
#include <events.h>

char *ev_name="yhscwk2";
int ev_id;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,1,1,ev_name);}
_ev_waitr(ev_id,2,2);
printf("Process B starting..Processing for 5 seconds..\n");
sleep(5);
printf("Process B exiting...\n");
_ev_setr(ev_id,2,3);
printf("Process B going to sleep...\n");
sleep(10);
printf("Process B waking up .. & processing indefinitely (e.g. 40 seconds)\n");
sleep(40);
_ev_unlink(ev_id);
_ev_delete(ev_name);
printf("Process B .. Terminated.\n");
}
