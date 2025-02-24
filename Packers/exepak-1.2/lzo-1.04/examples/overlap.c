/* overlap.c -- example program: overlapping (de)compression

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


/*************************************************************************
// This program shows how to do overlapping compression and
// in-place decompression.
//
// Please study LZO.FAQ and simple.c first.
**************************************************************************/

#include <lzo1x.h>
#include "lutil.h"


/* Overhead (in bytes) for the in-place decompression buffer.
 * Most files need only 16 !
 * (try `overlap -16 file' or even `overlap -8 file')
 *
 * Worst case (for files that are compressible by only a few bytes)
 * is 'in_len / 64 + 16 + 3'. See step 5a) below.
 *
 * For overlapping compression '0xbfff + in_len / 64 + 16 + 3' bytes
 * will be needed. See step 4a) below.
 */

static lzo_uint opt_overhead = 0;	/* assume worst case */


#if 0 && defined(__GNUC__) && defined(__i386__)
/* used for test purposes only */
LZO_EXTERN_CDECL(int)
lzo1x_decompress_asm_fast ( const lzo_byte *src, lzo_uint  src_len,
                                  lzo_byte *dst, lzo_uint *dst_len,
                                  lzo_voidp wrkmem /* NOT USED */ )
                           __asm__("_lzo1x_decompress_asm_fast");
#endif


/*************************************************************************
//
**************************************************************************/

static const char *progname = NULL;

static unsigned long total_files = 0;
static unsigned long total_in = 0;


/* some systems have a xmalloc in their C library... */
#undef xmalloc
#define xmalloc		my_xmalloc

static lzo_byte *xmalloc(lzo_uint len)
{
	lzo_byte *p;

	p = lzo_malloc(len > 0 ? len : 1);
	if (p == NULL)
	{
		printf("%s: out of memory\n", progname);
		exit(1);
	}
	return p;
}


/*************************************************************************
//
**************************************************************************/

