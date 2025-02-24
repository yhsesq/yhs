/* lzo1b_9x.c -- implementation of the LZO1B-999 compression algorithm

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



#include <lzoconf.h>
#if !defined(LZO_999_UNSUPPORTED)

#include <stdio.h>
#include "config1b.h"

#if 0
#undef NDEBUG
#include <assert.h>
#endif


/***********************************************************************
//
************************************************************************/

#define N		   0xffffL			/* size of ring buffer */
#define THRESHOLD	    2			/* lower limit for match length */
#define F		     2048			/* upper limit for match length */


#define LZO1B
#define LZO_COMPRESS_T	lzo1b_999_t
#define lzo_swd_t		lzo1b_999_swd_t
#include "lzo_mchw.ch"



/***********************************************************************
//
************************************************************************/

static lzo_byte *
code_match ( LZO_COMPRESS_T *c, lzo_byte *op, lzo_uint m_len, lzo_uint m_off )
{
	if (m_len <= M2_MAX_LEN && m_off <= M2_MAX_OFFSET)
	{
		assert(m_len >= M2_MIN_LEN);
		assert(m_off >= M2_MIN_OFFSET);

		m_off -= M2_MIN_OFFSET;
		/* code match len + low offset bits */
		*op++ = LZO_BYTE(((m_len - (M2_MIN_LEN - 2)) << M2O_BITS) |
		                  (m_off & M2O_MASK));
		/* code high offset bits */
		*op++ = LZO_BYTE(m_off >> M2O_BITS);
		c->m2_m++;
	}
	else
	{
		assert(m_len >= M3_MIN_LEN);
		assert(m_off <= M3_MAX_OFFSET);

		m_off -= M3_MIN_OFFSET - M3_EOF_OFFSET;
		/* code match len */
		if (m_len <= M3_MAX_LEN)
			*op++ = LZO_BYTE(M3_MARKER | (m_len - (M3_MIN_LEN - 1)));
		else
		{
			assert(m_len >= M4_MIN_LEN);
			/* code M4 match len flag */
			*op++ = M4_MARKER;
			/* code match len */
			m_len -= M4_MIN_LEN - 1;
			while (m_len > 255)
			{
				m_len -= 255;
				*op++ = 0;
			}
			assert(m_len > 0);
			*op++ = LZO_BYTE(m_len);
		}
		/* code low offset bits */
		*op++ = LZO_BYTE(m_off & M3O_MASK);
		/* code high offset bits */
		*op++ = LZO_BYTE(m_off >> M3O_BITS);

		c->r1_m_len = 0;
		c->m3_m++;
	}
	return op;
}


/***********************************************************************
// this is a public function, but there is no prototype in a header file
************************************************************************/

LZO_EXTERN(int)
lzo1b_999_compress_callback ( const lzo_byte *in , lzo_uint  in_len,
                                    lzo_byte *out, lzo_uint *out_len,
                                    lzo_voidp wrkmem,
                                    lzo_progress_callback_t cb,
                                    lzo_uint max_chain );

