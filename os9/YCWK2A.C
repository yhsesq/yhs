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
printf("Time 0 starting now..\n");
_ev_wait(ev_id,1,1);
printf("Process A starting..Processing for 5 seconds..\n");
sleep(5);
printf("Process A exiting...\n");
_ev_setr(ev_id,1,2);
printf("Process A going to sleep...\n");
sleep(10);
printf("Process A waking up .. & processing indefinitely (e.g. 40 seconds)\n");
sleep(40);
_ev_unlink(ev_id);
_ev_delete(ev_name);
printf("Process A .. Terminated.\n");
}
