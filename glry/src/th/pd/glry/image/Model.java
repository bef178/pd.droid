package th.pd.glry.image;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.net.Uri;

import th.pd.common.android.mime.MimeTypeUtil;

class Model {

    private List<Uri> dataList = new LinkedList<Uri>();

    public boolean add(File file) {
        if (file != null) {
            if (file.isFile()
                    && MimeTypeUtil.isImage(
                            MimeTypeUtil.mimeTypeByFile(file))) {
                dataList.add(Uri.fromFile(file));
                return true;
            }
        }
        return false;
    }

    public void add(Uri uri) {
        dataList.add(uri);
    }

    public void clear() {
        dataList.clear();
    }

    public int getCount() {
        return dataList.size();
    }

    public Uri getData(int i) {
        if (hasIndex(i)) {
            return dataList.get(i);
        }
        return null;
    }

    public boolean hasIndex(int i) {
        return i >= 0 && i < dataList.size();
    }

    public int indexOf(Uri uri) {
        return dataList.indexOf(uri);
    }
}
