package t.typedef.droid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class DataNetworkUtility {

    private static NetworkInfo getConnectedNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                return networkInfo;
            }
        }
        return null;
    }

    public static boolean isConnected(Context context) {
        return getConnectedNetwork(context) != null;
    }

    public static boolean isMobile(Context context) {
        NetworkInfo connected = getConnectedNetwork(context);
        return connected != null
                && connected.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isWifi(Context context) {
        NetworkInfo connected = getConnectedNetwork(context);
        return connected != null
                && connected.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private DataNetworkUtility() {
        // dummy
    }
}
