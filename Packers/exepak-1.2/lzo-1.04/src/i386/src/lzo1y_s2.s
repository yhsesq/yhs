/* lzo1y_s2.s -- LZO1Y decompression in assembler (i386 + gcc)

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

#define LZO_TEST_DECOMPRESS_OVERRUN_INPUT
#define LZO_TEST_DECOMPRESS_OVERRUN_OUTPUT
#define LZO_TEST_DECOMPRESS_OVERRUN_LOOKBEHIND

#include "lzo_asm.h"

	.text

	LZO_PUBLIC(lzo1y_decompress_asm_safe)

#define LZO1Y

#include "enter.sh"
#include "lzo1x_d.sh"
#include "leave.sh"

	LZO_PUBLIC_END(lzo1y_decompress_asm_safe)


/*
vi:ts=4
*/

