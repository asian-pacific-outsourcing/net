package com.apo.net.client;
/********************************************************************
* @(#)SysNetDAO.java 1.00 20110208
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* SysNetDAO: An implementation of SysDAO that accesses the system
* information over the network via the app server.
*
* @author Rick Salamone
* @version 1.00, 20110319 rts initial version
*******************************************************/
import com.shanebow.dao.*;
import com.apo.employee.*;
import com.apo.net.*;
import com.apo.net.client.Client;

public final class SysNetDAO
	extends SysDAO
	{
	private final Client fConnection;

	public SysNetDAO( Client aConnection )
		{
		fConnection = aConnection;
		}

	@Override public void shutdown()
		{
		}

	@Override public final long getServerTime()
		{
		return fConnection.getServerTime();
		}

	@Override public void purgeWorkQueue( long aAccess )
		throws DataFieldException
		{
		syscmd(false, ClientOp.SC_PURGE_WORKQ, "" + aAccess);
		}

	@Override public long sqlCount( String aTables, String aWhereClause )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aTables).append(Message.SEP);
		ladding.append(aWhereClause);
		Message reply = fConnection.send(ClientOp.COUNT, ladding.toString());
		checkForError(reply); // throw an exception for other problems
com.shanebow.util.SBLog.write( reply.data()
 + "=sqlCount("+aTables+" ,"+aWhereClause+")");
		return Long.parseLong(reply.data());
		}

	@Override public int sqlUpdate( String aSQLStatement )
		throws DataFieldException
		{
		Message reply = fConnection.send(ClientOp.UPDATE, aSQLStatement );
		checkForError(reply); // throw an exception for other problems
		return Integer.parseInt(reply.data());
		}

	@Override public Message syscmd( byte cmd, String... pieces )
		throws DataFieldException
		{
		return syscmd(true, cmd, pieces);
		}

	private Message syscmd( boolean wantReply, byte cmd, String... pieces )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(cmd);
		for ( String piece : pieces )
			ladding.append(Message.SEP).append(piece);
		Message reply = fConnection.send(ClientOp.SYSCMD, ladding.toString(), wantReply);
		if ( wantReply )
			{
			checkForError(reply); // throw an exception for other problems
			return reply;
			}
		else return null;
		}

	@Override public Role getRole( long aAccess )
		throws DataFieldException
		{
		Message reply = syscmd(ClientOp.SC_ROLE_GET, "" + aAccess);
		return Role.unmarshall(reply.data());
		}

	@Override public void setRole( Role aRole )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(ClientOp.SC_ROLE_SET);
		ladding.append(Message.SEP).append(aRole.access());
		ladding.append(Message.SEP).append(aRole.marshall());
System.out.println("setRole: " + ladding);
		fConnection.send(ClientOp.SYSCMD, ladding.toString(), false);
		}

/****************
	@Override public final void sqlQuery(List<String> aList, int aMaxRecords, String aQuery)
		throws DataFieldException
		{
		logSeparate( "Execute SQL: " + aQuery );

		String options = "" + aMaxRecords + Message.SEP
		               + "0" + Message.SEP  // no headers
		               + "0" + Message.SEP; // don't lock records
		Message reply = fConnection.send(ClientOp.QUERY, options + aQuery);
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
****************/

	// PRIVATE //
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
