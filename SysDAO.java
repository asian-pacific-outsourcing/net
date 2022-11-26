package com.apo.net;
/********************************************************************
* @(#)SysDAO.java 1.00 20110208
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* SysDAO: An interface that defines IO methods for Raw contact information.
* Concrete implementations should be written to access data directly from
* the database, over the network, or via a file.
*
* @author Rick Salamone
* @version 1.00, 20110208 rts initial version
*******************************************************/
import com.shanebow.dao.*;
import com.apo.employee.*;
import com.shanebow.util.SBLog;
import java.util.List;

public abstract class SysDAO
	{
	private static SysDAO _theSysDAO;
	public static final SysDAO DAO() { return _theSysDAO; }

	protected SysDAO() { _theSysDAO = this; }

	public abstract long getServerTime();

	/**
	* Intended to be called at app exit - Make a best effort
	* to close db/net/file connections.
	*/
	public abstract void shutdown();
	public abstract void purgeWorkQueue( long aAccess )
		throws DataFieldException;
	public abstract long sqlCount( String aTables, String aWhereClause )
		throws DataFieldException;
	public abstract int sqlUpdate( String aSQLStatement )
		throws DataFieldException;
	public abstract Message syscmd( byte cmd, String... pieces )
		throws DataFieldException;
	public abstract Role getRole( long aAccess )
		throws DataFieldException;
	public abstract void setRole( Role aRole )
		throws DataFieldException;

	// Logging support
	private final String MODULE = getClass().getSimpleName();
	private static final String SEPARATOR="==================================================";
	protected String lastError = "";

	public final String getLastError() { return lastError; }

	protected final void log( String fmt, Object... args )
		{
		SBLog.write( MODULE, String.format(fmt, args));
		}

	protected final void logSeparate( String msg )
		{
		SBLog.write( SEPARATOR );
		SBLog.write( MODULE, msg );
		}

	protected final boolean logError( String msg )
		{
		java.awt.Toolkit.getDefaultToolkit().beep();
		lastError = msg;
		SBLog.error(MODULE + " ERROR", msg );
		return false;
		}

	protected final boolean logSuccess()
		{
		lastError = "";
		SBLog.write(MODULE, "Success" );
		return true;
		}
	}
