package com.zhanghuang.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghuang.BuildConfig;
import com.zhanghuang.MainApplication;
import com.zhanghuang.R;
import com.zhanghuang.modes.VsnMode;

import java.io.File;

/**
 * Created by yuanlei on 2017/4/25.
 */

public class AppUpdate {

    private static final AppUpdate aui = new AppUpdate();

    Context mContext = null;
    private String oldVm;
    private String newVm;
    private String downUrl;
    private String[] upIntro;
    private boolean isOldUse = true;
    private DownloadManager downloadManager;
    private BroadcastReceiver receiver = null;
    private long downloadId = 0;

    private AppUpdate(){}

    public static AppUpdate getInstance(){
        return aui;
    }


    public boolean checkAppVer(Context ctx, VsnMode vm){
        mContext = ctx;
        newVm = vm.getVer();
        downUrl = vm.getUrl();
        if (null == newVm){
            return false;
        }
        upIntro = vm.getUpIntro().split("\\|");
        String[] one = newVm.split("\\.");
        int nMajor = Integer.parseInt(one[0]);
        int nMinor = Integer.parseInt(one[1]);
        int nVer = Integer.parseInt(one[2]);

        oldVm = MainApplication.ver;
        String[] two = oldVm.split("\\.");
        int oMajor = Integer.parseInt(two[0]);
        int oMinor = Integer.parseInt(two[1]);
        int oVer = Integer.parseInt(two[2]);

        if(oMajor > nMajor){
            return false;
        }
        if(oMajor == nMajor){
            if(oMinor > nMinor){
                return false;
            }
            if(oMinor == nMinor){
                if(oVer >= nVer){
//                    if (checkApk()){
//                        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Constants.APPNAME);
//                        f.delete();
//                    }
                    return false;
                }else{
                    showDialog(upIntro);
                    return true;
                }
            }else{
                showDialog(upIntro);
                return true;
            }
        }else{
            showDialog(upIntro);
            return true;
        }
    }

    //显示是否更新对话框
    private void showDialog(String[] upintro){
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.setCancelable(true);
        View view = View.inflate(mContext, R.layout.update_view,null);
        TextView old_text = (TextView)view.findViewById(R.id.update_view_old_v);
        TextView new_text = (TextView)view.findViewById(R.id.update_view_new_v);
        TextView tishi_text = (TextView)view.findViewById(R.id.update_view_tishi_text);
        Button confirm_button = (Button)view.findViewById(R.id.update_view_confirm_button);
        Button cancel_button = (Button)view.findViewById(R.id.update_view_cancel_button);

        StringBuffer sb = new StringBuffer();
        if (upintro.length > 1){
            for (int i = 0 ; i < upintro.length ; i++){
                sb = sb.append(upintro[i]).append("\n");
            }
        }else{
            sb = sb.append(upintro[0]);
        }
        tishi_text.setText(sb.toString());
        old_text.setText("当前版本:V"+oldVm);
        new_text.setText("最新版本:V"+newVm);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                boolean isSdOn = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                if (!isSdOn){
                    Toast.makeText(mContext,"SD卡不存在！",Toast.LENGTH_SHORT).show();
                }else{
                    if (checkApk()){
                        installApk();
                    }else{
                        downloadId = download(Uri.parse(downUrl));
                        MainApplication._pref.edit().putLong(Constants.PREF_APP_DOWNID,downloadId).apply();
                        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                        receiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                installApk();
                            }
                        };
                        mContext.registerReceiver(receiver,filter);
                    }
                }
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
        dialog.setContentView(view,new ViewGroup.LayoutParams(MainApplication.screenWidth-MainApplication.screenWidth/6,ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private long download(Uri uri){
        long downloadRef;
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(Constants.APPNAME);
        request.setDescription("下载完后请点击打开");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Constants.APPNAME);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        downloadRef = downloadManager.enqueue(request);
        return downloadRef;
    }

    private boolean checkApk(){
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Constants.APPNAME);
        if (f.exists()){
            return true;
        }
        return false;
    }

    private void installApk(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),Constants.APPNAME);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            apkUri = FileProvider.getUriForFile(mContext,
                    BuildConfig.APPLICATION_ID + ".provider",
                    f);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }else{
            apkUri = Uri.fromFile(f);
        }
        intent.setDataAndType(apkUri,
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
