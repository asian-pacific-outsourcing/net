package com.apo.net.client;
/********************************************************************
* @(#)NekoAction.java.java 1.01 20110304
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* NekoAction.java: Action to start a game in user apps.
*
* @author Rick Salamone
* @version 1.00 20110311 rts demo version
*******************************************************/
import com.apo.employee.*;
import com.apo.net.Access;
import com.apo.net.Audience;
import com.apo.net.ClientOp;
import com.shanebow.dao.DataFieldException;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;

public final class NekoAction
	extends SBAction
	{
	public NekoAction( ClientApp aClientApp )
		{
		super( "Tic Tac Toe", 'T', "Toggle tic-tac-toe", null );
		fClientApp = aClientApp;
		}

	@Override public void actionPerformed(ActionEvent evt)
		{
 		String[] options = { "OK", "Cancel" };
		JPanel panel = buildPanel();
		while ( true )
			{
			if ( 0 != JOptionPane.showOptionDialog(fClientApp, buildPanel(),
				LAF.getDialogTitle(toString()), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;

			try
				{
				Role[] roles = fRoles.getSelected();
				Office[] offices = fOffices.getSelected();
				if ( roles.length == 0 || offices.length == 0 )
					throw new DataFieldException("You must specify at least one office and one role");
				Audience audience = new Audience( roles, offices );
				fClientApp.broadcast( ClientOp.NEKO, audience, "");
				SBDialog.inform(fClientApp, toString(), "Successful" );
				return;
				}
			catch (Exception e) { SBDialog.error("Data Access Error", e.getMessage()); }
			}
		}

	// PRIVATE
	private final ClientApp   fClientApp;
	private final ChkOffice   fOffices = new ChkOffice();
	private final ChkRole     fRoles = new ChkRole();

	private JPanel buildPanel()
		{
		JPanel it = new JPanel(new BorderLayout());
		JPanel recipients = new JPanel(new BorderLayout());
		it.add(LAF.titled(recipients, "Recipients"), BorderLayout.CENTER);
		recipients.add(LAF.titled(fRoles, "User Role"), BorderLayout.NORTH);
		recipients.add(LAF.titled(fOffices, "Locations"), BorderLayout.SOUTH);
		return it;
		}
	}
