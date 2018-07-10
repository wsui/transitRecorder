package com.example.wen.transitrecorder;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LineUpdateActivity extends AppCompatActivity {
    Button bus_update_btn;
    Button subway_update_btn;
    TextView bus_line_url;
    TextView subway_line_url;
    TextView bus_line_update_state;
    TextView subway_line_update_state;
    //    DownloadManager downloadManager;
    BroadcastReceiver receiver1;
    BroadcastReceiver receiver2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_update);
        bus_update_btn = (Button) findViewById(R.id.bus_update_btn);
        bus_line_update_state = (TextView) findViewById(R.id.bus_line_update_state);
        bus_line_url = (TextView) findViewById(R.id.bus_line_url);

        subway_update_btn = (Button) findViewById(R.id.subway_update_btn);
        subway_line_update_state = (TextView) findViewById(R.id.subway_line_update_state);
        subway_line_url = (TextView) findViewById(R.id.subway_line_url);
//        String serviceString = Context.DOWNLOAD_SERVICE;
//        downloadManager = (DownloadManager) getSystemService(serviceString);

        //显示文件信息
        get_file_info("bus.xls");
        get_file_info("subway.xls");

        bus_update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = bus_line_url.getText().toString();
                String download_filename = "bus"+String.valueOf(Calendar.getInstance().getTimeInMillis())+".xls";
                if ("xls".equals(url.substring(url.lastIndexOf(".") + 1))) {
                    bus_line_update_state.setText("下载中...请稍等");
                    long reference = downloadFile2("_transit", download_filename, url);
                    initFinishRecicever1(reference, download_filename);
                } else {
                    Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#ff0000'>公交更新失败！文件名必须为.xls</font> "), Toast.LENGTH_SHORT).show();
                }
            }
        });
        subway_update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = subway_line_url.getText().toString();
                String download_filename = "subway"+String.valueOf(Calendar.getInstance().getTimeInMillis())+".xls";
                if ("xls".equals(url.substring(url.lastIndexOf(".") + 1))) {
                    subway_line_update_state.setText("下载中...请稍等");
                    long reference = downloadFile2("_transit", download_filename, url);
                    initFinishRecicever2(reference, download_filename);
                } else {
                    Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#ff0000'>地铁更新失败！文件名必须为.xls</font> "), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void delete_file(String filename) {
        File filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        File del_file = new File(filedir + "/" + filename);
        if (del_file.exists()) {
            del_file.delete();
        }
    }

    public void rename_file(String oldname, String newname) {
        File filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        File old_file = new File(filedir + "/" + oldname);
        File new_file = new File(filedir + "/" + newname);
        if (old_file.exists()) {
            if (new_file.exists()) {
                new_file.delete();
            }
            File tonamefile = new File(filedir + "/" + newname);
            old_file.renameTo(tonamefile);
        }
    }

    //下载后的更新显示信息
    public void get_file_info(String filename) {
        File filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        File xls_file = new File(filedir + "/" + filename);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年M月d日 HH:mm");
        Calendar cal = Calendar.getInstance();
        String file_info = "系统数据";
        if (xls_file.exists()) {
            long time = xls_file.lastModified();
            cal.setTimeInMillis(time);
            String update_time = formatter.format(cal.getTime());
            file_info = "上次更新：" + update_time;
        }
        if (filename.equals("bus.xls")) {
            bus_line_update_state.setText(file_info);
        } else {
            subway_line_update_state.setText(file_info);
        }
    }


    /**
     * 初始化下载管理器
     */

    private long downloadFile2(String path, String filename, String url) {
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM,"huge.jpg");//保存到公共图片文件夹
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir(path, filename);
        request.allowScanningByMediaScanner();//允许被扫描
        request.setVisibleInDownloadsUi(true);//通知栏一直显示
        request.setTitle("线路更新下载:" + filename);
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//下载完成也会持续显示
        return downloadManager.enqueue(request);//得到下载文件的唯一id，即为reference
    }

    private void initFinishRecicever1(final long reference, final String download_filename) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long references = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (references == reference) {
                    DownloadManager.Query query = new DownloadManager.Query().setFilterById(reference);
                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Cursor c = downloadManager.query(query);
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == downloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#00ff00'>公交线路更新完成 </font> "), Toast.LENGTH_SHORT).show();
                            rename_file(download_filename, "bus.xls");
                            get_file_info("bus.xls");
                            MainActivity.bus_data = ReadExcel.getExcelData(LineUpdateActivity.this, "bus.xls");
                        } else {
                            Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#ff0000'>公交线路更新失败 </font> "), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                unregisterReceiver(receiver1);
            }
        };
        registerReceiver(receiver1, intentFilter);
    }

    private void initFinishRecicever2(final long reference, final String download_filename) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long references = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (references == reference) {
                    DownloadManager.Query query = new DownloadManager.Query().setFilterById(reference);
                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Cursor c = downloadManager.query(query);
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == downloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#00ff00'>地铁线路更新完成 </font> "), Toast.LENGTH_SHORT).show();
                            rename_file(download_filename, "subway.xls");
                            get_file_info("subway.xls");
                            MainActivity.subway_data = ReadExcel.getExcelData(LineUpdateActivity.this, "subway.xls");
                        } else {
                            Toast.makeText(LineUpdateActivity.this, Html.fromHtml("<font color='#ff0000'>地铁线路更新失败 </font> "), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                unregisterReceiver(receiver2);
            }
        };
        registerReceiver(receiver2, intentFilter);
    }
}