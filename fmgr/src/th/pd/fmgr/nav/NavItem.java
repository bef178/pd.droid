package th.pd.fmgr.nav;

import java.io.File;

import android.net.Uri;

public class NavItem {

    /**
     * display name in the screen
     */
    protected CharSequence caption = null;

    /**
     * the data to locate, share and view
     */
    protected Uri uri = null;

    protected CharSequence getCaption() {
        return caption;
    }

    public Uri getUri() {
        return uri;
    }

    public NavItem initializeBy(File file) {
        if (file != null) {
            this.uri = Uri.fromFile(file);
            this.caption = file.getName();
        }
        return this;
    }

    public void setCaption(CharSequence caption) {
        this.caption = caption;
    }
}
