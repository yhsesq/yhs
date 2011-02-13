  /*

MultiSlice RTP Environment
(C) 1998,1999,2000,2001,2002 Yohann Sulaiman.
(C) 1998 Gloria Bueno
(C) 2002  Free Software Foundation, Inc. 


    This file is part of the source code of the MultiSlice RTP Environment.

    MultiSlice RTP Environment is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    MultiSlice RTP Environment  is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MultiSlice RTP Environment; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/


/* Get file name from command line */    
   for (i = 1; i < argc; i ++) 
    {
    if(strcmp("-command",argv[i]) == 0)
        {
        
/* r_file */
        }

        if(strcmp("-load", argv[i]) == 0)
        {
            file_yhs = argv[i+1];
            file_loader=1;
        }
        if(strcmp("-falsecolor", argv[i]) == 0) 
        {
            engage_false_colour=1;
        }
        if(strcmp("-forceload", argv[i]) == 0) 
        {
            forcer=1;
        }

        if((strcmp("-print", argv[i]) == 0) && file_loader == 1) 
        {
    XtAppAddTimeOut(app,5*1000,(XtTimerCallbackProc)print_callback,NULL); 
        }
    
      
    }
