#include <stdio.h>
#include <events.h>

char *ev_name="YSPartTwo";
char pname='C';
int ev_id;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
printf("WC");
}
sleep(5);
while (_ev_read(ev_id) != 3){}
_ev_set(ev_id,1,0);
printf("Process %c processing indefinitely\n",pname);
sleep(30);
while(_ev_delete(ev_name) != -1){
_ev_unlink(ev_id);
_ev_delete(ev_name);}
}
