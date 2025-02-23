#include "cheader.h"

char *ev_name="YSPartOne";
char pname='B';
int ev_id;
int a;
int ev_value;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
ev_id=_ev_creat(1,-1,1,ev_name);
}
while(_ev_read(ev_id) != 2) {}
   modptr = modlink(modname,lang_type);
    dataptr = (dx*)(modptr+((mod_exec*)modptr) -> _mexec);
printf("Starting address of module %s = %x\n",modname,modptr);
printf("Starting address of data is %x \n",dataptr);
printf("Process %c entering input mode..\n",pname);
printf("Enter your number : ");
scanf ("%f",&dataptr->kl);
printf("\nProcess %c exiting\n",pname);
_ev_set(ev_id,3,0);
    _ev_unlink(ev_id);
    _ev_delete(ev_name);
}
