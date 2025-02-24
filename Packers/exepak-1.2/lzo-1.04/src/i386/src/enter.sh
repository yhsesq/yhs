/* enter.sh -- LZO assembler stuff

   This file is part of the LZO real-time data compression library.

   Copyright (C) 1997 Markus Franz Xaver Johannes Oberhumer
   Copyright (C) 1996 Markus Franz Xaver Johannes Oberhumer

   The LZO library is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of
   the License, or (at your option) any later version.

   The LZO library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with the LZO library; see the file COPYING.
   If not, write to the Free Software Foundation, Inc.,
   59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

   Markus F.X.J. Oberhumer
   markus.oberhumer@jk.uni-linz.ac.at
 */


/***********************************************************************
//
************************************************************************/

        pushl   %ebp
        pushl   %edi
        pushl   %esi
        pushl   %ebx
        pushl   %ecx
        pushl   %edx
        subl    $12,%esp

        cld

        movl    INP,%esi
        movl    OUTP,%edi
#if defined(N_3_EBP)
        movl    $3,%ebp
#endif
#if defined(N_255_EBP)
        movl    $255,%ebp
#endif

#if defined(LZO_TEST_DECOMPRESS_OVERRUN_INPUT)
#if defined(INIT_OVERRUN)
        INIT_OVERRUN
# undef INIT_OVERRUN
#endif
        leal    -3(%esi),%eax       /* 3 == length of EOF code */
        addl    INS,%eax
        movl    %eax,INEND
#endif

#if defined(LZO_TEST_DECOMPRESS_OVERRUN_OUTPUT)
#if defined(INIT_OVERRUN)
        INIT_OVERRUN
# undef INIT_OVERRUN
#endif
        movl    %edi,%eax
        movl    OUTS,%edx
        addl    (%edx),%eax
        movl    %eax,OUTEND
#endif


/*
vi:ts=4
*/

