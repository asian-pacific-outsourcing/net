package com.apo.net;
/********************************************************************
* @(#)Snafu.java	1.00 10/07/04
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Snafu: The various problem codes the server can send in response to
* a client request.
*
* @version 1.00 07/04/10
* @author Rick Salamone
* 20100704 RTS Created
* 20101111 RTS added code for no data found
*******************************************************/
import com.shanebow.util.SBArray;

public final class Snafu extends HexCode
	{
	private static final SBArray<Snafu> _all = new SBArray<Snafu>(1);
	public static final byte CODE_NONE        = 0x00; // No snafu
	public static final byte CODE_OK        = CODE_NONE; // No snafu
	public static final byte CODE_READ_ERROR = 0x01;
	public static final byte CODE_BAD_VERSION = 0x02;
	public static final byte CODE_BAD_CHECKSUM = 0x03;
	public static final byte CODE_BAD_LENGTH = 0x04;
	public static final byte CODE_ACCESS_DENIED = 0x05;
	public static final byte CODE_BAD_OP = 0x06;
	public static final byte CODE_BAD_CSV_COUNT = 0x07;
	public static final byte CODE_PARSE_ERROR = 0x08;
	public static final byte CODE_BAD_LOGIN_NAME = 0x09;
	public static final byte CODE_BAD_PASSWORD = 0x0A;
	public static final byte CODE_WRONG_EOT = 0x0B;
	public static final byte CODE_SQL_ERROR = 0x0C;
	public static final byte CODE_LOCKED_LEAD = 0x0D;
	public static final byte CODE_NOT_FOUND = 0x0E;
	public static final byte CODE_DUPLICATE = 0x0F;
	public static final byte CODE_ALREADY_IN = 0x10;

	public static final Snafu NONE           = new Snafu( CODE_NONE, "No problem");
	public static final Snafu READ_ERROR     = new Snafu( CODE_READ_ERROR, "Read error");
	public static final Snafu BAD_VERSION    = new Snafu( CODE_BAD_VERSION, "Unsupported Version: please upgrade");
	public static final Snafu BAD_CHECKSUM   = new Snafu( CODE_BAD_CHECKSUM, "Bad Checksum");
	public static final Snafu BAD_LENGTH     = new Snafu( CODE_BAD_LENGTH, "Data length mismatch");
	public static final Snafu ACCESS_DENIED  = new Snafu( CODE_ACCESS_DENIED, "Access Denied");
	public static final Snafu BAD_OP         = new Snafu( CODE_BAD_OP, "Unrecognized Operation");
	public static final Snafu BAD_CSV_COUNT  = new Snafu( CODE_BAD_CSV_COUNT, "Wrong number of columns");
	public static final Snafu PARSE_ERROR    = new Snafu( CODE_PARSE_ERROR, "Error parsing numeric value");
	public static final Snafu BAD_LOGIN_NAME = new Snafu( CODE_BAD_LOGIN_NAME, "Invalid login name");
	public static final Snafu BAD_PASSWORD   = new Snafu( CODE_BAD_PASSWORD, "Incorrect password");
	public static final Snafu WRONG_EOT      = new Snafu( CODE_WRONG_EOT, "Wrong EOT");
	public static final Snafu SQL_ERROR      = new Snafu( CODE_SQL_ERROR, "Database Error");
	public static final Snafu LOCKED_LEAD    = new Snafu( CODE_LOCKED_LEAD,
																			"Contact record is locked by another user");
	public static final Snafu NOT_FOUND    = new Snafu( CODE_NOT_FOUND, "No data found that matches your request");
	public static final Snafu DUPLICATE    = new Snafu( CODE_DUPLICATE, "Attempt to add duplicate record");
	public static final Snafu ALREADY_IN   = new Snafu( CODE_ALREADY_IN, "Only one login allowed per user");
/**********
	public  static final int countAll() { return _all.capacity(); }
	public  static final Iterable<Snafu> getAll()  { return _all; }
**********/

	public static Snafu find( byte code )
	//	throws ClientException
		{
		for ( Snafu op : _all )
			if ( op.code() == code )
				return op;
		return null;
		}

	private Snafu( byte code, String desc )
		{
		super( code, desc );
		_all.add( this );
		}
	}
