package th.pd.common.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import libcore.net.MimeUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.DocumentsContract.Document;

public class MimeTypeUtil {

    private static final String TAG_ALL = "*";
    private static final String TAG_UNKNOWN = "unknown";

    private static Map<String, Integer> mimeType2resId = null;
    private static Map<String, Integer> audio2resId = null;
    private static Map<String, Integer> video2resId = null;
    private static Map<String, Integer> image2resId = null;
    private static List<Map.Entry<String, Integer>> mimeTypeSortedList = null;

    static {
        setupMimeTypeMap();
        setupAudioMap();
        setupVideoMap();
        setupImageMap();
    }

    private static void addToMap(Map<String, Integer> map, Object[] a) {
        for (int i = 0; i < a.length; i += 2) {
            map.put((String) a[i], (Integer) a[i + 1]);
        }
    }

    /**
     * "use strict";<br/>
     * return ["unknown", "unknown"] if incorrect grammar<br/>
     * always return a legal two-tuple
     */
    private static String[] divideMimeType(String mimeType) {
        if (mimeType != null) {
            int i = mimeType.indexOf('/');
            if (i > 0 && i < mimeType.length() - 1) {
                return new String[] {
                        mimeType.substring(0, i),
                        mimeType.substring(i + 1)
                };
            }
        }
        return new String[] {
                TAG_UNKNOWN,
                TAG_UNKNOWN
        };
    }

    public static Drawable drawableByMimeType(Context context,
            String mimeType) {
        int resId = resIdByMimeType(mimeType);
        return context.getResources().getDrawable(resId);
    }

    public static boolean isAudio(String mimeType) {
        return audio2resId.containsKey(mimeType);
    }

    public static boolean isImage(String mimeType) {
        return image2resId.containsKey(mimeType);
    }

    public static boolean isMedia(String mimeType) {
        return isImage(mimeType) || isVideo(mimeType) || isAudio(mimeType);
    }

    public static boolean isVideo(String mimeType) {
        return video2resId.containsKey(mimeType);
    }

