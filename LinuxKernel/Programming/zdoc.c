/*
 * ZDOC [ Ver.5.1.0b ]
 *
 * This program is distributed under the terms of the GPL v2.0 or later
 * Download the GNU Public License (GPL) from www.gnu.org
 *      
 *  Support the Free Software Foundation.
 *
 * (C) 1998,1999,2000 by Zurk Technology Inc.
 * zurk@geocities.com
 * http://zurk.netpedia.net/zdoc.html
 * June/July 1998
 * Patched: Dec 1998 (Field Handler Patch)
 * Modifications by :  
 * 1999/05-09 mizotec<HGH01156@nifty.ne.jp>
 * http://member.nifty.ne.jp/mizotec/
 * Merged into ZDOC 2/2000.
 */

#pragma pack(2)
#include <Common.h>
#include <System/SysAll.h>
#include <UI/UIAll.h>
#include <SerialMgr.h>

#include "zdoc.h"
#define ZdocAppID 'ZURK'   
#define DocTypeID	'TEXt'
#define DISP_BITS 11
#define COUNT_BITS 3
#define BUFSIZE 6000
#define MAXTEXTSIZE 4096
#define MAXDOC 20

#define KEYLEN 8
#define UPDATE_GOPOS 10
#define UPDATE_DB 20
#define UPDATE_NAME 21
#define INSPTOFF 1000

static int StartApplication(void);

static void EventLoop(void);
static void StopApplication(void);
static Boolean hello(EventPtr event);
static Boolean hellonew(EventPtr event);
static void SaveField(void);
static Err DisplayRecord(UInt index);


typedef struct
{
	int     version;			// 2	1:plain, 2:compressed, 3:ext
 int     reserved1;    // 2 - i preferred crap since its garbage but i wont argue.
	long    doc_size;			// 4   in bytes, when uncompressed
	int     count;				// 2   numRecords-1
 int     rec_size; // 2 usually MAXTEXTSIZE 
 long reserved2;   // 4 more crap.
} DOCInfoType;            //         16 bytes

typedef struct
{	/* Display Doc Record  outside */
	FieldPtr field;
	ULong	len;	/* original */
	UInt	index;	/* rec no */
} DOCFieldType;


/* read  db->raw, raw->field | raw->cook->field */
/* write fild->raw, raw->db | raw->cook->db */
// hmm..ok..i can live with the cook stuff. 
typedef struct
{	/* DOC Record in inside */
	Char	raw[MAXTEXTSIZE+1];	/* Raw Text, compress */
	Char	cook[BUFSIZE];	/* compress/uncompress Text */
	ULong	rawLen;	/* rawLen */
	ULong	len;	/* cooked len */
	ULong	orgLen;	/* original len, uncompress */
	CharPtr	text;	/* uncompress/compress text ptr */
	UInt	index;	/* read rec no */
} DOCTxtType;

static DOCInfoType DOC_Values =  { 0,0,0,0,0,0 } ;
static DOCTxtType  DOC_Text;
static DOCFieldType	DOC_Field;

/* doc list variable */
static	UInt	docNum;	/* number of DOC */
static	ListPtr list;
static	LocalID store[MAXDOC];
static	CharPtr array[MAXDOC+1];
static	char    name[MAXDOC+1][dmDBNameLength+2]; /* dmDBNameLength+1 pack */
static	char    newName[dmDBNameLength+2]; /* dmDBNameLength+1 */
//	char    nam[21];  /* fieldID_new MAXCHARS +1 */

static	int     key[KEYLEN+1] = {0,0,0,0,0,0,0,0,0};

static	FormPtr   g_form;


/*	DmOpenRef RAM; */
/*	LocalID   RAMID; */
/*	VoidPtr  romx;	*/
/*	VoidHand rom;	*/

static	LocalID	g_dbID;
static	DmOpenRef g_dbR;	/* DmOpenRef to open database,0 no open */
/*	unsigned char *ram;	*/
static	char	newRecord[]="newRecord:";

static	int	go_page, go_pos;
static	char	findStr[32+1]="";
static	char    dspStr[40+1];	/* Work for Display String */
static	int	fs_scrollH=1;
static	int	fs_scrollS=10;
static	int	fs_scrollF=0;
static	Boolean	fs_save=true;
static	FontID fs_font = stdFont;
static Word fs_listxtem = 0;

typedef struct {
	unsigned char *buf;
	int   len;
	int   bSpace;
} Buffer;




DWord  PilotMain (Word cmd, Ptr cmdPBP, Word launchFlags)
{
	int error;


	if ( cmd == sysAppLaunchCmdNormalLaunch ){

		error = StartApplication();  // Application start code
		if ( error ) return error;

		EventLoop();  // Event loop

		StopApplication (); // Application stop code
	}
	return 0;
}

#ifndef	DEBUG
#define debuginfo( x, y )	((void)0)
#else
static void debuginfo(CharPtr str, Long x)
{
	int i;
	UInt len;
static pos = 20;
	for (i=0;i<20;i++){dspStr[i]=' ';}
	StrIToA((CharPtr) &dspStr[0], x);
	for (i=0;i<20;i++){
		if (dspStr[i]=='\0') {dspStr[i]=' ';}
	}

	pos = pos + 10;
	if( pos > 130){
		pos = 20;
	}
	len = StrLen(str);
	WinDrawChars(str,len,20,pos);
	WinDrawChars((CharPtr) &dspStr[0],8,90, pos);

}
#endif

static void infoPos(void)
{
	static UInt	index;
	static Word	pos;

	UInt	len;
	if(DOC_Field.index == index && FldGetInsPtPosition(DOC_Field.field) == pos){
		return;
	}
	index = DOC_Field.index;
	pos = FldGetInsPtPosition(DOC_Field.field);
	MemSet(dspStr, sizeof(dspStr), ' ');
	StrIToA(dspStr , DOC_Field.index);
	len = StrLen(dspStr);
	dspStr[len] = ',';
	StrIToA(dspStr + len + 1 , pos);
	len = StrLen(dspStr);
	dspStr[len] = ' ';
	if(len<7){
		len = len + (7-len)*2;
		/* 4ng 3ng */
	}

//	len = StrLen(dspStr);
//	StrCopy(dspStr + len , "12");
	WinDrawChars(dspStr,len, pos_about+1,145);
}

