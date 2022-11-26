package com.apo.net.client;
/********************************************************************
* @(#)OrderDAO.java 1.00 20110208
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* OrderNetDAO: An interface that defines networkIO methods for orders.
*
* @author Rick Salamone
* @version 1.00, 20110208 rts initial version
*******************************************************/
import com.apo.contact.touch.TouchCode;
import com.apo.order.Order;
import com.apo.order.OrderDAO;
import com.shanebow.dao.*;
import com.shanebow.util.CSV;
import com.shanebow.ui.SBDialog;
import com.apo.net.*;
import java.util.List;

public final class OrderNetDAO
	extends OrderDAO
	{
	private final Client fConnection;

	public OrderNetDAO(Client aConnection)
		{
		fConnection = aConnection;
		}

	@Override public final When getServerTime()
		{
		return new When(fConnection.getServerTime());
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

	@Override public final OrderID add(Order order)
		throws DataFieldException
		{
		logSeparate( "add: " + order );
		Message reply = fConnection.send(ClientOp.NEWORDER, order.toCSV());
		checkForError(reply);
		return OrderID.parse(reply.data());
		}

	@Override public final void delete(Order order)
		throws DataFieldException
		{
		throw new DataFieldException("Delete order not supported");
		}

	@Override public final void update( Order aOrder, When aTime, EmpID aEmpID,
		TouchCode aTouchCode, Comment aComment )
		throws DataFieldException
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aOrder.toCSV()).append(Message.SEP);
		ladding.append(aTime.csvRepresentation()).append(Message.SEP);
		ladding.append(aEmpID.csvRepresentation()).append(Message.SEP);
		ladding.append(aTouchCode.csvRepresentation()).append(Message.SEP);
		ladding.append(aComment.toString());
		Message reply = fConnection.send(ClientOp.ORDMOD, ladding.toString());
		checkForError(reply);
		}

	public final boolean fetch(List<Order> aList, ContactID aID)
		{
		String query = "SELECT * FROM " + Order.DB_TABLE
		             + " WHERE rawID=" + aID.dbRepresentation()
		             + " ORDER by id DESC";
		return fetch( aList, -1, query);
		}

	public final boolean fetch(List<Order> aList, int aMaxRecords, String aQuery)
		{
		logSeparate( "Execute SQL: " + aQuery );
		String options = "" + aMaxRecords + Message.SEP
		               + "0" + Message.SEP  // no headers
		               + "0" + Message.SEP; // don't lock records
		Message reply = fConnection.send(ClientOp.QUERY, options + aQuery);
		byte opCode;
		int errors = 0; // number of errors encountered
		do
			{
			if ( reply.hasSnafu())
				{
				SBDialog.error( "Error: " + reply.getSnafu().toString(), reply.data());
				return logError(reply.getSnafu().toString());
				}
			if ( reply.err() != 0 )
				{
				SBDialog.error( "Error: " + com.apo.net.Snafu.find(reply.err()).toString(), reply.data());
				return logError(com.apo.net.Snafu.find(reply.err()).toString());
				}
			opCode = reply.op();
			int currentPiece = 0; // which csv value is being processed
			String[] pieces = CSV.split( reply.data(), 2 );
			int nRows = Integer.parseInt(pieces[currentPiece++]);
			int nCols = Integer.parseInt(pieces[currentPiece++]);
			int expectedPieces = 2 + nRows * nCols; // #rows, #cols & the field data

			pieces = CSV.split( reply.data(), expectedPieces );
			//	log("parse %d rows, %d cols: expected %d pieces, found %d",
			//	nRows, nCols, expectedPieces, pieces.length );

			String[] rowData = new String[nCols];
			for ( int r = 0; r < nRows; r++ )        // For each row in the result
				{                                      // set fill the rowData
				for ( int c = 0; c < nCols; c++ )      // String array with column
					rowData[c] = pieces[currentPiece++]; // values used to construct the
				fixUSD(rowData, Order.COST, Order.COMM, Order.FEES );
				try { aList.add(new Order(rowData)); } // Order object that is added to list
				catch (Exception e) { logError( e.toString()); ++errors; }
				}
try {
			if ( opCode == ClientOp.CODE_PARTIAL )
				reply = fConnection.waitForReply();
}
catch (Exception e) { return logError("Fetch ERROR: " + e.toString()); }
			}
		while ( opCode == ClientOp.CODE_PARTIAL );
		return (errors == 0) ? logSuccess() : logError( "" + errors + " ERRORS!!" );
		}

	private void fixUSD(String[] rowData, int... usdFields )
		{
		for ( int field : usdFields )
			rowData[field] = centsToDollars(rowData[field]);
		}

	private String centsToDollars( String cents )
		{
		int length = cents.length();
		switch ( length )
			{
			case 0: return cents;
			case 1: return ".0" + cents;
			case 2: return "." + cents;
			default: return cents.substring( 0, length - 2 )
			             + "." + cents.substring( length - 2 );
			}
		}

	public final boolean supportsDelete()
		{
		return false;
		}
	}
