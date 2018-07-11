package com.android.update.fir;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.update.R;


public class FirDialog {

    private MaterialDialog alertDialog;
    private MaterialDialog progressDialog;

    private OnClickDownloadDialogListener onClickDownloadDialogListener;

    public void showAppInfoDialog(Context context, FirAppInfo.AppInfo appInfo) {

        if (alertDialog == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("名称：").append(appInfo.appName);
            sb.append("\n版本：").append(appInfo.appVersionName);
            sb.append("\n文件大小：").append(FirUpdaterUtils.getMeasureSize(appInfo.appSize));
            if (!TextUtils.isEmpty(appInfo.appChangeLog)) {
                sb.append("\n\n更新日志：").append(appInfo.appChangeLog);
            }
            alertDialog = new MaterialDialog.Builder(context).build();

            alertDialog.getBuilder().backgroundColorRes(R.color.material_dialog_background_color)
                    .titleColorRes(R.color.material_dialog_title_text_color)
                    .contentColorRes(R.color.material_dialog_content_text_color)
                    .positiveColorRes(R.color.material_dialog_positive_text_color)
                    .negativeColorRes(R.color.material_dialog_negative_text_color)
                    .canceledOnTouchOutside(false)
                    .title(R.string.android_auto_update_dialog_title)
                    .content(sb)
                    .positiveText(R.string.android_auto_update_dialog_btn_download)
                    .negativeText(R.string.android_auto_update_dialog_btn_cancel)
//                    .neutralText(R.string.android_auto_update_dialog_btn_ingore)
//                    .checkBoxPromptRes(R.string.android_auto_update_dialog_btn_ingore, false, null)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            onClickDownloadDialogListener.onClickDownload(dialog);
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            onClickDownloadDialogListener.onClickDownload(dialog);
                        }
                    })
                    .show();

        }
    }

    public void showDownloadDialog(Context context, int progress) {


        if (progressDialog == null) {
            progressDialog = new MaterialDialog.Builder(context).backgroundColorRes(R.color.material_dialog_background_color)
                    .titleColorRes(R.color.material_dialog_title_text_color)
                    .contentColorRes(R.color.material_dialog_content_text_color)
                    .positiveColorRes(R.color.material_dialog_positive_text_color)
                    .negativeColorRes(R.color.material_dialog_negative_text_color)
                    .positiveText(R.string.android_auto_update_dialog_btn_background)
                    .negativeText(R.string.android_auto_update_dialog_btn_download_cancel)
                    .title(R.string.android_auto_update_download)
                    .canceledOnTouchOutside(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (onClickDownloadDialogListener != null) {
                                onClickDownloadDialogListener.onClickBackgroundDownload(dialog);
                            }
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (onClickDownloadDialogListener != null) {
                                onClickDownloadDialogListener.onClickCancelDownload(dialog);
                            }
                        }
                    })
                    .progress(false, 100, true)
                    .build();
            progressDialog.show();

        }
        if (progressDialog.isShowing()){
            progressDialog.incrementProgress(1);
        }
    }

    public void dismissDownloadDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void setOnClickDownloadDialogListener(OnClickDownloadDialogListener onClickDownloadDialogListener) {
        this.onClickDownloadDialogListener = onClickDownloadDialogListener;
    }

    public interface OnClickDownloadDialogListener {
        void onClickDownload(DialogInterface dialog);

        void onClickBackgroundDownload(DialogInterface dialog);

        void onClickCancelDownload(DialogInterface dialog);
    }

}
