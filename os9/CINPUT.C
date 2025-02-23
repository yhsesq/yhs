#include "cheader.h"

main() {

ev_id = _ev_link(ev_name);
if (ev_id == ERROR) {
ev_id = _ev_creat(1,-1,2,ev_name);
}

_ev_wait(ev_id,1,1);

modptr = _mkdata_module(modname,modsize,attr,perm);
printf("Starting address of module %s = %x\n",modname,modptr);
dataptr = (dx *)(modptr + ((mod_exec *)modptr) -> _mexec);
printf("Starting address of data is %x \n",dataptr);

printf("\n Coefficients kp,kd,kl :");
scanf("%f",&dataptr->kp);
scanf("%f",&dataptr->kd);
scanf("%f",&dataptr->kl);
printf("OK\n");
_ev_signal(ev_id,0);
_ev_wait(ev_id,3,3);
munlink(modptr);
_ev_unlink(ev_id);
_ev_delete(ev_name);
}

