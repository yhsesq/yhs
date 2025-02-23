#include <stdio.h>
#include <events.h>

char *ev_name="semaevent";
int ev_id;
main()
{
int count=0;
ev_id = ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,-1,1,ev_name);
}
while (count++ < 2)
{
_ev_wait(ev_id,1,1);
_print("Process 1 entering critical..");
sleep(2);
printf("Process 1 exiting\n");
_ev_signal(ev_id,0);
printf("P1 doing noncritical\n");
sleep(1);
}
_ev_unlink(ev_id);
_ev_delete(ev_name);
printf("P1 EXIT\n");
}
