package th.pd.mail;

import java.util.Locale;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MailApp extends Application {

    public static String appName = "pd.mail";
    public static String packageName = "unknown";
    public static int versionCode = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        setInfo();
    }

    private void setInfo() {
        Resources defRes = getResources();
        AssetManager assets = defRes.getAssets();
        DisplayMetrics metrics = defRes.getDisplayMetrics();
        Configuration config = new Configuration(getResources().getConfiguration());
        config.locale = new Locale("en");
        Resources res = new Resources(assets, metrics, config);
        appName = res.getString(R.string.app_label);

        packageName = getPackageName();

        try {
            versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // dummy
        }
    }
}
