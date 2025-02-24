/* ZDOC Ver. 5.0.0b
 * This program is distributed under the terms of the GPL v2.0 or later
 * Download the GNU Public License (GPL) from www.gnu.org
 *      
 *  Support the Free Software Foundation.
 *
 * by Zurk Technology Inc.
 * zurk@geocities.com
 * http://www.geocities.com/Area51/7689/zdoc.html
 * June/July 1998
 * 
 * This file is an attempt to improve(?) the existing source code
 * to more human-readable forms. Since ZDOC was produced in a very
 * short period of time, all commenting was left out of the code. This
 * version should help further development efforts by people other than myself.
 */

#pragma pack(2)
// bit shifting compiler directive [GCC m68k family cross compiler]
#include <Common.h>
#include <System/SysAll.h>
#include <UI/UIAll.h>
#include <SerialMgr.h>
#include "zdoc.h"
#define ZdocAppID    'ZURK'   
#define DISP_BITS 11
#define COUNT_BITS 3
#define BUFSIZE 6000
#define MAXTEXTSIZE 4096
// end definitions.

// statically define some functions
static int StartApplication(void);
static Boolean OpenDatabase(void);
static void EventLoop(void);
static void StopApplication(void);
static Boolean hello(EventPtr event);
static Boolean hellonew(EventPtr event);

// Define structure of DOC header [ZDOC FAQ -- DOC file formats] 
typedef struct
    {
    int     version;          //          2
    int     crap;             //          2
    long    uncomplength;     //          4
    int     count;            //          2
    int     uncompsizeoftext; //          2
    int     morecrap;         //          2
    } DOCInfoType;            //         14 bytes

typedef struct
     {
     char   text[4097];
     } DOCTxtType;           //         4096 bytes

static DOCInfoType DOC_Values =  { 0,0,0,0,0,0 } ;
static DOCTxtType  DOC_Text;
// some of my more entertaining variable defns..
        int alien=0;
	ListPtr list;
	CharPtr listItem;
	LocalID store[20];
        char    nam[21];
        int     key[9]={0,0,0,0,0,0,0,0,0};
	FieldPtr field;
        ControlPtr control_comp;
        ControlPtr control_enc;
        FieldPtr newfield;
        FieldPtr abcfield;
        DmOpenRef dbR;
        DmOpenRef dbW;
	DmOpenRef RAM;
	LocalID   RAMID;
	VoidPtr  romx;
	VoidHand rom;
        int     listxtem;
	int     open=0;
	unsigned char *ram;
	ULong    uncompsize=4096;
	int currentrec=1;

typedef struct {
    unsigned char *buf;
    int   len;
    int   bSpace;
} Buffer;

// main()

DWord  PilotMain (Word cmd, Ptr cmdPBP, Word launchFlags)
	{
	int error;

	if ( cmd == sysAppLaunchCmdNormalLaunch )
		{

		error = StartApplication();  // Application start code
		if ( error ) return error;

		EventLoop();  // Event loop

		StopApplication (); // Application stop code
		}
	return 0;
	}

// scan DOC header and extract info
static void info(void)
{
	char      test[50];
	VoidPtr   testhandle;
        int       i;

if (open==1){
        for (i=0;i<20;i++){test[i]=' ';}
        for (i=0;i<20;i++){if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "Version: ",9,40,40);
        if (DOC_Values.version == 1)
{WinDrawChars((CharPtr) "Uncompressed.",12,80,40);}

        if (DOC_Values.version == 2)
{WinDrawChars((CharPtr) "Compressed.",10,80,40);}

        if (DOC_Values.version == 3)
{WinDrawChars((CharPtr) "Encrypted.",9,80,40);}

for (i=0;i<20;i++) {test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.uncomplength);

        for (i=0;i<20;i++){if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "TotalTx: ",9,40,60);
WinDrawChars((CharPtr) &test[0],8,80,60);

for (i=0;i<20;i++){test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.count);

for (i=0;i<20;i++) {if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "Num Rec: ",9,40,80);
WinDrawChars((CharPtr) &test[0],8,80,80);

for (i=0;i<20;i++){test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.crap);

for (i=0;i<20;i++){if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "ExtrHdX: ",9,40,70);
WinDrawChars((CharPtr) &test[0],8,80,70);

for (i=0;i<20;i++){test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.morecrap);

for (i=0;i<20;i++){if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "ExtrHdY: ",9,40,90);
WinDrawChars((CharPtr) &test[0],8,80,90);

for (i=0;i<20;i++){test[i]=' ';}

StrIToA((CharPtr) &test[0],DOC_Values.uncompsizeoftext);

for (i=0;i<20;i++){if (test[i]=='\0') {test[i]=' ';}}

WinDrawChars((CharPtr) "SizeTxt: ",9,40,100);

for (i=0;i<1;i++){WinDrawChars((CharPtr) &test[0],8,80,100);}

}
}

