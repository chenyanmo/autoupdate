package com.android.update.fir;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.update.utils.AppUtils;
import com.android.update.R;

import java.io.File;

class FirUpdaterTask extends AsyncTask<Void, Void, Boolean> {

    private boolean isManual = false;
    private String appId;
    private String appVersionUrl;
    private String apkPath;
    private FirAppInfo.AppInfo appInfo;
    private boolean isBackgroundDownload = false;
    private boolean forceShowDialog = false;

    private FirDialog firDialog;
    private FirDownloader firDownloader;
    private FirNotification firNotification;

    private Context mContext;
    private int mType;

    FirUpdaterTask(Context context, String appId, String appVersionUrl, String apkPath, boolean showProgressDialog, boolean isManual) {

        this.mContext = context;
        this.appId = appId;
        this.appVersionUrl = appVersionUrl;
        this.apkPath = apkPath;
        this.forceShowDialog = showProgressDialog;
        this.isManual = isManual;

    }

    @Override
    protected void onPreExecute() {
        if (forceShowDialog) {
            if (isManual) {
                Toast.makeText(mContext, R.string.android_auto_update_dialog_checking, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPostExecute(Boolean result) {

        if (result || forceShowDialog) {
            initFirDialog();
        } else {
            if (isManual) {
                Toast.makeText(mContext, R.string.android_auto_update_toast_no_new_update, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected Boolean doInBackground(Void... args) {
        String result = null;
        appInfo = new FirAppInfo().requestAppInfo(appVersionUrl);
        if (appInfo == null) {
            return null;
        }

        String apkName = appInfo.appName + "-" + appInfo.appVersionName + ".apk";
        if (TextUtils.isEmpty(apkPath)) {
            apkPath = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator;
            File file = new File(apkPath);
            if (file == null || !file.isDirectory()) {
                file.mkdirs();
            }

        }


        appInfo.appId = appId;
        appInfo.apkName = apkName;
        appInfo.apkPath = apkPath;
        appInfo.apkLocalUrl = apkPath + apkName;
        FirUpdaterUtils.logger(appInfo.toString());

        boolean needUpdate = appInfo.appVersionCode > FirUpdaterUtils.getVersionCode(mContext);

        return needUpdate;
    }

    private void initFirDialog() {

        firDialog = new FirDialog();
        firDialog.showAppInfoDialog(mContext, appInfo);
        firDialog.setOnClickDownloadDialogListener(new FirDialog.OnClickDownloadDialogListener() {
            @Override
            public void onClickDownload(DialogInterface dialog) {
                downloadApk();
            }

            @Override
            public void onClickBackgroundDownload(DialogInterface dialog) {
                isBackgroundDownload = true;
            }

            @Override
            public void onClickCancelDownload(DialogInterface dialog) {
                firDownloader.cancel();
            }
        });
    }

    private void downloadApk() {
        File apkFile = new File(appInfo.apkLocalUrl);
        if (apkFile.exists()) {
            FirUpdaterUtils.installApk(mContext, appInfo.apkLocalUrl);
            return;
        }

        firNotification = new FirNotification();
        firNotification.createBuilder(mContext);
        firNotification.setContentTitle("正在下载" + appInfo.appName);

        firDownloader = new FirDownloader(mContext.getApplicationContext(), appInfo);
        firDownloader.setOnDownLoadListener(new FirDownloader.OnDownLoadListener() {
            @Override
            public void onProgress(int progress) {
                firDialog.showDownloadDialog(mContext, progress);
                if (isBackgroundDownload) {
                    firNotification.setContentText(progress + "%");
                    firNotification.notifyNotification(progress);
                }
            }

            @Override
            public void onSuccess() {
                firDialog.dismissDownloadDialog();
                if (isBackgroundDownload) {
                    firNotification.cancel();
                }
                FirUpdaterUtils.installApk(mContext, appInfo.apkLocalUrl);
            }

            @Override
            public void onError() {

            }
        });
        firDownloader.downloadApk();

//        AppUtils.goToDownload(mContext, appInfo.appInstallUrl, appInfo.apkName, isBackgroundDownload);
    }
}
