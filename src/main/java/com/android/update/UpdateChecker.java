package com.android.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.update.utils.AppUtils;
import com.android.update.utils.Constants;
import com.android.update.utils.PermissionHelper;

/**
 * Description :更新服务
 * User: chenyanmo(master@chenyanmo.com)
 * Date: 2016/9/30
 * Time: 16:27
 * <p>
 * //==使用默认升级==注意配置Constatns==
 * UpdateChecker.checkForDialog(this);
 * //UpdateChecker.checkForNotification(this);
 * //UpdateChecker.checkForSilent(this);
 * //==使用自定义==
 * UpdateChecker updateChecker = new UpdateChecker();
 * updateChecker.initCheckForDialog(mContext);
 * //updateChecker.initCheckForNotification(mContext);
 * //updateChecker.initCheckForSilent(mContext);
 * 需在检测回调中调用下面三个方法
 * updateChecker.onPreExecute()
 * updateChecker.onPostExecute()
 * updateChecker.onPostNoDetectedDismiss()
 *
 */
public class UpdateChecker {

    private MaterialDialog dialog;
    private Context mContext;
    private int mType;
    private boolean mShowProgressDialog;

    /**
     * 弹出更新提示对话框
     * @param context
     */
    public static void checkForDialog(final Context context) {
        if (context != null) {

            PermissionHelper.getInstant().requestPermission(context, new PermissionHelper.OnPermissionCallback() {
                @Override
                public void onGranted() {
                    new CheckUpdateTask(context, Constants.TYPE_DIALOG, true).execute();
                }

                @Override
                public void onDenied() {
                    Toast.makeText(context,"申请权限未通过",Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }
    }

    /**
     * 弹出更新提示通知栏
     * @param context
     */
    public static void checkForNotification(final Context context) {
        if (context != null) {

            PermissionHelper.getInstant().requestPermission(context, new PermissionHelper.OnPermissionCallback() {
                @Override
                public void onGranted() {
                    new CheckUpdateTask(context, Constants.TYPE_NOTIFICATION, false).execute();
                }

                @Override
                public void onDenied() {
                    Toast.makeText(context,"申请权限未通过",Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }

    }
    /**
     * 静默更新
     * @param context
     */
    public static void checkForSilent(final Context context) {
        if (context != null) {
            PermissionHelper.getInstant().requestPermission(context, new PermissionHelper.OnPermissionCallback() {
                @Override
                public void onGranted() {
                    new CheckUpdateTask(context, Constants.TYPE_SILENT, false).execute();
                }

                @Override
                public void onDenied() {

                }
            });
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }

    }

    /**
     * 检查更新预方法
     */
    public void onPreExecute() {
        if (mShowProgressDialog && mType!=Constants.TYPE_SILENT) {
            dialog = new MaterialDialog.Builder(mContext)
                    .backgroundColorRes(R.color.material_dialog_background_color)
                    .titleColorRes(R.color.material_dialog_title_text_color)
                    .contentColorRes(R.color.material_dialog_content_text_color)
                    .title(mContext.getString(R.string.android_auto_update_dialog_tip))
                    .canceledOnTouchOutside(false)
                    .content(mContext.getString(R.string.android_auto_update_dialog_checking))
                    .progress(true, 0)
//                    .progressIndeterminateStyle(true)
                    .show();
//            dialog = new ProgressDialog(mContext);
//            dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
//            dialog.show();
        }
    }

    /**
     * 检查更新结果
     * @param updateMessage 更新日志
     * @param apkUrl 更新地址
     */
    public void onPostExecute(String updateMessage, String apkUrl) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(apkUrl)) {
            if (mType == Constants.TYPE_NOTIFICATION) {
                showNotification(mContext, updateMessage, apkUrl);
            } else if (mType == Constants.TYPE_DIALOG) {
                showDialog(mContext, updateMessage, apkUrl);
            }else if (mType == Constants.TYPE_SILENT){
                AppUtils.goToDownload(mContext, apkUrl, true);
            }
        } else if (mShowProgressDialog) {
            Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 未检测到有更新
     */
    public void onPostNoDetectedDismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    /**
     * Show dialog
     */
    private void showDialog(Context context, String content, String apkUrl) {
         UpdateDialog.show(context, content, apkUrl);
    }

    /**
     * Show Notification
     */
    private void showNotification(Context context, String content, String apkUrl) {
        Intent myIntent = new Intent(context, DownloadService.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        myIntent.putExtra(Constants.APK_DOWNLOAD_URL, apkUrl);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int smallIcon = context.getApplicationInfo().icon;
        Notification notify = new NotificationCompat.Builder(context)
                .setTicker(context.getString(R.string.android_auto_update_notify_ticker))
                .setContentTitle(context.getString(R.string.android_auto_update_notify_content))
                .setContentText(content)
                .setSmallIcon(smallIcon)
                .setContentIntent(pendingIntent).build();

        notify.flags = android.app.Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notify);
    }
}
