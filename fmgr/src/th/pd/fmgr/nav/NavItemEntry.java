package th.pd.fmgr.nav;

import java.io.File;

import android.net.Uri;
import android.provider.DocumentsContract.Document;

import th.pd.fmgr.content.ItemEntry;

public class NavItemEntry extends ItemEntry {

    public NavItemEntry by(File file) {
        if (file != null) {
            // FIXME would be a content uri
            setContentUri(Uri.fromFile(file));
            setMimeType(Document.MIME_TYPE_DIR);
            setCaption(file.getName());
        }
        return this;
    }
}
