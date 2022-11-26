package com.apo.net;
/********************************************************************
* @(#)Message.java	1.0 10/06/29
* Copyright 2010-2011 by Richard T. Salamone, Jr. All rights reserved.
*
* Message: A message consists of lading enveloped by a header and
* tail. The header and tail are fixed length, while the lading is
* of arbitry length. A message may carry a request from the client
* to be processed by the server, or a response from the server to
* client. A third message type is a command issued from the server
* to the client.
* An request message sent from a client to the server is always
* followed by a response message from the server back to the client.
* Requests and responses share the same message structure. However,
* in a request message, the err field is always 0, and the ladding
* provides data for the server. In a response, the err field may be
* non-zero inwhich case the lading carries an error description. If
* the response err field is 0, then the lading contains data from the
* server.
*
* @author Rick Salamone
* @version 1.00 20100629 rts created
* @version 1.01 20100825 rts using nio ByteBuffers
* @version 1.02 20101016 rts decoupled encode & send for non-blocking server
* @version 1.03 20101017 rts added isComplete() to check if full msg received
* @version 1.04 20101028 rts constructor(ByteBuffer) in a try/catch
* @version 1.05 20101028 rts isComplete returns true if wrong opCode received
* @version 2.00 20101101 rts login returns server time
* @version 3.00 20110130 rts now have fields for op and err, seq now short
* @version 4.00 20110310 rts returns sql query partial results
* @version 5.00 20110310 rts Country ID's changed and order message changed
* @version 6.00 20110404 rts order flow defined & modify raw message changed
*******************************************************/
import com.apo.net.Snafu;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class Message
	{
	public static final byte VERSION = 0x06; // 2 hex digits
	public static final String SEP = "\f"; // separates pieces in the ladding
	public static final byte WANT_REPLY = 0x01;
	public static final byte UNSOLICITED= 0x02;
	public static final byte FORWARDED= 0x04;

	public static final int HEAD_BYTES = 12; // bytes
	public static final int TAIL_BYTES = 1; // bytes
//	public static final int MAX_DATA_LENGTH = 2048;
	public static final byte EOT = (byte)0x80;

	protected static final void _log(String fmt, Object... args)
		{
		SBLog.write( "Message", String.format( fmt, args));
		}

	public static Message parse(ByteBuffer bb)
		{
		return new Message(bb);
		}

	public static void send( OutputStream os, byte op, short uid, short seq, String data, boolean aWantReply )
		throws IOException
		{
		byte flags = aWantReply? WANT_REPLY : 0;
		send( os, encode( op, (byte)0, flags, uid, seq, data ));
		}

	public static void send( OutputStream os, byte code, short uid, short seq, String data )
		throws IOException
		{
		send( os, encode( code, (byte)0, WANT_REPLY, uid, seq, data ));
		}

	private static void send( OutputStream os, ByteBuffer bb )
		throws IOException
		{
		byte[] bytes = new byte[bb.position()];
		bb.flip();
		bb.get(bytes);
		os.write(bytes);
		}

	public static ByteBuffer encode( byte op, byte err, short uid, short seq, String data )
		{
		byte flags = 0;
		return encode( op, err, flags, uid, seq, data );
		}

	public static ByteBuffer encode( byte op, byte err, byte flags, short uid, short seq, String data )
		{
		short len = (short)data.length();
		short sum = 0; // unused checksum
		ByteBuffer bb = ByteBuffer.allocate( HEAD_BYTES + len + TAIL_BYTES );
		bb.put(VERSION);   //  0 1
		bb.put(op);        //  1 1
		bb.put(err);       //  2 1
		bb.put(flags);     //  3 1
		bb.putShort(uid);  //  4 2
		bb.putShort(seq);  //  6 2
		bb.putShort(sum);  //  8 2
		bb.putShort(len);  // 10 2
		char[] work = data.toCharArray();
		for ( short i = 0; i < len; i++ )
			{
			bb.put((byte)work[i]);
			}
		bb.put(EOT);
//		_log( "ENCODED: op:%02X, err:%02X uid:%d seq:%02X data: '%s'",
//		              (int)op, (int)err, (int)uid, (int)seq, data);
		return bb;
		}

	public static boolean isComplete(ByteBuffer bb)
		{
		int p = bb.position();
		if (( p > 0) && (bb.get(0) != VERSION))
			return true;
		if (p < (HEAD_BYTES + TAIL_BYTES))
			return false;
		short dataLen = bb.getShort(10);
		int expected = HEAD_BYTES + TAIL_BYTES + dataLen;
		return p == expected;
		}

	private final byte   m_op;       // operation
	private final byte   m_err;      // error
	private final byte   m_flags;    // error
	private final short  m_uid;      // user id
	private final short  m_seq;      // sequence #
	private final String m_data;     // lading
	private final long   m_time;
	private final Snafu  m_snafu;

	public Message(InputStream input)
		throws IOException
		{
		m_time = SBDate.timeNow();

		// read & parse the message header
		byte[] header = new byte[HEAD_BYTES];
		for ( int i = 0; i < HEAD_BYTES; i++ )
			{
			int x = input.read();
			if ( x == -1 )
				throw new IOException( "Unexpected EOF");
			header[i] = (byte)x;
			}
		ByteBuffer bb = ByteBuffer.wrap(header);
		if ( bb.get() != VERSION)   //  0 1
			throw new IOException( "Wrong version");
		m_op = bb.get();            //  1 1
		m_err = bb.get();           //  2 1
		m_flags = bb.get();         //  3 1
		m_uid = bb.getShort();      //  4 2
		m_seq = bb.getShort();      //  6 2
		short sum = bb.getShort();  //  8 2
		short len = bb.getShort();  // 10 2

		// read message data
		byte[] buffer = new byte[len];
		for ( int i = 0; i < len; i++ )
			{
			int x = input.read();
			if ( x == -1 )
				throw new IOException( "Unexpected EOF");
			buffer[i] = (byte)x;
			}
		m_data = new String(buffer);

		// check the tail
		int eot = input.read();
		if ((byte)eot != EOT )
			throw new IOException( "Incorrect EOT");
//		_log( toString());
		m_snafu = Snafu.NONE;
		}

	public Message(ByteBuffer bb)
		{
		m_time = SBDate.timeNow();
		byte code = 0;
		byte err = 0;
		byte flg = 0;
		Snafu snafu = null;
		String data = null;
		short uid = 0;
		short seq = 0;
		try
			{
			if ( bb.get() != VERSION)   //  0 1
				snafu = Snafu.BAD_VERSION;
			code = bb.get();            //  1 1
			err  = bb.get();            //  2 1
			flg = bb.get();             //  3 1
			uid = bb.getShort();        //  4 2
			seq = bb.getShort();        //  6 2
			short sum = bb.getShort();  //  8 2
			short len = bb.getShort();  // 10 2
			// read message data
			byte[] buffer = new byte[len];
			bb.get(buffer, 0, len );
			data = new String(buffer);

			// check the tail
			int eot = bb.get();
			if ( (byte)eot != EOT)
				snafu = Snafu.WRONG_EOT;
			}
		catch ( Throwable e )
			{
			snafu = Snafu.READ_ERROR;
			data = e.getMessage();
			_log( "Exception: " + data );
			}
		finally
			{
			m_op = code;
			m_err = err;
			m_flags = flg;
			m_uid = uid;
			m_seq = seq;
			m_data = data;
			m_snafu = snafu;
			}
		}

	public  final boolean hasSnafu()
		{
		return (m_err != Snafu.CODE_NONE)
		      || (m_snafu != null && m_snafu != Snafu.NONE);
		}
	public  final Snafu getSnafu() { return m_snafu ; }
	public  Snafu   getReplySnafu()
		{
		if ( m_err != 0 )
			return Snafu.find(m_err);
		if ( m_snafu != null && m_snafu != Snafu.NONE )
			return getSnafu();
		return Snafu.NONE;
		}

	public final byte   op()    { return m_op; }
	public final byte   err()   { return m_err; }
	public final short  uid()   { return m_uid;  }
	public final short  seq()   { return m_seq; }
	public final long   time()  { return m_time; }
	public final String data()  { return m_data; }
	public final boolean wantReply()   { return (m_flags & WANT_REPLY) == WANT_REPLY; }
	public final boolean unsolicited() { return (m_flags & UNSOLICITED) == UNSOLICITED; }
	public final boolean forwarded() { return (m_flags & FORWARDED) == FORWARDED; }
	public final String toString()
		{
		return String.format("op:%02X, err:%02X uid:%d seq:%04X data: '%s'",
		              (int)m_op, (int)m_err, (int)m_uid, m_seq, m_data);
		}
	}