    public static String mimeTypeByFile(File file) {
        if (file.isDirectory()) {
            // "vnd.android.document/directory"
            return Document.MIME_TYPE_DIR;
        }

        String path = file.getName();
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            String ext = path.substring(i + 1).toLowerCase();
            return MimeUtils.guessMimeTypeFromExtension(ext);
        }
        return null;
    }

    /**
     * <code>null</code> matches nothing<br/>
     */
    public static boolean mimeTypeMatches(String acceptable, String test) {
        if (acceptable == null) {
            return false;
        }

        if (test == null) {
            return false;
        }

        String[] acceptablePieces = divideMimeType(acceptable);
        String[] testPieces = divideMimeType(test);
        return mimeTypePieceMatches(acceptablePieces[0], testPieces[0])
                && mimeTypePieceMatches(acceptablePieces[1], testPieces[1]);
    }

    /**
     * MIME, see http://en.wikipedia.org/wiki/MIME<br/>
     * <code>null</code> matches nothing<br/>
     */
    public static boolean mimeTypeMatches(String[] acceptables,
            String[] tests) {
        if (acceptables == null) {
            return false;
        }

        if (tests == null) {
            return false;
        }

        for (String acceptable : acceptables) {
            for (String test : tests) {
                if (mimeTypeMatches(acceptable, test)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <code>null</code> matches nothing<br/>
     */
    private static boolean mimeTypePieceMatches(String piece,
            String testPiece) {
        if (piece == null || piece.equalsIgnoreCase(TAG_UNKNOWN)) {
            return false;
        }
        return piece.equalsIgnoreCase(TAG_ALL)
                || piece.equalsIgnoreCase(testPiece);
    }

    public static List<Map.Entry<String, Integer>> mimeTypeSortedList() {
        if (mimeTypeSortedList == null) {
            mimeTypeSortedList = new ArrayList<Map.Entry<String, Integer>>(
                    mimeType2resId.size() + audio2resId.size()
                            + image2resId.size() + video2resId.size());

            mimeTypeSortedList.addAll(mimeType2resId.entrySet());
            mimeTypeSortedList.addAll(audio2resId.entrySet());
            mimeTypeSortedList.addAll(video2resId.entrySet());
            mimeTypeSortedList.addAll(image2resId.entrySet());

            Collections.sort(mimeTypeSortedList,
                    new Comparator<Map.Entry<String, Integer>>() {

                        @Override
                        public int compare(Entry<String, Integer> lhs,
                                Entry<String, Integer> rhs) {
                            return lhs.getKey().compareTo(rhs.getKey());
                        }
                    });
        }
        return mimeTypeSortedList;
    }

    /**
     * return at least the resId of generic file<br/>
     */
    public static int resIdByMimeType(String mimeType) {
        if (mimeType != null) {
            Integer resId = resIdByMimeTypeExactly(mimeType);
            if (resId != null) {
                return resId;
            }

            resId = resIdByMimeTypeExactly(divideMimeType(mimeType)[0]
                    + "/*");
            if (resId != null) {
                return resId;
            }
        }
        return mimeType2resId.get("*/*");
    }

    private static Integer resIdByMimeTypeExactly(String mimeType) {
        if (mimeType != null) {
            Integer resId = mimeType2resId.get(mimeType);
            if (resId != null) {
                return resId;
            }
            resId = audio2resId.get(mimeType);
            if (resId != null) {
                return resId;
            }
            resId = video2resId.get(mimeType);
            if (resId != null) {
                return resId;
            }
            resId = image2resId.get(mimeType);
            if (resId != null) {
                return resId;
            }
        }
        return null;
    }

    private static void setupAudioMap() {
        audio2resId = new HashMap<String, Integer>();
        addToMap(audio2resId, new Object[] {
                "application/ogg",
                R.drawable.mime_audio,
                "application/x-flac",
                R.drawable.mime_audio,
                "audio/*",
                R.drawable.mime_audio,
                "audio/mpeg",
                R.drawable.mime_mp3,
                "audio/mpeg3",
                R.drawable.mime_mp3,
                "audio/ogg",
                R.drawable.mime_audio,
                "audio/x-mpeg-3",
                R.drawable.mime_mp3,
                "audio/x-ms-wma",
                R.drawable.mime_audio,
                "audio/x-wav",
                R.drawable.mime_audio,
        });

    }

    private static void setupImageMap() {
        image2resId = new HashMap<String, Integer>();
        addToMap(image2resId, new Object[] {
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
        });
    }

    private static void setupMimeTypeMap() {
        mimeType2resId = new HashMap<String, Integer>();

        // generic
        addToMap(mimeType2resId, new Object[] {
                // source code
                "application/rdf+xml",
                R.drawable.mime_text,
                "application/rss+xml",
                R.drawable.mime_text,
                "application/x-object",
                R.drawable.mime_text,
                "application/xhtml+xml",
                R.drawable.mime_text,
                "text/css",
                R.drawable.mime_text,
                "text/html",
                R.drawable.mime_text,
                "text/xml",
                R.drawable.mime_text,
                "text/x-c++hdr",
                R.drawable.mime_text,
                "text/x-c++src",
                R.drawable.mime_text,
                "text/x-chdr",
                R.drawable.mime_text,
                "text/x-csrc",
                R.drawable.mime_text,
                "text/x-dsrc",
                R.drawable.mime_text,
                "text/x-csh",
                R.drawable.mime_text,
                "text/x-haskell",
                R.drawable.mime_text,
                "text/x-java",
                R.drawable.mime_text,
                "text/x-literate-haskell",
                R.drawable.mime_text,
                "text/x-pascal",
                R.drawable.mime_text,
                "text/x-tcl",
                R.drawable.mime_text,
                "text/x-tex",
                R.drawable.mime_text,
                "application/x-latex",
                R.drawable.mime_text,
                "application/x-texinfo",
                R.drawable.mime_text,
                "application/atom+xml",
                R.drawable.mime_text,
                "application/ecmascript",
                R.drawable.mime_text,
                "application/json",
                R.drawable.mime_text,
                "application/javascript",
                R.drawable.mime_text,
                "application/xml",
                R.drawable.mime_text,
                "text/javascript",
                R.drawable.mime_text,
                "application/x-javascript",
                R.drawable.mime_text,

                // package
                "application/vnd.android.package-archive",
                R.drawable.mime_apk,

                // generic
                "*/*",
                R.drawable.mime_file,
        });

        // document
        addToMap(
                mimeType2resId,
                new Object[] {
                        // text
                        "text/*",
                        R.drawable.mime_text,
                        "text/plain",
                        R.drawable.mime_text,

                        // rtf
                        "application/rtf",
                        R.drawable.mime_doc,

                        // document
                        "application/vnd.oasis.opendocument.text",
                        R.drawable.mime_doc,
                        "application/vnd.oasis.opendocument.text-master",
                        R.drawable.mime_doc,
                        "application/vnd.oasis.opendocument.text-template",
                        R.drawable.mime_doc,
                        "application/vnd.oasis.opendocument.text-web",
                        R.drawable.mime_doc,
                        "application/vnd.stardivision.writer",
                        R.drawable.mime_doc,
                        "application/vnd.stardivision.writer-global",
                        R.drawable.mime_doc,
                        "application/vnd.sun.xml.writer",
                        R.drawable.mime_doc,
                        "application/vnd.sun.xml.writer.global",
                        R.drawable.mime_doc,
                        "application/vnd.sun.xml.writer.template",
                        R.drawable.mime_doc,
                        "application/x-abiword",
                        R.drawable.mime_doc,
                        "application/x-kword",
                        R.drawable.mime_doc,

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
                });

        // compressed
        addToMap(mimeType2resId, new Object[] {
                "application/x-rar-compressed",
                R.drawable.mime_rar,
                "application/rar",
                R.drawable.mime_rar,
                "application/zip",
                R.drawable.mime_zip,
                // use zip icon as default compressed
                "application/mac-binhex40",
                R.drawable.mime_zip,
                "application/x-apple-diskimage",
                R.drawable.mime_zip,
                "application/x-debian-package",
                R.drawable.mime_zip,
                "application/x-gtar",
                R.drawable.mime_zip,
                "application/x-iso9660-image",
                R.drawable.mime_zip,
                "application/x-lha",
                R.drawable.mime_zip,
                "application/x-lzh",
                R.drawable.mime_zip,
                "application/x-lzx",
                R.drawable.mime_zip,
                "application/x-stuffit",
                R.drawable.mime_zip,
                "application/x-tar",
                R.drawable.mime_zip,
                "application/x-webarchive",
                R.drawable.mime_zip,
                "application/x-webarchive-xml",
                R.drawable.mime_zip,
                "application/gzip",
                R.drawable.mime_zip,
                "application/x-7z-compressed",
                R.drawable.mime_zip,
                "application/x-deb",
                R.drawable.mime_zip,
        });
    }

    private static void setupVideoMap() {
        video2resId = new HashMap<String, Integer>();
        addToMap(video2resId, new Object[] {
                "video/*",
                R.drawable.mime_video,
                "video/mp4",
                R.drawable.mime_video,
                "video/quicktime",
                R.drawable.mime_video,
                "video/mpeg",
                R.drawable.mime_video,
                "application/x-quicktimeplayer",
                R.drawable.mime_video,
                "application/x-shockwave-flash",
                R.drawable.mime_video,
        });
    }
}
