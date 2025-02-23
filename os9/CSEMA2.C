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
_ev_wait(ev_id,0,0);
printf("Process 2 entering critical..");
sleep(5);
printf("Process 2 exiting\n");
_ev_signal(ev_id,2);
printf("Process 2 sleeping..\n");
sleep(10);
_ev_unlink(ev_id);
_ev_delete(ev_name);
printf("Process 2 processing indefinitely..\n"); sleep(40);
}
