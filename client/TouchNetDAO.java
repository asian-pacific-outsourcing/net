package com.apo.net.client;
/********************************************************************
* @(#)TouchNetDAO.java 1.00 20110218
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* TouchNetDAO: An implementation of RawDAO that accesses the Touch contact
* information over the network via the app serverver.
*
* @author Rick Salamone
* @version 1.00, 20110218 rts initial version
*******************************************************/
import com.apo.contact.touch.Touch;
import com.apo.contact.touch.TouchCode;
import com.apo.contact.touch.TouchDAO;
import com.shanebow.dao.ContactID;
import com.shanebow.dao.DataFieldException;
import com.apo.net.*;
import com.apo.net.client.Client;
import com.shanebow.ui.SBDialog;
import com.shanebow.util.SBLog;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public final class TouchNetDAO
	extends TouchDAO
	{
	private final com.apo.net.client.Client fConnection;

	public TouchNetDAO( Client aConnection )
		{
		fConnection = aConnection;
		}

	@Override public final long getServerTime()
		{
		return fConnection.getServerTime();
		}

	@Override public final void add(Touch aTouch)
		throws DataFieldException
		{
		String ladding = aTouch.getTouchCode().csvRepresentation()
		           + Message.SEP + aTouch.getContactID().csvRepresentation()
		           + Message.SEP + aTouch.getDetails().toString();
		fConnection.send(ClientOp.ADDHIST, ladding, false);
		}

	@Override public final List<Touch> fetch(ContactID id)
		throws DataFieldException
		{
		logSeparate( "fetch " + id );

		Message reply = fConnection.send(ClientOp.GETHIST, id.csvRepresentation(), true );
		checkForError(reply);
		int errors = 0;
		List<Touch> list = new Vector<Touch>();
		StringTokenizer st = new StringTokenizer(reply.data(), Message.SEP);
		while (st.hasMoreTokens())
			{
			String csv = st.nextToken();
			try { list.add(new Touch(csv)); }
			catch (Exception e) { ++errors; logError(e.getMessage());}
			}
		return list;
		}

	private void checkForError(Message reply)
		throws DataFieldException
		{
		if ( reply.err() == Snafu.CODE_NONE )
			return;
		String msg = reply.data();
		logError(msg);
		throw new DataFieldException(msg);
		}
	}
