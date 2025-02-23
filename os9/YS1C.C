#include "cheader.h"

char *ev_name="YSPartOne";
char pname='C';
int ev_id;
float a,b,c;
main()
{
int count=0;
ev_id = _ev_link(ev_name);
if (ev_id==-1){
}
while (_ev_read(ev_id) != 3){}
printf("Process %c starting .. Enter final value : \n",pname);
scanf ("%f",&c);
   modptr = modlink(modname,lang_type);
    dataptr = (dx*)(modptr+((mod_exec*)modptr) -> _mexec);
    printf("Starting address of data is %x \n",dataptr);
    sum = (dataptr->kp) + (dataptr->kl);
    printf("\n The sum of the co-efficients is %f\n",sum);
printf(" The value entered is : %f \n",c);
if (c==sum) { printf("\nXX-->> X The two values match OK.\n"); }
if (c!=sum) { printf("\nXX-->> ? The two values do not match.\n");}
_ev_signal(ev_id,0);
_ev_set(ev_id,0,0);
for (count=0;count<=4;count++){
    munlink(modptr);
    munload(modname,lang_type);
    _ev_unlink(ev_id);
    _ev_delete(ev_name);
    }
}
