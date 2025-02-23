/*
 * _Doc.c - Python module to do DOC e-text (de)compression
 * Code snagged from makedoc7.cpp by Harold Bamford, Rick Bram,
 *   and Pat Bierne
 * "Conversion" to C and pythonization by Rob Tillotson <rob@io.com>
 *
 * $Id: _Doc.c,v 1.1 1998/03/24 02:47:46 rob Exp $
 */

#include "stdio.h"
#include "stdlib.h"
#include "Python.h"

#define DISP_BITS 11
#define COUNT_BITS 3

#define BUFSIZE 6000
#define MAXTEXTSIZE 4096

/* The original code uses a C++ class to handle the compressed stream.
 */

typedef struct {
    unsigned char *buf;
    int   len;
    int   bSpace;
} Buffer;

unsigned char* memfind(unsigned char* t, int t_len, unsigned char* m, int m_len)
{
	int i;

	for (i = t_len - m_len + 1 ; i>0; i--, t++)
		if (t[0]==m[0] && memcmp(t,m,m_len)==0)
			return t;
	return 0;
}

unsigned int issue(Buffer *b, unsigned char src) {

    int iDest = b->len;
    unsigned char *dest = b->buf;
    
    if (b->bSpace) {
	if (src >= 0x40 && src <= 0x7f)
	    dest[iDest++] = src ^ 0x80;
	else {
	    dest[iDest++] = ' ';
	    if (src < 0x80 && (src == 0 || src > 8))
		dest[iDest++] = src;
	    else {
		dest[iDest++] = 1;
		dest[iDest++] = src;
	    }
	}
	b->bSpace = 0;
    }
    else {
	if (src == ' ')
	    b->bSpace = 1;
	else {
	    if (src < 0x80 && (src == 0 || src > 8))
		dest[iDest++] = src;
	    else {
		dest[iDest++] = 1;
		dest[iDest++] = src;
	    }
	}
    }
    b->len = iDest;
    return iDest;
}

unsigned int compress(Buffer *b) {
    int i, j;

    unsigned char *pBuffer;
    unsigned char *pHit;
    unsigned char *pPrevHit;
    unsigned char *pTestHead;
    unsigned char *pTestTail;
    unsigned char *pEnd;

    unsigned int dist, compound, k;
    
    pHit = pPrevHit = pTestHead = pBuffer = b->buf;
    pTestTail = pTestHead+1;
    pEnd = b->buf + b->len;

    b->buf = malloc(6000);
    b->len = 0;

    for (; pTestHead != pEnd; pTestTail++) {
	if (pTestHead - pPrevHit > ((1 << DISP_BITS)-1))
	    pPrevHit = pTestHead - ((1 << DISP_BITS)-1);
	pHit = memfind(pPrevHit, pTestTail - pPrevHit, pTestHead, pTestTail - pTestHead);
	if (pHit == 0)
	    fprintf(stderr, "!!bug!!");
	if (pHit == 0
	    || pHit == pTestHead
	    || pTestTail-pTestHead > (1 << COUNT_BITS)+2
	    || pTestTail == pEnd) {
	    if (pTestTail-pTestHead < 4) {
		issue(b, pTestHead[0]);
		pTestHead++;
	    } else {
		if (b->bSpace) {
		    b->buf[b->len++] = ' ';
		    b->bSpace = 0;
		}
		dist = pTestHead - pPrevHit;
		compound = (dist << COUNT_BITS) + pTestTail-pTestHead-4;
		b->buf[b->len++] = 0x80 + (compound >> 8);
		b->buf[b->len++] = compound & 0xff;
		pTestHead = pTestTail - 1;
	    }
	    pPrevHit = pBuffer;
	} else {
	    pPrevHit = pHit;
	}
	if (pTestTail == pEnd) pTestTail--;
    }

    if (b->bSpace) b->buf[b->len++] = ' ';

    for (i=k=0; i < b->len; i++, k++) {
	b->buf[k] = b->buf[i];
	if (b->buf[k] >= 0x80 && b->buf[k] < 0xc0)
	    b->buf[++k] = b->buf[++i];
	else if (b->buf[k] == 1) {
	    b->buf[k+1] = b->buf[i+1];
	    while (i + 2 < b->len && b->buf[i+2] == 1 && b->buf[k] < 8) {
		b->buf[k]++;
		b->buf[k+b->buf[k]] = b->buf[i+3];
		i += 2;
	    }
	    k += b->buf[k];
	    i++;
	}
    }

    free(pBuffer);
    b->len = k;

    return k;
}


unsigned int uncompress(Buffer *b) {
    unsigned char *pOut;
    unsigned char *in_buf;
    unsigned char *out_buf;
    int i, j, m, n;
    unsigned int c;
    
    pOut = malloc(6000);
    in_buf = b->buf;
    out_buf = pOut;

    for (j=i=0; j < b->len; ) {
	c = in_buf[j++];

	if (c > 0 && c < 9)
	    while (c--) out_buf[i++] = in_buf[j++];
	else if (c < 0x80)
	    out_buf[i++] = c;
	else if (c >= 0xc0) {
	    out_buf[i++] = ' ';
	    out_buf[i++] = c ^ 0x80;
	} else {
	    c <<= 8;
	    c += in_buf[j++];
	    m = (c & 0x3fff) >> COUNT_BITS;
	    n = c & ((1 << COUNT_BITS)-1);
	    n += 3;
	    while (n--) {
		out_buf[i] = out_buf[i-m];
		i++;
	    }
	}
    }

    free(b->buf);
    b->buf = pOut;
    b->len = i;

    return i;
}


static PyObject *compress_str(PyObject *self, PyObject *args) {
    char *str;
    int len;
    Buffer b;
    PyObject *o;
    
    if (!PyArg_ParseTuple(args, "s#", &str, &len)) return NULL;

    if (len > MAXTEXTSIZE || len < 1) {
	PyErr_SetString(PyExc_ValueError, "string must be 1 to 4096 bytes long");
	return NULL;
    }

    b.buf = malloc(BUFSIZE);
    memcpy(b.buf, str, len);
    b.len = len;
    b.bSpace = 0;
    
    compress(&b);

    o = Py_BuildValue("s#", b.buf, b.len);
    free(b.buf);

    return o;
}

static PyObject *uncompress_str(PyObject *self, PyObject *args) {
    char *str;
    int len;
    Buffer b;
    PyObject *o;
    
    if (!PyArg_ParseTuple(args, "s#", &str, &len)) return NULL;

    if (len > MAXTEXTSIZE || len < 1) {
	PyErr_SetString(PyExc_ValueError, "string must be 1 to 4096 bytes long");
	return NULL;
    }

    b.buf = malloc(BUFSIZE);
    memcpy(b.buf, str, len);
    b.len = len;
    b.bSpace = 0;
    
    uncompress(&b);

    o = Py_BuildValue("s#", b.buf, b.len);
    free(b.buf);

    return o;
}


static PyMethodDef _DocMethods[] = {
    { "compress", compress_str, 1 },
    { "uncompress", uncompress_str, 1 },
    { NULL, NULL }
};

void init_Doc() {
    (void) Py_InitModule("_Doc", _DocMethods);
}

