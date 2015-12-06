package th.pd.mail.dao;

/**
 * e.g. INBOX, trash, draft, etc
 */
public class MailDir {

    private int autoId = -1;
    private String addr;
    private String caption;
    private String path; // the tree hierarchy path XXX or use parentId to up-track
    private long lastSync; // timestamp in milliseconds, updated on sync done
    private int syncStatus;
    private int flags; // sync_periodly; visible;
    private int type;
    //	private String syncAuth;
    private int lastFullSync;

    private String remoteId;
    private String remotePath; // XXX or use remoteParentId

    public String getAddr() {
        return this.addr;
    }

    public int getAutoId() {
        return this.autoId;
    }

    // FIXME several unknown:
    // delimiter, syncLookback, lastTouchTime, uiSyncStatus, uiLastSyncResult,
    // lastNotifiedMessageKey, lastNotifiedMessageCount, hierarchicalName

    public String getCaption() {
        return this.caption;
    }

    public int getFlags() {
        return this.flags;
    }

    public long getLastSync() {
        return this.lastSync;
    }

    public String getPath() {
        return this.path;
    }

    public int getSyncStatus() {
        return this.syncStatus;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }
}
