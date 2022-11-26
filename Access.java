package com.apo.net;
/********************************************************************
* @(#)Access.java 1.00 10/07/06
* Copyright (c) 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Access: IP adresses, ports, and permisission flag definitions for
* users of the APO system
*
* @version 1.00 07/06/10
* @author Rick Salamone
* 20100706 RTS Created from User.java
* 20100802 RTS added parseCmdLine()
* 20100927 RTS added shortened names of privledges, added NONE
* 20100927 RTS added office names
* 20101018 RTS added cmd line args -mode and x509
* 20110410 RTS added cmd line args for houston & flipper
* 20110418 RTS added allowedTo method for checking access to a role
*******************************************************/
import com.apo.contact.Dispo;
import com.apo.contact.Source;
import com.shanebow.dao.EmpID;
import com.apo.employee.Role;
import java.util.*;

public final class Access
	{
	public static boolean _debug = false;
	public static final int MAX_UID = 5000; // used for check in and check out

	public static final boolean AUTONUMBER = false; // true if db autonumbers new contacts

	public static final String DEFAULT_USER = "";
	public static String _login = DEFAULT_USER; // the user's login name
	private static short  _uid;
	public static long   _role;
	private static EmpID _empID;
	private static boolean _flipper = false;
	private static long _rights;

	public static boolean isFlipper() { return _flipper; }
	public static void setUID(short aUID)
		{
		_uid = aUID;
		_empID = new EmpID(aUID);
		_rights = Source.find(_uid).access();
		}

	public static final long rights() { return _rights; }

/*************
	public static boolean toAny(long... aAccesses)
		{
		for ( long access : aAccesses )
			if ((_rights & access) == access)
				return true;
		return false;
		}

	public static boolean toAll(long... aAccesses)
		{
		for ( long access : aAccesses )
			if ((_rights & access) != access)
				return false;
		return true;
		}
*************/

	public static boolean allowedTo(long aAccess)
		{
		return (_rights & aAccess) != 0;
		}

	public static short getUID() { return _uid; }
	public static Source usrID() { return Source.find(_uid); }
	public static EmpID empID() { return _empID; }

	private static Role fRole;
	public static void setRole(Role aRole)
		{
		fRole = aRole;
for ( Role r : Role.getAll() )
System.out.println ( "Role " + r + " mgr: " + r.isManager() + " call: " + r.isCaller());
System.out.println ( "fRole " + fRole + " mgr: " + fRole.isManager() + " call: " + fRole.isCaller());
		}
	public static Role getRole() { return fRole; }

	public static Set<Dispo> accessibleDispos()
		{
		Set<Dispo> dispos = new TreeSet<Dispo>();
		for ( Role role : Role.getAll())
			if ( Access.allowedTo(role.access()))
				{
				dispos.addAll(Arrays.asList(role.fetchDispos()));
				dispos.addAll(Arrays.asList(role.saveDispos()));
				}
		return dispos;
		}

	// IP Addresses
	public static final String IP_LOCAL_SERVER = "127.0.0.1";
	public static final String IP_HOME_SERVER  = "192.168.7.50";
	public static final String IP_APO_SERVER   = "192.168.0.2";
	public static final String IP_APO_REMOTE   = "58.181.180.165";
	public static final String DEFAULT_SERVER_IP = IP_APO_SERVER;
	public static String _serverIP = DEFAULT_SERVER_IP;

	// Port Assignments
	public static final int DEFAULT_SERVER_PORT = 8080;
	public static int _port = DEFAULT_SERVER_PORT;

	// User Privledges
	public static final long NONE = 0x0000;
	public static final long DBA  = 0x0001; // Database Administrator
	public static final long DM   = 0x0002; // run the Data Miner app
	public static final long RD   = 0x0004; // run the Raw Dispo app
	public static final long TQ   = 0x0008; // run the Telephone Qualifier app
	public static final long VO   = 0x0010; // run the Vero app
	public static final long AO   = 0x0020; // run the Account Opener app
	public static final long LO   = 0x0040; // run the Loader app
	public static final long MS   = 0x0080; // Mail Staff
	public static final long MGR  = 0x1000; // run as Manager
	public static final long ADM  = 0x2000; // run as Admin
	public static final long VM   = MGR|DM|TQ|VO; // run as DM, TQ, VO Manager
	public static final long MM   = MGR|MS; // run as MS Manager
	public static final long MGRAD= MGR|ADM; // Admin or Manager
	public static final long RM   = MGR|AO|LO; // run as AO & LO Manager
	public static final long RA   = ADM|AO|LO; // run as AO & LO Manager
	public static final long ALL  = DM|RD|TQ|VO|AO|LO|MS|MGR|ADM;
	public static final long GOD  = DBA|ALL;

	public static final AccessRole[] ROLES =
		{
		new AccessRole("DM",  Access.DM), // run the Data Miner app
		new AccessRole("RD",  Access.RD), // run the Raw Dispo app
		new AccessRole("TQ",  Access.TQ), // run the Telephone Qualifier app
		new AccessRole("VO",  Access.VO), // run the Vero app
		new AccessRole("AO",  Access.AO), // run the Account Opener app
		new AccessRole("LO",  Access.LO), // run the Loader app
		new AccessRole("VM",  Access.VM), // run as the DM, TQ, VO Manager
		new AccessRole("MS",  Access.MS), // Mail Staff
		new AccessRole("MM",  Access.MM), // run as MS Manager
		new AccessRole("RM",  Access.RM), // run as AO Manager
		new AccessRole("DBA", Access.DBA), // Database Administrator
		};

	public static final long[] manages()
		{
		int numRolesManaged = 0;
		for ( long i = 1; i < MGR; i *= 2 )
			if ((i & _role) != 0 ) ++numRolesManaged;
		long[] it = new long[numRolesManaged];
		int j = 0;
		for ( long i = 1; i < MGR; i *= 2 )
			if ((i & _role) != 0 ) it[j++] = i;
		return it;
		}

	// Security: use X509 certificates or not
	public static boolean _x509 = false;

	// Mode: an application can used this value in any way it sees fit
	public static String _mode = null;

	// Code

	public static final void parseCmdLine( String[] args )
		{
		for ( String arg : args )
			{
			if ( arg.startsWith("-u:" ))
				_login = arg.substring(3);
			else if ( arg.startsWith("-port:" ))
				_port = Integer.parseInt(arg.substring(6));
			else if ( arg.startsWith("-mode:" ))
				_mode = arg.substring(6);
			else if ( arg.equalsIgnoreCase("x509"))
				_x509 = true;
			else if ( arg.equalsIgnoreCase("flipper"))
				{
				_flipper = true;
				_serverIP = "192.168.0.2";
				}
			else if ( arg.equalsIgnoreCase("houston1"))
				_serverIP = "192.168.0.50";
			else if ( arg.equalsIgnoreCase("houston2"))
				_serverIP = "192.168.0.52";
			else if ( arg.equalsIgnoreCase("loopback"))
				_serverIP = Access.IP_LOCAL_SERVER;
			else if ( arg.equalsIgnoreCase("remote"))
				_serverIP = Access.IP_APO_REMOTE;
			else if ( arg.equalsIgnoreCase("condo"))
				_serverIP = Access.IP_HOME_SERVER;
			else if ( arg.equalsIgnoreCase("-d"))
				_debug = true;
			}
		}
	}
