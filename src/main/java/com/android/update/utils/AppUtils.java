package com.android.update.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.update.DownloadService;


/**
 * Description :
 * User: chenyanmo(master@chenyanmo.com)
 * Date: 2016-07-05
 * Time: 16:27
 */
public class AppUtils {

    public static int getVersionCode(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return 0;
    }

    public static String getVersionName(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        return "";
    }

    public static void goToDownload(Context context, String downloadUrl, String downloadName, boolean bSilent) {
        if (DownloadService.isRunning()) {
            return;
        }
        //经过和OPPO工程师沟通，这个问题的原因是OPPO手机自动熄屏一段时间后，会启用系统自带的电量优化管理，禁止一切自启动的APP（用户设置的自启动白名单除外）。所以，类似的崩溃常常集中在用户休息之后的夜里或者凌晨，但是并不影响用户平时的正常使用。至于会出现user 0 is restricted，我觉得是coloros系统电量优化管理做得不好的地方。
        try {
            Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
            intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
            intent.putExtra(Constants.APK_DOWNLOAD_NAME, downloadName);
            intent.putExtra(Constants.APK_DOWNLOAD_MODE, bSilent);
            context.startService(intent);
        } catch (Exception e) {
            Log.d("AppUtils", "goToDownload: user 0 is restricted Error");
        }
    }

    public static void goToDownload(Context context, String downloadUrl, boolean bSilent) {
        goToDownload(context, downloadUrl, null, bSilent);
    }
}