// improvised delay loop and error handler

static void delay(int b)
{
int a;
char testing[5];
// <-----------------------------------------Delay Loop
for (a=0;a<1000;a++)
{
WinDrawChars((CharPtr) "-EHANDLR-",9,30,30);
}
StrIToA((CharPtr) &testing[0],b);
for (a=0;a<1000;a++)
{
WinDrawChars((CharPtr) &testing[0],5,30,30);
}
// <-----------------------------------------End Delay Loop
}

// GPLised RLE-based uncompression routine [ZDOC FAQ for more info] 

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

// Psuedo random number generator for [a] overwriting the buffer
// and [b] adding a leyer of protection to prevent encryption crackers

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

// Seek and thou shalt find. (:-)

unsigned char* memfind(unsigned char* t, int t_len, unsigned char* m, int m_len)
{
	int i;

	for (i = t_len - m_len + 1 ; i>0; i--, t++)
		if (t[0]==m[0] && MemCmp(t,m,m_len)==0)
			return t;
	return 0;
}

// compression/decompression chunks

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

// GPLised RLE-based compression routine. [ZDOC FAQ]

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

// Loader -- not very elegant but it works..

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
{

i=x;
b.buf = MemPtrNew(BUFSIZE);
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

// Save a record -- not elegant, but it works..
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

// K.I.S.S.
static int StartApplication(void)
        { int y;
	FrmGotoForm(formID_zdoc);
	}

// ugh.
static void StopApplication(void)
	{
if (open == 1)
{       open=0;
        FldSetTextHandle(field,NULL);
	DmCloseDatabase(RAM);
        DmDeleteDatabase(0,DmFindDatabase(0,"ZRAM"));
	DmCloseDatabase(dbR);
	}
        FldEraseField(field);
        FrmCloseAllForms();
	while ( 0 );
	}

// standard event handler

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

		if ( SysHandleEvent(&event) )   continue;
		if ( MenuHandleEvent((void *)0, &event, &err) ) continue;

		if ( event.eType == frmLoadEvent )
			{
			formID = event.data.frmLoad.formID;
			form = FrmInitForm(formID);
			FrmSetActiveForm(form);
			switch ( formID )
				{
// New DOC..i think.
case alertID_new:
FrmSetEventHandler(form,(FormEventHandlerPtr) hellonew);
newfield=(FieldPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,fieldID_new));
control_comp=(ControlPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,checkID_comp));
control_enc=(ControlPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,checkID_enc));
FldSetUsable(newfield,true);
FrmDrawForm(form);
break;

// dunno.
case formID_null:
FrmSetEventHandler(form,(FormEventHandlerPtr) hellonew);
abcfield=(FieldPtr) FrmGetObjectPtr(form,FrmGetObjectIndex(form,fieldID_abc));
FldSetUsable(abcfield,true);
FrmDrawForm(form);
break;

// startup
case formID_zdoc:
guess='Z'*256*256*256+'R'*256*256+'A'*256+'M';
DmCreateDatabase(0,"ZRAM",guess,guess,(Boolean) false);
RAMID=DmFindDatabase(0,"ZRAM"); y=0;
RAM=DmOpenDatabase(0,RAMID,dmModeReadWrite);
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
{if (y<20) {j=0; while ((namei[j] != '\0') && (j<30))
{name[y][j]=namei[j]; j++;} name[y][j]='\0'; store[y]=DmGetDatabase(0,i);
array[y]=(CharPtr) &name[y][0]; y++;}}}
LstSetListChoices(list,&array[0],(y)); listItem=LstGetSelectionText(list,0);
if (!listItem) {listItem="                       "; alien=1;}
// too many replays of ripley in action i guess.
popup=(ControlPtr)FrmGetObjectPtr(form,FrmGetObjectIndex(form,listID_popuplist));
CategorySetTriggerLabel(popup,listItem); break;
				}
			}
		FrmDispatchEvent(&event);
		} while ( event.eType != appStopEvent );
	}

