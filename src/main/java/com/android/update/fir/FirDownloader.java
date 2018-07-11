package com.android.update.fir;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.update.utils.Constants;
import com.android.update.utils.HttpUtils;
import com.android.update.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class FirDownloader {

    private static final int STATE_SUCCESS = -1;
    private static final int STATE_ERROR = -2;

    private Context context;
    private FirAppInfo.AppInfo appInfo;
    private int fileLength;
    private int currLength;

    private boolean isGoOn;
    private int lastProgress = 0;
    private OnDownLoadListener onDownLoadListener;

    public FirDownloader(Context context, FirAppInfo.AppInfo appInfo) {
        this.context = context;
        this.appInfo = appInfo;
    }

    public void downloadApk() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    isGoOn = true;


                    // trust all hosts
                    HttpUtils.trustAllHosts();
                    URL url = new URL(appInfo.appInstallUrl);

                    if (url.getProtocol().toLowerCase().equals("https")) {
                        HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
                        httpsConnection.setHostnameVerifier(HttpUtils.DO_NOT_VERIFY);
                        conn = httpsConnection;
                    } else {
                        conn = (HttpURLConnection) url.openConnection();
                    }


                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10 * 1000);

                    currLength = FirUpdaterUtils.getCurrLengthValue(context, appInfo.apkName);
                    fileLength = FirUpdaterUtils.getFileLengthValue(context, appInfo.apkName);

                    if (currLength>=0 && fileLength >=0 & currLength < fileLength) {
                        conn.setRequestProperty("Range", "bytes=" + currLength + "-" + fileLength);
                        FirUpdaterUtils.logger("currLength: " + currLength + " fileLength: " + fileLength);
                    }
                    conn.connect();
                    fileLength = conn.getContentLength();
                    FirUpdaterUtils.putFileLengthValue(context, appInfo.apkName, fileLength);


//                    if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                        InputStream is = conn.getInputStream();
                        RandomAccessFile raf = new RandomAccessFile(appInfo.apkLocalUrl, "rwd");
                        raf.setLength(fileLength);
                        raf.seek(currLength);

                        int len;
                        int lastProgress = 0;
                        byte[] buffer = new byte[1024];

                        while ((len = is.read(buffer)) != -1) {
                            if (!isGoOn) {
                                break;
                            }

                            if (onDownLoadListener != null) {
                                currLength += len;
                                int progress = (int) ((float) currLength / (float) fileLength * 100);
                                if (lastProgress != progress) {
                                    lastProgress = progress;
                                    handler.sendEmptyMessage(progress);
                                }
                            }

                            raf.write(buffer, 0, len);
                        }

                        FirUpdaterUtils.closeQuietly(is, raf);

                        if (!isGoOn && currLength < fileLength) {
                            FirUpdaterUtils.putCurrLengthValue(context, appInfo.apkName, currLength);
                        } else {
                            FirUpdaterUtils.putCurrLengthValue(context, appInfo.apkName, 0);
                            FirUpdaterUtils.putFileLengthValue(context, appInfo.apkName, 0);

                            handler.sendEmptyMessage(100);
                            handler.sendEmptyMessage(STATE_SUCCESS);
                        }
//                    } else {
//                        handler.sendMessage(handler.obtainMessage(STATE_ERROR, "下载受限啦，明日早来哦^_^"));
//                    }
                    conn.disconnect();
                } catch (Exception e) {
                    FirUpdaterUtils.loggerError(e);
                    FirUpdaterUtils.putCurrLengthValue(context, appInfo.apkName, 0);
                    FirUpdaterUtils.putFileLengthValue(context, appInfo.apkName, 0);

                    handler.sendEmptyMessage(STATE_ERROR);
                } finally {
                    isGoOn = false;
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    public boolean isGoOn() {
        return isGoOn;
    }

    public boolean isGoOnWithAppId(String appId) {
        return isGoOn && appInfo.appId.equalsIgnoreCase(appId);
    }

    public void cancel() {
        isGoOn = false;
    }

    public void setOnDownLoadListener(OnDownLoadListener onDownLoadListener) {
        this.onDownLoadListener = onDownLoadListener;
    }

    public interface OnDownLoadListener {
        void onProgress(int progress);

        void onSuccess();

        void onError();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (onDownLoadListener != null) {
                switch (msg.what) {
                    case STATE_SUCCESS:
                        onDownLoadListener.onSuccess();
                        break;
                    case STATE_ERROR:
                        onDownLoadListener.onError();
                        if (msg.obj != null && msg.obj instanceof String) {
                            Toast.makeText(context, (String) msg.obj, Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                        if (lastProgress != msg.what) {
                            lastProgress = msg.what;
                            onDownLoadListener.onProgress(msg.what);
                        }
                        break;
                }
            }
        }
    };
}
