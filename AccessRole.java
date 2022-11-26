package com.apo.net;
/********************************************************************
* @(#)Access.java	1.00 10/07/06
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Access: IP adresses, ports, and permisission flag definitions for
* users of the APO system
*
* @version 1.00 07/06/10
* @author Rick Salamone
* 20100706 RTS Created from User.java
* 20100802 RTS added parseCmdLine()
* 20100927 RTS added shortened names of privledges, added NONE
* 20100927 RTS added office names
* 20101018 RTS added cmd line args -mode and x509
*******************************************************/
import com.apo.net.Access;
import com.apo.employee.Role;

public final class AccessRole
	{
	private String fCode;
	private long fAccess;

	public AccessRole(String aCode, long aAccess)
		{
		fCode = aCode;
		fAccess = aAccess;
		}

	public long access() { return fAccess; }
	@Override public String toString() { return fCode; }
	}
