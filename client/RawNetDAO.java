package com.apo.net.client;
/********************************************************************
* @(#)RawNetDAO.java 1.00 20110208
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* RawNetDAO: An implementation of RawDAO that accesses the Raw
* contact information over the network via the app serverver.
*
* @author Rick Salamone
* @version 1.00, 20110208 rts initial version
*******************************************************/
import com.apo.contact.Raw;
import com.apo.contact.RawDAO;
import com.apo.contact.Source;
import com.apo.contact.touch.TouchCode;
import com.shanebow.dao.*;
import com.apo.net.*;
import com.apo.net.client.Client;
import com.shanebow.util.CSV;
import java.util.List;
import java.util.Vector;

public final class RawNetDAO
	extends RawDAO
	{
	private final com.apo.net.client.Client fConnection;

	public RawNetDAO( Client aConnection )
		{
		fConnection = aConnection;
		}

	@Override public void shutdown()
		{
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

	@Override public final long getServerTime()
		{
		return fConnection.getServerTime();
		}

	@Override public final void keepAlive()
		throws DataFieldException
		{
		fConnection.send(ClientOp.SYSCMD, "" + ClientOp.SC_KEEPALIVE, false);
		}

	@Override public ContactID addLead( Raw aRaw, EmpID aEmpID,
		TouchCode aTouchCode, String aSource )
		throws DataFieldException, DuplicateException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aRaw.toCSV()).append(Message.SEP);
		ladding.append(aEmpID.csvRepresentation()).append(Message.SEP);
		ladding.append(aTouchCode.csvRepresentation()).append(Message.SEP);
		ladding.append(aSource);
		Message reply = fConnection.send(ClientOp.ADDLEAD, ladding.toString());
		if ( reply.err() == Snafu.CODE_DUPLICATE )
			throw new DuplicateException(reply.data());
		checkForError(reply); // throw an exception for other problems
		return ContactID.parse(reply.data());
		}

	@Override public void addRaw( Raw aRaw, EmpID aEmpID,
		TouchCode aTouchCode, String aSource )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aRaw.toCSV()).append(Message.SEP);
		ladding.append(aEmpID.csvRepresentation()).append(Message.SEP);
		ladding.append(aTouchCode.csvRepresentation()).append(Message.SEP);
		ladding.append(aSource);
		fConnection.send(ClientOp.ADDLEAD, ladding.toString(), false);
		}

	@Override public void assign( Source aTo, Source aBy, String aCsvRawIDs )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aTo.dbRepresentation()).append(Message.SEP);
		ladding.append(aBy.dbRepresentation()).append(Message.SEP);
		ladding.append(aCsvRawIDs);
		Message reply = fConnection.send(ClientOp.ASSIGN, ladding.toString());
		checkForError(reply);
		}

	@Override public int countWork(String aCriteria)
		throws DataFieldException
		{
		Message reply = fConnection.send(ClientOp.CNTWORK, aCriteria);
		checkForError(reply);
		try { return Integer.parseInt(reply.data()); }
		catch (Exception e) { throw new DataFieldException(e.getMessage()); }
		}

	@Override public boolean supportsDelete() { return false; }
	@Override public boolean delete( ContactID id )
		throws DataFieldException
		{
		logSeparate( "Delete Contact #" + id );
		throw new DataFieldException("Delete Contact: Access Denied");
		}

	@Override public Raw getWork(String aCriteria)
		throws DataFieldException
		{
		Message reply = fConnection.send(ClientOp.GETWORK, aCriteria);
		Snafu snafu = reply.getReplySnafu();
		if ( snafu == Snafu.NOT_FOUND )
			return null;
		checkForError(reply); // throw an exception for other problems
		return new Raw(reply.data());
		}

	@Override public void reqEmail(ContactID aRawID, EMailAddress aEmail)
		throws DataFieldException
		{
		String csv = aRawID.toString() + Message.SEP + aEmail.toString();
		Message reply = fConnection.send(ClientOp.EMAIL, csv);
		checkForError(reply);
		}

	@Override public void mailReq(Comment aDesc, When aTime, EmpID aEmpID, ContactID aRawID)
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aDesc.toString()).append(Message.SEP);
		ladding.append(aTime.csvRepresentation()).append(Message.SEP);
		ladding.append(aEmpID.csvRepresentation()).append(Message.SEP);
		ladding.append(aRawID.csvRepresentation());
		Message reply = fConnection.send(ClientOp.BROCHURE, ladding.toString());
		checkForError(reply);
		}

	@Override public void sentMail( Comment aDesc, When aWhenSent, Source aSentBy,
	                                boolean aScheduleCall, String aIdCsvSentTo )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aDesc.toString()).append(Message.SEP);
		ladding.append(aWhenSent.csvRepresentation()).append(Message.SEP);
		ladding.append(aSentBy.dbRepresentation()).append(Message.SEP);
		ladding.append(aScheduleCall?"1":"0").append(Message.SEP);
		ladding.append(aIdCsvSentTo);
		Message reply = fConnection.send(ClientOp.MAILSENT, ladding.toString());
		checkForError(reply);
		}

	@Override public void release( Raw raw )
		throws DataFieldException
		{
		String ladding = (raw == null)? "" : raw.id().dbRepresentation();
		Message reply = fConnection.send(ClientOp.CANWORK, ladding );
		checkForError(reply);
		}

	@Override public void update( Raw aRaw, boolean aReleaseLock,
		TouchCode aTouchCode, String touchDetails, long when, short uID )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aTouchCode.csvRepresentation()).append(Message.SEP);
		ladding.append(touchDetails).append(Message.SEP);
		ladding.append(aReleaseLock? "1" : "0").append(Message.SEP);
		ladding.append(uID).append(Message.SEP);
		ladding.append(aRaw.toCSV());
		fConnection.send(ClientOp.MODLEAD, ladding.toString(), false);
		}

	@Override public Raw fetch(ContactID id)
		throws DataFieldException
		{
		if ( id == null )
			return null;
		Message reply = fConnection.send(ClientOp.GETLEAD, id.toString());
		checkForError(reply);
		return new Raw(reply.data());
		}

	@Override public final void checkOut(List<Raw> aList, int aMaxRecords, int aPerPage,
		String aWhereClause, short uid)
		throws DataFieldException
		{
		logSeparate( "Check Out " + aMaxRecords + " contacts "
			          + aPerPage + " per page,\n" + aWhereClause );

		String ladding = "" + ClientOp.SC_CHECKOUT + Message.SEP
								+ aMaxRecords + Message.SEP
		               + aPerPage + Message.SEP
		               + uid + Message.SEP
		              + aWhereClause;
		Message reply = fConnection.send(ClientOp.SYSCMD, ladding);
		receive( aList, reply );
		}

	@Override public final void fetch(List<Raw> aList, int aMaxRecords, String aQuery)
		throws DataFieldException
		{
		logSeparate( "Execute SQL: " + aQuery );

		String options = "" + aMaxRecords + Message.SEP
		               + "0" + Message.SEP  // no headers
		               + "0" + Message.SEP; // don't lock records
		Message reply = fConnection.send(ClientOp.QUERY, options + aQuery);
		receive( aList, reply );
		}

	private void receive(List<Raw> aList, Message reply)
		throws DataFieldException
		{
		byte opCode;
		int parseErrors = 0;
		do
			{
			checkForError(reply);
			opCode = reply.op();
			try
				{
				String[] pieces = CSV.split( reply.data(), 2 );
				int nRows = Integer.parseInt(pieces[0]);
				int nCols = Integer.parseInt(pieces[1]);
				int expectedPieces = 2 // nRows & nCols
                + nRows * nCols; // the field data
				pieces = CSV.split( reply.data(), expectedPieces );
System.out.format("opCode %02X parse %d rows, %d cols: expected %d pieces, found %d\n",
//			log("opCode %02X parse %d rows, %d cols: expected %d pieces, found %d",
 			(int)opCode, nRows, nCols, expectedPieces, pieces.length );
				int currentPiece = 1;

				// Get all rows.
				for ( int r = 0; r < nRows; r++ )
					{
					String[] rowData = new String[nCols];
					for ( int c = 0; c < nCols; c++ )
						rowData[c] = pieces[++currentPiece];
					try { aList.add(new Raw(rowData)); }
					catch(Exception e) { ++parseErrors; logError(e.toString());}
					}
				if ( opCode == ClientOp.CODE_PARTIAL )
					reply = fConnection.waitForReply();
				}
			catch (Exception ex)
				{
				String msg = ex.toString();
				logError(msg);
				if (ex instanceof DataFieldException)
					throw (DataFieldException)ex;
				else throw new DataFieldException(msg);
				}
			}
		while ( opCode == ClientOp.CODE_PARTIAL );
		if ( parseErrors > 0 )
			logError( "done with " + parseErrors + " bad data fields" );
		else
			logSuccess();
		}

	@Override public long nextCheckOutPage()
		throws DataFieldException
		{
		String ladding = "" + ClientOp.SC_NEXT_CHECKOUT_PAGE;
		Message reply = fConnection.send(ClientOp.SYSCMD, ladding);
		checkForError(reply);
		try { return Long.parseLong(reply.data()); }
		catch (Exception e) { throw new DataFieldException(e.getMessage()); }
		}
	}
