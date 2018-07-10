package com.example.wen.transitrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class subway extends AppCompatActivity {

    private Button line_btn;
    private Spinner direction;
    private RadioGroup subway_state;
    private EditText subway_code;
    private Spinner zhanzuo;
    private Spinner station;
    private RadioGroup section;
    private RadioGroup subway_position1;
    private RadioGroup subway_position2;
    private RadioGroup door;
    private RadioGroup density;
    private RadioButton p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18;
    private RadioGroup g_rg;
    private RadioGroup h_rg;
    private RadioGroup grp_air_vent;
    private RadioGroup grp_wind;
    private LinearLayout before_layout;
    private GridLayout get_on_layout;
    private LinearLayout guanceceng;
    private RadioGroup layer;
    private LinearLayout after_layout;

    private ArrayAdapter<String> dirAdapter;
    private ArrayAdapter<String> stationAdapter;

    //文件写入
    private BufferedWriter writer;

    //记录值
    private String sp1, sp2, sp3, sp4, sp5, sp6, sp7, sp8, sp9, sp10, sp11, gh;
    private String today;

    //地铁数据
    String[][] subway_data = MainActivity.subway_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subway);

        //如果没有读取数据，则再读取excel数据
        if (!MainActivity.flag) {
            subway_data = ReadExcel.getExcelData(this, "subway.xls");
        }

        //根据时间生成当天的文件
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        today = sdf.format(date);

        direction = (Spinner) findViewById(R.id.dir);
        subway_state = (RadioGroup) findViewById(R.id.subway_state);
        subway_code = (EditText) findViewById(R.id.subway_code);
        zhanzuo = (Spinner) findViewById(R.id.bj);
        station = (Spinner) findViewById(R.id.station);
        section = (RadioGroup) findViewById(R.id.section);
        subway_position1 = (RadioGroup) findViewById(R.id.subway_position1);
        subway_position2 = (RadioGroup) findViewById(R.id.subway_position2);
        door = (RadioGroup) findViewById(R.id.door);
        density = (RadioGroup) findViewById(R.id.density);
        g_rg = (RadioGroup) findViewById(R.id.g_rg);
        h_rg = (RadioGroup) findViewById(R.id.h_rg);

        before_layout = (LinearLayout) findViewById(R.id.beforeLayout);
        get_on_layout = (GridLayout) findViewById(R.id.onBusLayout);
        after_layout = (LinearLayout) findViewById(R.id.afterLayout);
        guanceceng = (LinearLayout) findViewById(R.id.guanceceng);
        layer = (RadioGroup) findViewById(R.id.layer);
        grp_air_vent = (RadioGroup) findViewById(R.id.grp_air_vent);
        grp_wind = (RadioGroup) findViewById(R.id.grp_wind);

        p1 = (RadioButton) findViewById(R.id.p1);
        p2 = (RadioButton) findViewById(R.id.p2);
        p3 = (RadioButton) findViewById(R.id.p3);
        p4 = (RadioButton) findViewById(R.id.p4);
        p5 = (RadioButton) findViewById(R.id.p5);
        p6 = (RadioButton) findViewById(R.id.p6);
        p7 = (RadioButton) findViewById(R.id.p7);
        p8 = (RadioButton) findViewById(R.id.p8);
        p9 = (RadioButton) findViewById(R.id.p9);
        p10 = (RadioButton) findViewById(R.id.p10);
        p11 = (RadioButton) findViewById(R.id.p11);
        p12 = (RadioButton) findViewById(R.id.p12);
        p13 = (RadioButton) findViewById(R.id.p13);
        p14 = (RadioButton) findViewById(R.id.p14);
        p15 = (RadioButton) findViewById(R.id.p15);
        p16 = (RadioButton) findViewById(R.id.p16);
        p17 = (RadioButton) findViewById(R.id.p17);
        p18 = (RadioButton) findViewById(R.id.p18);

        //初始化选项
        subway_state.check(R.id.c1);
        density.check(R.id.d1);
        section.check(R.id.s1);
        door.check(R.id.k1);

        subway_state.setOnCheckedChangeListener(new OnMyBusStateCheckedChangeListener());
        subway_position1.setOnCheckedChangeListener(new OnMyBusPosition1CheckedChangeListener());
        subway_position2.setOnCheckedChangeListener(new OnMyBusPosition2CheckedChangeListener());

        line_btn = (Button) findViewById(R.id.line_btn);
        setBusLine("1");
        line_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(subway.this);
                View promptsView = li.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(subway.this);
                alertDialogBuilder.setView(promptsView);
                final EditText line_input = (EditText) promptsView.findViewById(R.id.line_input);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //将输入的线路信息加载到下拉列表
                                        String bus_line_name = line_input.getText().toString();
                                        setBusLine(bus_line_name);
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }

        });
    }


    public void SaveBtnClick(View view) {
        //创建文件夹及文件
        File filedir = null;
        File file1 = null;
        filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        if (!filedir.exists()) {
            filedir.mkdirs();
        }
        file1 = new File(filedir + "/" + "subway" + today + ".txt");
        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer = new BufferedWriter(new FileWriter(file1, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //写入文件
        if (subway_state.getCheckedRadioButtonId() == R.id.c2) {
            if (!TextUtils.isEmpty(subway_code.getText())) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("确认信息")
                        .setMessage("确定保存吗？")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                SaveData2();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                ToastUtil.showRedToast(this, "请填写车厢号！");
            }

        } else {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("确认信息")
                    .setMessage("确定保存吗？")
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“确认”后的操作
                            SaveData1();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    public void SaveData1() {
        //获取记录值
        //获取当前时间
        Date nowDate = new Date();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdFormat.format(nowDate);
        //地铁线路名
        sp1 = line_btn.getText().toString();
        //线路方向
        sp2 = direction.getSelectedItem().toString();
        //地铁站点
        sp3 = station.getSelectedItem().toString();
        //sp4 = "" + layer.indexOfChild(layer.findViewById(layer.getCheckedRadioButtonId()));
        RadioButton layer_rbt = (RadioButton) findViewById(layer.getCheckedRadioButtonId());
        //观测层
        sp4 = layer_rbt.getText().toString();
        //乘车状态
        sp5 = "" + subway_state.indexOfChild(subway_state.findViewById(subway_state.getCheckedRadioButtonId()));
        //乘车过程：站口，刷卡，站台
        if (sp5.equals("0")) {
            //gh = "" + g_rg.indexOfChild(g_rg.findViewById(g_rg.getCheckedRadioButtonId()));
            RadioButton grg_rbt = (RadioButton) findViewById(g_rg.getCheckedRadioButtonId());
            gh = grg_rbt.getText().toString();
        } else {
            //gh = "" + (h_rg.indexOfChild(h_rg.findViewById(h_rg.getCheckedRadioButtonId())) + 3);
            RadioButton hrg_rbt = (RadioButton) findViewById(h_rg.getCheckedRadioButtonId());
            gh = hrg_rbt.getText().toString();
        }


        /* 记录值：【时间】【线路名】【方向】【站点】【层次】【乘车状态】【乘车过程】
        * 【方向】：去往的站点名称
        * 【层次】：0地面层，1高架一层及以上，2站厅层，3负二层，4负三层及以下
        * 【乘车状态】：0上车前，1车内，2下车后
        * 【乘车过程】：站口，刷卡，站台，站台，刷卡，站口
        * */
        //Log.d("记录值1", nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + gh + ",null,null,null,null,null\r\n");
        try {
            writer.write(nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + gh + ",null,null,null,null,null,null,null\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastUtil.showGreenToast(this, "保存成功！");
//        Toast toast = Toast.makeText(this, Html.fromHtml("<font color='#00ff00'>" + "保存成功！" + "</font>"), Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.BOTTOM, 0, -50);
//        toast.show();
    }

    public void SaveData2() {
        //获取记录值
        //获取当前时间
        Date nowDate = new Date();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdFormat.format(nowDate);
        //地铁线路名
        sp1 = line_btn.getText().toString();
        //线路方向
        sp2 = direction.getSelectedItem().toString();
        //地铁站点
        sp3 = station.getSelectedItem().toString();
        //观测层
        //sp4 = "" + layer.indexOfChild(layer.findViewById(layer.getCheckedRadioButtonId()));
        RadioButton layer_rbt = (RadioButton) findViewById(layer.getCheckedRadioButtonId());
        sp4 = layer_rbt.getText().toString();
        //乘车状态
        sp5 = "" + subway_state.indexOfChild(subway_state.findViewById(subway_state.getCheckedRadioButtonId()));
        //RadioButton rbt = (RadioButton) findViewById(subway_state.getCheckedRadioButtonId());
        //sp5 = rbt.getText().toString();
        //车厢编号
        sp6 = subway_code.getText().toString();
        sp6 = sp6.replace(" ", "");
        sp6 = sp6.replace(",", "&");
        //通风口
        RadioButton air_vent_rbt = (RadioButton) findViewById(grp_air_vent.getCheckedRadioButtonId());
        String air_vent = air_vent_rbt.getText().toString();
        //风感
        RadioButton wind_rbt = (RadioButton) findViewById(grp_wind.getCheckedRadioButtonId());
        String wind = wind_rbt.getText().toString();
        //站坐
        sp7 = zhanzuo.getSelectedItem().toString();
        //地铁车厢内位置
        if (subway_position1.getCheckedRadioButtonId() == -1) {
            RadioButton p2_rbt = (RadioButton) findViewById(subway_position2.getCheckedRadioButtonId());
            sp8 = p2_rbt.getText().toString();
        } else {
            RadioButton p1_rbt = (RadioButton) findViewById(subway_position1.getCheckedRadioButtonId());
            sp8 = p1_rbt.getText().toString();
        }
        //车厢人流密度
        RadioButton density_rbt = (RadioButton) findViewById(density.getCheckedRadioButtonId());
        sp9 = density_rbt.getText().toString();
        //路段
        RadioButton section_rbt = (RadioButton) findViewById(section.getCheckedRadioButtonId());
        sp10 = "路段" + section_rbt.getText().toString();
        //开门方向,左侧或右侧
        RadioButton door_rbt = (RadioButton) findViewById(door.getCheckedRadioButtonId());
        sp11 = door_rbt.getText().toString();

        /* 记录值：【时间】【线路名】【方向】【站名】【层次】【乘车状态】【车辆编码】【通风口】【风感】【站坐】【车厢位置】【人流密度】【路段】【开启车门】
        * 【方向】：去往的站点名称
        * 【层次】：0地面层，1高架一层及以上，-1站厅层，-2负二层，-3负三层及以下
        * 【乘车状态】：0上车前，1车内，2下车后
        * 【站坐】：站和坐
        * 【车厢位置】：如图所示
        * 【人流密度】：1、2、3、4、5
        * 【路段】：路段1，路段2，路段3
        * 【开启车门】：左侧、右侧
        * */
        //Log.d("记录值2", nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + sp6 + "," + air_vent + "," + wind + "," + sp7 + "," + sp8 + "," + sp9 + "," + sp10 + "," + sp11 + "\r\n");
        try {
            writer.write(nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + sp6 + "," + air_vent + "," + wind + "," + sp7 + "," + sp8 + "," + sp9 + "," + sp10 + "," + sp11 + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastUtil.showGreenToast(this, "保存成功！");
//        Toast toast = Toast.makeText(this, Html.fromHtml("<font color='#00ff00'>" + "保存成功！" + "</font>"), Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.BOTTOM, 0, -50);
//        toast.show();
        //自动选择下一段，当处于3段时自动选择下一站
        if (section.getCheckedRadioButtonId() == R.id.s3) {
            int n = station.getAdapter().getCount();
            int m = station.getSelectedItemPosition() + 1;
            if (m < n) {
                station.setSelection(m, true);
            }
            section.check(R.id.s1);
        } else if (section.getCheckedRadioButtonId() == R.id.s1) {
            section.check(R.id.s2);
        } else {
            section.check(R.id.s3);
        }
    }

    //切换等车、车内和下车三种状态
    private class OnMyBusStateCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.c1: {
                    guanceceng.setVisibility(View.VISIBLE);
                    before_layout.setVisibility(View.VISIBLE);
                    get_on_layout.setVisibility(View.GONE);
                    after_layout.setVisibility(View.GONE);
                    break;
                }
                case R.id.c2: {
                    guanceceng.setVisibility(View.GONE);
                    before_layout.setVisibility(View.GONE);
                    get_on_layout.setVisibility(View.VISIBLE);
                    after_layout.setVisibility(View.GONE);
                    break;
                }
                case R.id.c3: {
                    guanceceng.setVisibility(View.VISIBLE);
                    before_layout.setVisibility(View.GONE);
                    get_on_layout.setVisibility(View.GONE);
                    after_layout.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    //region 下面两个内部类用于实现两个RadioGroup的单选
    private class OnMyBusPosition1CheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int position) {
            switch (position) {
                case R.id.p1:
                    if (p1.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p2:
                    if (p2.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p3:
                    if (p3.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p4:
                    if (p4.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p5:
                    if (p5.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p6:
                    if (p6.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p7:
                    if (p7.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p8:
                    if (p8.isChecked())
                        subway_position2.clearCheck();
                    break;
                case R.id.p9:
                    if (p9.isChecked())
                        subway_position2.clearCheck();
                    break;
            }
        }
    }

    private class OnMyBusPosition2CheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int position) {
            switch (position) {
                case R.id.p10:
                    if (p10.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p11:
                    if (p11.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p12:
                    if (p12.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p13:
                    if (p13.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p14:
                    if (p14.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p15:
                    if (p15.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p16:
                    if (p16.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p17:
                    if (p17.isChecked())
                        subway_position1.clearCheck();
                    break;
                case R.id.p18:
                    if (p18.isChecked())
                        subway_position1.clearCheck();
                    break;
            }
        }
    }
    //endregion

    //根据线路名设置线路方向和站点
    public void setBusLine(String line_name) {
        if ("".equals(line_name.trim())) {
            new android.support.v7.app.AlertDialog.Builder(this).setTitle("提示").setMessage("输入线路为空").setPositiveButton("确定", null).show();
            return;
        }
        //获取根据线路名从subway_data提取出线路信息
        boolean is_get_line = false;
        int x = 1;
        for (int i = 0; i < subway_data[0].length; ++i) {
            if (line_name.equals(subway_data[0][i].trim())) {
                x = i;
                is_get_line = true;
            }
        }
        if (is_get_line) {
            line_btn.setText(line_name);
        } else {
            new android.support.v7.app.AlertDialog.Builder(this).setTitle("提示").setMessage("没有找到该线路，请重新输入").setPositiveButton("确定", null).show();
            return;
        }

        String dir_0 = subway_data[1][x].trim();
        String dir_1 = subway_data[2][x].trim();
        String stops0 = subway_data[3][x].trim();
        String stops1 = subway_data[4][x].trim();

        final String[] dir_array = {dir_1, dir_0};
        final String[][] stop_array = {stops0.split("、"), stops1.split("、")};


        //region 线路和方向切换
        dirAdapter = new ArrayAdapter<String>(this, R.layout.custom_spiner_text_item1, dir_array);
        dirAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item1);
        direction.setAdapter(dirAdapter);
        //direction.setSelection(0,true);

        stationAdapter = new ArrayAdapter<String>(this, R.layout.custom_spiner_text_item1, stop_array[0]);
        stationAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item1);
        station.setAdapter(stationAdapter);
        //station.setSelection(0,true);

        direction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                stationAdapter = new ArrayAdapter<String>(subway.this, R.layout.custom_spiner_text_item1, stop_array[i]);
                stationAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item1);
                station.setAdapter(stationAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
