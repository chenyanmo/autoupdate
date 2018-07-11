package com.android.update.fir;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.update.utils.PermissionHelper;


public class FirUpdater {

    private Context context;
    private String apiToken;
    private String appId;
    private String appVersionUrl;
    private String apkPath;
    private FirAppInfo.AppInfo appInfo;
    private boolean isBackgroundDownload = false;
    private boolean forceShowDialog = false;
    private boolean isManual = false;

    public FirUpdater(Context context) {
        this(context, null, null);
    }

    public FirUpdater(Context context, String apiToken, String appId) {
        this.context = context;
        this.apiToken = apiToken;
        this.appId = appId;
    }

    public FirUpdater apiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public FirUpdater appId(String appId) {
        this.appId = appId;
        return this;
    }

    public FirUpdater apkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public FirUpdater forceShowDialog(boolean enable) {
        this.forceShowDialog = enable;
        return this;
    }

    public void checkVersion() {
        checkVersion(false);
    }

    public void checkVersion(boolean manual) {
        this.isManual = manual;
        if (TextUtils.isEmpty(apiToken) || TextUtils.isEmpty(appId)) {
            Toast.makeText(context, "请设置 API TOKEN && APP ID", Toast.LENGTH_LONG).show();
            return;
        }

        this.appVersionUrl = "http://api.fir.im/apps/latest/" + appId + "?api_token=" + apiToken;

        PermissionHelper.getInstant().requestPermission(context, new PermissionHelper.OnPermissionCallback() {
            @Override
            public void onGranted() {
                requestAppInfo();
            }

            @Override
            public void onDenied() {
                FirUpdaterUtils.loggerError("申请权限未通过");
            }
        });
    }

    private void requestAppInfo() {

        if (isManual) {
            Toast.makeText(context, "正在获取应用包信息", Toast.LENGTH_LONG).show();
        }

        if (context != null) {
            new FirUpdaterTask(context, appId, appVersionUrl, apkPath, forceShowDialog, isManual).execute();
        } else {
            Toast.makeText(context, "The arg context is null", Toast.LENGTH_LONG).show();
        }
    }
}
