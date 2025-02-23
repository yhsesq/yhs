#include "cheader.h"

char *ev_name="YSPartOne";
char pname='A';
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
modptr = _mkdata_module(modname,modsize,attr,perm);
printf("Starting address of module %s = %x\n",modname,modptr);
dataptr = (dx *)(modptr + ((mod_exec *)modptr) -> _mexec);
printf("Starting address of data is %x \n",dataptr);

printf("Process %c entering input mode..\n",pname);
printf("Enter your number : ");
scanf ("%f",&dataptr->kp);
printf("\nProcess %c exiting\n",pname);
_ev_set(ev_id,2,0);
    _ev_unlink(ev_id);
    _ev_delete(ev_name);

}
