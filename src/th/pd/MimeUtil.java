package th.pd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class MimeUtil {

	public static Drawable drawableByMimeType(Context context, String mimeType) {
		return context.getResources().getDrawable(resIdByMimeType(mimeType));
	}

	private static String[] getMimeTuple(String mime) {
		if (mime != null) {
			int i = mime.indexOf('/');
			if (i > 0 && i < mime.length() - 1) {
				return new String[] {
						mime.substring(0, i),
						mime.substring(i + 1)
				};
			}
		}
		return new String[] { TAG_UNKNOWN, TAG_UNKNOWN };
	}

	private static void initMimeMapAudio(Map<String, Integer> map) {
		Object[] mapped = {
				"application/ogg", R.drawable.mime_ogg,
				"application/x-flac", R.drawable.mime_audio,
				"audio/*", R.drawable.mime_audio,
				"audio/mpeg", R.drawable.mime_mp3,
				"audio/mpeg3", R.drawable.mime_mp3,
				"audio/ogg", R.drawable.mime_ogg,
				"audio/x-ms-wma", R.drawable.mime_wma,
				"audio/x-mpeg-3", R.drawable.mime_mp3,
				"audio/x-wav", R.drawable.mime_wav,
		};
		putToMap(map, mapped);
	}

	private static void initMimeMapCompressed(Map<String, Integer> map) {
		Object[] mapped = {
				"application/x-rar-compressed", R.drawable.mime_rar,
				"application/rar", R.drawable.mime_rar,
				"application/zip", R.drawable.mime_zip,
				// use zip icon as default compressed
				"application/mac-binhex40", R.drawable.mime_zip,
				"application/x-apple-diskimage", R.drawable.mime_zip,
				"application/x-debian-package", R.drawable.mime_zip,
				"application/x-gtar", R.drawable.mime_zip,
				"application/x-iso9660-image", R.drawable.mime_zip,
				"application/x-lha", R.drawable.mime_zip,
				"application/x-lzh", R.drawable.mime_zip,
				"application/x-lzx", R.drawable.mime_zip,
				"application/x-stuffit", R.drawable.mime_zip,
				"application/x-tar", R.drawable.mime_zip,
				"application/x-webarchive", R.drawable.mime_zip,
				"application/x-webarchive-xml", R.drawable.mime_zip,
				"application/gzip", R.drawable.mime_zip,
				"application/x-7z-compressed", R.drawable.mime_zip,
				"application/x-deb", R.drawable.mime_zip,
		};
		putToMap(map, mapped);
	}

	private static void initMimeMapDocument(Map<String, Integer> map) {
		Object[] mapped = {
				"text/*",
				R.drawable.mime_txt,
				"text/plain",
				R.drawable.mime_txt,

				// rtf
				"application/rtf",
				R.drawable.mime_rtf,

				// document
				"application/vnd.oasis.opendocument.text",
				R.drawable.mime_document,
				"application/vnd.oasis.opendocument.text-master",
				R.drawable.mime_document,
				"application/vnd.oasis.opendocument.text-template",
				R.drawable.mime_document,
				"application/vnd.oasis.opendocument.text-web",
				R.drawable.mime_document,
				"application/vnd.stardivision.writer",
				R.drawable.mime_document,
				"application/vnd.stardivision.writer-global",
				R.drawable.mime_document,
				"application/vnd.sun.xml.writer",
				R.drawable.mime_document,
				"application/vnd.sun.xml.writer.global",
				R.drawable.mime_document,
				"application/vnd.sun.xml.writer.template",
				R.drawable.mime_document,
				"application/x-abiword",
				R.drawable.mime_document,
				"application/x-kword",
				R.drawable.mime_document,

				// pdf
				"application/pdf",
				R.drawable.mime_pdf,

				// doc
				"application/msword",
				R.drawable.mime_doc,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
				R.drawable.mime_doc,
				"application/vnd.openxmlformats-officedocument.wordprocessingml.template",
				R.drawable.mime_doc,

				// xls
				"application/vnd.ms-excel",
				R.drawable.mime_xls,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				R.drawable.mime_xls,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.template",
				R.drawable.mime_xls,
				"application/vnd.oasis.opendocument.spreadsheet",
				R.drawable.mime_xls,
				"application/vnd.oasis.opendocument.spreadsheet-template",
				R.drawable.mime_xls,
				"application/vnd.stardivision.calc",
				R.drawable.mime_xls,
				"application/vnd.sun.xml.calc",
				R.drawable.mime_xls,
				"application/vnd.sun.xml.calc.template",
				R.drawable.mime_xls,
				"application/x-kspread",
				R.drawable.mime_xls,

				// ppt
				"application/vnd.ms-powerpoint",
				R.drawable.mime_ppt,
				"application/vnd.openxmlformats-officedocument.presentationml.presentation",
				R.drawable.mime_ppt,
				"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
				R.drawable.mime_ppt,
				"application/vnd.openxmlformats-officedocument.presentationml.template",
				R.drawable.mime_ppt,
				"application/vnd.openxmlformats-officedocument.presentationml.slide",
				R.drawable.mime_ppt,
		};
		putToMap(map, mapped);
	}

	private static void initMimeMapGeneric(Map<String, Integer> map) {
		Object[] mapped = {
				// certificate
				// "application/pgp-keys", R.drawable.mime_certificate,
				// "application/pgp-signature", R.drawable.mime_certificate,
				// "application/x-pkcs12", R.drawable.mime_certificate,
				// "application/x-pkcs7-certreqresp",
				// R.drawable.mime_certificate,
				// "application/x-pkcs7-crl",R.drawable.mime_certificate,
				// "application/x-x509-ca-cert", R.drawable.mime_certificate,
				// "application/x-x509-user-cert",R.drawable.mime_certificate,
				// "application/x-pkcs7-certificates",R.drawable.mime_certificate,
				// "application/x-pkcs7-mime", R.drawable.mime_certificate,
				// "application/x-pkcs7-signature",
				// R.drawable.mime_certificate,

				// source code
				// "application/rdf+xml", R.drawable.mime_src,
				// "application/rss+xml", R.drawable.mime_src,
				// "application/x-object", R.drawable.mime_src,
				// "application/xhtml+xml", R.drawable.mime_src,
				// "text/css", R.drawable.mime_src,
				// "text/html", R.drawable.mime_src,
				// "text/xml", R.drawable.mime_src,
				// "text/x-c++hdr", R.drawable.mime_src,
				// "text/x-c++src", R.drawable.mime_src,
				// "text/x-chdr", R.drawable.mime_src,
				// "text/x-csrc", R.drawable.mime_src,
				// "text/x-dsrc", R.drawable.mime_src,
				// "text/x-csh", R.drawable.mime_src,
				// "text/x-haskell", R.drawable.mime_src,
				// "text/x-java", R.drawable.mime_src,
				// "text/x-literate-haskell", R.drawable.mime_src,
				// "text/x-pascal", R.drawable.mime_src,
				// "text/x-tcl", R.drawable.mime_src,
				// "text/x-tex", R.drawable.mime_src,
				// "application/x-latex", R.drawable.mime_src,
				// "application/x-texinfo", R.drawable.mime_src,
				// "application/atom+xml", R.drawable.mime_src,
				// "application/ecmascript", R.drawable.mime_src,
				// "application/json", R.drawable.mime_src,
				// "application/javascript", R.drawable.mime_src,
				// "application/xml", R.drawable.mime_src,
				// "text/javascript", R.drawable.mime_src,
				// "application/x-javascript", R.drawable.mime_src,

				// contact
				// "text/x-vcard", R.drawable.mime_contact;
				// "text/vcard", R.drawable.mime_contact;

				// calendar event
				// "text/calendar", R.drawable.mime_event,
				// "text/x-vcalendar", R.drawable.mime_event,

				// font
				// "application/x-font", R.drawable.mime_font,
				// "application/font-woff", R.drawable.mime_font,
				// "application/x-font-woff", R.drawable.mime_font,
				// "application/x-font-ttf", R.drawable.mime_font,

				// package
				"application/vnd.android.package-archive",
				R.drawable.mime_apk,

				// generic
				"*/*",
				R.drawable.mime_file,
		};
		putToMap(map, mapped);
	}

	private static void initMimeMapImage(Map<String, Integer> map) {
		Object[] mapped = {
				"image/*",
				R.drawable.mime_image,
				"application/vnd.oasis.opendocument.graphics",
				R.drawable.mime_image,
				"application/vnd.oasis.opendocument.graphics-template",
				R.drawable.mime_image,
				"application/vnd.oasis.opendocument.image",
				R.drawable.mime_image,
				"application/vnd.stardivision.draw",
				R.drawable.mime_image,
				"application/vnd.sun.xml.draw",
				R.drawable.mime_image,
				"application/vnd.sun.xml.draw.template",
				R.drawable.mime_image,
		};
		putToMap(map, mapped);
	}

	private static void initMimeMapVideo(Map<String, Integer> map) {
		Object[] mapped = {
				"video/*",
				R.drawable.mime_video,
				"video/mp4",
				R.drawable.mime_mp4,
				"video/quicktime",
				R.drawable.mime_mov,
				"video/mpeg",
				R.drawable.mime_mpg,
				"application/x-quicktimeplayer",
				R.drawable.mime_video,
				"application/x-shockwave-flash",
				R.drawable.mime_video,
		};
		putToMap(map, mapped);
	}

	public static Map<String, Integer> mimeMap() {
		return mimeMap;
	}

	public static List<Map.Entry<String, Integer>> mimeListSorted() {
		if (sortedMimeList == null) {
			sortedMimeList = new ArrayList<Map.Entry<String, Integer>>(
					mimeMap.size());
			Set<Map.Entry<String, Integer>> entrySet = mimeMap.entrySet();
			Iterator<Map.Entry<String, Integer>> it = entrySet.iterator();
			while (it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				sortedMimeList.add(entry);
			}
			Collections.sort(sortedMimeList,
					new Comparator<Map.Entry<String, Integer>>() {
						@Override
						public int compare(Entry<String, Integer> lhs,
								Entry<String, Integer> rhs) {
							return lhs.getKey().compareTo(rhs.getKey());
						}
					});
		}
		return sortedMimeList;
	}

	public static boolean mimeMatches(String acceptable, String test) {
		if (acceptable == null) {
			// as if accepts all
			return true;
		}

		if (test == null) {
			return false;
		}

		String[] acceptableTuple = getMimeTuple(acceptable);
		String[] testTuple = getMimeTuple(test);
		return mimeTagMatches(acceptableTuple[0], testTuple[0])
				&& mimeTagMatches(acceptableTuple[1], testTuple[1]);
	}

	/**
	 * MIME, see http://en.wikipedia.org/wiki/MIME<br/>
	 */
	public static boolean mimeMatches(String[] acceptables, String[] tests) {
		if (acceptables == null) {
			// as if accepts all
			return true;
		}

		if (tests == null) {
			return false;
		}

		for (String acceptable : acceptables) {
			for (String test : tests) {
				if (mimeMatches(acceptable, test)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean mimeTagMatches(String acceptableTag, String testTag) {
		if (acceptableTag == null
				|| acceptableTag.equalsIgnoreCase(TAG_UNKNOWN)) {
			return false;
		}
		return acceptableTag.equalsIgnoreCase(TAG_ALL)
				|| acceptableTag.equalsIgnoreCase(testTag);
	}

	private static void putToMap(Map<String, Integer> map, Object[] mapped) {
		for (int i = 0; i < mapped.length; i += 2) {
			map.put((String) mapped[i], (Integer) mapped[i + 1]);
		}
	}

	private static int resIdByMimeType(String mimeType) {
		if (mimeType == null) {
			return mimeMap.get("*/*");
		}

		Integer resId = mimeMap.get(mimeType);
		if (resId != null) {
			return resId;
		}

		final String type = mimeType.split("/")[0];
		resId = mimeMap.get(type + "/*");
		if (resId != null) {
			return resId;
		}
		return mimeMap.get("*/*");
	}

	private static final String TAG_ALL = "*";

	private static final String TAG_UNKNOWN = "unknown";

	private static Map<String, Integer> mimeMap = null;
	private static List<Map.Entry<String, Integer>> sortedMimeList = null;

	static {
		mimeMap = new HashMap<String, Integer>();
		initMimeMapCompressed(mimeMap);
		initMimeMapAudio(mimeMap);
		initMimeMapImage(mimeMap);
		initMimeMapVideo(mimeMap);
		initMimeMapDocument(mimeMap);
		initMimeMapGeneric(mimeMap);
	}
}
