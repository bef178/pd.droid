package th.pd.common.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class DataNetworkUtil {

    public static boolean isConnected(Context context) {
        return getConnected(context) != null;
    }

    public static boolean isMobile(Context context) {
        NetworkInfo connected = getConnected(context);
        return connected != null
                && connected.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isWifi(Context context) {
        NetworkInfo connected = getConnected(context);
        return connected != null
                && connected.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private static NetworkInfo getConnected(Context context) {
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
}
