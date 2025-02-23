#include "cheader.h"

main()
{
    float sum;

    ev_id = _ev_link(ev_name);
    if(ev_id == ERROR) {
    ev_id = _ev_creat(1,-1,2,ev_name);
    }
    _ev_wait(ev_id,2,2);
    modptr = modlink(modname,lang_type);
    dataptr = (dx*)(modptr+((mod_exec*)modptr) -> _mexec);
    printf("Starting address of data is %x \n",dataptr);
    sum = (dataptr->kp) + (dataptr->kd) + (dataptr->kl);
    printf("\n The sum of the co-efficients is %f\n",sum);
    _ev_signal(ev_id,0);
    munlink(modptr);
    _ev_unlink(ev_id);
    _ev_delete(ev_name);
}
