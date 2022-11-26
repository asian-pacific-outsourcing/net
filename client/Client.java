package com.apo.net.client;
/********************************************************************
* @(#)Client.java 1.0 20100629
* Copyright 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Client: Connects to the APO server then sends requests to for
* processing and retrieves the responses. To promote security,
* the connection is made by a protected static call that is intended
* to be made only from the login dialog. The constructor is private
* and actually sends the login request to the server and stores the
* user id as a member variable for use in subsequent requests.
*
* NOTE: May disable the Nagle algorithm (may not be a good thing to do),
* by using the setsockopt call with TCP_NODELAY
*
* @author Rick Salamone
* @version 1.00 20100629 rts created
* @version 1.01 20101020 rts checks for socket isClosed 
* @version 1.02 20101020 rts no longer closes socket after receive 
* @version 1.03 20101020 rts no close now closes in & out streams 
* @version 1.04 20101020 rts send has attempt counter 
* @version 1.05 20101115 rts synchronized send method 
*******************************************************/
import com.apo.employee.Role;
import com.apo.net.*;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBProperties;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.*;

public final class Client
	{
	public static final int CLOSED = 0;
	public static final int OPENING = 1;
	public static final int OPENED = 2;
	public static final int CLOSING = 3;
	public static final int EXITED = 4;

	private static final String MODULE="Client";
	private boolean fPersistantConnection = true;
	private volatile int fState = CLOSED;

	/**
	* All messages received from the server (by the receive thread) are
	* placed into this Queue for asynchonous processing by the ClientApp.
	*/
	private BlockingQueue<Message> fMessageQ = new ArrayBlockingQueue<Message>(50);

	/**
	* ClientApp extends frame and is the base class for all client applications
	* It is responsible for handling asynchronous messages from the server, such
	* as bulletins, 'neko,' and forced logout.
	*/
	private ClientApp fClientApp;
	public void setClientApp( ClientApp aClientApp) { fClientApp = aClientApp; }

	/**
	* Messages pulled off the fMessageQ that are replies to previous requests.
	* These messages are generally processed synchronously
	*/
	private BlockingQueue<Message> fReplyQ = new ArrayBlockingQueue<Message>(50);

	protected static Client connect(String usr, String pwd, long access)
		throws IOException
		{
		Access._login = usr;
		return new Client( usr + Message.SEP + pwd + Message.SEP + access
		                   + Message.SEP + SBProperties.get("app.version"));
		}

	private static void log(String fmt, Object... args)
		{
//		if ( Access._debug )
		SBLog.write(MODULE, String.format(fmt, args));
System.out.format(fmt+ "\n", args);
		}

	private static long _serverTimeAdjust = 0;
	InetAddress inetAddress;
	private Socket         m_socket = null;
	private OutputStream   m_out = null;
	private InputStream    m_in = null;
	private short          m_sequence = 1;
	private Thread recvThread;

	private Client( String loginData )
		throws IOException
		{
		inetAddress = InetAddress.getByName(Access._serverIP);
		Runnable processor = new Runnable()
			{
			public void run() { doProcess(); }
			};
		Thread procThread = new Thread(processor, "Net Msg Processor" );
		procThread.start();
		connect();
		Message reply = send(ClientOp.LOGIN, loginData );
		if ( reply.err() != Snafu.CODE_NONE )
			throw ( new IOException("Login Failed:\n" + reply.getReplySnafu()));
		String[] pieces = reply.data().split(Message.SEP, 3);
		log ( "Logged in as: '%s'", Access._login );
		try
			{
			Access.setUID(Short.parseShort(pieces[0]));
			Access.setRole( Role.unmarshall(pieces[2]));
			}
		catch (Exception e) { log( "Parse error " + reply.data()); }

		try
			{
			long serverTime = Long.parseLong(pieces[1]);
			long myTime = SBDate.timeNow();
			_serverTimeAdjust = serverTime - myTime;
			SBLog.write ( MODULE, "Time Adjust"
			              + "\nServer:\t" + SBDate.yyyymmdd__hhmmss(serverTime)
			              + "\nClient:\t" + SBDate.yyyymmdd__hhmmss(myTime)
			              + "\ndifference: " + _serverTimeAdjust + " seconds" );
			}
		catch (NumberFormatException e) { log( "Parse error " + reply.data()); }

		finally { if (!fPersistantConnection) close(); }
		}

	public static long getServerTime() { return _serverTimeAdjust + SBDate.timeNow(); }

	public void disconnect()
		{
//		send(ClientOp.CANWORK, "");
		send(ClientOp.LOGOUT, "", false);
		fState = EXITED;
		close();
		}

	private void connect()
		throws IOException
		{
		log( "connecting to server" );
		m_socket = new Socket(inetAddress, Access._port);
		m_out = m_socket.getOutputStream();
		m_in = m_socket.getInputStream();
		fState = OPENED;
		Runnable receiver = new Runnable()
			{
			public void run() { doRecv(); }
			};
		Thread recvThread = new Thread(receiver, "NET RECV" );
		recvThread.start();
		}

	private void close()
		{
		fState = CLOSED;
		if ( m_socket == null )
			return;
		log("close()");
		try { if ( m_out != null ) m_out.close(); }
		catch (Exception e) { log("Error closing output stream: " + e.getMessage()); }
		try { if ( m_in != null ) m_in.close(); }
		catch (Exception e) { log("Error closing input stream: " + e.getMessage()); }
		try { m_socket.close(); }
		catch (Exception e) { log("Error closing socket: " + e.getMessage()); }
		finally { m_in = null; m_out = null; m_socket = null; }
		}

	private void doRecv()
		{
		log("doRecv: started" );
		while( fState != CLOSED )
			{
			try
				{
				Message msg = new Message(m_in);
				byte op = msg.op();
//				log( "doRecv: " + ClientOp.find(op));
//		System.out.println("doRecv: " + ClientOp.find(op));
				if ( msg.unsolicited())
					fMessageQ.put(msg);
				else // it's a server reply to a client request
					fReplyQ.put(msg);
				}
			catch (Exception e)
				{
				log( "doRecv ERROR: " + e );
				close();
				}
			}
		log("doRecv: exited" );
		}

	private boolean nekoToggle;
	private void doProcess()
		{
		log("doProcess: started" );
		while( fState != EXITED )
			{
			try
				{
				Message msg = fMessageQ.take();
				log("doProcess asynch: " + msg );
					{
					switch ( msg.op())
						{
						case ClientOp.CODE_NEKO:
							log( "NEKO NOW!" );
							fClientApp.neko( nekoToggle = !nekoToggle );
							break;
						case ClientOp.CODE_ECHO:
							fClientApp.showBulletin(msg.data());
							break;
						case ClientOp.CODE_LOGOUT:
							LAF.exit(1);
							break;

						default:
							if ( msg.err() == Snafu.CODE_NONE )
								fClientApp.unsolicited(msg);
							else
								fClientApp.error(msg);
							break;
						}
					}
				}
			catch (InterruptedException e)
				{
				log("doProces interrupted");
				}
			}
		}

	public synchronized Message send(ClientOp op, String data)
		{
		try
			{
			if (( m_socket == null ) || m_socket.isClosed())
				{
				close();
				connect();
				}
			log("send " + op );
			Message.send( m_out, op.code(), Access.getUID(), m_sequence++, data );
m_out.flush();
			return waitForReply();
			}
		catch (Exception e)
			{
			log("Network Error: %s", e.toString());
			close();
			}
		return null;
		}

	public synchronized Message send(ClientOp op, String data, boolean aWantReply)
		{
		try
			{
			if (( m_socket == null ) || m_socket.isClosed())
				{
				close();
				connect();
				}
	log("send " + op + " want reply: " + aWantReply );
			Message.send( m_out, op.code(), Access.getUID(), m_sequence++, data, aWantReply );
m_out.flush();
			return aWantReply? waitForReply() : null;
			}
		catch (Exception e)
			{
System.out.println("Network Error: " + e.toString());
			log("Network Error: %s", e.toString());
			close();
			}
		return null;
		}

	public Message waitForReply()
		throws InterruptedException
		{
		return fReplyQ.take();
		}
	}
