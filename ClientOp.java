package com.apo.net;
/********************************************************************
* @(#)ClientOp.java	1.00 10/06/30
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* ClientOp: The various operations a client can ask the server to
* perform.
*
* @author Rick Salamone
* @version 1.00, 20100630 rts created
* @version 1.01, 20100706 rts Modified to extend HexCode
* @version 1.02, 20101020 rts added COMMENT op code
* @version 1.03, 20101022 rts replaced COMMENT with ADDHIST op code
* @version 1.04, 20101023 rts added GETHIST op code
* @version 1.05, 20101025 rts renamed several op codes & added MODLEAD
* @version 1.06, 20101026 rts added QUERY op code
* @version 1.07, 20101104 rts added ECHO op code
* @version 1.08, 20101111 rts added CNTWORK, EMAIL, ASSIGN op codes
*******************************************************/
import com.shanebow.util.SBArray;
public final class ClientOp extends HexCode
	{
	private static final SBArray<ClientOp> _all = new SBArray<ClientOp>(25);
	public static final byte CODE_LOGIN    = 0x00;
	public static final byte CODE_LOGOUT   = 0x01;
	public static final byte CODE_GETWORK  = 0x03; // returns a raw record and locks it to user
	public static final byte CODE_ADDLEAD  = 0x04; // add a new verified lead to the data base
	public static final byte CODE_CANWORK  = 0x05;
	public static final byte CODE_ADDHIST  = 0x06;
	public static final byte CODE_GETHIST  = 0x07; // fetch the touch history for a contact
	public static final byte CODE_MODLEAD  = 0x08; // modify fields in the raw contact record
	public static final byte CODE_GETLEAD  = 0x09; // retrieve a specific contact record
	public static final byte CODE_QUERY    = 0x0A; // aribitrary SQL select
	public static final byte CODE_UPDATE   = 0x0B; // aribitrary SQL update
	public static final byte CODE_EMAIL    = 0x0C; // SQL Count request
	public static final byte CODE_CNTWORK  = 0x0D; // SQL Count request
	public static final byte CODE_ASSIGN   = 0x0E; // SQL Count request
	public static final byte CODE_BROCHURE = 0x0F; // brochure request
	public static final byte CODE_NEWORDER = 0x10; // new client order
	public static final byte CODE_ORDMOD   = 0x11; // modify client order
	public static final byte CODE_GETORDER = 0x12; // modify client order
	public static final byte CODE_MAILSENT = 0x14; // mail sent to contacts
	public static final byte CODE_MAILBAD  = 0x15; // mail returned as undeliverable
	public static final byte CODE_PARTIAL  = 0x16; // partial results from QUERY
	public static final byte CODE_COUNT    = 0x17; // aribitrary SQL count
	public static final byte CODE_ECHO     = 0x77; // echo service
	public static final byte CODE_NEKO     = 0x78; // Hide application
	public static final byte CODE_BROADCAST= 0x79; // broadcast service
	public static final byte CODE_SYSCMD   = 0x7A; // System command

	public static final ClientOp LOGIN    = new ClientOp(CODE_LOGIN,    "Login");
	public static final ClientOp LOGOUT   = new ClientOp(CODE_LOGOUT,   "Logout");
	public static final ClientOp GETWORK  = new ClientOp(CODE_GETWORK,  "Fetch Scheduled Contact");
	public static final ClientOp ADDLEAD  = new ClientOp(CODE_ADDLEAD,  "Add Raw Contact");
	public static final ClientOp CANWORK  = new ClientOp(CODE_CANWORK,  "Cancel Current Work");
	public static final ClientOp ADDHIST  = new ClientOp(CODE_ADDHIST,  "Add History");
	public static final ClientOp GETHIST  = new ClientOp(CODE_GETHIST,  "Get History");
	public static final ClientOp MODLEAD  = new ClientOp(CODE_MODLEAD,  "Modify Contact");
	public static final ClientOp GETLEAD  = new ClientOp(CODE_GETLEAD,  "Fetch Specific Contact");
	public static final ClientOp QUERY    = new ClientOp(CODE_QUERY,    "Database Query");
	public static final ClientOp UPDATE   = new ClientOp(CODE_UPDATE,   "Database Update");
	public static final ClientOp EMAIL    = new ClientOp(CODE_EMAIL,    "Send eMail");
	public static final ClientOp CNTWORK  = new ClientOp(CODE_CNTWORK,  "Count Contacts");
	public static final ClientOp ASSIGN   = new ClientOp(CODE_ASSIGN,   "Assign Work");
	public static final ClientOp BROCHURE = new ClientOp(CODE_BROCHURE, "Request brochure");
	public static final ClientOp NEWORDER = new ClientOp(CODE_NEWORDER, "Create Order");
	public static final ClientOp ORDMOD   = new ClientOp(CODE_ORDMOD,   "Modify Order");
	public static final ClientOp GETORDER = new ClientOp(CODE_GETORDER, "Fetch Orders");
	public static final ClientOp MAILSENT = new ClientOp(CODE_MAILSENT, "Mail Sent");
	public static final ClientOp MAILBAD  = new ClientOp(CODE_MAILBAD,  "Mail Undeliverable");
	public static final ClientOp PARTIAL  = new ClientOp(CODE_PARTIAL,  "Partial results");
	public static final ClientOp COUNT    = new ClientOp(CODE_COUNT,    "Database Count");
	public static final ClientOp ECHO     = new ClientOp(CODE_ECHO,     "Echo");
	public static final ClientOp NEKO     = new ClientOp(CODE_NEKO,     "Neko");
	public static final ClientOp BROADCAST= new ClientOp(CODE_BROADCAST,"Broadcast");
	public static final ClientOp SYSCMD   = new ClientOp(CODE_SYSCMD,   "System Command");
		public static final byte SC_USR_LIST = (byte)0;
		public static final byte SC_ROLE_GET = (byte)1;
		public static final byte SC_ROLE_SET = (byte)2;
		public static final byte SC_KEEPALIVE = (byte)3;
		public static final byte SC_STATS_GET = (byte)4;
		public static final byte SC_STATS_RESET = (byte)5;
		public static final byte SC_CRITERIA_GET = (byte)6;
		public static final byte SC_CRITERIA_SET = (byte)7;
		public static final byte SC_CHECKOUT = (byte)8;
		public static final byte SC_NEXT_CHECKOUT_PAGE = (byte)9;
		public static final byte SC_PURGE_WORKQ = (byte)10;

	public static final Iterable<ClientOp> getAll()  { return _all; }

	public static ClientOp find( byte code )
		{
		for ( ClientOp op : _all )
			if ( op.code() == code )
				return op;
		return null;
		}

	private ClientOp( byte code, String desc )
		{
		super(code,desc);
		_all.add( this );
		}
	}
