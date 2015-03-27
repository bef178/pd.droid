package th.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.DocumentsContract.Document;
import android.util.SparseIntArray;

import libcore.net.MimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MimeUtil {

    private static Map<String, Integer> mime2res = null;

    private static List<Map.Entry<String, Integer>> mimeListSorted = null;

    private static SparseIntArray res2large = null;

    private static final String TAG_ALL = "*";

    private static final String TAG_UNKNOWN = "unknown";

    static {
        mime2res = new HashMap<String, Integer>();
        initMimeMapCompressed(mime2res);
        initMimeMapAudio(mime2res);
        initMimeMapImage(mime2res);
        initMimeMapVideo(mime2res);
        initMimeMapDocument(mime2res);
        initMimeMapGeneric(mime2res);

        res2large = new SparseIntArray();
        initIconMapLarge(res2large);
    }

    public static Drawable drawableByMimeType(Context context, String mimeType) {
        return context.getResources().getDrawable(resIdByMimeType(mimeType));
    }

    public static Drawable drawableLargeByMimeType(Context context,
            String mimeType) {
        int resId = resIdByMimeType(mimeType);
        return context.getResources().getDrawable(res2large.get(resId));
    }

    private static void initIconMapLarge(SparseIntArray map) {
        int[] mapped = {
                R.drawable.mime_apk, R.drawable.mime_apk_156,
                R.drawable.mime_generic_audio,
                R.drawable.mime_generic_audio_156,
                R.drawable.mime_certificate, R.drawable.mime_certificate_156,
                R.drawable.mime_generic_code,
                R.drawable.mime_generic_code_156,
                R.drawable.mime_contact, R.drawable.mime_contact_156,
                R.drawable.mime_doc, R.drawable.mime_doc_156,
                R.drawable.mime_document, R.drawable.mime_document_156,
                R.drawable.mime_event, R.drawable.mime_event_156,
                R.drawable.mime_generic_file,
                R.drawable.mime_generic_file_156,
                R.drawable.mime_generic_image,
                R.drawable.mime_generic_image_156,
                R.drawable.mime_log, R.drawable.mime_log_156,
                R.drawable.mime_m4v, R.drawable.mime_m4v_156,
                R.drawable.mime_mov, R.drawable.mime_mov_156,
                R.drawable.mime_mp3, R.drawable.mime_mp3_156,
                R.drawable.mime_mp4, R.drawable.mime_mp4_156,
                R.drawable.mime_mpg, R.drawable.mime_mpg_156,
                R.drawable.mime_ogg, R.drawable.mime_ogg_156,
                R.drawable.mime_pdf, R.drawable.mime_pdf_156,
                R.drawable.mime_ppt, R.drawable.mime_ppt_156,
                R.drawable.mime_rar, R.drawable.mime_rar_156,
                R.drawable.mime_rtf, R.drawable.mime_rtf_156,
                R.drawable.mime_txt, R.drawable.mime_txt_156,
                R.drawable.mime_generic_video,
                R.drawable.mime_generic_video_156,
                R.drawable.mime_wav, R.drawable.mime_wav_156,
                R.drawable.mime_wma, R.drawable.mime_wma_156,
                R.drawable.mime_xls, R.drawable.mime_xls_156,
                R.drawable.mime_zip, R.drawable.mime_zip_156,

        };
        for (int i = 0; i < mapped.length; i += 2) {
            map.put(mapped[i], mapped[i + 1]);
        }
    }

    private static void initMimeMapAudio(Map<String, Integer> map) {
        Object[] mapped = {
                "application/ogg", R.drawable.mime_ogg,
                "application/x-flac", R.drawable.mime_generic_audio,
                "audio/*", R.drawable.mime_generic_audio,
                "audio/mpeg", R.drawable.mime_mp3,
                "audio/mpeg3", R.drawable.mime_mp3,
                "audio/ogg", R.drawable.mime_ogg,
                "audio/x-mpeg-3", R.drawable.mime_mp3,
                "audio/x-ms-wma", R.drawable.mime_wma,
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
                "application/rdf+xml", R.drawable.mime_generic_code,
                "application/rss+xml", R.drawable.mime_generic_code,
                "application/x-object", R.drawable.mime_generic_code,
                "application/xhtml+xml", R.drawable.mime_generic_code,
                "text/css", R.drawable.mime_generic_code,
                "text/html", R.drawable.mime_generic_code,
                "text/xml", R.drawable.mime_generic_code,
                "text/x-c++hdr", R.drawable.mime_generic_code,
                "text/x-c++src", R.drawable.mime_generic_code,
                "text/x-chdr", R.drawable.mime_generic_code,
                "text/x-csrc", R.drawable.mime_generic_code,
                "text/x-dsrc", R.drawable.mime_generic_code,
                "text/x-csh", R.drawable.mime_generic_code,
                "text/x-haskell", R.drawable.mime_generic_code,
                "text/x-java", R.drawable.mime_generic_code,
                "text/x-literate-haskell", R.drawable.mime_generic_code,
                "text/x-pascal", R.drawable.mime_generic_code,
                "text/x-tcl", R.drawable.mime_generic_code,
                "text/x-tex", R.drawable.mime_generic_code,
                "application/x-latex", R.drawable.mime_generic_code,
                "application/x-texinfo", R.drawable.mime_generic_code,
                "application/atom+xml", R.drawable.mime_generic_code,
                "application/ecmascript", R.drawable.mime_generic_code,
                "application/json", R.drawable.mime_generic_code,
                "application/javascript", R.drawable.mime_generic_code,
                "application/xml", R.drawable.mime_generic_code,
                "text/javascript", R.drawable.mime_generic_code,
                "application/x-javascript", R.drawable.mime_generic_code,

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
                R.drawable.mime_generic_file,
        };
        putToMap(map, mapped);
    }

    private static void initMimeMapImage(Map<String, Integer> map) {
        Object[] mapped = {
                "image/*",
                R.drawable.mime_generic_image,
                "application/vnd.oasis.opendocument.graphics",
                R.drawable.mime_generic_image,
                "application/vnd.oasis.opendocument.graphics-template",
                R.drawable.mime_generic_image,
                "application/vnd.oasis.opendocument.image",
                R.drawable.mime_generic_image,
                "application/vnd.stardivision.draw",
                R.drawable.mime_generic_image,
                "application/vnd.sun.xml.draw",
                R.drawable.mime_generic_image,
                "application/vnd.sun.xml.draw.template",
                R.drawable.mime_generic_image,
        };
        putToMap(map, mapped);
    }

    private static void initMimeMapVideo(Map<String, Integer> map) {
        Object[] mapped = {
                "video/*",
                R.drawable.mime_generic_video,
                "video/mp4",
                R.drawable.mime_mp4,
                "video/quicktime",
                R.drawable.mime_mov,
                "video/mpeg",
                R.drawable.mime_mpg,
                "application/x-quicktimeplayer",
                R.drawable.mime_generic_video,
                "application/x-shockwave-flash",
                R.drawable.mime_generic_video,
        };
        putToMap(map, mapped);
    }

    public static boolean isAudio(String mime) {
        return mime2Tuple(mime)[0].equals("audio");
    }

    public static boolean isImage(File file) {
        return isImage(mimeTypeByPath(file.getPath()));
    }

    public static boolean isImage(String mime) {
        return mime2Tuple(mime)[0].equals("image");
    }

    public static boolean isMedia(String mime) {
        return isImage(mime) || isVideo(mime) || isAudio(mime);
    }

    public static boolean isVideo(String mime) {
        return mime2Tuple(mime)[0].equals("video");
    }

    private static String[] mime2Tuple(String mime) {
        if (mime != null) {
            int i = mime.indexOf('/');
            if (i > 0 && i < mime.length() - 1) {
                return new String[] {
                        mime.substring(0, i),
                        mime.substring(i + 1)
                };
            }
        }
        return new String[] {
                TAG_UNKNOWN, TAG_UNKNOWN
        };
    }

    public static List<Map.Entry<String, Integer>> mimeListSorted() {
        if (mimeListSorted == null) {
            mimeListSorted = new ArrayList<Map.Entry<String, Integer>>(
                    mime2res.size());
            Set<Map.Entry<String, Integer>> entrySet = mime2res.entrySet();
            Iterator<Map.Entry<String, Integer>> it = entrySet.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                mimeListSorted.add(entry);
            }
            Collections.sort(mimeListSorted,
                    new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Entry<String, Integer> lhs,
                                Entry<String, Integer> rhs) {
                            return lhs.getKey().compareTo(rhs.getKey());
                        }
                    });
        }
        return mimeListSorted;
    }

    public static boolean mimeMatches(String acceptable, String test) {
        if (acceptable == null) {
            // as if accepts all
            return true;
        }

        if (test == null) {
            return false;
        }

        String[] acceptableTuple = mime2Tuple(acceptable);
        String[] testTuple = mime2Tuple(test);
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

    public static String mimeTypeByFile(File file) {
        if (file.isDirectory()) {
            // "vnd.android.document/directory"
            return Document.MIME_TYPE_DIR;
        } else {
            return mimeTypeByPath(file.getName());
        }
    }

    private static String mimeTypeByPath(String path) {
        int dot = path.lastIndexOf('.');
        if (dot >= 0) {
            String ext = path.substring(dot + 1).toLowerCase();
            return MimeUtils.guessMimeTypeFromExtension(ext);
        }
        return null;
    }

    private static void putToMap(Map<String, Integer> map, Object[] mapped) {
        for (int i = 0; i < mapped.length; i += 2) {
            map.put((String) mapped[i], (Integer) mapped[i + 1]);
        }
    }

    public static int resIdByMimeType(String mimeType) {
        if (mimeType == null) {
            return mime2res.get("*/*");
        }

        Integer resId = mime2res.get(mimeType);
        if (resId != null) {
            return resId;
        }

        final String type = mimeType.split("/")[0];
        resId = mime2res.get(type + "/*");
        if (resId != null) {
            return resId;
        }
        return mime2res.get("*/*");
    }
}
