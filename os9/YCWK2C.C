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
_ev_waitr(ev_id,3,4);
printf("Process C starting .. & processing indefinitely (e.g. 40 seconds)\n");
sleep(40);
printf("Process C Terminated...");
_ev_unlink(ev_id);
_ev_delete(ev_name);
}
