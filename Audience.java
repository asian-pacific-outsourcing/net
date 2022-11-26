package com.apo.net;
/********************************************************************
* @(#)Audience.java 1.00 20110311
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* Audience: Models a set of users targeted by a broadcast or system command.
* An audience is defined in one of three ways:
* 1) everybody,
* 2) an explict list of users,
* 3) a list of roles and offices.
*
* @author Rick Salamone
* @version 1.00 20110311 rts created
*******************************************************/
import com.shanebow.dao.DataFieldException;
import com.shanebow.dao.EmpID;
import com.apo.employee.*;
import com.shanebow.util.CSV;
import java.util.StringTokenizer;

public class Audience
	{
	public static final String SEP = ";"; // separates pieces in the ladding
	private static final int EVERYBODY = 0;
	private static final int USERS = 1;
	private static final int ROLE_OFFICE = 2;

	private final int        fEncoding;
	private final EmpID[]    fEmpIDs;
	private final Role[]     fRoles;
	private final Office[]   fOffices;

	/**
	* The inverse of csvRepresentation(), this method parses a String
	* into an Audience object. Pieces of the String are separated by
	* SEP and the first piece is the encoding. Valid formats are:
	* 1) <EVERYBODY>
	* 2) <USERS><SEP><csv user ids>
	* 3) <ROLE_OFFICE><SEP><csv role codes><SEP><csv offices codes>
	*/
	public static Audience parse( String text )
		throws DataFieldException
		{
		int encoding = -1;
		try
			{
			String[] pieces = text.split(SEP);
			encoding = Integer.parseInt(pieces[0]);
			switch ( encoding )
				{
				case EVERYBODY: return new Audience(EVERYBODY, null, null, null);
				case USERS:
					EmpID[] eids = new EmpID[CSV.columnCount(pieces[1])];
					int i = 0;
					StringTokenizer st = new StringTokenizer(pieces[1], ",");
					while (st.hasMoreTokens())
						eids[i++] = EmpID.parse(st.nextToken());
					return new Audience(USERS, eids, null, null);

				case ROLE_OFFICE:
					Role[] roles = new Role[CSV.columnCount(pieces[1])];
					i = 0;
					st = new StringTokenizer(pieces[1], ",");
					while (st.hasMoreTokens())
						roles[i++] = Role.parse(st.nextToken());
					Office[] offices = new Office[CSV.columnCount(pieces[2])];
					i = 0;
					st = new StringTokenizer(pieces[2], ",");
					while (st.hasMoreTokens())
						offices[i++] = Office.parse(st.nextToken());
					return new Audience(ROLE_OFFICE, null, roles, offices);

				default:
					throw new DataFieldException("Unrecognized Audience encoding: " + encoding);
				}
			}
		catch (Exception e)
			{
			throw new DataFieldException("Audience parse error: " + e);
			}
		}

	public Audience()
		{
		this( EVERYBODY, null, null, null );
		}

	public Audience( EmpID... aEmpIDs )
		{
		this( USERS, aEmpIDs, null, null );
		}

	public Audience( Role[] aRoles, Office[] aOffices )
		{
		this( ROLE_OFFICE, null, aRoles, aOffices );
		}

	private Audience( int aEncoding, EmpID[] aEmpIDs, Role[] aRoles, Office[] aOffices )
		{
		fEncoding = aEncoding;
		fEmpIDs = aEmpIDs;
		fRoles = aRoles;
		fOffices = aOffices;
		}

	public boolean member( User aUser, long access )
		{
		if ( fEncoding == EVERYBODY )
			return true;

		else if ( fEncoding == USERS )
			{
			for ( EmpID empID : fEmpIDs )
				if ( aUser.empID().equals(empID))
					return true;
			}

		else if ( fEncoding == ROLE_OFFICE )
			{
// System.out.println("audience check: " + aUser.office() + " access " + access);
			for ( Office office : fOffices )
				if ( office.equals(aUser.office()))
					for ( Role role : fRoles )
						if ( role.access() == access )
							return true;
			}
		return false;
		}

	public String csv()
		{
		StringBuilder it = new StringBuilder();
		it.append(fEncoding).append(SEP);

		if ( fEncoding == USERS )
			{
			for ( EmpID empID : fEmpIDs )
				it.append(empID).append(",");
			}

		else if ( fEncoding == ROLE_OFFICE )
			{
			for ( Role role : fRoles )
				it.append(role.csvRepresentation()).append(",");
			it.deleteCharAt(it.length()-1);
			it.append(SEP);
			for ( Office office : fOffices )
				it.append(office.csvRepresentation()).append(",");
			}
		it.deleteCharAt(it.length()-1);
		return it.toString();
		}
	}
