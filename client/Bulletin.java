package com.apo.net.client;
/********************************************************************
* @(#)Bulletin.java 1.00 20110311
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* Bulletin: Model object of a user announcement. Bulletins are
* displayed to users based on their role and location. For example,
* there might be a bulletin announcing new office hours displayed
* to all users in a particular office. Bulletins have an expiration
* date when they are automatically purged from the database by the
* server.
*
* @author Rick Salamone
* @version 1.00 20110311 rts created
*******************************************************/
import com.apo.contact.Source;
import com.shanebow.dao.*;
import com.shanebow.util.CSV;

public final class Bulletin
	{
	public static final String DB_TABLE = "nitellub";
//	public static BulletinDAO DAO;

	private final Comment    fMessage;
	private final Source   fAuthor;
	private final When       fExpiration;

	public Bulletin( Comment aMessage, Source aAuthor, When aExpiration )
		{
		fMessage = aMessage;
		fAuthor = aAuthor;
		fExpiration = aExpiration;
		}

	public String toCSV()
		{
		return  fAuthor.csvRepresentation()
		+ "," + fMessage.csvRepresentation()
		+ "," + fExpiration.csvRepresentation();
		}

	public final Comment  message() { return fMessage; }
	public final Source author() { return fAuthor; }
	public final When     expiration() { return fExpiration; }

	@Override public String toString()
		{
		return "Posted by " + fAuthor + "\n" + fMessage;
		}
	}
