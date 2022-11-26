package com.apo.net.client;
/********************************************************************
* @(#)Broadcast.java.java 1.01 20110304
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* Broadcast.java: Prompts the user for an estimated time of arrival
* then marks the contacts as having been set the brochure.
*
* @author Rick Salamone
* @version 1.00 20110311 rts demo version
*******************************************************/
import com.apo.contact.Raw;
import com.apo.contact.RawDAO;
import com.apo.contact.report.RawTableModel;
import com.apo.contact.touch.Touch;
import com.shanebow.dao.*;
import com.shanebow.dao.edit.*;
import com.apo.employee.*;
import com.apo.net.Access;
import com.apo.net.Audience;
import com.apo.net.ClientOp;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.calendar.MonthCalendar;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public final class BroadcastAction
	extends SBAction
	{
	public BroadcastAction( ClientApp aClientApp )
		{
		super( "Broadcast", 'B', "Send a message to a group of users", null );
		fClientApp = aClientApp;
		}

	@Override public void actionPerformed(ActionEvent evt)
		{
 		String[] options = { "OK", "Cancel" };
		JPanel panel = buildPanel();
		while ( true )
			{
			if ( 0 != JOptionPane.showOptionDialog(fClientApp, panel,
				LAF.getDialogTitle(toString()), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;

			try
				{
				Comment msg = fMessage.get();
				if ( msg.isEmpty())
					throw new DataFieldException("Comment cannot be blank");
				Role[] roles = fRoles.getSelected();
				Office[] offices = fOffices.getSelected();
				if ( roles.length == 0 || offices.length == 0 )
					throw new DataFieldException("You must specify at least one office and one role");
				Audience audience = new Audience( roles, offices );
				fClientApp.broadcast( ClientOp.ECHO, audience, msg.toString());
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
	private final EditComment fMessage = new EditComment();

	private JPanel buildPanel()
		{
		JPanel it = new JPanel(new BorderLayout());

		JPanel recipients = new JPanel(new BorderLayout());
		LAF.titled(fRoles, "User Role");
		recipients.add(fRoles, BorderLayout.NORTH);
		LAF.titled(fOffices, "Locations");
		recipients.add(fOffices, BorderLayout.SOUTH);

		LAF.titled(fMessage, "Message");
		LAF.titled(recipients, "Recipients");

		it.add(recipients, BorderLayout.NORTH);
		it.add(fMessage, BorderLayout.CENTER);
		return it;
		}
	}
