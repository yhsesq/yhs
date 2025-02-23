#include <stdio.h>
#include <events.h>

char *ev_name="YSPartTwo";
int ev_id;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
while(ev_id==-1){
sleep(1);}
printf("Sending emergency signal..\n");
_ev_set(ev_id,3,0);
}
