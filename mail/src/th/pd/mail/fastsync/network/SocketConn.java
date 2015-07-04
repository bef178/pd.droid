package th.pd.mail.fastsync.network;

import th.pd.mail.fastsync.Const;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.ssl.SSLException;

public class SocketConn {
	private static final int SOCKET_CONN_TIMEOUT = 10000;
	private static final int SOCKET_READ_TIMEOUT = 60000;

	private Socket mSocket;
	private BufferedInputStream mIstream;
	private BufferedOutputStream mOstream;

	void conn(SocketAddress remoteAddr) {
		Socket socket = new Socket();
		BufferedInputStream is = null;
		BufferedOutputStream os = null;
		try {
			socket.connect(remoteAddr, SOCKET_CONN_TIMEOUT);
			socket.setSoTimeout(SOCKET_READ_TIMEOUT);
			is = new BufferedInputStream(socket.getInputStream(), 1024);
			os = new BufferedOutputStream(socket.getOutputStream(), 512);
		} catch (SSLException e) {
			// TODO
			Const.logd("SSLException " + e.getMessage());
		} catch (IOException e) {
			// TODO
			Const.logd("IOException " + e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO
			Const.logd("IllegalArgumentException " + e.getMessage());
		}

		mSocket = socket;
		mIstream = is;
		mOstream = os;
	}

	void connEnd() {
		if (mIstream != null) {
			try {
				mIstream.close();
			} catch (Exception e) {
				// dummy
			}
		}

		if (mOstream != null) {
			try {
				mOstream.close();
			} catch (Exception e) {
				// dummy
			}
		}

		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (Exception e) {
				// dummy
			}
		}

		mIstream = null;
		mOstream = null;
		mSocket = null;
	}

	BufferedOutputStream getBufferedOutputStream() {
		return mOstream;
	}

	String getLine() throws IOException {
		StringBuffer sb = new StringBuffer();
		int c = -1;
		L: while ((c = mIstream.read()) != -1) {
			switch (c) {
				case '\r':
					break;
				case '\n':
					break L;
				default:
					sb.append((char) c);
			}
		}
		// TODO c == -1 may be an error state, log it
		return sb.toString();
	}

	InetAddress getLocalAddress() {
		if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed()) {
			return mSocket.getLocalAddress();
		}
		return null;
	}

	void putLine(String s) throws IOException {
		mOstream.write(s.getBytes());
		mOstream.write('\r');
		mOstream.write('\n');
		mOstream.flush();
	}
}
