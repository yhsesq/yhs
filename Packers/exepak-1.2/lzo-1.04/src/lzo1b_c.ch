/* lzo1b_c.ch -- implementation of the LZO1B compression algorithm

   This file is part of the LZO real-time data compression library.

   Copyright (C) 1998 Markus Franz Xaver Johannes Oberhumer
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

#if !defined(LZO_HAVE_R1) && !defined(LZO_NO_R1)
#  define LZO_HAVE_R1
#endif

#if !defined(LZO_HAVE_M3) && !defined(LZO_NO_M3)
#  if (M3O_BITS < 8)
#    define LZO_HAVE_M3
#  endif
#endif


#define MI
#define SI		MI
#if (DD_BITS > 0)
#define DI		++ii; DVAL_NEXT(dv,ii); UPDATE_D(dict,drun,dv,ii,in); MI
#define XI		assert(ii < ip); ii = ip; DVAL_FIRST(dv,(ip));
#else
#define DI		++ii; DINDEX1(dindex,ii); UPDATE_I(dict,0,dindex,ii,in); MI
#define XI		assert(ii < ip); ii = ip;
#endif


/***********************************************************************
// compress a block of data.
//
// I really apologize for this spaghetti code.
************************************************************************/

LZO_PRIVATE(int)
do_compress    ( const lzo_byte *in , lzo_uint  in_len,
                       lzo_byte *out, lzo_uint *out_len,
                       lzo_voidp wrkmem )
{
/* this seems to work with buggy gcc */
/* #if defined(LZO_OPTIMIZE_GNUC_i386) */
#if 0 && defined(__GNUC__) && defined(__i386__)
	register const lzo_byte *ip __asm__("%esi");
#else
	register const lzo_byte *ip;
#endif
#if (DD_BITS > 0)
#if defined(__LZO_HASH_INCREMENTAL)
	lzo_uint32 dv;
#endif
	unsigned drun = 0;
#endif
	lzo_byte *op;
	const lzo_byte * const in_end = in + in_len;
	const lzo_byte * const ip_end = in + in_len - MIN_LOOKAHEAD;
	const lzo_byte *ii;
#if defined(LZO_HAVE_R1)
	const lzo_byte *r1 = ip_end;	/* pointer for R1 match (none yet) */
#endif
#if defined(LZO_HAVE_M3)
	lzo_byte *m3 = out + 1;			/* pointer after last m3/m4 match */
#endif

	lzo_dict_p const dict = (lzo_dict_p) wrkmem;


#if defined(LZO_COLLECT_STATS)
	lzo_stats->r_bits   = R_BITS;
	lzo_stats->m3o_bits = M3O_BITS;
	lzo_stats->dd_bits  = DD_BITS;
	lzo_stats->clevel   = CLEVEL;
	lzo_stats->d_bits   = D_BITS;
	lzo_stats->min_lookahead  = MIN_LOOKAHEAD;
	lzo_stats->max_lookbehind = MAX_LOOKBEHIND;
	lzo_stats->compress_id    = _LZO_MEXPAND(COMPRESS_ID);
#endif

	/* init dictionary */
#if defined(LZO_DETERMINISTIC)
	BZERO8_PTR(dict,sizeof(lzo_dict_t),D_SIZE);
#endif


	op = out;
	ip = in;
	ii = ip;			/* point to start of current literal run */


#if (DD_BITS > 0)
	DVAL_FIRST(dv,ip);
	UPDATE_D(dict,drun,dv,ip,in);
	ip++;
	DVAL_NEXT(dv,ip);
#else
	ip++;
#endif

	assert(ip < ip_end);
	for (;;)
	{
		const lzo_byte *m_pos;
#if !defined(NDEBUG)
		const lzo_byte *m_pos_sav = NULL;
#endif
		lzo_moff_t m_off;
#if (DD_BITS == 0)
		lzo_uint dindex;
#endif
		lzo_uint m_len;


/***********************************************************************
// search for a match
************************************************************************/

#if !defined(LZO_SEARCH_MATCH_INCLUDE_FILE)
#  define LZO_SEARCH_MATCH_INCLUDE_FILE		"lzo1b_sm.ch"
#endif

#include LZO_SEARCH_MATCH_INCLUDE_FILE


#if !defined(LZO_TEST_MATCH_INCLUDE_FILE)
#  define LZO_TEST_MATCH_INCLUDE_FILE		"lzo1b_tm.ch"
#endif

#include LZO_TEST_MATCH_INCLUDE_FILE



/***********************************************************************
// found a literal
************************************************************************/


	/* a literal */
literal:
#if (DD_BITS == 0)
		UPDATE_I(dict,0,dindex,ip,in);
#endif
		if (++ip >= ip_end)
			break;
#if (DD_BITS > 0)
		DVAL_NEXT(dv,ip);
#endif
		continue;



/***********************************************************************
// found a match
************************************************************************/

match:
#if (DD_BITS == 0)
		UPDATE_I(dict,0,dindex,ip,in);
#endif
		/* we have found a match of at least M2_MIN_LEN */


#if !defined(LZO_CODE_RUN_INCLUDE_FILE)
#  define LZO_CODE_RUN_INCLUDE_FILE		"lzo1b_cr.ch"
#endif

#include LZO_CODE_RUN_INCLUDE_FILE


		/* ii now points to the start of the current match */
		assert(ii == ip);


/***********************************************************************
// code the match
************************************************************************/

#if !defined(LZO_CODE_MATCH_INCLUDE_FILE)
#  define LZO_CODE_MATCH_INCLUDE_FILE	"lzo1b_cm.ch"
#endif

#include LZO_CODE_MATCH_INCLUDE_FILE


		/* ii now points to the start of the next literal run */
		assert(ii == ip);

	}


/***********************************************************************
// end of block
************************************************************************/

	assert(ip <= in_end);

#if defined(LZO_COLLECT_STATS)
	{
		lzo_uint i;
		const lzo_byte *p;

		for (i = 0; i < D_SIZE; i++)
		{
			p = dict[i];
			if (BOUNDS_CHECKING_OFF_IN_EXPR(p == NULL || p < in || p > in_end))
				lzo_stats->unused_dict_entries++;
		}
		lzo_stats->unused_dict_entries_percent =
			100.0 * lzo_stats->unused_dict_entries / D_SIZE;
	}
#endif


#if defined(LZO_RETURN_IF_NOT_COMPRESSIBLE)
	/* return if op == out to indicate that we
	 * couldn't compress and didn't copy anything.
	 */
	if (op == out)
	{
		*out_len = 0;
		return LZO_E_NOT_COMPRESSIBLE;
	}
#endif

	/* store the final literal run */
	if (in_end - ii > 0)
	{
		lzo_uint t = in_end - ii;
		op = STORE_RUN(op,ii,t);
	}

	*out_len = op - out;
	return LZO_E_OK;				/* compression went ok */
}


/*
vi:ts=4
*/
