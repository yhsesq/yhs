/*

SentinelD v1.0.0c - Sentinel Daemon frontend.
(C) 1999 Zurk Technology Inc.

*/

/********************************************************************
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 ********************************************************************
 */


#include <assert.h>
#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#ifdef GETOPTLONG
#include <getopt.h>
#endif /* GETOPTLONG */
#include <pwd.h>
#include <regex.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/time.h>
#include <unistd.h>
#include "rmd160mc.h"
#define RMDsize 160
#include "sentinel.h"

void ping(const char *hostname)
{
int fried=0;
int length=0;
char *stringstuff="SentinelD: Sentinel executing checks......";
struct hostent *server;
struct sockaddr_in addr;

fried=socket(AF_INET,SOCK_STREAM,0);
if (fried == -1)
{return;}
server=gethostbyname(hostname);
if (server==NULL)
{addr.sin_addr.s_addr = inet_addr(hostname);}
else
{memcpy(&addr.sin_addr,server->h_addr, server->h_length);
addr.sin_family=AF_INET;
addr.sin_port=htons(80);}
if (connect(fried, (struct sockaddr *)&addr, sizeof(addr)))
{return;}
write(fried,stringstuff,42);
return;
}

int main(int argc, char **argv)
{
int pinghost=0;
int i=0;
char longstring[16384];

if (argc==3)
{pinghost=1;}
if (geteuid()!=0){printf("\nSentinelD: WARNING: No root permissions for SentinelD. Have you given root perms to sentinel ? \n");}
printf("\nSentinelD v1.0.0c - Sentinel daemon frontend.\n");
printf("Will run %s every 12 hours..Kill PID: %d to stop.\n",executable,getpid());
printf("Put it in your /etc/init.d to start automatically at bootup. \n sentineld -URL <webserver> will hit the webserver before launching sentinel.\n");
longstring[0]='\0';
strcat(&longstring[0],executable);
strcat(&longstring[0]," -check &");
printf("Executing %s and backgrounding. No further output.",&longstring[0]); 

while (i != 99){

if (pinghost==1){ping(argv[2]);}
system(&longstring[0]);

for (i=0; i < 12; i++){sleep(3600);}
i=0;
}

exit(0);
}