#if 0
static void info(void)
{
	UInt	len;
	if (g_dbID != 0){
		StrCopy(dspStr, " Version:");
		switch(DOC_Values.version){
		case 1:
			StrCat(dspStr, "Uncompressed.");
			break;
		case 2:
			StrCat(dspStr, "Compressed.");
			break;
		case 3:
			StrCat(dspStr, "Encrypted.");
			break;
		default:
			StrCat(dspStr, "Unknown.");
		}
		len = StrLen(dspStr);
		WinDrawChars(dspStr,len, 40,40);

		StrCopy(dspStr, " Size:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Values.doc_size);
		len = StrLen(dspStr);
		WinDrawChars(dspStr,len, 40,50);

		StrCopy(dspStr, " Rec Num:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Values.count);
		len = StrLen(dspStr);
		WinDrawChars(dspStr,len, 40,60);

		StrCopy(dspStr, " current:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Field.index);
		len = StrLen(dspStr);
		dspStr[len] = ',';
		StrIToA(dspStr + len + 1 , FldGetInsPtPosition(DOC_Field.field));
		len = StrLen(dspStr);
		WinDrawChars(dspStr,len, 40,70);
		infoPos();
	}else{
		WinDrawChars((CharPtr) "No open",7,40,40);
		StrCopy(dspStr, " docNum:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , docNum);
		len = StrLen(dspStr);
		WinDrawChars(dspStr,len, 40,60);
	}
}
#endif

static FieldPtr SetFieldTextFromHandle(FieldPtr fldP, Handle txtH)
{
	Handle oldTxtH;
	//          FormPtr frm = FrmGetActiveForm();
	//          FieldPtr fldP;
	// NEW Field handler patch -- WARNING : Removes backup protection.
	// get the field and the field's current text handle.
	// fldP = FrmGetObjectPtr(frm, FrmGetObjectIndex(frm, fieldID));
	// ErrNonFatalDisplayIf(!fldP, "missing field");
	oldTxtH = FldGetTextHandle(fldP);
	// set the field's text to the new text.
	FldSetTextHandle(fldP, txtH);
	FldDrawField(fldP);
	// free the handle AFTER we call FldSetTextHandle().
	if (oldTxtH){MemHandleFree((VoidHand) oldTxtH);}
	return fldP;
}

static void ClearFieldText(FieldPtr fieldP)
{
	SetFieldTextFromHandle(fieldP,NULL);
}
static FieldPtr SetFieldTextFromStr(FieldPtr fieldP, CharPtr strP, ULong size)
{
	Handle txtH;
	VoidPtr  textPtr;
	size = size + 1;	/* add '\0' size */
	txtH= (Handle) MemHandleNew(size);
	if (!txtH) return NULL;
	textPtr = MemHandleLock((VoidHand) txtH);
	MemMove(textPtr,strP,size);
//	MemHandleUnlock((VoidHand) txtH);
	MemPtrUnlock(textPtr);
	ClearFieldText(fieldP);
	return SetFieldTextFromHandle(fieldP,txtH);
}

/* 99/05/05 delay is debug ? - yes. -Zurk-. */
static void delay(int b)
{
	int a;
	char dspStr[6];
	StrIToA((CharPtr) &dspStr[0],b);
	// <-----------------------------------------Delay Loop
	for (a=0;a<1000;a++){
		WinDrawChars((CharPtr) "-EHANDLR-",9,30,30);
		WinDrawChars((CharPtr) &dspStr[0],5,30,70);
	}
	// <-----------------------------------------End Delay Loop
}

/* read  db->raw, raw->field | raw->cook->field */
/*	Replace the given buffer with an uncompressed version of itself. */
static unsigned int uncompress(Buffer *b)
{	/* b->buf == DOC_Text.raw */
	unsigned char *in_buf;
	unsigned char *out_buf;
	int j=0, i=0;

	out_buf = DOC_Text.cook;
	in_buf = b->buf;

	for (i=j=0; i < b->len; ) {
		unsigned int c = in_buf[i++];

		if (c > 0 && c < 9){
			while (c--) {
				out_buf[j++] = in_buf[i++];
			}
		}else if (c < 0x80){
			out_buf[j++] = c; 
		}else if (c >= 0xc0) {
			out_buf[j++] = ' ';
			out_buf[j++] = c ^ 0x80; 
		} else {
			int di, n;
			c = (c << 8) + in_buf[ i++ ];
			di = (c & 0x3fff) >> COUNT_BITS;
			n = (c & ((1 << COUNT_BITS)-1)) + 3;
			while (n--) {
				out_buf[j] = out_buf[j-di];
				j++; 
			}
		}
	}


	if(j > MAXTEXTSIZE){j = MAXTEXTSIZE;}
	b->buf = out_buf;
	b->len = j;
	return j;
}

#if 1
#define scrambuf()	((void)0)
#else
/* The motive for this function is not clear. mizotec
It scrambles the text when writing a new record in encrypted mode. -Zurk- */
static void scrambuf()
{
	int i;
	int x;
	int a=0;
	int b=0;
	for (i=0;i < MAXTEXTSIZE;i++){
		a=(int) TimGetTicks();
		for (x=0;x<5;x++){}
		b=(int) TimGetTicks();
		x=(int) b*a;
		while (x>256){x=x-256;}
		DOC_Text.raw[i]=x;
	}
}
#endif

static unsigned char* memfind(unsigned char* t, int t_len, unsigned char* m, int m_len)
{
	int i;

	for (i = t_len - m_len + 1 ; i>0; i--, t++)
	if (t[0]==m[0] && MemCmp(t,m,m_len)==0) return t;
	return 0;
}

/* read  db->raw, raw->field | raw->cook->field */
static void issue(Buffer *b, unsigned char src)
{	/* b->buf == DOC_Text.cook. */
	int iDest = b->len;
	unsigned char *dest = b->buf;

	if (b->bSpace) {
		b->bSpace = 0;
		if (src >= 0x40 && src <= 0x7f){
			dest[iDest++] = src ^ 0x80;
			b->len = iDest;
			return;
		}
		dest[iDest++] = ' ';
	}else {
		if (src == ' '){
			b->bSpace = 1;
			b->len = iDest;
			return;
		}
	}
	if((src >=1 && src<=8 )|| src>=0x80){
		dest[iDest++] = 1;
	}
	dest[iDest++] = src;
	b->len = iDest;
	return;
}

/* write fild->raw, raw->db | raw->cook->db */
/*	Replace the given buffer with a compressed version of itself. */
static unsigned int compress(Buffer *b)
{	/* b->buf == DOC_Text.raw. */
	int i;

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

	b->buf = DOC_Text.cook;
	b->len = 0;

	while ( pTestHead != pEnd ) {
		if (pTestHead - pPrevHit > ((1 << DISP_BITS)-1))
		pPrevHit = pTestHead - ((1 << DISP_BITS)-1);

		pHit = memfind(pPrevHit, pTestTail - pPrevHit, pTestHead, pTestTail - pTestHead);
		if (pHit == 0) delay(0);
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
		if (pTestTail != pEnd) pTestTail++;
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

	b->len = k;
	return k;
}

static void doc_uncompress(Buffer *b)
{
	unsigned char *buf;
	if(DOC_Values.version != 2){
		MemMove(DOC_Text.cook, b->buf, b->len);
		b->buf = DOC_Text.cook;
		buf = b->buf;
	}
	if(DOC_Values.version == 2){
		b->bSpace = 0;
		uncompress(b);
//		b->buf = DOC_Text.cook;
		buf = b->buf;
	}

	if (DOC_Values.version == 3){
		int a;

		key[KEYLEN]=0;
		for (a=0;a<b->len;a++){
			buf[a]=(256-buf[a])-key[key[KEYLEN]];
			key[KEYLEN]++;
			if(key[KEYLEN]>=KEYLEN){ key[KEYLEN]=0; }
		}
	}
	/* if(len > MAXTEXTSIZE){len = MAXTEXTSIZE;} */
	*(b->buf + b->len) = '\0';
}

static void DrawMark(int s){
	if(s == 0){
		WinDrawChars((CharPtr) "\x9d",1,150,0);   /* Normal */
	}else{
		WinDrawChars((CharPtr) "§",1,150,0);   /* Wait */
	}
}

/* read  db->raw, raw->field | raw->cook->field */
/* load recode No.index */
/*   out: DOC_Text.len DOC_Text.text */
static void loadrec(UInt index)
{

	VoidPtr   recPtr=NULL;
	VoidHand  recHand=NULL;

	Buffer b;
	int len=MAXTEXTSIZE;

	if(DOC_Values.count < index ){
		DOC_Text.len = 0;
	}
	if(DOC_Text.index == index){
		return;
	}
//	WinDrawChars((CharPtr) "§",1,150,0);
	DOC_Text.index = index;
	recHand=DmQueryRecord(g_dbR, index);
	recPtr=MemHandleLock(recHand);
	len=(int) MemPtrSize(recPtr);
	if(len > MAXTEXTSIZE){len = MAXTEXTSIZE;}
	scrambuf();
//	MemMove(DOC_Text.raw, recPtr, len);
	DOC_Text.rawLen = len;
//	MemHandleUnlock(recHand);
//	b.buf = DOC_Text.raw;
	b.buf = recPtr;
	b.len = len;

	doc_uncompress(&b);
//	MemHandleUnlock(recHand);
	MemPtrUnlock(recPtr);
	DmReleaseRecord(g_dbR,DOC_Text.index,(Boolean) false);
	len = b.len;

	if (b.len > DOC_Values.doc_size) { delay(101); }
//		if (len > DOC_Values.rec_size) { delay(102); }
	if (b.len < 2) { delay(103); }
	// MemHandleFree(recHand);
	// MemPtrFree(recPtr);
	DOC_Text.orgLen = len;
	DOC_Text.len = len;
	DOC_Text.text = b.buf;
//	WinDrawChars((CharPtr) "ù",1,150,0);
}

static void WriteValues(DmOpenRef dbRef, Boolean new)
{
	VoidHand  recHand=NULL;
	VoidPtr   recPtr=NULL;
	if(new){
		UInt	at = 0;
		recHand=(VoidHand) DmNewRecord(dbRef,&at,sizeof DOC_Values);
	}else{
		recHand = DmGetRecord(dbRef, 0);
	}
	ErrFatalDisplayIf(recHand == 0, "Cannot get record handle.");
	recPtr = MemHandleLock(recHand);
	ErrFatalDisplayIf(recPtr == 0, "Cannot lock record handle.");
	DmSet(recPtr, 0, sizeof DOC_Values, 0);
	DmWrite(recPtr,0,&DOC_Values, sizeof DOC_Values);
//	MemHandleUnlock(recHand);
	MemPtrUnlock(recPtr);
	DmReleaseRecord(dbRef, 0, true);
}

/* write fild->raw, raw->db | raw->cook->db */
/* prepare raw for saverec */
/*  DOC_Text. raw, rawLen, orgLen */
static void saverec(DmOpenRef dbRef, Boolean new, UInt index)
{
	VoidHand  recHand=NULL;
	VoidPtr   recPtr=NULL;
	long oldSize;
	int oldCount;
	Buffer c;
	oldSize = DOC_Values.doc_size;
	oldCount = DOC_Values.count;

	c.buf = DOC_Text.raw;
	c.len = DOC_Text.rawLen;
	if(DOC_Values.version == 2){
		c.bSpace = 0;
		compress(&c);
		*(c.buf + c.len)='\0';
	}
	if (DOC_Values.version == 3){
		int	a;
		key[KEYLEN]=0;
		for (a=0;a<c.len;a++){
			DOC_Text.raw[a]=256-(DOC_Text.raw[a]+key[key[KEYLEN]]);
			key[KEYLEN]++;
			if (key[KEYLEN]>=KEYLEN){key[KEYLEN]=0;}
		}
	}
	if(!new){
		DmRemoveRecord(dbRef, index);
		DOC_Values.count--;
		DOC_Values.doc_size = DOC_Values.doc_size - DOC_Text.orgLen;
		DOC_Text.orgLen = DOC_Text.rawLen;
	}
	recHand=DmNewRecord(dbRef, &index, c.len);
	recPtr=MemHandleLock(recHand);
	DmWrite(recPtr,0,c.buf, c.len);
//	MemHandleUnlock(recHand);
	MemPtrUnlock(recPtr);
	DmReleaseRecord(dbRef, index, true);
	DOC_Values.count++;
	DOC_Values.doc_size = DOC_Values.doc_size + DOC_Text.rawLen;
	if( DOC_Values.doc_size != oldSize || DOC_Values.count != oldCount ){
		WriteValues(dbRef, false);
	}
	DOC_Text.index = 0;
}

static int StartApplication(void)
{
	FrmGotoForm(formID_zdoc);
	return 0;
}

static void CloseDoc(void)
{
	if (g_dbID != 0){
	    if((fs_save != false) &&( FldDirty(DOC_Field.field) != false) ){
			SaveField();
	    }
		fs_listxtem= LstGetSelection(list);

		DmCloseDatabase(g_dbR);
		DOC_Text.index = 0;
		DOC_Field.index = 0;
		DOC_Values.count=0;
		g_dbR = 0;
		g_dbID = 0;
		  
	}
    ClearFieldText(DOC_Field.field);
}


static void StopApplication(void)
{
	if (g_dbID != 0){
		CloseDoc();
	}
	//        FldFreeMemory(DOC_Field.field);
	FldEraseField(DOC_Field.field);
	FrmCloseAllForms();
}

static CharPtr StriStr(CharPtr s, CharPtr t)
{
	CharPtr  t1;
	t1 = t;
	while(*s != '\0'){
		CharPtr s2;
		s2 = s;
		t1 = t;
		while(*t1 != '\0'){
			Char c1, c2;
			c1 = *t1;
			c2 = *s2;
			if(c1 != c2){
				if(c1 > c2){
					Char c3;
					c3 = c2;
					c2 = c1;
					c1 = c3;
				}
				if(c1 < 'A' || c1 >'Z'){
					break;
				}
				c1 = c1 + 32;
				if(c1 != c2){
					break;
				}
			}
			s2++;
			t1++;
		}
		if(*t1 == '\0'){
			return s;
		}
		s++;
	}

	return NULL;
}


char findwork[sizeof(findStr)*2];
/* Find string/rec Set page, pos */
static Boolean	FindPos(void)
{
	VoidHand textHand;
	VoidPtr  textPtr;
	CharPtr p; int page;
	int len1;
	int len2;
	int	find_len;
	int	pos;

	p = NULL;
	find_len = StrLen(findStr);
//	find_len = 0;
	if(find_len == 0){
		return false;
	}
//	WinDrawChars((CharPtr) "§",1,150,0);
	DrawMark(1);	   

	len1 = FldGetTextLength(DOC_Field.field);
	pos = FldGetInsPtPosition(DOC_Field.field);
	page = DOC_Field.index;
	if( len1 > pos){
		textHand=(VoidHand) FldGetTextHandle(DOC_Field.field);
		textPtr=MemHandleLock(textHand);
		len2 = len1 - pos;
		MemMove(DOC_Text.cook+pos, textPtr+pos, len2);
//		MemHandleUnlock(textHand);
		MemPtrUnlock(textPtr);
		DOC_Text.len = len1;
		DOC_Text.text = DOC_Text.cook;
		DOC_Text.cook[len1] = '\0';
		p = StriStr(DOC_Text.cook+pos+1, findStr);
		if( p!= NULL){
			go_page = page;
			go_pos = p - DOC_Text.text;
			DrawMark(0);
			return true;
		}
		if(len2 > find_len){
			len2 = find_len - 1;
			pos = len1 - len2;
		}else{
			pos++;
			len2--;
		}
	}else{
		len2 = 0;
	}


	page++;
	while(page<=DOC_Values.count){
//		debuginfo("find len2:", len2);
		MemMove(findwork, DOC_Text.text+pos, len2);
		loadrec(page);
#if 1
		MemMove(findwork+len2, DOC_Text.text, find_len - 1);
		findwork[len2+find_len-1] = '\0';
		p = StriStr(findwork, findStr);
//		debuginfo("Frm", page);
//		debuginfo("FrmGetObjectPtr(findfield)", 3);
#endif
		if(p != NULL){
			go_page = page - 1;
			go_pos = pos + (p - findwork);
			DrawMark(0);
			return true;
		}
		p = StriStr(DOC_Text.text, findStr);
		if( p != NULL){
			go_page = page;
			go_pos = p - DOC_Text.text;
			DrawMark(0);
			return true;
		}
		len1 = DOC_Text.len;
		len2 = len1;
		if(len2 > find_len){
			len2 = find_len - 1;
		}else{
		}
		pos = len1 - len2;
		page++;
	}
	DrawMark(0);
	return false;
}

static VoidPtr GetObjectPtr(Word objID)
{
	return  FrmGetObjectPtr(g_form, FrmGetObjectIndex(g_form, objID));
}

static Boolean EditMenuEvent(EventPtr event)
{
	void * control;
	Word objIndex;
	FormObjectKind kind;

	if(menuitemID_undo > event->data.menu.itemID 
	|| event->data.menu.itemID > menuitemID_select){
		return false;
	}

	objIndex = FrmGetFocus(g_form);
	if(objIndex == noFocus){
		return true;
	}
	control = FrmGetObjectPtr(g_form, objIndex);
	kind = FrmGetObjectType(g_form, objIndex);
	if(frmFieldObj == kind || frmTableObj == kind){
		switch (event->data.menu.itemID){
		case menuitemID_undo:
			FldUndo(control);
			break;
		case menuitemID_cut:
			FldCut(control);
			break;
		case menuitemID_copy:
			FldCopy(control);
			break;
		case menuitemID_paste:
			FldPaste(control);
			break;
  case menuitemID_rx:
 //  FldRx(control);
			break;
  case menuitemID_tx:
//   FldTx(control);
			break;
		case menuitemID_select:
			FldSetSelection(control, 0, FldGetTextLength(control));
			break;
		}
	}
	return true;
}


/* Find Form */
static Boolean FindFormHandleEvent(EventPtr event)
{
	Boolean		handled = false;

	FieldPtr findfield;

	g_form = FrmGetActiveForm();
	findfield=GetObjectPtr(fieldID_find);

	switch(event->eType){
	case frmOpenEvent:
		FrmDrawForm(g_form);
		SetFieldTextFromStr(findfield, findStr, sizeof(findStr));
		handled = true;
		break;
	case penDownEvent:
		if(event->screenY<0
		|| event->screenY>50){
			FrmReturnToForm(formID_zdoc);
			handled = true;
		}
		break;
	case ctlSelectEvent:
		if(event->data.ctlSelect.controlID == buttonID_cancel){
//			FrmGotoForm(formID_zdoc);
			FrmReturnToForm(formID_zdoc);
			handled = true;
		}else if(event->data.ctlSelect.controlID == buttonID_find){
			int len;

			debuginfo("FrmGetObjectPtr(findfield)", findfield);
			len = FldGetTextLength(findfield);
			if (len > 0 ){
				ListPtr list;
				Word	listxtem;
				Boolean	hit;
				list=FrmGetObjectPtr(g_form,FrmGetObjectIndex(g_form,listID_find));
				listxtem = LstGetSelection(list);
				debuginfo("listxtem", listxtem);
				if(listxtem == 0){
					if(len > sizeof(findStr) + 1){
						len = sizeof(findStr) - 1;
					}
					StrNCopy(findStr, FldGetTextPtr(findfield), len);
					findStr[len] = '\0';
					hit = FindPos();
				}else{
					CharPtr p;
					StrCopy(dspStr, FldGetTextPtr(findfield));
					p = StrChr(dspStr, ',');
					if( p == NULL){
						go_pos = 0;
					}else{
						*p = '\0';
						go_pos = StrAToI(p+1);
					}
					go_page = StrAToI(dspStr);
					hit = true;
				}
				if(hit != false){
						/* frmUpdateEvent */
					FrmUpdateForm(formID_zdoc, UPDATE_GOPOS);
				}
			}
			FrmReturnToForm(formID_zdoc);
			handled = true;
		}
		break;
	case menuEvent:
		handled = EditMenuEvent(event);
		break;
	}
	return handled;
}

static short GetValue(Word objID)
{
	return CtlGetValue(GetObjectPtr(objID));
}
static void SetValue(Word objID, short value)
{
	CtlSetValue(GetObjectPtr(objID), value);
}

/* Info Form */
static Boolean InfoFormHandleEvent(EventPtr event)
{
	Boolean		handled = false;

	FieldPtr field;
	ControlPtr control;
   	UInt	len;

	UInt cardNo;
	LocalID dbID;

	UInt attributes;
   	ULong numRecords;
   	ULong totalBytes;
   	ULong dataBytes;

	g_form = FrmGetActiveForm();
	field=GetObjectPtr(fieldID_Info1);

	switch(event->eType){
	case frmOpenEvent:
		FrmDrawForm(g_form);

		StrIToA(dspStr , fs_scrollH);
		len = StrLen(dspStr);
		field=GetObjectPtr(fieldID_scrollH);
		SetFieldTextFromStr(field, dspStr, len);

	   	StrIToA(dspStr , fs_scrollS);
		len = StrLen(dspStr);
		field=GetObjectPtr(fieldID_scrollS);
		SetFieldTextFromStr(field, dspStr, len);

	   	StrIToA(dspStr , fs_scrollF);
		len = StrLen(dspStr);
		field=GetObjectPtr(fieldID_scrollF);
		SetFieldTextFromStr(field, dspStr, len);

	   	StrIToA(dspStr , fs_font);
		len = StrLen(dspStr);
		field=GetObjectPtr(fieldID_font);
		SetFieldTextFromStr(field, dspStr, len);

     if(fs_save != false){
		   SetValue(checkID_save, true);
	       	}else{
		  SetValue(checkID_save, false);
		}
	   
	   	
   		StrCopy(dspStr, " Version:");
		switch(DOC_Values.version){
		case 1:
			StrCat(dspStr, "Uncompressed.");
			break;
		case 2:
			StrCat(dspStr, "Compressed.");
			break;
		case 3:
			StrCat(dspStr, "Encrypted.");
			break;
		default:
			StrCat(dspStr, "Unknown.");
		}
		len = StrLen(dspStr);


		StrCat(dspStr, " Size:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Values.doc_size);

		len = StrLen(dspStr);
		field=GetObjectPtr(fieldID_Info1);
		SetFieldTextFromStr(field, dspStr, len);

		StrCopy(dspStr, " Rec Num:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Values.count);
		len = StrLen(dspStr);
		StrCopy(dspStr+len, " current:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , DOC_Field.index);
		len = StrLen(dspStr);
		dspStr[len] = ',';
		StrIToA(dspStr + len + 1 , FldGetInsPtPosition(DOC_Field.field));

	   	len = StrLen(dspStr);
	   	field=GetObjectPtr(fieldID_Info2);
		SetFieldTextFromStr(field, dspStr, len);

		fs_listxtem= LstGetSelection(list);
//		len = StrLen(array[listxtem]);
//	   	field=GetObjectPtr(fieldID_new);
//		SetFieldTextFromStr(field, array[listxtem], len);
		cardNo = 0;	  
	   	dbID =  store[fs_listxtem];
	   	DmDatabaseSize(cardNo, dbID, &numRecords, &totalBytes, &dataBytes);

	   	StrCopy(dspStr, " Rec:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , numRecords);
		len = StrLen(dspStr);
		StrCopy(dspStr+len, "  Total:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len , totalBytes);
		len = StrLen(dspStr);
		StrCopy(dspStr+len, "  Data:");
		len = StrLen(dspStr);
		StrIToA(dspStr + len, dataBytes);

	   	len = StrLen(dspStr);
	   	field=GetObjectPtr(fieldID_Info3);
		SetFieldTextFromStr(field, dspStr, len);

	   
		DmDatabaseInfo(cardNo, dbID, newName, &attributes, NULL, NULL,
			       NULL, NULL, NULL, NULL, NULL, NULL, NULL);
		len = StrLen(newName);
	    
	   	field=GetObjectPtr(fieldID_new);
		SetFieldTextFromStr(field, newName, len);
	   	if(attributes & dmHdrAttrBackup){
		   SetValue(checkID_back, true);
	       	}else{
		  SetValue(checkID_back, false);
		}
	   	if(attributes & dmHdrAttrReadOnly){
			SetValue(checkID_read, true);
		}else{
			SetValue(checkID_read, false);
		}
	   	if(attributes & dmHdrAttrOpen){
		   SetValue(checkID_open, true);
	       	}else{
		  SetValue(checkID_open, false);
		}
		
	   	if(g_dbID == dbID){
		   	field=GetObjectPtr(fieldID_new);
			FldSetUsable(field, false);
		   	control=GetObjectPtr(checkID_back);
			CtlSetUsable(control, false);
		   	control=GetObjectPtr(checkID_read);
			CtlSetUsable(control, false);
		   	control=GetObjectPtr(buttonID_del);
			CtlHideControl(control);
		}

		handled = true;
		break;
	case ctlSelectEvent:
		if(event->data.ctlSelect.controlID == buttonID_cancel){
//			FrmGotoForm(formID_zdoc);
			FrmReturnToForm(formID_zdoc);
			handled = true;
		}else if(event->data.ctlSelect.controlID == buttonID_ok){
		    field=GetObjectPtr(fieldID_scrollH);
		    StrCopy(dspStr, FldGetTextPtr(field));
		    fs_scrollH = StrAToI(dspStr);
    		field=GetObjectPtr(fieldID_scrollS);
		    StrCopy(dspStr, FldGetTextPtr(field));
		    fs_scrollS = StrAToI(dspStr);
    		field=GetObjectPtr(fieldID_scrollF);
		    StrCopy(dspStr, FldGetTextPtr(field));
		    fs_scrollF = StrAToI(dspStr);
    		field=GetObjectPtr(fieldID_font);
		    StrCopy(dspStr, FldGetTextPtr(field));
		    fs_font = StrAToI(dspStr);
			if(fs_font > ledFont){
				fs_font = stdFont;
			}

			fs_save = GetValue(checkID_save);
			cardNo = 0;
			dbID =  store[fs_listxtem];
			DmDatabaseInfo(cardNo, dbID, newName, &attributes, NULL, NULL,
			       NULL, NULL, NULL, NULL, NULL, NULL, NULL);
			
		    if( (GetValue(checkID_back)>0) !=  ((attributes & dmHdrAttrBackup)>0)
			   || (GetValue(checkID_read)>0) != ((attributes & dmHdrAttrReadOnly)>0)
			   || FldDirty(GetObjectPtr(fieldID_new))){
				if (GetValue(checkID_back)==1){
				    attributes = attributes |(dmHdrAttrBackup);
				}else{
				    attributes = attributes &(~dmHdrAttrBackup);
				}
				if (GetValue(checkID_read)==1){
				    attributes = attributes |(dmHdrAttrReadOnly);
				}else{
				    attributes = attributes &(~dmHdrAttrReadOnly);
				}
				field=GetObjectPtr(fieldID_new);
				StrCopy(dspStr, FldGetTextPtr(field));
				if(DmSetDatabaseInfo(cardNo, dbID, dspStr, &attributes, NULL, NULL,
				       NULL, NULL, NULL, NULL, NULL, NULL, NULL) == 0){
//					StrCopy(name[listxtem], dspStr);
					FrmUpdateForm(formID_zdoc, UPDATE_NAME);

				}
		    }

		    
		    FrmReturnToForm(formID_zdoc);
		    handled = true;
		}else if(event->data.ctlSelect.controlID == buttonID_del){
			dbID =  store[fs_listxtem];
		    if(g_dbID != dbID){
			    if (fs_listxtem>=0 && docNum > 0){
					DmDeleteDatabase(0, dbID);
			    }
				FrmUpdateForm(formID_zdoc, UPDATE_NAME);
			}
			FrmReturnToForm(formID_zdoc);
			handled = true;
		}
		break;
	case menuEvent:
		handled = EditMenuEvent(event);
		break;
	}
	return handled;
}

static void SetListArray(Word num){
    int i;
    int x=0;
    CharPtr listItem;
    ControlPtr popup;

    x=DmNumDatabases(0);
    docNum = 0;
    array[MAXDOC] = NULL;
    for (i=0;i<x;i++){
	ULong dbType;	
	DmDatabaseInfo(0,DmGetDatabase(0,i),&newName[0],NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,&dbType,NULL);
//	WinDrawChars((CharPtr) &dbType,4,pos_about+1,80);
	if(dbType == DocTypeID){
	    if (docNum<MAXDOC) {
		int j=0;
		while ((newName[j] != '\0') && (j<dmDBNameLength)){
		    name[docNum][j]=newName[j];
		    j++;
		}
		name[docNum][j]='\0';
		store[docNum]=DmGetDatabase(0,i);
		array[docNum]=(CharPtr) &name[docNum][0];
		docNum++;
	    }
	}
    }
    array[docNum] = NULL;
    list=(ListPtr)FrmGetObjectPtr(g_form,FrmGetObjectIndex(g_form, listID_dblist));
    LstSetListChoices(list, &array[0], (docNum)); 
    if(num>= docNum){
    	num = 0;
    }
	LstSetSelection(list, num);
    listItem=LstGetSelectionText(list, num);
    if (!listItem) {
		listItem="*No Doc*";
    }
    popup=(ControlPtr)FrmGetObjectPtr(g_form,FrmGetObjectIndex(g_form,listID_popuplist));
    CategorySetTriggerLabel(popup,listItem);

}
    
    
static void EventLoop(void)
{
	/*	ULong   guess=0;	*/
	short err;
	int formID;

	EventType event;

	do{
		EvtGetEvent(&event, 200);

		if ( SysHandleEvent(&event) )   continue;
		if ( MenuHandleEvent((void *)0, &event, &err) ) continue;

		if ( event.eType == frmLoadEvent ){


			formID = event.data.frmLoad.formID;

			g_form = FrmInitForm(formID);


			FrmSetActiveForm(g_form);

			switch ( formID ){
			case alertID_new:
				FrmSetEventHandler(g_form,(FormEventHandlerPtr) hellonew);
				FrmDrawForm(g_form);
				break;

			case formID_null:
					FrmSetEventHandler(g_form,(FormEventHandlerPtr) hellonew);
					FrmDrawForm(g_form);
					break;
			case formID_find:
					debuginfo("FrmPopupForm", 300+formID_find);
					FrmSetEventHandler(g_form, FindFormHandleEvent);
					debuginfo("FrmPopupForm", 400+formID_find);
					FrmDrawForm(g_form);
					break;
			case formID_info:
					FrmSetEventHandler(g_form, InfoFormHandleEvent);
					FrmDrawForm(g_form);
					break;

			case formID_zdoc:
				// guess='Z'*256*256*256+'R'*256*256+'A'*256+'M';
				// DmCreateDatabase(0,"ZRAM",guess,guess,(Boolean) false);
				// RAMID=DmFindDatabase(0,"ZRAM");
				// RAM=DmOpenDatabase(0,RAMID,dmModeReadWrite);
					FrmSetEventHandler(g_form, (FormEventHandlerPtr) hello);
				
					scrambuf();
					DOC_Field.field=(FieldPtr) FrmGetObjectPtr(g_form,FrmGetObjectIndex(g_form,fieldID_zdoc));
					FldSetUsable(DOC_Field.field,false);
				SetListArray(fs_listxtem);
					break;
			}
		}

		FrmDispatchEvent(&event);
	} while ( event.eType != appStopEvent );
}

static void SetNewRecord(void)
{
	CharPtr      cPtr;
	SystemPreferencesType sysPrefs;	/* User's Pilot preferences. */
	DateTimeType dateTime;
	UInt len;

	cPtr = DOC_Text.raw;

	PrefGetPreferences( &sysPrefs );
	MemMove(cPtr, newRecord, sizeof newRecord ); 
	len = StrLen(cPtr);
	TimSecondsToDateTime(TimGetSeconds(), &dateTime);
	DateToAscii(dateTime.month, dateTime.day, dateTime.year, sysPrefs.dateFormat, cPtr+len);
	StrCat(cPtr, " ");
	len = StrLen(cPtr);
	TimeToAscii(dateTime.hour, dateTime.minute, sysPrefs.timeFormat, cPtr+len);
	DOC_Text.orgLen = 0;
	DOC_Text.rawLen = StrLen(cPtr);
}

static Err CreateDoc(void)
{
	DmOpenRef dbRef;
	FieldPtr newfield;
	Err err;

	newfield = GetObjectPtr(fieldID_new);
	if (FldGetTextLength(newfield) ==0 ){
		return 0;
	}

	StrCopy(newName, FldGetTextPtr(newfield));
	err = DmCreateDatabase(0,newName, 'R'*256*256*256+'E'*256*256+'A'*256+'d','T'*256*256*256+'E'*256*256+'X'*256+'t', false);
	if(err != 0){
		WinDrawChars("Error  ", 7, pos_about, 145);
		return 1;
	}
	dbRef=DmOpenDatabase(0,DmFindDatabase(0,newName),dmModeReadWrite);
	DOC_Values.reserved1=0;
	DOC_Values.doc_size=0;
	DOC_Values.count=0;
	DOC_Values.rec_size=MAXTEXTSIZE;
	DOC_Values.reserved2=0;
	if (GetValue(checkID_comp)==1){
		DOC_Values.version=2;
	}else{
		DOC_Values.version=1;
	}
	if (GetValue(checkID_enc)==1){
		DOC_Values.version=3;
	}
	WriteValues(dbRef, true);
	SetNewRecord();
	saverec(dbRef, true, 1);
	DmCloseDatabase(dbRef);
	return 0;
}


static Boolean hellonew(EventPtr event)
{
	int       i=0;

	int       handled = 0;

	g_form = FrmGetActiveForm();
	switch ( event->eType )
	{
		case frmOpenEvent:
			FrmDrawForm(g_form);
			handled = 1;
		break;

		case ctlSelectEvent:     // A control button was pressed and released.
			if ( event->data.ctlEnter.controlID== buttonID_zdoc ){
				FrmAlert (alertID_about);
				handled = 1;
			}else if ( event->data.ctlEnter.controlID== buttonID_new ){
				CloseDoc();
				if(CreateDoc() == 0){
					LstEraseList(list);
					// FrmReturnToForm(formID_zdoc);
					FrmCloseAllForms();
					FrmGotoForm(formID_zdoc);
				}
				handled = 1;
			}else if ( event->data.ctlEnter.controlID== buttonID_abc ){
				FieldPtr abcfield;
				abcfield = GetObjectPtr(fieldID_abc);
				if (FldGetTextLength(abcfield)!=0){
					char      temp[21]; /* 21 is abcfield max len +1 */
					for (i=0;i<KEYLEN;i++){
						key[i]=0;
						temp[i]=0;
					}
					StrCopy(&temp[0],FldGetTextPtr(abcfield));
					for (i=0;i<KEYLEN;i++){
						key[i]=temp[i];
					}
					DOC_Text.index = 0;
				} 
				FrmReturnToForm(formID_zdoc);
				handled = 1;
			}
		case menuEvent:
			handled = EditMenuEvent(event);
			break;
		case nilEvent:
			handled = 1;
			break;
	}
	return handled;
}

static void delrec(void){
	if (g_dbID != 0){
		if (DOC_Field.index<DOC_Values.count){
			DmOpenRef dbW;
			dbW=DmOpenDatabase(0, g_dbID,dmModeReadWrite);
			DmRemoveRecord(dbW, DOC_Field.index);
			DOC_Values.count--;
			DOC_Values.doc_size = DOC_Values.doc_size - DOC_Field.len;
			WriteValues(dbW, false);
			DmCloseDatabase(dbW);
			DisplayRecord(DOC_Field.index);
		}
	}
}

static void SaveField(void)
{
	if (g_dbID != 0){
		DmOpenRef dbW;
		VoidHand textHand;
		VoidPtr  textPtr;
		ULong len;
		len = FldGetTextLength(DOC_Field.field);
		if(len == 0){
			delrec();
		}else{
			DrawMark(1);
//			WinDrawChars((CharPtr) "§",1,150,0);
			FldSetUsable(DOC_Field.field,false);
			dbW=DmOpenDatabase(0,g_dbID,dmModeReadWrite);
			textHand=(VoidHand) FldGetTextHandle(DOC_Field.field);
			textPtr=MemHandleLock(textHand);
			scrambuf();
			MemMove(DOC_Text.raw, textPtr, len);
			//		MemHandleUnlock(textHand);
			MemPtrUnlock(textPtr);
			DOC_Text.rawLen = len;
			DOC_Text.orgLen = DOC_Field.len;
			saverec(dbW, false, DOC_Field.index);
			DmCloseDatabase(dbW);
//			WinDrawChars((CharPtr) "\x9d",1,150,0);
			DrawMark(0);

			FldSetUsable(DOC_Field.field,true);
			FldSetDirty(DOC_Field.field, false);
			FldDrawField(DOC_Field.field);
		}
	}
}

static Err DisplayRecord(UInt index)
{
	if (index > 0 && index <= DOC_Values.count ){

		if((fs_save != false)&& (FldDirty(DOC_Field.field) != false)){
			SaveField();
		}
		DrawMark(1);	   
		FldSetUsable(DOC_Field.field,false);
		loadrec(index);

		DOC_Field.len = DOC_Text.len;
		DOC_Field.index = DOC_Text.index;
		SetFieldTextFromStr(DOC_Field.field, DOC_Text.text, DOC_Field.len);
		FldSetUsable(DOC_Field.field,true);
		FldDrawField(DOC_Field.field);
		FldSetInsPtPosition(DOC_Field.field, 0);
		DrawMark(0);	   
		return 0;
	}
	return 1;
}

static Err OpenReadDatabase(UInt cardNo, LocalID dbID)
{
	VoidHand  recHand=NULL;
	VoidPtr   recPtr=NULL;
	UInt maxrec;
	g_dbR=DmOpenDatabase(cardNo, dbID, dmModeReadOnly);
	if(g_dbR == 0){
		WinDrawChars(" Cannot open.", 12, 40, 40);
		delay(201);
		return 1;
	}
	maxrec=DmNumRecords(g_dbR);
	recHand=DmQueryRecord(g_dbR,0);
	if(recHand == 0){
		CloseDoc();
		WinDrawChars((CharPtr) " DocInfo missing.",17,40,40);
		delay(202);
		return 2;
	}
	recPtr=MemHandleLock(recHand);
	MemMove(&DOC_Values, recPtr, sizeof DOC_Values);
//	MemHandleUnlock(recHand);
	MemPtrUnlock(recPtr);
	DmReleaseRecord(g_dbR,0,(Boolean) false);
	if(maxrec <= DOC_Values.count){
		WinDrawChars((CharPtr) " maxrec low.",12, 40, 3);
		DOC_Values.count = maxrec -1;
	}
	return 0;
}

static Boolean NextRecord(void)
{
	if(g_dbID != 0){
	    if(DisplayRecord(DOC_Field.index + 1) == 0){
			return true;
	    }
	}
	return false;
}

static Boolean PrevRecord(void)
{
	if(g_dbID != 0){
		if(DisplayRecord(DOC_Field.index - 1) == 0){
			return true;
		}
	}
	return false;
}

static void SetInsPt(Word pos)
{
	FldEraseField(DOC_Field.field);
	FldSetScrollPosition(DOC_Field.field, pos);	
	FldSetInsPtPosition(DOC_Field.field, pos);
	FldSetSelection(DOC_Field.field, pos, pos);
	FldDrawField(DOC_Field.field);
	FldGrabFocus(DOC_Field.field);
	InsPtEnable(true);
}


/* Go to Bottom of Page */
static Boolean GoBottom(void)
{
	Word	cpos;
	Word	bpos;
	Word	spos;
	FldEraseField(DOC_Field.field);
	bpos = FldGetTextLength(DOC_Field.field);
	if(bpos == FldGetInsPtPosition(DOC_Field.field) ){
		cpos = FldGetScrollPosition(DOC_Field.field);
		FldSetScrollPosition(DOC_Field.field, bpos);
		spos = FldGetScrollPosition(DOC_Field.field);
		if(spos <= cpos){
			FldDrawField(DOC_Field.field);
			return false;
		}
	}
	FldSetInsPtPosition(DOC_Field.field, bpos);
	FldSetSelection(DOC_Field.field, bpos, bpos);
	FldDrawField(DOC_Field.field);
	FldGrabFocus(DOC_Field.field);
	InsPtEnable(true);
/*	SetInsPt(pos); */
	return true;
}

/* Go to Top of Page */
static Boolean GoTop(void)
{
	Word pos;
	pos = FldGetScrollPosition(DOC_Field.field);
	if(pos == 0 ){
		pos = FldGetInsPtPosition(DOC_Field.field);
		if(pos == 0){
			return false;
		}
	}
	SetInsPt(0);
	return true;
}

static void fld_scroll(Word lines, DirectionType direction){
    if (g_dbID != 0){
		Word pos;
		if(direction == up){		       /* up */
			if(lines<=0){
				PrevRecord();
			}else{
				if(FldScrollable(DOC_Field.field, up)){
					FldScrollField(DOC_Field.field, lines, up);
					if(!FldScrollable(DOC_Field.field, up)){
						GoTop();
					}
				}else{
					if(PrevRecord() != false){
						GoBottom();
					}
				}
			}
		}else{			    	/* down */
			if(lines<=0){
				NextRecord();
			}else{
				if(FldScrollable(DOC_Field.field, down)){
					FldScrollField(DOC_Field.field, lines, down);
					if(!FldScrollable(DOC_Field.field, down)){
						GoBottom();
					}
				}else{
					if(NextRecord() != false){
						GoTop();
					}
				}
			}
		}
		pos = FldGetScrollPosition(DOC_Field.field);
		if(abs(pos - FldGetInsPtPosition(DOC_Field.field)) > INSPTOFF ){
			FldSetInsPtPosition(DOC_Field.field, pos);
		}
		infoPos();
    }
}
		 

		 
static Boolean hello(EventPtr event)
{
	int       handled = 0;
	/*	Handle    garbage;	*/

	g_form = FrmGetActiveForm();

	switch ( event->eType )
	{
	case frmOpenEvent:
		FrmDrawForm(g_form);
		handled = 1;
		break;
	case keyDownEvent:
		if (event->data.keyDown.chr == pageUpChr){ /* Prev */
		    fld_scroll(fs_scrollH, up);
			handled = 1;
		}else if (event->data.keyDown.chr == pageDownChr){ /* next */
		    fld_scroll(fs_scrollH, down);
			handled = 1;
		}
		break;
	case ctlSelectEvent:     // A control button was pressed and released.
		if ( event->data.ctlEnter.controlID== buttonID_zdoc ){
			if(g_dbID == 0){
				FrmAlert (alertID_about);
			}else{
				fld_scroll(fs_scrollS, down);
			}
			handled = 1;
		}
		if ( event->data.ctlEnter.controlID== buttonID_open ){
			if (g_dbID == 0){
				Word	listxtem;
				listxtem= LstGetSelection(list);
				if (listxtem>=0 && docNum > 0){
					void * control;
					FldSetUsable(DOC_Field.field,false);
					FldSetFont(DOC_Field.field, fs_font);
					if(OpenReadDatabase(0, store[listxtem]) == 0){
						control = GetObjectPtr(buttonID_open);
						CtlSetLabel(control, "Close");
						g_dbID = store[listxtem];
						DisplayRecord(1);
//						WinDrawChars((CharPtr) "ù",1,150,0);
					}
				}else{
					WinDrawChars(" No doc.", 8, 40, 40);
				}
			}else{
				CloseDoc();
				LstEraseList(list);
				FrmCloseAllForms();
				FrmGotoForm(formID_zdoc);
			}
			handled = 1;
		}
		if ( event->data.ctlEnter.controlID== buttonID_up ){
		    fld_scroll(fs_scrollS, up);
			handled = 1;
		}
		if ( event->data.ctlEnter.controlID== buttonID_down ){
		    fld_scroll(fs_scrollS, down);
			handled = 1;
		}
		if ( event->data.ctlEnter.controlID== buttonID_save ){
			SaveField();
			handled = 1;
		}
		if ( event->data.ctlEnter.controlID== buttonID_find ){
			FrmPopupForm(formID_find);
			handled = true;
		}else if ( event->data.ctlEnter.controlID== buttonID_again ){
			if(FindPos() != false){
					/* frmUpdateEvent */
				FrmUpdateForm(formID_zdoc, UPDATE_GOPOS);
			}
			handled = true;
		}
		break;
	case menuEvent:
		handled = EditMenuEvent(event);
		if(handled != false){
			break;
		}
		switch (event->data.menu.itemID){
		case menuitemID_about:
			FrmAlert (alertID_about);
			break;
		case menuitemID_info:
			FrmPopupForm(formID_info);
			break;
		case menuitemID_close:
			if (g_dbID != 0){
				CloseDoc();
				LstEraseList(list);
				FrmCloseAllForms();
				FrmGotoForm(formID_zdoc);
			}
			break;
		case menuitemID_new:
				FrmPopupForm(alertID_new);
				break;
		case menuitemID_pass:
				FrmPopupForm(formID_null);
				break;
		case menuitemID_Find:
				debuginfo("FrmPopupForm", 100+formID_find);
				FrmPopupForm(formID_find);
				debuginfo("FrmPopupForm", 200+formID_find);
				break;
		case menuitemID_Again:
			if(FindPos() != false){
					/* frmUpdateEvent */
				FrmUpdateForm(formID_zdoc, UPDATE_GOPOS);
			}
			break;
		case menuitemID_Save:
			SaveField();
			break;
		case menuitemID_rec:
			if (g_dbID != 0){
				DmOpenRef dbW;
				dbW=DmOpenDatabase(0, g_dbID,dmModeReadWrite);
				SetNewRecord();
				saverec(dbW, true, DOC_Field.index+1);
				DmCloseDatabase(dbW);
			}
			break;
		case menuitemID_delete:
			if (1){	/* old g_dbID == 0 */
			    Word	listxtem;
				LocalID	dbID;
			    listxtem= LstGetSelection(list);
			    dbID = store[listxtem];
			    if(g_dbID != dbID){
				    if (listxtem>=0 && docNum > 0){
						DmDeleteDatabase(0,store[listxtem]);
				    }
				    SetListArray(listxtem);
				}
			}
			break;
		case menuitemID_delrec:
			delrec();
			break;
		case menuitemID_Bottom:	/* down */
			if(GoBottom() == false){
				if(DOC_Field.index < DOC_Values.count){
					DisplayRecord(DOC_Values.count);
				}
			}
			break;
		case menuitemID_Top:
			if(GoTop() == false){
				DisplayRecord(1);
			}
			break;
		case menuitemID_next:
			NextRecord();
			break;
		case menuitemID_prev:
			PrevRecord();
			break;
		case menuitemID_Reload:
			DisplayRecord(DOC_Field.index);
			break;
		}
		handled = 1;
		break;
	case nilEvent:
		{
			if (g_dbID != 0){
				void * control;

				control = GetObjectPtr(buttonID_save);

//				CtlSetEnabled(control, FldDirty(DOC_Field.field));
//				CtlSetUsable(control, FldDirty(DOC_Field.field));

#if 1
				if(FldDirty(DOC_Field.field)){
					CtlShowControl(control);
				}else{
					CtlHideControl(control);
				}
#endif
				infoPos();
			}
			handled = 1;
			break;
		}
	case frmUpdateEvent:
		/* event->data.frmUpdate.updateCode	*/
		if( event->data.frmUpdate.updateCode == UPDATE_GOPOS){
			Word pos;
			if(DOC_Field.index != go_page){
				DisplayRecord(go_page);
			}
			SetInsPt(go_pos);
			FldSetSelection(DOC_Field.field, go_pos, go_pos+1);
			if(fs_scrollF != 0){
				FldEraseField(DOC_Field.field);
				FldScrollField(DOC_Field.field, 1, down);
				pos = FldGetScrollPosition(DOC_Field.field);
				FldSetScrollPosition(DOC_Field.field, go_pos);
				FldDrawField(DOC_Field.field);
				if(pos>go_pos){
					if(fs_scrollF < 0){
						fld_scroll(-fs_scrollF, down);
					}else{
						fld_scroll(fs_scrollF, up);
					}
				}
			}
			handled = 1;
		}else if( event->data.frmUpdate.updateCode == UPDATE_NAME){
			Word	listxtem;
			listxtem= LstGetSelection(list);
			SetListArray(listxtem);
		    handled = 1;
		}
		break;
	}
	return handled;
}

/*
-Zurk-
These are kept for educational use. Also if someone wants to implement
serial tx/rx again..these are for you.

static void info(void)
{
	char      test[50];
	VoidPtr   testhandle;
        int       i;

if (open==1){

for (i=0;i<20;i++)
{test[i]=' ';}

for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "Version: ",9,40,40);
if (DOC_Values.version == 1)
{WinDrawChars((CharPtr) "Uncompressed.",12,80,40);}
if (DOC_Values.version == 2)
{WinDrawChars((CharPtr) "Compressed.",10,80,40);}
if (DOC_Values.version == 3)
{WinDrawChars((CharPtr) "Encrypted.",9,80,40);}

for (i=0;i<20;i++)
{test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.uncomplength);
for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "TotalTx: ",9,40,60);
WinDrawChars((CharPtr) &test[0],8,80,60);

for (i=0;i<20;i++)
{test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.count);
for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "Num Rec: ",9,40,80);
WinDrawChars((CharPtr) &test[0],8,80,80);

for (i=0;i<20;i++)
{test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.crap);
for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "ExtrHdX: ",9,40,70);
WinDrawChars((CharPtr) &test[0],8,80,70);
for (i=0;i<20;i++)
{test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.morecrap);
for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "ExtrHdY: ",9,40,90);
WinDrawChars((CharPtr) &test[0],8,80,90);

for (i=0;i<20;i++)
{test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.uncompsizeoftext);
for (i=0;i<20;i++)
{if (test[i]=='\0') {test[i]=' ';}}
WinDrawChars((CharPtr) "SizeTxt: ",9,40,100);
for (i=0;i<1;i++){
WinDrawChars((CharPtr) &test[0],8,80,100);}
}
}

static FieldPtr SetFieldTextFromHandle(FieldPtr fldP, Handle txtH)
  {
      Handle oldTxtH;
--          FormPtr frm = FrmGetActiveForm();
--          FieldPtr fldP;
-- NEW Field handler patch -- WARNING : Removes backup protection.
-- get the field and the field's current text handle.
-- fldP = FrmGetObjectPtr(frm, FrmGetObjectIndex(frm, fieldID));
-- ErrNonFatalDisplayIf(!fldP, "missing field");
oldTxtH = FldGetTextHandle(fldP);
-- set the field's text to the new text.
FldSetTextHandle(fldP, txtH);
FldDrawField(fldP);
-- free the handle AFTER we call FldSetTextHandle().
if (oldTxtH)
MemHandleFree((VoidHand) oldTxtH);
return fldP;
}

static void ClearFieldText(FieldPtr fieldP)
{
SetFieldTextFromHandle(fieldP,NULL);
}
static FieldPtr SetFieldTextFromStr(FieldPtr fieldP, CharPtr strP)
{
Handle txtH;
txtH= (Handle) MemHandleNew(4096);
if (!txtH)
       return NULL;
       MemMove(MemHandleLock((VoidHand) txtH),strP,4096);
       MemHandleUnlock((VoidHand) txtH);
       ClearFieldText(fieldP);
       return SetFieldTextFromHandle(fieldP,txtH);
}

static HardButtonHandleEvent(EventPtr event)
{
Boolean handled = false;
if (event->eType == keyDownEvent)
{  if (event->data.keyDown.chr == findChr)
{  handled = true;   }  }
  return handled;
}

unsigned int uncompress(Buffer *b) {
    unsigned char *pOut;
    unsigned char *in_buf;
    unsigned char *out_buf;
    int i=0, j=0, m=0, n=0;
    int total=0;
    unsigned int c=0;
    
    pOut = MemPtrNew(BUFSIZE);
    in_buf = b->buf;
    out_buf = pOut;
    total=0;

    for (j=i=0; j < b->len; ) {
	c = in_buf[j++];

	if (c > 0 && c < 9)
	    while (c--) {out_buf[i++] = in_buf[j++];total++;}
	else if (c < 0x80)
	    {out_buf[i++] = c; total++;}
	else if (c >= 0xc0) {
	    out_buf[i++] = ' ';
	    out_buf[i++] = c ^ 0x80; total++;
	} else {
	    c <<= 8;
	    c += in_buf[j++];
	    m = (c & 0x3fff) >> COUNT_BITS;
	    n = c & ((1 << COUNT_BITS)-1);
	    n += 3;
	    while (n--) {
		out_buf[i] = out_buf[i-m];
		i++; total++;
	    }
	}
    }

    MemPtrFree(b->buf);
    b->buf = pOut;
    b->len = i;
    uncompsize=total;
    return total;
}

static void scrambuf()
{
int i;
int x;
int a=0;
int b=0;
for (i=0;i<4097;i++){
a=(int) TimGetTicks();
for (x=0;x<5;x++){}
b=(int) TimGetTicks();
x=(int) b*a;
while (x>256)
{x=x-256;}
DOC_Text.text[i]=x;
}}

unsigned char* memfind(unsigned char* t, int t_len, unsigned char* m, int m_len)
{
	int i;

	for (i = t_len - m_len + 1 ; i>0; i--, t++)
		if (t[0]==m[0] && MemCmp(t,m,m_len)==0)
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

    b->buf = MemPtrNew(6000);
    b->len = 0;

    for (; pTestHead != pEnd; pTestTail++) {
	if (pTestHead - pPrevHit > ((1 << DISP_BITS)-1))
	    pPrevHit = pTestHead - ((1 << DISP_BITS)-1);
	pHit = memfind(pPrevHit, pTestTail - pPrevHit, pTestHead, pTestTail - pTestHead);
	if (pHit == 0)
        delay(0);
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

    MemPtrFree(pBuffer);
    b->len = k;

    return k;
}


static void loadrec(int x)
{
	int i=1;
	int zz=0;
	int z=0;
        int a;
	VoidPtr   RecPointerx=NULL;
	VoidHand  rechandlex=NULL;
	char *str;
	int len;
        Buffer b;
	int uncompsizeeftext=4096;

if (open==1)
{i=x; b.buf = MemPtrNew(BUFSIZE);
rechandlex=DmQueryRecord(dbR,i);
RecPointerx=MemHandleLock(rechandlex);
len=(int) MemPtrSize(RecPointerx);
uncompsizeeftext=(int) MemPtrSize(RecPointerx);
scrambuf();
MemMove(&DOC_Text, RecPointerx, uncompsizeeftext);
DmReleaseRecord(dbR,currentrec,(Boolean) false);
if(DOC_Values.version == 2)
{str=(CharPtr) &DOC_Text.text[0];
MemMove(b.buf, &DOC_Text.text[0], len);
b.len = len;
b.bSpace = 0;
z=uncompress(&b);
uncompsizeeftext=(int) z;
MemMove(&DOC_Text.text[0], b.buf, uncompsizeeftext);
MemPtrFree(b.buf);}
if (DOC_Values.version == 3)
{key[9]=0;
for (a=0;a<4097;a++){
DOC_Text.text[a]=(256-DOC_Text.text[a])-key[key[9]];
key[9]++;
if(key[9]>7){key[9]=0;}
}}

DOC_Text.text[4097]='\0';
uncompsize=uncompsizeeftext;
if (uncompsize > DOC_Values.uncomplength) { delay(0); }
if (uncompsize > DOC_Values.uncompsizeoftext) { delay(0); }
if (uncompsize < 2) { delay(0); }
MemHandleUnlock(rechandlex);
currentrec=i;
}}

unsigned int saverec(int x)
{	int i=1;
	int zz=0;
	int z=0;
	VoidPtr   RecPointerx=NULL;
	VoidHand  rechandlex=NULL;
	char *str;
	int len;
        Buffer c;
	int uncompsizeeftext=4096;

if (open==1)
{
i=x;
len = uncompsize;
if(DOC_Values.version == 2)
{c.buf = MemPtrNew(BUFSIZE);
str=(CharPtr) &DOC_Text.text[0];
MemMove(c.buf, &DOC_Text.text[0], len);
c.len = len;
c.bSpace = 0;
z=compress(&c);
uncompsizeeftext=(int) z;
MemMove(&DOC_Text.text[0], c.buf, uncompsizeeftext);
MemPtrFree(c.buf);
}
DOC_Text.text[4097]='\0';}
return(z);
}

static void EventLoop(void)
	{
	CharPtr array[50];
	char    text2[4];
	ULong   typeP[4];
	ULong   guess=0;
	char    name[20][30];
	char    namei[50];
        int     y=0;
	int i;
	int j=0;
	int x=0;
	ControlPtr popup;
        CharPtr  poo;
	short err;
	int formID;
	FormPtr form;
	EventType event;

	do
		{

		EvtGetEvent(&event, 200);
                if (HardButtonHandleEvent(&event)) continue;
		if ( SysHandleEvent(&event) )   continue;
		if ( MenuHandleEvent((void *)0, &event, &err) ) continue;

		if ( event.eType == frmLoadEvent )
			{
			formID = event.data.frmLoad.formID;
			form = FrmInitForm(formID);
			FrmSetActiveForm(form);
			switch ( formID )
				{
case alertID_new:
FrmSetEventHandler(form,(FormEventHandlerPtr) hellonew);
newfield=(FieldPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,fieldID_new));
control_comp=(ControlPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,checkID_comp));
control_enc=(ControlPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,checkID_enc));
FldSetUsable(newfield,true);
FrmDrawForm(form);
break;

case formID_null:
FrmSetEventHandler(form,(FormEventHandlerPtr) hellonew);
abcfield=(FieldPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,fieldID_abc));
FldSetUsable(abcfield,true);
FrmDrawForm(form);
break;

case formID_zdoc:
y=0;
FrmSetEventHandler(form, (FormEventHandlerPtr) hello);
scrambuf();
list=(ListPtr)FrmGetObjectPtr(form,FrmGetObjectIndex(form,listID_dblist));
field=(FieldPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,fieldID_zdoc));
FldSetUsable(field,false);
x=DmNumDatabases(0);for (i=0;i<x;i++){
DmDatabaseInfo(0,DmGetDatabase(0,i),&namei[0],NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,&typeP[0],NULL);
StrCopy((CharPtr) &text2[0], (CharPtr) &typeP[0]);
text2[4]='\0'; WinDrawChars((CharPtr) &text2[0],4,80,80);
if (text2[0]==84 && text2[1]==69 && text2[2]==88 && text2[3]==116)
{abug++;
if (y<20) {j=0; while ((namei[j] != '\0') && (j<30))
{name[y][j]=namei[j]; j++;} name[y][j]='\0'; store[y]=DmGetDatabase(0,i);
array[y]=(CharPtr) &name[y][0]; y++;}}}
LstSetListChoices(list,&array[0],(y)); listItem=LstGetSelectionText(list,0);
if (!listItem) {listItem="                       "; alien=1;}
popup=(ControlPtr)FrmGetObjectPtr(form,FrmGetObjectIndex(form,listID_popuplist));
CategorySetTriggerLabel(popup,listItem); break;
				}
			}
		FrmDispatchEvent(&event);
		} while ( event.eType != appStopEvent );
	}

static Boolean hellonew(EventPtr event)
	{
	int       a=0;
	int       zz=0;
	int       z=0;
	int       i=0;
        char      temp[21];
	FormPtr   form=NULL;
	int       handled = 0;
	VoidHand  rechandle=NULL;
	VoidPtr   RecPointer=NULL;
	UInt      recptr=0;
        DmOpenRef recx;
        
	switch ( event->eType )
		{
		case frmOpenEvent:
			form = FrmGetActiveForm();
			FrmDrawForm(form);
			handled = 1;
			break;

  case ctlSelectEvent:     -- A control button was pressed and released.
			if ( event->data.ctlEnter.controlID== buttonID_zdoc )
				{
	    FrmAlert (alertID_about);
				handled = 1;
				}
                        if ( event->data.ctlEnter.controlID== buttonID_new )
				{
if (FldGetTextLength(newfield)!=0)
{
StrCopy(&nam[0],FldGetTextPtr(newfield));
DmCreateDatabase(0,&nam[0],'R'*256*256*256+'E'*256*256+'A'*256+'d','T'*256*256*256+'E'*256*256+'X'*256+'t',(Boolean) false);
recx=DmOpenDatabase(0,DmFindDatabase(0,nam),dmModeReadWrite);
recptr=0;
rechandle=(VoidHand) DmNewRecord(recx,&recptr,14);
RecPointer=MemHandleLock(rechandle);
DOC_Values.crap=3;
DOC_Values.uncomplength=4096;
DOC_Values.count=1;
DOC_Values.uncompsizeoftext=4096;
DOC_Values.morecrap=0;
if (CtlGetValue(control_comp)==1)
{DOC_Values.version=2;}
else
{DOC_Values.version=1;}
if (CtlGetValue(control_enc)==1)
{DOC_Values.version=3;}
DmWrite(RecPointer,0,&DOC_Values,14);
MemHandleUnlock(rechandle);
recptr=1;
rechandle=(VoidHand) DmNewRecord(recx,&recptr,4096);
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text,4096);
DmCloseDatabase(recx);}
currentrec=1;
FldSetTextHandle(field,NULL);
if (open==1)
{DmCloseDatabase(dbR);
open=0;}
ClearFieldText(field);
LstEraseList(list);
FrmCloseAllForms();
alien=0;
FrmGotoForm(formID_zdoc);
				handled = 1;
				}
                        if ( event->data.ctlEnter.controlID== buttonID_abc )
				{
if (FldGetTextLength(abcfield)!=0)
{
for (i=0;i<8;i++)
{key[i]=0;
temp[i]=0;}
StrCopy(&temp[0],FldGetTextPtr(abcfield));
for (i=0;i<8;i++)
{key[i]=temp[i];}
} FrmReturnToForm(formID_zdoc);
				handled = 1;
				}

		case nilEvent:
			handled = 1;
			break;
		}
	return handled;
}


static Boolean hello(EventPtr event)
	{
	int       a=0;
	int       zz=0;
	int       z=0;
	int       i=0;
	FormPtr   form=NULL;
	UInt      maxrec=NULL;
	short int compress;
        char      tempz[3];
        UInt      spare;
	ULong     uncompsizeoftext=4096;
	int       handled = 0;
	int       titanic=0;
	VoidHand  rechandle=NULL;
	VoidPtr   RecPointer=NULL;
	Handle    garbage;
	UInt      recptr=0;
        DmOpenRef recx;
        UInt      cowbunga;
        Handle    romhandle;
        
	switch ( event->eType )
		{
		case frmOpenEvent:
			form = FrmGetActiveForm();
			FrmDrawForm(form);
			handled = 1;
			break;
        case keyDownEvent:             
                if (event->data.keyDown.chr == findChr)
	   		{
                        handled = 1;
                        return 1;
	   		}
	   	if (event->data.keyDown.chr == pageUpChr)
	   		{
                        if (open==1){
                        FldScrollField(field,1,up);
                                    }
                        handled = 1;
	   		}
	   	else if (event->data.keyDown.chr == pageDownChr)
	   		{
                        if (open==1){
                        FldScrollField(field,1,down);
                                    }
                        handled = 1;
	   		}
			break;
  case ctlSelectEvent:     -- A control button was pressed and released.
			if ( event->data.ctlEnter.controlID== buttonID_zdoc )
				{
	    FrmAlert (alertID_about);
				handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_open )
				{
if (alien==0)
{
if (open == 0)
{
listxtem=0;
listxtem=(int) LstGetSelection(list);
if (abug > 0)
{
FldSetUsable(field,true);
FldSetUsable(field,false);
open=1;
dbR=DmOpenDatabase(0,store[listxtem],dmModeReadOnly);
maxrec=DmNumRecords(dbR);
rechandle=DmQueryRecord(dbR,maxrec-maxrec);
RecPointer=MemHandleLock(rechandle);
MemMove(&DOC_Values, RecPointer, 14);
DmReleaseRecord(dbR,maxrec-maxrec,(Boolean) false);
i=0;
MemHandleUnlock(rechandle);
i=1;
loadrec(i);
uncompsizeoftext=uncompsize;
recptr=0;
SetFieldTextFromStr(field, &DOC_Text.text[0]);
FldSetUsable(field,true);
FldDrawField(field);
WinDrawChars((CharPtr) "ù",1,150,0);
}}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_up )
				{
				if(open==1){
				if (currentrec>1){
                                FldSetUsable(field,false);
				currentrec--;
loadrec(currentrec);
uncompsizeoftext=uncompsize;
-- error!
-- DmDeleteRecord(RAM,recptr);
-- error!
recptr=0;
SetFieldTextFromStr(field, &DOC_Text.text[0]);
FldSetUsable(field,true);
FldDrawField(field);
WinDrawChars((CharPtr) "ù",1,150,0);
				}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_down )
				{
				if(open==1){
				if (currentrec<DOC_Values.count){
                                FldSetUsable(field,false);
				currentrec++;
				loadrec(currentrec);
uncompsizeoftext=uncompsize;
recptr=0;
SetFieldTextFromStr(field, &DOC_Text.text[0]);
FldSetUsable(field,true);
FldDrawField(field);
WinDrawChars((CharPtr) "ù",1,150,0);
				}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_save )
				{
if (open == 1){
FldScrollField(field,25,up); // get to top of field
FldSetUsable(field,false);
spare=(UInt) currentrec;
dbW=DmOpenDatabase(0,store[listxtem],dmModeReadWrite);
DmRemoveRecord(dbW,spare);
rom=(VoidHand) FldGetTextHandle(field);
romx=MemHandleLock(rom);
scrambuf();
MemMove(&DOC_Text.text[0], romx, uncompsize);
MemHandleUnlock(rom);
recptr=0;
if (DOC_Values.version==1)
{spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) uncompsize);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0], uncompsize);
WinDrawChars((CharPtr) "ù",1,150,0);}
if (DOC_Values.version == 3)
{key[9]=0;
for (a=0;a<4097;a++){
DOC_Text.text[a]=256-(DOC_Text.text[a]+key[key[9]]);
key[9]++;
if (key[9]>7){key[9]=0;}
}
spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) uncompsize);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0], uncompsize);
WinDrawChars((CharPtr) "ù",1,150,0);}
if (DOC_Values.version==2)
{WinDrawChars((CharPtr) "§",1,150,0);
i=saverec(0);
DOC_Text.text[i+1]='\0';
spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) i);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0],(ULong) i);
WinDrawChars((CharPtr) "ù",1,150,0);}
DmReleaseRecord(dbW,spare,(Boolean) true);
DmCloseDatabase(dbW);
SetFieldTextFromStr(field, &DOC_Text.text[0]);
FldSetUsable(field,true);
FldDrawField(field);
                                }
handled = 1;
				}
			break;

		case menuEvent:
			switch (event->data.menu.itemID)
				{
				case menuitemID_about:
					FrmAlert (alertID_about);
					break;
                                case menuitemID_info:
                                        info();
                                        break;
                                case menuitemID_close:
if (open==1){
currentrec=1;
FldSetTextHandle(field,NULL);
DmCloseDatabase(dbR);
open=0;
LstEraseList(list);
ClearFieldText(field);
FrmCloseAllForms();
FrmGotoForm(formID_zdoc);}
                                        break;
                                case menuitemID_new:
                                FrmPopupForm(alertID_new);
                                        break;
                                case menuitemID_pass:
                                FrmPopupForm(formID_null);
                                        break;
                                case menuitemID_rec:
if (open==1)
{dbW=DmOpenDatabase(0,store[listxtem],dmModeReadWrite);
spare=(UInt) currentrec;
spare++;
rechandle=DmNewRecord(dbW,&spare, 4096);
spare=(UInt) currentrec;
spare++;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text, 4096);
MemHandleUnlock(rechandle);
spare=0;
DmRemoveRecord(dbW,spare);
spare=0;
rechandle=DmNewRecord(dbW,&spare, 14);
spare=0;
RecPointer=MemHandleLock(rechandle);
DOC_Values.count++;
DmWrite(RecPointer,0,&DOC_Values, 14);
DmCloseDatabase(dbW);}
                                        break;
                                case menuitemID_delete:
if (alien==0)
{ if (open==0){ listxtem=(int) LstGetSelection(list);
DmDeleteDatabase(0,store[listxtem]);
if (open==1)
{DmCloseDatabase(dbR);
open=0;}
ClearFieldText(field);
LstEraseList(list);
FrmCloseAllForms();
FrmGotoForm(formID_zdoc);} }
                                        break;
                                case menuitemID_copy:
if (open==1)
{FldCopy(field);}
                                break;
                                case menuitemID_paste:
if (open==1)
{FldPaste(field);}
                                break;
-- here we go for the serial tx/rx. 
                                case menuitemID_rx:
SysLibFind("Serial Library",&cowbunga);
SerOpen(cowbunga,0,9600);
WinDrawChars((CharPtr) "§",1,150,0);
if (open == 1)
{ 
FldSetUsable(field,false);
spare=(UInt) currentrec;
dbW=DmOpenDatabase(0,store[listxtem],dmModeReadWrite);
DmRemoveRecord(dbW,spare);
WinDrawChars((CharPtr) "RECV 9600 å",11,40,40);
SerReceive10(cowbunga,&DOC_Text.text,4096,6000);
WinDrawChars((CharPtr) "RECV 9600 –",11,40,40);
recptr=0;
if (DOC_Values.version==1)
{spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) uncompsize);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0], uncompsize);
WinDrawChars((CharPtr) "ù",1,150,0);}
if (DOC_Values.version==2)
{WinDrawChars((CharPtr) "§",1,150,0);
i=saverec(0);
DOC_Text.text[i+1]='\0';
spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) i);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0],(ULong) i);
WinDrawChars((CharPtr) "ù",1,150,0);}
DmReleaseRecord(dbW,spare,(Boolean) true);
DmCloseDatabase(dbW);
FldSetUsable(field,false);
SetFieldTextFromStr(field, &DOC_Text.text[0]);
FldSetUsable(field,true);
FldDrawField(field);
}
SerClose(cowbunga);
WinDrawChars((CharPtr) "ù",1,150,0);

                                        break;
                                case menuitemID_tx:
if (alien==0){
if (open == 0)
{
SysLibFind("Serial Library",&cowbunga);
SerOpen(cowbunga,0,9600);
WinDrawChars((CharPtr) "§",1,150,0);
listxtem=0;
listxtem=(int) LstGetSelection(list);
if (abug > 0)
{WinDrawChars((CharPtr) "XMIT 9600 ",10,40,40);
dbR=DmOpenDatabase(0,store[listxtem],dmModeReadOnly);
maxrec=DmNumRecords(dbR);
rechandle=DmQueryRecord(dbR,maxrec-maxrec);
RecPointer=MemHandleLock(rechandle);
MemMove(&DOC_Values, RecPointer, 14);
DmReleaseRecord(dbR,maxrec-maxrec,(Boolean) false);
MemHandleUnlock(rechandle);
for(i=1;i<DOC_Values.count+1;i++)
{loadrec(i);
WinDrawChars((CharPtr) "XMIT 9600 å",11,40,40);
SerSend10(cowbunga,&DOC_Text.text,4096);
WinDrawChars((CharPtr) "XMIT 9600 –",11,40,40);}}}
SerClose(cowbunga);
WinDrawChars((CharPtr) "ù",1,150,0);
break;
            } }
	 handled = 1;
			break;

		case nilEvent:
			handled = 1;
			break;
		}
	return handled;
}

*/

// EOF
