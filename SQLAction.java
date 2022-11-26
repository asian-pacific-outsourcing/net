package com.apo.net;
/********************************************************************
* @(#)SQLAction.java.java 1.01 20110304
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* SQLAction.java: Prompts the user for an SQL query
* then marks the contacts as having been set the brochure.
*
* @author Rick Salamone
* @version 1.00 20110311 rts demo version
*******************************************************/
import com.shanebow.dao.*;
import com.apo.net.SysDAO;
import com.apo.net.ClientOp;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public final class SQLAction
	extends SBAction
	{
	public SQLAction()
		{
		super( "SQL", 'Q', "Run an SQL update query", null );
		}

	@Override public void actionPerformed(ActionEvent evt)
		{
		String title = LAF.getDialogTitle(toString());
 		String[] options = { "OK", "Cancel" };
		JPanel panel = buildPanel();
		while ( true )
			{
			if ( 0 != JOptionPane.showOptionDialog(null, buildPanel(),
				title, JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;

			try
				{
				String stmt = fQuery.getText().trim();
				if ( stmt.isEmpty())
					throw new DataFieldException("Statement cannot be blank");
				int count = SysDAO.DAO().sqlUpdate(stmt);
				SBDialog.inform(null, title, "Updated " + count + " records");
				return;
				}
			catch (Exception e) { SBDialog.error(title + " Data Access Error", e.getMessage()); }
			}
		}

	// PRIVATE
	private final JTextArea   fQuery = new JTextArea(5,30);

	private JPanel buildPanel()
		{
		JPanel it = new JPanel(new BorderLayout());
		JLabel help = new JLabel("<HTML><UL>"
			+ "<LI>UPDATE touch SET touchCode=6 WHERE touchCode=5 AND employeeID=19"
			+ "<LI>UPDATE raw SET CountryID=20 WHERE CountryID=1"
			+ "<LI>DELETE FROM raw WHERE CountryID=327"
			);

		LAF.titled(help, "Examples");
		LAF.titled(fQuery, "SQL Statement");

		it.add(help, BorderLayout.NORTH);
		it.add(fQuery, BorderLayout.CENTER);
//		it.add(expires, BorderLayout.EAST);
		return it;
		}
	}
