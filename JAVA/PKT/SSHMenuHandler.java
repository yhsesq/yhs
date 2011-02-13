/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: josh $
 * $Date: 2001/02/03 00:47:00 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.PopupMenu;

import mindbright.application.MindTerm;
import mindbright.terminal.TerminalMenuListener;
import mindbright.terminal.TerminalWin;

public class SSHMenuHandler implements TerminalMenuListener {
    boolean havePopupMenu = false;

    public void init(MindTerm mindterm, SSHInteractiveClient client, Frame parent, TerminalWin term) {
    }
    public void update() {
    }
    public void setPopupButton(int popButtonNum) {
    }
    public void prepareMenuBar(MenuBar mb) {
    }
    public void preparePopupMenu(PopupMenu popupmenu) {
    }
    public int getPopupButton() {
	return 0;
    }
    public boolean confirmDialog(String message, boolean defAnswer) {
	return false;
    }
    public void alertDialog(String message) {
    }
    public void textDialog(String head, String text, int rows, int cols, boolean scrollbar) {
    }
}
