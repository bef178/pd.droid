package th.pd.mail.fastsync.network;

import android.util.Base64;

import th.pd.mail.dao.Message;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class MessageExporter {
	private static final SimpleDateFormat DATE_FORMAT =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

	// rfc 5322 2.1.1
	private static final int MAX_LINE_LENGTH = 78;

	/**
	 * *this* byte will not be checked, allows -1 as start
	 */
	private static int nextUtf8(byte[] bytes, int start) {
		if (start < -1) {
			return -1;
		}
		while (++start < bytes.length && ((bytes[start] & 0xC0) == 0x80)) {
			// dummy
		}
		return (start < bytes.length) ? start : -1;
	}

	// rfc 2047 section 2. Syntax of encoded-words
	// encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
	private static void putEncodedHeader(OutputStream ostream, String key,
			String value) throws IOException {
		final byte[] keyBytes = key.getBytes("UTF-8");
		final byte[] prefixBytes = "=?UTF-8?B?".getBytes("UTF-8");
		final byte[] suffixBytes = "?=".getBytes("UTF-8");

		final int maxLength = MAX_LINE_LENGTH - prefixBytes.length
				- suffixBytes.length;

		ostream.write(keyBytes);
		ostream.write(':');

		// we would decode each encoded-word respectively and then join them
		// always use base64 rather than quoted-printable: very few people read the raw mail

		byte[] utf8Bytes = value.getBytes("UTF-8");
		int capacity = (maxLength - keyBytes.length - 2) * 3 / 4; // ":" and SP
		int i = 0;
		int j = 0;
		while (i < utf8Bytes.length) {
			while (true) {
				int next = nextUtf8(utf8Bytes, j);
				if (next < 0) {
					// write the rest bytes
					j = utf8Bytes.length;
					break;
				}
				if (next - i > capacity) {
					// too many
					break;
				}
				j = next;
			}
			if (j > i) {
				ostream.write(' ');
				ostream.write(prefixBytes);
				ostream.write(Base64.encode(utf8Bytes, i, j - i,
						Base64.NO_WRAP));
				ostream.write(suffixBytes);
				ostream.write('\r');
				ostream.write('\n');
			}

			capacity = (maxLength - 1) * 3 / 4; // SP
			i = j;
		}
	}

	private static void putHeader(OutputStream ostream, String key,
			String value) throws IOException {
		if (shouldEncode(key, value)) {
			putEncodedHeader(ostream, key, value);
		} else {
			ostream.write(key.getBytes());
			ostream.write(':');
			ostream.write(' ');
			ostream.write(value.getBytes());
			ostream.write('\r');
			ostream.write('\n');
		}
	}

	static void putMessage(BufferedOutputStream ostream, Message message)
			throws IOException {

		// rfc 822 4.1
		// "Return-Path"
		// "Received"
		putHeader(ostream, "Date", DATE_FORMAT.format(new Date()));
		putHeader(ostream, "From", message.getSender());
		putHeader(ostream, "Subject", message.getSubject());
		// "Sender",
		// "To"
		// "cc"

		// TODO body and attachement

		ostream.flush();
	}

	/**
	 * a simple analysis that return <code>true</code> iff the bytes are in one
	 * line and all alphanum
	 */
	private static boolean shouldEncode(String key, String value) {
		if (key == null || value == null) {
			return false;
		}

		// key should not be encoded
		int taken = key.length() + 2;
		if (taken + value.length() > MAX_LINE_LENGTH) {
			return true;
		}

		if (value.length() > 1
				&& value.charAt(0) == '='
				&& value.charAt(1) == '?') {
			// the literal is as if the encoded format
			return true;
		}

		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch < 0x20 || ch >= 0x7F) {
				return true;
			}
		}
		return false;
	}
}
