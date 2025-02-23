#include <stdio.h>
#include <events.h>

char *ev_name="semaevent";
int ev_id;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,-1,1,ev_name);
}
_ev_wait(ev_id,2,2);
printf("Process 3 entering critical..");
sleep(5);
printf("Process 3 exiting\n");
_ev_signal(ev_id,4);
_ev_unlink(ev_id);
_ev_delete(ev_name);
printf("Process 3 processing indefinitely.\n");
sleep(40);
}
