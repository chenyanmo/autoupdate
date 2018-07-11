package com.android.update;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.update.utils.AppUtils;
import com.android.utils.PreferencesUtils;

class UpdateDialog {

    static void show(final Context context, final String content, final String downloadUrl) {
        String ingoreUrl = PreferencesUtils.getInstance(context).getString("DOWNLOADURL", "");
        if (isContextValid(context)&& !ingoreUrl.equals(downloadUrl)) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle(R.string.android_auto_update_dialog_title);
//            builder.setMessage(Html.fromHtml(content))
//                    .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            AppUtils.goToDownload(context, downloadUrl, false);
//                        }
//                    })
//                    .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    });
//
//            AlertDialog dialog = builder.create();
//            //点击对话框外面,对话框不消失
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();

            new MaterialDialog.Builder(context)
                    .backgroundColorRes(R.color.material_dialog_background_color)
                    .titleColorRes(R.color.material_dialog_title_text_color)
                    .contentColorRes(R.color.material_dialog_content_text_color)
                    .positiveColorRes(R.color.material_dialog_positive_text_color)
                    .negativeColorRes(R.color.material_dialog_negative_text_color)
                    .canceledOnTouchOutside(false)
                    .title(R.string.android_auto_update_dialog_title)
                    .content(Html.fromHtml(content))
                    .positiveText(R.string.android_auto_update_dialog_btn_download)
                    .negativeText(R.string.android_auto_update_dialog_btn_cancel)
//                    .neutralText(R.string.android_auto_update_dialog_btn_ingore)
//                    .checkBoxPromptRes(R.string.android_auto_update_dialog_btn_ingore, false, null)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AppUtils.goToDownload(context, downloadUrl, false);
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        }
                    })
//                    .onNegative(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        }
//                    })
//                    .onAny(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                             if (dialog.isPromptCheckBoxChecked()){
//                                 PreferencesUtils.getInstance(context).putString("DOWNLOADURL",downloadUrl);
//                             }
//                        }
//                    })
                    .show();

        }else{
            Toast.makeText(context, context.getString(R.string.android_auto_update_toast_no_update_detected), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }
}
