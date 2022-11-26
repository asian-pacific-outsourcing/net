package com.apo.net.client;
/********************************************************************
* @(#)DlgBulletin.java 1.0 20110130
* Copyright 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgBulletin: Dialog to view bulletins sent from the management.
*
* @version 1.00 20110130
* @author Rick Salamone
*******************************************************/
import com.apo.net.Message;
import com.shanebow.ui.SBTextPanel;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DlgBulletin extends JDialog
	{
	private static final String DLG_TITLE="Bulletins";
	private static final String CMD_CLOSE="Close";
	private static final String SEPARATOR
		="-----------------------------------------";

	private final SBTextPanel theLog = new SBTextPanel(null, false, Color.WHITE);

	public DlgBulletin( JFrame f )
		{
		super((Frame)null, LAF.getDialogTitle(DLG_TITLE), true);
		JPanel top = new JPanel(new BorderLayout());
		top.setBorder(LAF.getStandardBorder());
		top.add(theLog, BorderLayout.CENTER);
		setContentPane(top);
		pack();
		SBProperties props = SBProperties.getInstance();
		setBounds( props.getRectangle( "usr.bulletin.bounds", 150, 10, 375, 200));
		addComponentListener( new ComponentAdapter()
			{
			public void componentMoved(ComponentEvent e) { saveBounds(); }
			public void componentResized(ComponentEvent e) { saveBounds(); }
			});
		LAF.addUISwitchListener(this);
		}

	private void saveBounds()
		{
		SBProperties.getInstance().setProperty( "usr.bulletin.bounds", getBounds());
		}

	public void show(String msg)
		{
		theLog.write(msg);
		theLog.write(SEPARATOR);
		setVisible(true);
		}
	}
