#include <stdio.h>
#include <events.h>

char *ev_name="YSPartTwo";
char pname='B';
int ev_id;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
printf("WB");
}
while(_ev_read(ev_id) != 2) {}
printf("Process %c entering processing for 5 seconds mode..\n",pname);
sleep(5);
printf("Process %c sleeping for 5 seconds.\n",pname);
_ev_set(ev_id,3,0);
sleep(5);
printf("Process %c processing indefinitely.\n",pname);
sleep(20);
_ev_unlink(ev_name);
_ev_delete(ev_name);
}