int do_file ( const char *in_name )
{
	int r;
	FILE *f = NULL;
	long l;

	lzo_byte *wrkmem = NULL;

	lzo_byte *in = NULL;
	lzo_uint in_len;				/* uncompressed length */

	lzo_byte *out = NULL;
	lzo_uint out_len;				/* compressed length */

	lzo_byte *overlap = NULL;
	lzo_uint overhead;
	lzo_uint offset;

	lzo_uint new_len = 0;

/*
 * Step 1: open the input file
 */
 	f = fopen(in_name,"rb");
	if (f == NULL)
	{
		printf("%s: %s: cannot open file\n", progname, in_name);
		goto next_file;
	}
	fseek(f,0,SEEK_END);
	l = ftell(f);
	fseek(f,0,SEEK_SET);
	if (l <= 0)
	{
		printf("%s: %s: empty file -- skipping\n", progname, in_name);
		goto next_file;
	}
	in_len = (lzo_uint) l;

/*
 * Step 2: allocate compression buffers and read the file
 */
	in = xmalloc(in_len);
	out = xmalloc(in_len + in_len / 64 + 16 + 3);
	wrkmem = xmalloc(LZO1X_1_MEM_COMPRESS);
	in_len = lzo_fread(f,in,in_len);
	fclose(f); f = NULL;
	printf("%s: %s: read %ld bytes\n", progname, in_name, (long) in_len);

	total_files++;
	total_in += in_len;

/*
 * Step 3: compress from `in' to `out' with LZO1X-1
 */
	r = lzo1x_1_compress(in,in_len,out,&out_len,wrkmem);
	if (r != LZO_E_OK || out_len > in_len + in_len / 64 + 16 + 3)
	{
		/* this should NEVER happen */
		printf("internal error - compression failed: %d\n", r);
		exit(1);
	}
	printf("%-26s %8lu -> %8lu\n", "LZO1X-1:", (long) in_len, (long) out_len);


/***** Step 4: overlapping compression *****/

/*
 * Step 4a: allocate the `overlap' buffer for overlapping compression
 */
	overhead  = in_len > 0xbfff ? 0xbfff : in_len;
 	overhead += in_len / 64 + 16 + 3;
	overlap = xmalloc(in_len + overhead);

/*
 * Step 4b: prepare data in `overlap' buffer.
 *          copy uncompressed data at the top of the overlap buffer
 */
 	/*** offset = in_len + overhead - in_len; ***/
 	offset = overhead;
	lzo_memcpy(overlap + offset, in, in_len);

/*
 * Step 4c: do an in-place compression within the `overlap' buffer
 */
	r = lzo1x_1_compress(overlap+offset,in_len,overlap,&new_len,wrkmem);
	if (r != LZO_E_OK)
	{
		/* this should NEVER happen */
		printf("overlapping compression failed: %d\n", r);
		exit(1);
	}

/*
 * Step 4d: verify overlapping compression
 */
 	if (new_len != out_len || lzo_memcmp(out,overlap,out_len) != 0)
	{
		/* As compression is non-deterministic there can be a difference
		 * (but this usually happens very seldom). So we have to verify
		 * compression by doing a temporary decompression.
		 */
		lzo_uint l = in_len;
		lzo_byte *tmp = xmalloc(l);
		r = lzo1x_decompress_safe(overlap,new_len,tmp,&l,NULL);
 		if (r != LZO_E_OK || l != in_len || lzo_memcmp(in,tmp,l) != 0)
		{
			/* this should NEVER happen */
			printf("overlapping compression data error\n");
			exit(1);
		}
		lzo_free(tmp);
	}

	printf("overlapping compression:   %8lu -> %8lu    overhead: %7ld\n",
			(long) in_len, (long) new_len, (long) overhead);
	lzo_free(overlap); overlap = NULL;


/***** Step 5: overlapping decompression *****/

/*
 * Step 5a: allocate the `overlap' buffer for in-place decompression
 */
 	if (opt_overhead == 0 || out_len >= in_len)
		overhead = in_len / 64 + 16 + 3;
	else
 		overhead = opt_overhead;
	overlap = xmalloc(in_len + overhead);

/*
 * Step 5b: prepare data in `overlap' buffer.
 *          copy compressed data at the top of the overlap buffer
 */
 	offset = in_len + overhead - out_len;
	lzo_memcpy(overlap + offset, out, out_len);

/*
 * Step 5c: do an in-place decompression within the `overlap' buffer
 */
	new_len = in_len;
#if 1
	r = lzo1x_decompress(overlap+offset,out_len,overlap,&new_len,NULL);
#else
	r = lzo1x_decompress_asm_fast(overlap+offset,out_len,overlap,&new_len,NULL);
#endif
	if (r != LZO_E_OK)
	{
		/* this may happen if overhead is too small */
		printf("overlapping decompression failed: %d - increase `opt_overhead'\n", r);
		exit(1);
	}

/*
 * Step 5d: verify decompression
 */
 	if (new_len != in_len || lzo_memcmp(in,overlap,in_len) != 0)
	{
		/* this may happen if overhead is too small */
		printf("overlapping decompression data error - increase `opt_overhead'\n");
		exit(1);
	}
	printf("overlapping decompression: %8lu -> %8lu    overhead: %7ld\n",
			(long) out_len, (long) new_len, (long) overhead);
	lzo_free(overlap); overlap = NULL;


next_file:
	lzo_free(overlap);
	lzo_free(wrkmem);
	lzo_free(out);
	lzo_free(in);
	if (f) fclose(f);

	return 0;
}


/*************************************************************************
//
**************************************************************************/

int main(int argc, char *argv[])
{
	int r;
	int i = 1;

#if defined(__EMX__)
	_response(&argc,&argv);
	_wildcard(&argc,&argv);
#endif

	printf("\nLZO real-time data compression library (v%s, %s).\n",
	        LZO_VERSION_STRING, LZO_VERSION_DATE);
	printf("Copyright (C) 1996, 1997, 1998 Markus Franz Xaver Johannes Oberhumer\n\n");

	progname = argv[0];
	if (i < argc && argv[i][0] == '-')
		opt_overhead = atoi(&argv[i++][1]);
#if 1
	if (opt_overhead != 0 && opt_overhead < 8)
	{
		printf("%s: invalid overhead value %d\n", progname, opt_overhead);
		exit(1);
	}
#endif
	if (i >= argc)
	{
		printf("usage: %s [-overhead_in_bytes] file..\n", progname);
		exit(1);
	}

/*
 * Step 1: initialize the LZO library
 */
	if (lzo_init() != LZO_E_OK)
	{
		printf("lzo_init() failed !!!\n");
		exit(1);
	}

/*
 * Step 2: process files
 */
	for (r = 0; r == 0 && i < argc; i++)
		r = do_file(argv[i]);

	printf("\nDone. Successfully processed %lu bytes in %lu files.\n",
	        total_in, total_files);
	return r;
}

/*
vi:ts=4
*/

