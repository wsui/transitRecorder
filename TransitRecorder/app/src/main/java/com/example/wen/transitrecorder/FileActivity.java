package com.example.wen.transitrecorder;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;


import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class FileActivity extends AppCompatActivity {
    private ListView fileListView;      //显示文件列表的ListView容器
    ArrayList fileList;                 //用于存放文件名的数组
    MyAdpter adapter;                   //数据绑定
    File fileDir;                       //文件存放目录
    File[] files;                       //目录下的所有文件
    Button cancel_select_btn;       //取消选择按钮
    Button share_btn;                //分享按钮
    Button delete_btn;              //删除按钮
    int selected_num;              //选中的文件数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        fileListView = (ListView) findViewById(R.id.file_listview);
        fileList = new ArrayList();
        cancel_select_btn = (Button) findViewById(R.id.cancel_select_btn);
        share_btn = (Button) findViewById(R.id.share_btn);
        delete_btn = (Button) findViewById(R.id.delete_btn);
        selected_num = 0;

        ToastUtil.showLongToast(this, "长按可打开文件");
        //获取文件存储目录，如果没有则创建
        fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        //获取文件夹下的所有文件（包括了文件夹）
        files = fileDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".txt")) {
                        HashMap map = new HashMap();
                        //获取文件名
                        //String file_name = fileName.substring(0, fileName.lastIndexOf(".")).toString();
                        map.put("Name", fileName);

                        //获取文件大小
                        DecimalFormat df = new DecimalFormat("0.00");
                        map.put("Size", df.format((float) file.length() / 1024).toString() + " KB");

                        //获取文件创建事件
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年M月d日 HH:mm");
                        Calendar cal = Calendar.getInstance();
                        long time = file.lastModified();
                        cal.setTimeInMillis(time);
                        map.put("Time", formatter.format(cal.getTime()));
                        //标记选中的文件
                        map.put("Selected", "0");
                        fileList.add(map);
                    }
                }
            }
        }
        adapter = new MyAdpter(this, fileList);
        fileListView.setAdapter(adapter);

        //fileListView点击事件
        fileListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //标记数组中选中的文件，并计数
                HashMap<String, String> map = (HashMap<String, String>) fileList.get(position);
                if (map.get("Selected").toString().equals("1")) {
                    map.put("Selected", "0");
                    selected_num--;
                } else {
                    map.put("Selected", "1");
                    selected_num++;
                }
                fileList.set(position, map);
                //设置删除和分享按钮是否可用，并显示选中的文件数目
                if (selected_num == 0) {
                    share_btn.setEnabled(false);
                    delete_btn.setEnabled(false);
                    cancel_select_btn.setText("取消选择");
                } else {
                    share_btn.setEnabled(true);
                    delete_btn.setEnabled(true);
                    cancel_select_btn.setText("取消选择（" + selected_num + "）");
                }
                //adapter更改刷新界面
                adapter.notifyDataSetChanged();
            }
        });

        //fileListView长按事件
        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                //长按打开文件
                try {
                    HashMap<String, String> hashMap = (HashMap<String, String>) fileList.get(position);
                    Intent intent = getTextFileIntent(fileDir + "/" + hashMap.get("Name"), false);
                    startActivity(intent);
                } catch (ActivityNotFoundException a) {
                    ToastUtil.showRedToast(FileActivity.this, "打开文件出错！");
                    //Log.e("note", a.getMessage());
                }
                return true;
            }
        });

        //取消选中按钮点击事件
        cancel_select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share_btn.setEnabled(false);
                delete_btn.setEnabled(false);
                cancel_select_btn.setText("取消选择");
                selected_num = 0;

                for (int i = 0; i < fileList.size(); i++) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) fileList.get(i);
                    if (hashMap.get("Selected").toString().equals("1")) {
                        hashMap.put("Selected", "0");
                        fileList.set(i, hashMap);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        //删除文件
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取需要删除的文件名
                String msg = "";
                for (int i = 0; i < fileList.size(); i++) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) fileList.get(i);
                    if (hashMap.get("Selected").toString().equals("1")) {
                        msg += hashMap.get("Name").toString() + "\n";
                    }
                }

                new AlertDialog.Builder(view.getContext())
                        .setTitle("确认删除这些文件吗")
                        .setMessage(msg)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                for (int i = 0; i < fileList.size(); i++) {
                                    HashMap<String, String> hashMap = (HashMap<String, String>) fileList.get(i);
                                    if (hashMap.get("Selected").toString().equals("1")) {
                                        File f = new File(fileDir + "/" + hashMap.get("Name"));
                                        if (f.exists()) {
                                            f.delete();
                                            fileList.remove(i);
                                            i--;
                                        }
                                    }
                                }
                                share_btn.setEnabled(false);
                                delete_btn.setEnabled(false);
                                selected_num = 0;
                                cancel_select_btn.setText("取消选择");
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        //分享文件
        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Uri> share_files = new ArrayList<Uri>();

                for (int i = 0; i < fileList.size(); i++) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) fileList.get(i);
                    if (hashMap.get("Selected").toString().equals("1")) {
                        share_files.add(Uri.fromFile(new File(fileDir + "/" + hashMap.get("Name"))));
                    }
                }

                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);//发送多个文件
                intent.setType("*/*");//多个文件格式
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, share_files);//Intent.EXTRA_STREAM同于传输文件流
                startActivity(intent);
            }
        });
        //当文件目录内没有文件时
        if (fileList.size() == 0) {
            delete_btn.setVisibility(View.GONE);
            share_btn.setVisibility(View.GONE);
            cancel_select_btn.setText("无文件！");
            cancel_select_btn.setBackgroundColor(Color.argb(0, 0, 0, 0));
            cancel_select_btn.setTextSize(20);
            cancel_select_btn.setClickable(false);
        }
    }

    //android获取一个用于打开文本文件的intent
    public Intent getTextFileIntent(String param, boolean paramBoolean) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.fromFile(new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    //自定义Adapter
    class MyAdpter extends BaseAdapter {
        private LayoutInflater inflater;
        ArrayList<HashMap> arrayList;

        public MyAdpter(Context context, ArrayList arrayList) {
            this.inflater = LayoutInflater.from(context);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList == null ? 0 : arrayList.size();
        }

        @Override
        public HashMap getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.file_textview, null);
            HashMap hashMap = getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.txt_tv);
            TextView c_date_tv = (TextView) view.findViewById(R.id.cDate_tv);
            TextView size_tv = (TextView) view.findViewById(R.id.size_tv);
            textView.setText(hashMap.get("Name").toString());
            c_date_tv.setText(hashMap.get("Time").toString());
            size_tv.setText(hashMap.get("Size").toString());

            if (hashMap.get("Selected").toString().equals("1")) {
                // 被选中
                view.setBackgroundColor(Color.rgb(176, 226, 255));
            } else {
                // 未选中
                view.setBackgroundColor(Color.WHITE);
            }
            return view;
        }
    }

}
