package com.apo.net.client;
/********************************************************************
* @(#)NetLogin.java 1.0 10/04/28
* Copyright 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Prompts user to log in to the APO server from an application
* across the network. A client connection object is returned to
* the app. A null connection indicates failure due to any of
* several possible problems, all of which are reported to the user.
*
* @author Rick Salamone
* @version 1.00 20100428 rts created
* @version 1.01 20100701 rts extends JDialog & creates connection
* @version 1.02 20101010 rts uses LabeledPairPanel
* @version 1.03 20101015 rts uses Access._login as default login name
* @version 1.04 20101020 rts saves login to Access._login upon success
* @version 1.05 20101101 rts constructor takes app title to build title
* @version 1.06 20110526 rts get login image as property
*******************************************************/
import com.apo.net.Access;
import com.apo.employee.User;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.LAF;
import com.shanebow.ui.layout.LabeledPairPanel;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.*;

public class NetLogin extends JDialog
	implements ActionListener
	{
	public static final String ERR_CONNECT = "Error Connecting to Server";
	private static final String CMD_LOGIN="Login";
	private static final String DEFAULT_USER = "";
	private static final String DEFAULT_PASSWORD = "";
	private static final String CONNECT_TITLE = "Login";

	JTextField  tfUserName;
	JTextField  tfPassword;

	private Client m_connection = null;

	public NetLogin( JFrame f )
		{
		super(f, true);
		long access = Access._role;
		setTitle(LAF.getDialogTitle("Login"));
		String loginImage = SBProperties.get("app.login.image");
		JPanel top = (loginImage != null)? new ImagePanel(loginImage)
		           : new JPanel();
		top.setLayout(new BorderLayout());
		top.setBorder(LAF.getStandardBorder());
//		setLocationRelativeTo(f);
		setLocationByPlatform(true);
		LabeledPairPanel main = new LabeledPairPanel();
//		main.setPreferredSize( new Dimension(200,60));

		main.addRow("User name: ", tfUserName = new JTextField());
		main.addRow( "Password: ", tfPassword = new JPasswordField());
		tfUserName.requestFocusInWindow();
		tfUserName.setText(Access._login);
	main.setOpaque(false);
		top.add(main, BorderLayout.CENTER);
		top.add(btnPanel(), BorderLayout.SOUTH);
		setContentPane(top);
		this.addWindowListener(new WindowAdapter()
			{
			public void windowClosing(WindowEvent e)
				{ System.out.println("Bailed here"); System.exit(1); }
			});
		pack();
		setResizable(false);
		}

	private void log ( String f, Object... args )
		{
		SBLog.write("LOGIN", String.format(f, args));
		}

	private JComponent btnPanel()
		{
		JButton btnLogin = new JButton(CMD_LOGIN);
		btnLogin.addActionListener(this);
		getRootPane().setDefaultButton(btnLogin);
		JComponent it = LAF.getCommandRow(btnLogin);
		it.setOpaque(false);
		return it;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (isValidLogin())
			setVisible(false);
		}

	public Client getConnection()
		{
		setVisible(true);
		return m_connection;
		}

	public String getUserName()
		{
		return tfUserName.getText().trim();
		}

	private boolean isValidLogin()
		{
		String usr = getUserName();
		if ( usr.isEmpty())
			return SBDialog.inputError( "User name cannot be blank" );
		try { m_connection = Client.connect( usr, tfPassword.getText(), Access._role ); }
		catch ( IOException e )
			{
			return SBDialog.error( ERR_CONNECT, e.getMessage());
			}
		return (m_connection != null);
		}
	}

class ImagePanel extends JPanel
	{
	private Image img;

	public ImagePanel(String img)
		{
		this(new ImageIcon(com.shanebow.util.SBMisc.findResource(
				        img)).getImage());
		}

	public ImagePanel(Image img)
		{
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setOpaque(true);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
		}

	public void paintComponent(Graphics g)
		{
		g.drawImage(img, 0, 0, null);
		}
	}