LZO_PUBLIC(int)
lzo1b_999_compress_callback ( const lzo_byte *in , lzo_uint  in_len,
                                    lzo_byte *out, lzo_uint *out_len,
                                    lzo_voidp wrkmem,
                                    lzo_progress_callback_t cb,
                                    lzo_uint max_chain )
{
	lzo_byte *op;
	const lzo_byte *ii;
	lzo_uint lit;
	lzo_uint m_len, m_off;
	LZO_COMPRESS_T cc;
	LZO_COMPRESS_T * const c = &cc;
	lzo_swd_t * const swd = (lzo_swd_t *) wrkmem;
	int r;

#if defined(__LZO_QUERY_COMPRESS)
	if (__LZO_IS_COMPRESS_QUERY(in,in_len,out,out_len,wrkmem))
		return __LZO_QUERY_COMPRESS(in,in_len,out,out_len,wrkmem,1,lzo_sizeof(lzo_swd_t));
#endif

	/* sanity check */
	if (!lzo_assert(LZO1B_999_MEM_COMPRESS >= lzo_sizeof(lzo_swd_t)))
		return LZO_E_ERROR;

	c->init = 0;
	c->ip = c->in = in;
	c->in_end = in + in_len;
	c->cb = cb;
	c->r1_r = c->m3_r = c->m2_m = c->m3_m = 0;

	op = out;
	ii = c->ip;				/* point to start of literal run */
	lit = 0;
	c->r1_m_len = 0;

	r = init_match(c,swd,NULL,0,0);
	if (r != 0)
		return r;
	if (max_chain > 0)
		swd->max_chain = max_chain;

	r = find_match(c,swd,0,0);
	if (r != 0)
		return r;
	while (c->look > 0)
	{
		int lazy_match_min_gain = -1;
		lzo_uint ahead = 0;

		m_len = c->m_len;
		m_off = c->m_off;

#if 0
		printf("%5ld: %5d len:%3d off:%5d\n", (c->ip-c->look)-in, c->look,
				m_len, m_off);
#endif

		assert(c->ip - c->look >= in);
		if (lit == 0)
			ii = c->ip - c->look;
		assert(ii + lit == c->ip - c->look);
		assert(swd->b_char == *(c->ip - c->look));

		if ((m_len < M2_MIN_LEN) ||
		    (m_len < M3_MIN_LEN && m_off > M2_MAX_OFFSET))
		{
			m_len = 0;
		}
		else
		{
			assert(c->ip - c->look - m_off >= in);
			assert(c->ip - c->look - m_off + m_len < c->ip);
			assert(lzo_memcmp(c->ip - c->look, c->ip - c->look - m_off,
			                  m_len) == 0);

			if (lit > 0)
			{
				/* we have a current literal run: do not try a lazy match,
				   if the literal could be coded into a r1 match */
				if (lit == 1 && c->r1_m_len == M2_MIN_LEN)
					lazy_match_min_gain = -1;
				else
					lazy_match_min_gain = 1;

#if (M2_MIN_LEN == 2)
				if (m_len == 2)
				{
					/* don't code a match of len 2 if we have to
					   code a literal run. Code a literal instead. */
					m_len = 0;
				}
#endif
#if (M2_MIN_LEN == M3_MIN_LEN)
				if (m_len == M2_MIN_LEN && m_off > M2_MAX_OFFSET)
				{
					/* don't code a M3 match of len 3 if we have to
					   code a literal run. Code a literal instead. */
					m_len = 0;
				}
#endif
			}
			else
			{
				/* no current literal run: only try a lazy match,
				   if the literal could be coded into a r1 match */
				if (c->r1_m_len == M2_MIN_LEN)
					lazy_match_min_gain = 0;
				else
					lazy_match_min_gain = -1;
			}
		}


		/* try a lazy match */
		if (m_len == 0)
			lazy_match_min_gain = -1;
		if (lazy_match_min_gain >= 0 && c->look > m_len)
		{
			assert(m_len > 0);

			r = find_match(c,swd,1,0);
			assert(r == 0);
			assert(c->look > 0);

			if (m_len <= M2_MAX_LEN && m_off <= M2_MAX_OFFSET &&
			    c->m_off > M2_MAX_OFFSET)
				lazy_match_min_gain += 1;

			if (c->m_len >= m_len + lazy_match_min_gain)
			{
				c->lazy++;
#if !defined(NDEBUG)
				m_len = c->m_len;
				m_off = c->m_off;
				assert(lzo_memcmp(c->ip - c->look, c->ip - c->look - m_off,
			                      m_len) == 0);
#endif
				lit++;
				assert(ii + lit == c->ip - c->look);
				continue;
			}
			else
			{
				ahead = 1;
				assert(ii + lit + 1 == c->ip - c->look);
			}
			assert(m_len > 0);
		}
		assert(ii + lit + ahead == c->ip - c->look);


		if (m_len == 0)
		{
			/* a literal */
			lit++;
			r = find_match(c,swd,1,0);
			assert(r == 0);
		}
		else
		{
			/* 1 - store run */
			if (lit > 0)
			{
				/* code current literal run */
				if (lit == 1 && c->r1_m_len == M2_MIN_LEN)
				{
					/* Code a context sensitive R1 match. */
					assert((op[-2] >> M2O_BITS) == (M2_MARKER >> M2O_BITS));
					op[-2] &= M2O_MASK;
					assert((op[-2] >> M2O_BITS) == 0);
					/* copy 1 literal */
					*op++ = *ii++;
					assert(ii + ahead == c->ip - c->look);
					c->r1_r++;
				}
				else
				{
					op = STORE_RUN(op,ii,lit);
				}
				if (lit < R0FAST)
					c->r1_m_len = m_len;
				else
					c->r1_m_len = 0;
				lit = 0;
			}
			else
				c->r1_m_len = 0;

			/* 2 - code match */
			op = code_match(c,op,m_len,m_off);
			r = find_match(c,swd,m_len,1+ahead);
			assert(r == 0);
		}

		c->codesize = op - out;
	}


	/* store final run */
	if (lit > 0)
		op = STORE_RUN(op,ii,lit);

#if defined(LZO_EOF_CODE)
	*op++ = M3_MARKER | 1;
	*op++ = 0;
	*op++ = 0;
#endif

	c->codesize = op - out;
	assert(c->textsize == in_len);

	*out_len = op - out;

	if (c->cb)
		(*c->cb)(c->textsize,c->codesize);

#if 0
	printf("%ld %ld -> %ld: %ld %ld %ld %ld %ld\n",
		(long) c->textsize, (long)in_len, (long) c->codesize,
		c->r1_r, c->m3_r, c->m2_m, c->m3_m, c->lazy);
#endif
	return LZO_E_OK;
}



/***********************************************************************
//
************************************************************************/

LZO_PUBLIC(int)
lzo1b_999_compress  ( const lzo_byte *in , lzo_uint  in_len,
                            lzo_byte *out, lzo_uint *out_len,
                            lzo_voidp wrkmem )
{
	return lzo1b_999_compress_callback(in,in_len,out,out_len,wrkmem,
									   (lzo_progress_callback_t) 0, 0);
}


#endif /* !defined(LZO_999_UNSUPPORTED) */

/*
vi:ts=4
*/

