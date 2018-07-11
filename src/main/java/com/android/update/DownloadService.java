package com.android.update;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.android.update.utils.Constants;
import com.android.update.utils.HttpUtils;
import com.android.update.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DownloadService extends IntentService {

    private static boolean bRunning = false;

    private static final String TAG = "DownloadService";

    private static final int NOTIFICATION_ID = 0;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;

    public DownloadService() {
        super("DownloadService");
    }

    public static boolean isRunning() {
        return bRunning;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean bSilent = intent.getBooleanExtra(Constants.APK_DOWNLOAD_MODE, false);
        if (!bSilent) {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this);
            String appName = "应用更新";
            int icon = R.drawable.md_transparent;

            try {
                appName = getString(getApplicationInfo().labelRes);
            } catch (Exception e) {
                try {
                    appName = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
                } catch (Exception ex) {
                    Log.d(TAG, "onHandleIntent: can't get application name!");
                }
            }

            try {
                icon = getApplicationInfo().icon;
            } catch (Exception e) {
                try {
                    icon = getApplicationInfo().logo;
                } catch (Exception ex) {
                    Log.d(TAG, "onHandleIntent: can't get application icon!");
                }
            }

            mBuilder.setContentTitle(appName).setSmallIcon(icon);

        }
        String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
        InputStream in = null;
        FileOutputStream out = null;
        HttpURLConnection urlConnection = null;

        try {
            // trust all hosts
            HttpUtils.trustAllHosts();
            URL url = new URL(urlStr);

            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
                httpsConnection.setHostnameVerifier(HttpUtils.DO_NOT_VERIFY);
                urlConnection = httpsConnection;
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            in = urlConnection.getInputStream();
            File dir = StorageUtils.getCacheDirectory(this);
            String apkName = intent.getStringExtra(Constants.APK_DOWNLOAD_NAME);
            if (TextUtils.isEmpty(apkName)) {
                apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
            }
            File apkFile = new File(dir, apkName);
            out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[HttpUtils.BUFFER_SIZE];

            int oldProgress = 0;

            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);

                int progress = (int) (bytesum * 100L / bytetotal);
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress && !bSilent) {
                    updateProgress(progress);
                }
                oldProgress = progress;
            }
            // 下载完成

            String fileType = apkName.substring(apkName.lastIndexOf(".") + 1);
            if ("apk".equals(fileType)) {
                installAPk(apkFile);
            }
            if (mNotifyManager != null) {
                mNotifyManager.cancel(NOTIFICATION_ID);
            }

        } catch (Exception e) {
            Log.e(TAG, "download apk file error");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {

                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    private void updateProgress(int progress) {
        if (mBuilder == null || mNotifyManager == null) {
            return;
        }
        //"正在下载:" + progress + "%"
        mBuilder.setContentText(this.getString(R.string.android_auto_update_download_progress, progress)).setProgress(100, progress, false);
        //setContentInent如果不设置在4.0+上没有问题，在4.0以下会报异常
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


    private void installAPk(File apkFile) {

        Uri apkUri;
        String packageName = BuildConfig.APPLICATION_ID;

        try {
            packageName = getApplicationInfo().packageName;
        } catch (Exception e) {
            Log.d(TAG, "onHandleIntent: can't get application packageName!");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //通过FileProvider创建一个content类型的Uri ,和清单文件保持一致
            apkUri = FileProvider.getUriForFile(this, packageName + ".update.fileProvider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }

        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        try {
            String[] command = {"chmod", "777", apkFile.toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
        } catch (IOException ignored) {
        }

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        bRunning = false;
        super.onDestroy();
    }
}