// patch..unnecessary but couldnt be bothered to unpatch it.
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

		case ctlSelectEvent:     // A control button was pressed and released.
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
FrmReturnToForm(formID_zdoc);
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

// original event handler

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
		case ctlSelectEvent:     // A control button was pressed and released.
			if ( event->data.ctlEnter.controlID== buttonID_zdoc )
				{
	    FrmAlert (alertID_about);
				handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_open )
				{
// more replays. must be getting to me.
if (alien==0)
{
if (open == 0)
{ open=1;
listxtem=(int) LstGetSelection(list);
dbR=DmOpenDatabase(0,store[listxtem],dmModeReadOnly);
maxrec=DmNumRecords(dbR);
rechandle=DmQueryRecord(dbR,maxrec-maxrec);
RecPointer=MemHandleLock(rechandle);
MemMove(&DOC_Values, RecPointer, 14);
DmReleaseRecord(dbR,maxrec-maxrec,(Boolean) false);
i=0;
MemHandleUnlock(rechandle);
// MemHandleFree(rechandle);
i=1;
loadrec(i);
uncompsizeoftext=uncompsize;
recptr=0;
rom=(VoidHand) DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text,uncompsizeoftext);
garbage=FldGetTextHandle(field);
if (garbage != 0) {MemHandleFree((VoidHand) garbage);}
FldCompactText(field);
FldFreeMemory(field);
DmReleaseRecord(RAM,0,(Boolean) true);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
FldSetTextHandle(field,(Handle) rom);
FldDrawField(field);
WinDrawChars((CharPtr) "�",1,150,0);
MemHandleUnlock(rom);
}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_up )
				{
				if(open==1){
				if (currentrec>1){
                                FldSetUsable(field,false);
				currentrec--;
// crude but crashable. do NOT use.
// garbage=FldGetTextHandle(field);
// if (garbage != 0) {MemHandleFree((VoidHand) garbage);}
DmRemoveRecord(RAM,0);
				loadrec(currentrec);
// FldCompactText(field);
// FldFreeMemory(field);
// MemHandleFree(rom);
uncompsizeoftext=uncompsize;
// error!
// DmDeleteRecord(RAM,recptr);
// error!
recptr=0;
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
// MemHandleUnlock(rom);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
FldSetTextHandle(field,(Handle) rom);
FldDrawField(field);
WinDrawChars((CharPtr) "�",1,150,0);
MemHandleUnlock(rom);
// MemHandleFree(rom);
// FldCompactText(field);
				}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_down )
				{
				if(open==1){
				if (currentrec<DOC_Values.count){
                                FldSetUsable(field,false);
				currentrec++;
DmRemoveRecord(RAM,0);
				loadrec(currentrec);
uncompsizeoftext=uncompsize;
recptr=0;
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
FldSetTextHandle(field,(Handle) rom);
FldDrawField(field);
WinDrawChars((CharPtr) "�",1,150,0);
MemHandleUnlock(rom);
				}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_save )
				{
if (open == 1){
FldScrollField(field,25,up); // get to top of field -- patch which failed.
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
DmRemoveRecord(RAM,0);
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
MemHandleUnlock(rom);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
MemMove(&DOC_Text.text[0], romx, uncompsize);
if (DOC_Values.version==1)
{spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) uncompsize);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0], uncompsize);
WinDrawChars((CharPtr) "�",1,150,0);}
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
WinDrawChars((CharPtr) "�",1,150,0);}
if (DOC_Values.version==2)
{WinDrawChars((CharPtr) "�",1,150,0);
i=saverec(0);
DOC_Text.text[i+1]='\0';
spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) i);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0],(ULong) i);
WinDrawChars((CharPtr) "�",1,150,0);}
DmReleaseRecord(RAM,0,(Boolean) true);
DmReleaseRecord(dbW,spare,(Boolean) true);
MemHandleUnlock(rom);
// MemHandleUnlock(rechandle);
DmCloseDatabase(dbW);
// reload record.
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
FldSetTextHandle(field,(Handle) rom);
FldDrawField(field);
// trying too hard. let PalmOS handle it.
// MemHandleUnlock(rom);
// MemHandleFree(rom);
// FldCompactText(field);
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
DmCloseDatabase(RAM);
DmDeleteDatabase(0,DmFindDatabase(0,"ZRAM"));
DmCloseDatabase(dbR);
open=0;
LstEraseList(list);
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
// MemHandleUnlock(rechandle);
DmCloseDatabase(dbW);}
                                        break;
                                case menuitemID_delete:
