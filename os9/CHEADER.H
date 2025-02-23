#include <stdio.h>
#include <module.h>
#include <events.h>

#define REVS 0x64
#define ERROR -1

short attr = (MA_REENT<<8)|REVS;               /* module attr */
short perm = MP_OWNER_READ|MP_OWNER_WRITE;     /* rw perms */
int lang_type = 0;                             /* english */

/* Declare variables & funcs for data_module */

typedef struct datapool {
                            float kp;
                            float kd;
                            float kl;
                        } dx;

unsigned modsize = sizeof(dx);
char *modname = "datamodule1";
char *modlink();
char *modptr;
char *_mkdata_module();
dx *dataptr;

float sum;


