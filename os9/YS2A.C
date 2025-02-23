#include <stdio.h>
#include <events.h>

char *ev_name="YSPartTwo";
char pname='A';
int ev_id;
int ev_value;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,-1,1,ev_name);
}
printf("Process %c entering processing for 5 seconds mode..\n",pname);
sleep(5);
printf("Process %c sleeping for 10 seconds. \n",pname);
_ev_set(ev_id,2,0);
sleep(10);
printf("Process %c processing indefinitely.\n",pname);
sleep(20);
_ev_unlink(ev_id);
_ev_delete(ev_name);
}