if (open==0){ listxtem=(int) LstGetSelection(list);
DmDeleteDatabase(0,store[listxtem]);}
                                        break;
                                case menuitemID_copy:
if (open==1)
{FldCopy(field);}
                                break;
                                case menuitemID_paste:
if (open==1)
{FldPaste(field);}
                                break;
                                case menuitemID_rx:
SysLibFind("Serial Library",&cowbunga);
// crude 4K recieve function -- anyone got any decent hardware to improve it ?
SerOpen(cowbunga,0,9600);
WinDrawChars((CharPtr) "�",1,150,0);
if (open == 1)
{ 
FldSetUsable(field,false);
spare=(UInt) currentrec;
dbW=DmOpenDatabase(0,store[listxtem],dmModeReadWrite);
DmRemoveRecord(dbW,spare);
WinDrawChars((CharPtr) "RECV 9600 �",11,40,40);
SerReceive10(cowbunga,&DOC_Text.text,4096,6000);
WinDrawChars((CharPtr) "RECV 9600 �",11,40,40);
recptr=0;
DmRemoveRecord(RAM,0);
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
MemHandleUnlock(rom);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
MemMove(&DOC_Text.text[0], romx, uncompsize);
if (DOC_Values.version==1)
{spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) uncompsize);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0], uncompsize);
WinDrawChars((CharPtr) "�",1,150,0);}
if (DOC_Values.version==2)
{WinDrawChars((CharPtr) "�",1,150,0);
i=saverec(0);
DOC_Text.text[i+1]='\0';
spare=(UInt) currentrec;
rechandle=DmNewRecord(dbW,&spare, (ULong) i);
spare=(UInt) currentrec;
RecPointer=MemHandleLock(rechandle);
DmWrite(RecPointer,0,&DOC_Text.text[0],(ULong) i);
WinDrawChars((CharPtr) "�",1,150,0);}
DmReleaseRecord(RAM,0,(Boolean) true);
DmReleaseRecord(dbW,spare,(Boolean) true);
MemHandleUnlock(rom);
// MemHandleUnlock(rechandle);
DmCloseDatabase(dbW);
// reload record.
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
FldSetTextHandle(field,(Handle) rom);
FldDrawField(field);
MemHandleUnlock(rom);
// MemHandleFree(rom);
// FldCompactText(field);
}
SerClose(cowbunga);
WinDrawChars((CharPtr) "�",1,150,0);

                                        break;
                                case menuitemID_tx:
SysLibFind("Serial Library",&cowbunga);
// dump it all out in one go. anyone got any hardware for improvement ?
SerOpen(cowbunga,0,9600);
WinDrawChars((CharPtr) "�",1,150,0);
if (open == 0)
{ listxtem=(int) LstGetSelection(list);
dbR=DmOpenDatabase(0,store[listxtem],dmModeReadOnly);
maxrec=DmNumRecords(dbR);
rechandle=DmQueryRecord(dbR,maxrec-maxrec);
RecPointer=MemHandleLock(rechandle);
MemMove(&DOC_Values, RecPointer, 14);
DmReleaseRecord(dbR,maxrec-maxrec,(Boolean) false);
MemHandleUnlock(rechandle);
for(i=1;i<DOC_Values.count+1;i++)
{loadrec(i);
WinDrawChars((CharPtr) "XMIT 9600 �",11,40,40);
SerSend10(cowbunga,&DOC_Text.text,4096);
WinDrawChars((CharPtr) "XMIT 9600 �",11,40,40);}}
SerClose(cowbunga);
WinDrawChars((CharPtr) "�",1,150,0);
                                        break;
	    }
	 handled = 1;
			break;

		case nilEvent:
			handled = 1;
			break;
		}
	return handled;
}

// Thats all folks !
// .EOF.
