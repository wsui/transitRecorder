package com.example.wen.transitrecorder;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    /* 笔记
    * 给顶部标题栏加上返回按钮，只需要设置父activity就行了，即在AndroidManifest里为activity添加android:parentActivityName=".MainActivity"
    * 避免按标题栏返回按钮后父页面重新加载，在AndroidManifest里为设置MainActivity属性android:launchMode="singleTop"
    * */

    public static String[][] bus_data;  //公交数据
    public static String[][] subway_data; //地铁数据
    public static Boolean flag = false; //是否加载数据
    public static Boolean is_custom_bus = false;
    public static Boolean is_custom_subway = false;
    private TextView data_source_text;
    public static String gps_time = "2000-01-01 00:00:00";
    public static int ct = 0;
    TextView download_text;//屏幕下载提示文字
    LinearLayout download_text_box;
    BroadcastReceiver receiver1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        //初始化记录的定位时间

        data_source_text = (TextView) findViewById(R.id.data_source_text);
        download_text_box = (LinearLayout) findViewById(R.id.download_text_box);
        download_text = (TextView) findViewById(R.id.download_text);
        download_text.setSelected(true);
        //读取excel数据
        if (!flag) {
            //Log.d("主界面", "开始读取数据");
            bus_data = ReadExcel.getExcelData(this, "bus.xls");
            subway_data = ReadExcel.getExcelData(this, "subway.xls");
            if (bus_data != null && subway_data != null) {
                flag = true;
            }
        }
        String source_text = "";
        if (is_custom_bus) {
            source_text += "公交线路：自定义    ";
        } else {
            source_text += "公交线路：系统数据    ";
        }
        if (is_custom_subway) {
            source_text += "地铁线路：自定义";
        } else {
            source_text += "地铁线路：系统数据";
        }
        data_source_text.setText(source_text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.file:
                Intent intent1;
                intent1 = new Intent(this, FileActivity.class);
                this.startActivity(intent1);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.line_update:
                Intent intent3;
                intent3 = new Intent(this, LineUpdateActivity.class);
                this.startActivity(intent3);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.update:
                //删除旧的apk
                File old_apk = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit/TransitRecorder_latast.apk");
                if (old_apk.exists()) {
                    old_apk.delete();
                }
                String url = "https://raw.githubusercontent.com/wsui/transitRecorder/master/TransitRecorder_latast.apk";
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir("_transit", "TransitRecorder_latast.apk");
                request.allowScanningByMediaScanner();//允许被扫描
                request.setVisibleInDownloadsUi(true);//通知栏一直显示
                request.setTitle("下载公共交通记录App");
                //  request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//下载完成也会持续显示
                Long reference = downloadManager.enqueue(request);
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/_transit/TransitRecorder_latast.apk";
                initFinishRecicever1(reference, filePath);
                download_text_box.setVisibility(View.VISIBLE);
                break;
            case R.id.version:
                Intent intent2;
                intent2 = new Intent(this, VerionActivity.class);
                this.startActivity(intent2);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.about:
                String versionNmae = BuildConfig.VERSION_NAME;
                //Toast.makeText(this,"Author:WS\nVersion:"+versionNmae+versionCode, Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(this).setTitle("关于").setMessage("作者: WS\n版本: " + versionNmae).setPositiveButton("确定", null).show();
                break;
            case R.id.exit:
                exitApp(getWindow().getDecorView().findViewById(android.R.id.content));
                break;
            default:
        }
        return true;
    }

    public void onClick1(View view) {
        Intent intent;
        intent = new Intent(this, bus.class);
        this.startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void onClick2(View view) {
        Intent intent;
        intent = new Intent(this, subway.class);
        this.startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    //退出app
    public void exitApp(View view) {
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void initFinishRecicever1(final long reference, final String filePath) {
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
                            Toast.makeText(MainActivity.this, Html.fromHtml("<font color='#00ff00'>app更新下载完成 </font> "), Toast.LENGTH_SHORT).show();
                            try {
                                File apk_file = new File(filePath);
                                Intent install = new Intent();
                                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                install.setAction(android.content.Intent.ACTION_VIEW);
                                install.setDataAndType(Uri.fromFile(apk_file), "application/vnd.android.package-archive");
                                startActivity(install);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, Html.fromHtml("<font color='#ff0000'>app更新安装未完成 </font> "), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, Html.fromHtml("<font color='#ff0000'>app更新下载失败 </font> "), Toast.LENGTH_SHORT).show();
                        }
                    }
                    download_text_box.setVisibility(View.GONE);
                }
                unregisterReceiver(receiver1);
            }
        };
        registerReceiver(receiver1, intentFilter);
    }

}
