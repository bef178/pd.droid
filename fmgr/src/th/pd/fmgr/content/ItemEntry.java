package th.pd.fmgr.content;

import java.io.File;

import android.annotation.SuppressLint;
import android.net.Uri;

import cc.typedef.droid.common.mime.MimeTypeUtil;

/**
 * the model indicating a row in content list view
 * @author tanghao
 *
 */
public class ItemEntry {

    private static String inferMimeType(Uri contentUri) {
        return MimeTypeUtil.mimeTypeByFile(
                new File(inferPhysicalPath(contentUri)));
    }

    private static String inferPhysicalPath(Uri contentUri) {
        //        List<String> pathSegmentList = contentUri.getPathSegments();
        //        String rootUid = pathSegmentList.remove(0);
        // TODO try get physical file path
        return null;
    }

    /**
     * display name in the screen
     */
    private CharSequence caption = null;

    /**
     * content uri with valid authority as the data to locate, view and share<br/>
     * <br/>
     * with following hierarchy so we can get something from the uri:<br/>
     * &emsp; "content://" authority "/" rootUid "/" path<br/>
     *
     */
    private Uri contentUri = null;

    /**
     * in some cases it cannot infer mime type from path, especially when the contentUri is raw<br/>
     * <br/>
     * "infer" happens when explicitly invoke #inferMimeType()
     */
    private String mimeType = null;

    /**
     * operations we can perform on this item<br/>
     * <br/>
     * initially inherited from parent
     */
    protected int flags = 0;

    public ItemEntry() {
        // dummy
    }

    public ItemEntry(String caption) {
        setCaption(caption);
    }

    public long getBytes() {
        return -1;
    }

    public CharSequence getCaption() {
        return caption;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public long getLastModified() {
        // TODO
        return -1;
    }

    public CharSequence getMimeType() {
        return mimeType;
    }

    @SuppressLint("Assert")
    public void inferMimeType() {
        assert contentUri != null;
        assert contentUri.isAbsolute();
        assert contentUri.isHierarchical();
        assert !contentUri.isOpaque();
        assert contentUri.getScheme().equals("content");
        this.mimeType = inferMimeType(contentUri);
    }

    public final void setCaption(CharSequence caption) {
        this.caption = caption;
    }

    public final void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public final void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
