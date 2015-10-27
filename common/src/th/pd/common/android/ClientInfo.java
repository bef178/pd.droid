package th.pd.common.android;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * basically, any info about the device could be retrieved from this class<br/>
 * but some(not all) will be saved into the object<br/>
 * <br/>
 * adt json ClientInfo {
 *   device: string optional eg("")
 *   android: string optional eg("4.4.2")
 *   build: string required
 *   serial: string optional eg("unknown")
 *   androidId: string optional
 *   deviceId: string optional default(null)
 *   subscriberId: string optional default(null)
 *   wifiMac: string optional eg("34:17:eb:bf:28:3d")
 * }
 *
 * @author tanghao
 */
public class ClientInfo {

    private static final String KEY_DEVICE = "device";
    private static final String KEY_ANDROID = "android";
    private static final String KEY_BUILD = "build";
    private static final String KEY_SERIAL = "serial";
    private static final String KEY_ANDROID_ID = "android_id";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_SUBSCRIBER_ID = "subscriber_id";
    private static final String KEY_WIFI_MAC = "wifi_mac";

    public static ClientInfo create(Context context) {
        ClientInfo client = new ClientInfo();

        client.device = Build.DEVICE;
        client.android = Build.VERSION.RELEASE;
        client.build = Build.VERSION.INCREMENTAL;
        client.serial = getSerial();
        client.androidId = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        client.deviceId = tm.getDeviceId();
        client.subscriberId = tm.getSubscriberId();
        client.wifiMac = getWifiMacAddr(context);

        return client;
    }

    public static ClientInfo from(JSONObject json) {
        ClientInfo client = new ClientInfo();
        client.device = json.optString(KEY_DEVICE, null);
        client.android = json.optString(KEY_ANDROID, null);
        client.build = json.optString(KEY_BUILD, null);
        client.serial = json.optString(KEY_SERIAL, null);
        client.androidId = json.optString(KEY_ANDROID_ID, null);
        client.deviceId = json.optString(KEY_DEVICE_ID, null);
        client.subscriberId = json.optString(KEY_SUBSCRIBER_ID, null);
        client.wifiMac = json.optString(KEY_WIFI_MAC, null);
        return client;
    }

    public static int getScreenDpi(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    private static String getSerial() {
        String serial = Build.SERIAL;
        if (serial.equals("unknown") || serial.equals("0123456789ABCDEF")) {
            serial = null;
        }
        return serial;
    }

    private static String getWifiMacAddr(Context context) {
        WifiManager wm = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        return wi.getMacAddress();
    }

    public String device = null; // ro.product.device
    public String android = null; // ro.build.version.release
    public String build = null; // ro.build.version.incremental
    public String serial;
    public String androidId; // will change if factory reset
    public String deviceId = null; // IMEI/MEID
    public String subscriberId = null; // IMSI
    public String wifiMac = null; // wlan0

    public JSONObject toJson() throws JSONException {
        return toJson(new JSONObject());
    }

    public JSONObject toJson(JSONObject json) throws JSONException {
        json.put(KEY_DEVICE, this.device);
        json.put(KEY_ANDROID, this.android);
        json.put(KEY_BUILD, this.build);
        json.put(KEY_SERIAL, this.serial);
        json.put(KEY_ANDROID_ID, this.androidId);
        json.put(KEY_DEVICE_ID, this.deviceId);
        json.put(KEY_SUBSCRIBER_ID, this.subscriberId);
        json.put(KEY_WIFI_MAC, this.wifiMac);
        return json;
    }

    @Override
    public String toString() {
        try {
            return "clientInfo:" + this.toJson().toString();
        } catch (JSONException e) {
            return super.toString();
        }
    }
}
