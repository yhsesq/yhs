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

import java.io.IOException;

public interface SSHInteractor {
    public void    startNewSession(SSHClient client);
    public void    sessionStarted(SSHClient client);

    public void    connected(SSHClient client);
    public void    open(SSHClient client);
    public void    disconnected(SSHClient client, boolean graceful);

    public void    report(String msg);
    public void    alert(String msg);

    public void    propsStateChanged(SSHPropertyHandler props);

    public boolean askConfirmation(String message, boolean defAnswer);

    public boolean quietPrompts();
    public String  promptLine(String prompt, String defaultVal) throws IOException;
    public String  promptPassword(String prompt) throws IOException;

    public boolean isVerbose();
}
