/*
 * ZPatch [ Ver.6.0.0a1 ]
 *
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
 */
/* This is the new ZDOC patch file with the new field handler code.
*/ 
static FieldPtr SetFieldTextFromHandle(FieldPtr fldP, Handle txtH)
  {
      Handle oldTxtH;
//          FormPtr frm = FrmGetActiveForm();
//              FieldPtr fldP;

                  // get the field and the field's current text handle.
//                      fldP = FrmGetObjectPtr(frm, FrmGetObjectIndex(frm, fieldID));
//                         ErrNonFatalDisplayIf(!fldP, "missing field");
                              oldTxtH = FldGetTextHandle(fldP);

                                  // set the field's text to the new text.
                                      FldSetTextHandle(fldP, txtH);
                                          FldDrawField(fldP);

                                              // free the handle AFTER we call FldSetTextHandle().
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
txtH= (Handle) MemHandleNew(4097);
if (!txtH)
       return NULL;
       MemMove(MemHandleLock((VoidHand) txtH),strP,4096);
       MemHandleUnlock((VoidHand) txtH);
       ClearFieldText(fieldP);
       return SetFieldTextFromHandle(fieldP,txtH);
}

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
open=1;
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
DmReleaseRecord(RAM,0,(Boolean) true);
// romhandle=(Handle) DmGetRecord(RAM,0);
delay(500);
SetFieldTextFromStr(field,&DOC_Text.text[0]);
delay(500);
FldSetUsable(field,true);
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
DmRemoveRecord(RAM,0);
loadrec(currentrec);
uncompsizeoftext=uncompsize;
// error!
// DmDeleteRecord(RAM,recptr);
// error!
MemHandleFree(rom);
recptr=0;
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
// MemHandleUnlock(rom);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
SetFieldTextFromHandle(field,(Handle) rom);
FldDrawField(field);
WinDrawChars((CharPtr) "ù",1,150,0);
// MemHandleUnlock(rom);
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
// error!
// DmDeleteRecord(RAM,recptr);
// error!
MemHandleFree(rom);
rom=DmNewRecord(RAM,&recptr,4100);
romx=MemHandleLock(rom);
DmWrite(romx,0,&DOC_Text.text[0],(ULong) uncompsize);
DmReleaseRecord(RAM,0,(Boolean) true);
// MemHandleUnlock(rom);
// MemHandleFree(rom);
// romhandle=(Handle) DmGetRecord(RAM,0);
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
SetFieldTextFromHandle(field,(Handle) rom);
FldDrawField(field);
WinDrawChars((CharPtr) "ù",1,150,0);
// MemHandleUnlock(rom);
// MemHandleFree(rom);
// FldCompactText(field);
				}}
handled = 1;
				}
			if ( event->data.ctlEnter.controlID== buttonID_save )
				{
if (open == 1){
MemHandleFree(rom);
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
DmReleaseRecord(RAM,0,(Boolean) true);
DmReleaseRecord(dbW,spare,(Boolean) true);
MemHandleUnlock(rom);
// MemHandleUnlock(rechandle);
DmCloseDatabase(dbW);
// reload record.
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
SetFieldTextFromHandle(field,(Handle) rom);
FldDrawField(field);
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
DmReleaseRecord(RAM,0,(Boolean) true);
DmReleaseRecord(dbW,spare,(Boolean) true);
MemHandleUnlock(rom);
// MemHandleUnlock(rechandle);
DmCloseDatabase(dbW);
// reload record.
rom=DmQueryRecord(RAM,0);
romx=MemHandleLock(rom);
FldSetUsable(field,true);
SetFieldTextFromHandle(field,(Handle) rom);
FldDrawField(field);
MemHandleUnlock(rom);
// MemHandleFree(rom);
// FldCompactText(field);
}
SerClose(cowbunga);
WinDrawChars((CharPtr) "ù",1,150,0);

                                        break;
                                case menuitemID_tx:
SysLibFind("Serial Library",&cowbunga);
SerOpen(cowbunga,0,9600);
WinDrawChars((CharPtr) "§",1,150,0);
if (open == 0)
{
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
	    }
	 handled = 1;
			break;

		case nilEvent:
			handled = 1;
			break;
		}
	return handled;
}

