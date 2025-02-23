#include <stdio.h>
#include <events.h>

char *ev_name="yhscwk2";
int ev_id;
char decide;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,-1,1,ev_name);}
printf("EMERGENCY MODE...Start C now (Y/N) ?\n");
scanf("%c",&decide);
if (decide == 'Y' || decide == 'y')
{
_ev_setr(ev_id,1,4);
printf("Process C signalled...\n");
}
}
