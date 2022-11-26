package com.apo.net.client;
/********************************************************************
* @(#)EditBulletinAction.java.java 1.01 20110304
* Copyright © 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* EditBulletinAction.java: Prompts the user for an estimated time of arrival
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
import java.awt.*;
import javax.swing.*;

public final class EditBulletinAction
	extends SBAction
	{
	public EditBulletinAction( ClientApp aClientApp )
		{
		super( "New Bulletin", 'U', "Create a new bulletin board message", null );
		fClientApp = aClientApp;
		}

	@Override public void action()
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
				Comment msg = fMessage.get();
				if ( msg.isEmpty())
					throw new DataFieldException("Comment cannot be blank");
				When expires = new When(fExpires.getDate() + (24 * 60 * 60) - 1);
				Role[] roles = fRoles.getSelected();
				Office[] offices = fOffices.getSelected();
				if ( roles.length == 0 || offices.length == 0 )
					throw new DataFieldException("You must specify at least one office and one role");
				Audience audience = new Audience( roles, offices );
System.out.println("Expires: '" + com.shanebow.util.SBDate.mmddyy_hhmm(expires.getLong()) + "'");
				fClientApp.broadcast( ClientOp.ECHO, audience, msg.toString());
				SBDialog.inform(fClientApp, "Edit Bulletin", "Successful" );
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
	private MonthCalendar fExpires = new MonthCalendar();

	private JPanel buildPanel()
		{
		JPanel it = new JPanel(new BorderLayout());

		JPanel recipients = new JPanel(new BorderLayout());
		LAF.titled(fRoles, "User Role");
		recipients.add(fRoles, BorderLayout.NORTH);
		LAF.titled(fOffices, "Locations");
		recipients.add(fOffices, BorderLayout.SOUTH);

		JPanel expires = new JPanel(new BorderLayout());
		expires.add(fExpires, BorderLayout.CENTER);
		fExpires.setPreferredSize(new Dimension(160,160));

		LAF.titled(fMessage, "Message");
		LAF.titled(recipients, "Recipients");
		LAF.titled(expires, "Expires");

		it.add(recipients, BorderLayout.NORTH);
		it.add(fMessage, BorderLayout.CENTER);
		it.add(expires, BorderLayout.EAST);
		return it;
		}
	}

/********************************************************************
* @(#)MgrCommand.java.java 1.01 20110304
* Copyright © 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* MgrCommand.java: The various management commands that can be
* broadcast to a group of recipients using the system.
*
* @author Rick Salamone
* @version 1.00 20110311 rts demo version
*******************************************************/
final class MgrCommand
	{
	public static final MgrCommand[] COMMANDS =
		{
		new MgrCommand("Logout", 1),
		new MgrCommand("Bulletin", 2),
		new MgrCommand("Neko", 3),
		};

	@Override public String toString() { return fDesc; }
	public int code() { return fCode; }

	// PRIVATE
	private final String fDesc;
	private final int fCode;

	private MgrCommand(String aDesc, int aCode)
		{
		fDesc = aDesc;
		fCode = aCode;
		}
	}
