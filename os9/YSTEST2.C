#include <stdio.h>
#include <events.h>
char *ev_name = "YSPartTwo";
int ev_id;int ev_value;
main()
{
ev_id = _ev_link(ev_name);
if (ev_id == -1) {ev_id = _ev_creat(1,-1,1,ev_name);}
_ev_wait(ev_id,1,1);
_ev_set(ev_id,4,0);
sleep(1);
ev_value=_ev_read(ev_id);
printf("ev_id = %d, ev_value = %d \n",ev_id,ev_value);
}
