package com.apo.net.client;
/********************************************************************
* @(#)ClientApp.java 1.00 20110131
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* ClientApp: The base class for any APO client application. This class
* extends JFrame and provides the means to login to the server, send
* requests to the server, and processes asynchronous messages from the
* server. Receipt of asynchronous communications such as bulletins from
* the server were made possible by enhancing the server to implement
* persistent connections.
*
* @author Rick Salamone
* @version 1.00 20110131 rts initial version
* @version 1.01 20110313 rts added broadcast()
* @version 1.02 20110315 rts doe login from the constructor
*******************************************************/
import com.apo.contact.Raw;
import com.apo.order.Order;
import com.apo.contact.touch.Touch;
import com.apo.net.*;
import com.apo.net.Access;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBProperties;
import com.shanebow.util.SBLog;
import java.awt.*;
import javax.swing.*;

public class ClientApp
	extends JFrame
	{
	private final Client fConnection;
	private DlgBulletin dlgBulletin;
	private Container fNeko;
	private Container fAppContent;

	public ClientApp(long access, int aWidth, int aHeight )
		{
		super();
		Access._role = access;
		SBProperties props = SBProperties.getInstance();
		String appName = props.getProperty("app.name");
		String appTitle = appName + " " + props.getProperty("app.version");
		setTitle(appTitle);
		setBounds(props.getRectangle("usr.app.bounds", 25, 25, aWidth, aHeight));
		fConnection = new NetLogin(this).getConnection();
		fConnection.setClientApp(this);
		new SysNetDAO(fConnection);
		Raw.DAO = new RawNetDAO(fConnection);
		Touch.DAO = new TouchNetDAO(fConnection);
		Order.DAO = new OrderNetDAO(fConnection);
		}

	protected final void disconnect()
		{
		fConnection.disconnect();
		}

	protected void unsolicited(Message msg)
		{
		String title = ClientOp.find(msg.op()).desc() + " Error";
		SBDialog.error(title, "<HTML><B>Unexpected unsolicited msg</B><BR>"
		                      + msg.toString());
		}

	protected void error(Message msg)
		{
		String title = ClientOp.find(msg.op()).desc() + " Error";
		String detail = "<HTML><B>" + msg.getReplySnafu() + "</B><BR>"
		           + msg.data() + "<BR>Sequence Number: " + msg.seq();
		if ( msg.op() == ClientOp.CODE_MODLEAD )
			{
			detail += "<BR><B>Serious unexpected error: the app will exit";
			SBDialog.error(title, detail );
			LAF.exit(2);
			}
		else SBDialog.error(title, detail );
		}

	public final void showBulletin(final String aString)
		{
		if ( dlgBulletin == null )
			dlgBulletin = new DlgBulletin(this);
		SwingUtilities.invokeLater( new Runnable()
			{
			public void run() { dlgBulletin.show(aString); }
			});
		}

	public void neko(final boolean on)
		{
		boolean nekoShowing = getContentPane().equals(fNeko);
		if ( on == nekoShowing )
			return;
		SwingUtilities.invokeLater( new Runnable()
			{
			public void run()
				{
				if ( on )
					{
					fAppContent = getContentPane();
					if ( fNeko == null )
						fNeko = new com.shanebow.toy.TicTacToe.TicTacToe();
					setContentPane(fNeko);
					}
				else setContentPane(fAppContent);
				validate();
				}
			});
		}

	public void broadcast( ClientOp aClientOp, Audience aAudience, String aData )
		{
		StringBuffer ladding = new StringBuffer();
		ladding.append(aClientOp.code()).append(Message.SEP);
		ladding.append(aAudience.csv()).append(Message.SEP);
		ladding.append(aData);
		fConnection.send( ClientOp.BROADCAST, ladding.toString());
		}

	protected final void log(String fmt, Object... args)
		{
		SBLog.write( getTitle(), String.format(fmt, args));
		}
	}
