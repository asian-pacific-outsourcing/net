package com.apo.net;
/********************************************************************
* @(#)HexCode.java	1.00 10/07/06
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* HexCode: The super class for OpCodes and ErrCodes which comprise
* the code field of a network client request or server response
* respectively.
*
* @version 1.00 07/06/10
* @author Rick Salamone
* 20100706 RTS Created
* 20100726 RTS Now uses byte for code
*******************************************************/
//import com.shanebow.util.SBArray;

public class HexCode
	{
	private byte   m_code;
	private String m_desc;

	HexCode( byte code, String desc )
		{
		m_code = code;
		m_desc = desc;
		}

	public byte   code() { return m_code; }
	public String desc() { return m_desc; }

	public String toString()
		{
		return String.format( "0x%02X(%s)", (int)m_code, m_desc );
		}
	}
