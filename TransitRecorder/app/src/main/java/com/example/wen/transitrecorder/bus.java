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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class bus extends AppCompatActivity {

    private Button line_btn;
    private Spinner direction;
    private RadioGroup bus_state;
    private EditText bus_code;
    private Spinner zhanzuo;
    private Spinner station;

    private RadioGroup section;
    private RadioGroup bus_position1;
    private RadioGroup bus_position2;
    private RadioGroup density;
    private RadioButton p1, p2, p3, p4, p5, p6, p7, p8;
    private RadioGroup grp_stop_position;
    private RadioGroup grp_lane_flow;
    private RadioGroup grp_bus_num;
    private RadioGroup grp_air_vent;
    private RadioGroup grp_wind;
    private RadioGroup grp_window;

    private GridLayout get_on_layout;
    private LinearLayout guanceceng;
    private TextView gpsTextView;
    private RadioButton lamp;
    private RadioGroup layer;

    //文件写入
    private BufferedWriter writer;
    private BufferedWriter writer2;

    //记录值
    private String sp1;
    private String sp2;
    private String sp3;
    private String sp4;
    private String sp5;
    private String sp6;
    private String sp7;
    private String sp8;
    private String sp9;
    private String sp10;
    private String today;
    private double latitude = 0;
    private double longitude = 0;
    private float radius = 0;
    private int errorCode = 0;
    private String stop_position;
    private String lane_flow;
    private String bus_num;
    private String gps_time;

    private ArrayAdapter<String> dirAdapter;
    private ArrayAdapter<String> stationAdapter;

    //公交数据
    String[][] bus_data = MainActivity.bus_data;

    //百度地图API记录位置
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        //如果没有读取数据，则再读取excel数据
        if (!MainActivity.flag) {
            bus_data = ReadExcel.getExcelData(this, "bus.xls");
        }
        //初始化gps_time

        gps_time = "2000-01-01 00:00:00";

        //百度地图定位初始化和设置
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(true);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)


        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //option.setWifiValidTime(5 * 60 * 1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(true);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient.start();


        //根据时间生成当天的文件
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        today = sdf.format(date);


        direction = (Spinner) findViewById(R.id.dir);
        bus_state = (RadioGroup) findViewById(R.id.bus_state);
        bus_code = (EditText) findViewById(R.id.bus_code);
        zhanzuo = (Spinner) findViewById(R.id.bj);
        station = (Spinner) findViewById(R.id.station);
        section = (RadioGroup) findViewById(R.id.section);
        bus_position1 = (RadioGroup) findViewById(R.id.bus_position1);
        bus_position2 = (RadioGroup) findViewById(R.id.bus_position2);
        density = (RadioGroup) findViewById(R.id.density);
        gpsTextView = (TextView) findViewById(R.id.gps);
        lamp = (RadioButton) findViewById(R.id.lamp);
        grp_stop_position = (RadioGroup) findViewById(R.id.stop_position);
        grp_lane_flow = (RadioGroup) findViewById(R.id.lane_flow);
        grp_bus_num = (RadioGroup) findViewById(R.id.bus_num);
        grp_air_vent = (RadioGroup) findViewById(R.id.grp_air_vent);
        grp_wind = (RadioGroup) findViewById(R.id.grp_wind);
        layer = (RadioGroup) findViewById(R.id.layer);
        get_on_layout = (GridLayout) findViewById(R.id.onBusLayout);
        guanceceng = (LinearLayout) findViewById(R.id.guanceceng);
        grp_window = (RadioGroup) findViewById(R.id.grp_window);

        p1 = (RadioButton) findViewById(R.id.p1);
        p2 = (RadioButton) findViewById(R.id.p2);
        p3 = (RadioButton) findViewById(R.id.p3);
        p4 = (RadioButton) findViewById(R.id.p4);
        p5 = (RadioButton) findViewById(R.id.p5);
        p6 = (RadioButton) findViewById(R.id.p6);
        p7 = (RadioButton) findViewById(R.id.p7);
        p8 = (RadioButton) findViewById(R.id.p8);

        //初始化选项
        bus_state.check(R.id.c1);
        density.check(R.id.d1);
        section.check(R.id.s1);

        bus_state.setOnCheckedChangeListener(new OnMyBusStateCheckedChangeListener());
        bus_position1.setOnCheckedChangeListener(new OnMyBusPosition1CheckedChangeListener());
        bus_position2.setOnCheckedChangeListener(new OnMyBusPosition2CheckedChangeListener());

        line_btn = (Button) findViewById(R.id.line_btn);
        //设置线路信息下拉列表
        setBusLine("323");
        line_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(bus.this);
                View promptsView = li.inflate(R.layout.prompts, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(bus.this);
                alertDialogBuilder.setView(promptsView);
                final EditText busLineInput = (EditText) promptsView.findViewById(R.id.line_input);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //将输入的线路信息加载到下拉列表
                                        String bus_line_name = busLineInput.getText().toString();
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
        File filedir = null;
        File file1 = null;
        File file2 = null;
        //创建文件夹及文件
        filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
        if (!filedir.exists()) {
            filedir.mkdirs();
        }
        file1 = new File(filedir + "/" + "bus" + today + ".txt");
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
        //创建gps记录文件
        file2 = new File(filedir + "/" + "gps" + today + ".txt");
        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer2 = new BufferedWriter(new FileWriter(file2, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean compareTime(String time1, String time2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//创建日期转换对象HH:mm:ss为时分秒，年月日为yyyy-MM-dd
        try {
            Date dt1 = df.parse(time1);//将字符串转换为date类型
            Date dt2 = df.parse(time2);
            if (dt1.getTime() > dt2.getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void WriteGPS() {

        /* 记录值：【GPS定位时间】【经度】【纬度】【精度】【定位状态码】
        * 【定位状态码】61:GPS定位结果；66:离线定位结果；161:表示网络定位结果
        * */
        //GPS定位时间为上次定位时间，可能距离当前时间较近，所以可能出现重复，要去除重复。

        if (compareTime(gps_time, MainActivity.gps_time)) {
            try {
                writer2.write(gps_time + "," + longitude + "," + latitude + "," + radius + "," + errorCode + "\r\n");
                writer2.flush();
                MainActivity.gps_time = gps_time;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //保存到文件
    public void SaveBtnClick(View view) {
        //写入文件
        if (bus_state.getCheckedRadioButtonId() == R.id.c2) {
            if (!TextUtils.isEmpty(bus_code.getText())) {
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
                ToastUtil.showRedToast(this, "请填写车牌号！");
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

    //保存车外数据
    public void SaveData1() {
        //获取记录值
        //获取当前时间
        Date nowDate = new Date();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdFormat.format(nowDate);
        sp1 = line_btn.getText().toString(); //线路名
        sp2 = direction.getSelectedItem().toString(); //线路去向站点
        sp3 = station.getSelectedItem().toString(); //车站名
        //观测层次
        //sp4 = "" + layer.indexOfChild(layer.findViewById(layer.getCheckedRadioButtonId()));
        RadioButton layer_rbt = (RadioButton) findViewById(layer.getCheckedRadioButtonId());
        sp4 = layer_rbt.getText().toString();
        sp5 = "" + bus_state.indexOfChild(bus_state.findViewById(bus_state.getCheckedRadioButtonId())); //乘车状态
        //站台位置
        RadioButton rbt_stop_position = (RadioButton) findViewById(grp_stop_position.getCheckedRadioButtonId());
        stop_position = rbt_stop_position.getText().toString();
        //车道流量
        RadioButton rbt_lane_flow = (RadioButton) findViewById(grp_lane_flow.getCheckedRadioButtonId());
        lane_flow = rbt_lane_flow.getText().toString();
        //公汽数目
        RadioButton rbt_bus_num = (RadioButton) findViewById(grp_bus_num.getCheckedRadioButtonId());
        bus_num = rbt_bus_num.getText().toString();

        //RadioButton rbt = (RadioButton) findViewById(bus_state.getCheckedRadioButtonId());
        //sp5 = rbt.getText().toString();
        /* 删除乘车流程
        if (sp5.equals("0")) {
            gh = "" + g_rg.indexOfChild(g_rg.findViewById(g_rg.getCheckedRadioButtonId()));
        } else {
            gh = "2";
        }
        */
        /* v1.2 记录值：【时间】【线路】【方向】【站名】【层次】【乘车状态】【乘车流程】【经度】【纬度】【定位精度】【定位状态码】【null】【null】【null】【null】
        *【乘车流程】0到达车站，1开始上车,2下车
        *  */
        /* v1.3 记录值：【时间】【线路】【方向】【站名】【层次】【乘车状态】【站台位置】【车道流量】【公汽数目】【经度】【纬度】【定位精度】【定位状态码】【null】【null】【null】【null】
        *【乘车流程】0到达车站，1开始上车,2下车
        * v1.4记录值：【时间】【线路】【方向】【站名】【层次】【乘车状态】【经度】【纬度】【定位精度】【定位状态码】【站台位置】【车道流量】【公汽数目】【null】【null】【null】【null】【null】
        *  */
        //Log.d("记录值1", nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + stop_position + "," + lane_flow + "," + bus_num + "," + longitude + "," + latitude + "," + radius + "," + errorCode + ",null,null,null,null\r\n");
        try {
            writer.write(nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + longitude + "," + latitude + "," + radius + "," + errorCode + "," + stop_position + "," + lane_flow + "," + bus_num + ",null,null,null,null,null\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastUtil.showGreenToast(this, "保存成功！");
//        Toast toast = Toast.makeText(this, Html.fromHtml("<font color='#00ff00'>" + "保存成功！" + "</font>"), Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.BOTTOM, 0, -50);
//        toast.show();
    }

    //保存车内数据
    public void SaveData2() {
        //获取记录值
        //获取当前时间
        Date nowDate = new Date();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdFormat.format(nowDate);
        sp1 = line_btn.getText().toString(); //线路名
        sp2 = "" + direction.getSelectedItem().toString(); //线路方向
        sp3 = station.getSelectedItem().toString(); //公交站点
        //观测层次
        //sp4 = "" + layer.indexOfChild(layer.findViewById(layer.getCheckedRadioButtonId()));
        RadioButton layer_rbt = (RadioButton) findViewById(layer.getCheckedRadioButtonId());
        sp4 = layer_rbt.getText().toString();
        sp5 = "" + bus_state.indexOfChild(bus_state.findViewById(bus_state.getCheckedRadioButtonId())); //乘车状态
        //RadioButton rbt = (RadioButton) findViewById(bus_state.getCheckedRadioButtonId());
        //sp5 = rbt.getText().toString();
        sp6 = bus_code.getText().toString(); //车牌
        sp6 = sp6.replace(" ", "");
        sp6 = sp6.replace(",", "&");
        sp7 = zhanzuo.getSelectedItem().toString(); //站坐
        //车厢位置
        if (bus_position1.getCheckedRadioButtonId() == -1) {
            RadioButton p2_rbt = (RadioButton) findViewById(bus_position2.getCheckedRadioButtonId());
            sp8 = p2_rbt.getText().toString();
        } else {
            RadioButton p1_rbt = (RadioButton) findViewById(bus_position1.getCheckedRadioButtonId());
            sp8 = p1_rbt.getText().toString();
        }
        //人流密度
        RadioButton density_rbt = (RadioButton) findViewById(density.getCheckedRadioButtonId());
        sp9 = density_rbt.getText().toString();
        //路段
        RadioButton section_rbt = (RadioButton) findViewById(section.getCheckedRadioButtonId());
        sp10 = "路段" + section_rbt.getText().toString();
        //通风口
        RadioButton air_vent_rbt = (RadioButton) findViewById(grp_air_vent.getCheckedRadioButtonId());
        String air_vent = air_vent_rbt.getText().toString();
        //风感
        RadioButton wind_rbt = (RadioButton) findViewById(grp_wind.getCheckedRadioButtonId());
        String wind = wind_rbt.getText().toString();
        //开窗
        RadioButton window_rbt = (RadioButton) findViewById(grp_window.getCheckedRadioButtonId());
        String window = window_rbt.getText().toString();

        //Log.d("记录值2：", nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + sp6 + "," + air_vent + "," + wind + "," + longitude + "," + latitude + "," + radius + "," + errorCode + "," + sp7 + "," + sp8 + "," + sp9 + "," + sp10 + "\r\n");
        /* v1.3记录值：【时间】【线路】【方向】【站名】【层次】【乘车状态】【车辆编码】【通风口】【风感】【经度】【纬度】【定位精度】【定位状态码】【站坐】【车厢位置】【人流密度】【路段】
        * v1.4记录值：【时间】【线路】【方向】【站名】【层次】【乘车状态】【经度】【纬度】【定位精度】【定位状态码】【车辆编码】【通风口】【风感】【开窗】【站坐】【车厢位置】【人流密度】【路段】
        * */
        try {
            writer.write(nowTime + "," + sp1 + "," + sp2 + "," + sp3 + "," + sp4 + "," + sp5 + "," + longitude + "," + latitude + "," + radius + "," + errorCode + "," + sp6 + "," + air_vent + "," + wind + "," + window + "," + sp7 + "," + sp8 + "," + sp9 + "," + sp10 + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ToastUtil.showGreenToast(this,"保存成功！");
//        Toast toast = Toast.makeText(this, Html.fromHtml("<font color='#00ff00'>" + "保存成功！" + "</font>"), Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.BOTTOM, 0, -50);
//        toast.show();
        //自动选择下一段，当处于3段时自动选择下一站,
        if (section.getCheckedRadioButtonId() == R.id.s3) {
            int n = station.getAdapter().getCount();
            int m = station.getSelectedItemPosition() + 1;
            if (m < n) {
                station.setSelection(m, true);
            }
            section.check(R.id.s1);
            //RadioButton s1=(RadioButton)findViewById(R.id.s1);
            //s1.setChecked(true);
        } else if (section.getCheckedRadioButtonId() == R.id.s1) {
            section.check(R.id.s2);
            //RadioButton s2=(RadioButton)findViewById(R.id.s2);
            //s2.setChecked(true);
        } else {
            section.check(R.id.s3);
            //RadioButton s3=(RadioButton)findViewById(R.id.s3);
            //s3.setChecked(true);
        }
    }


    //切换等车、车内和下车三种状态
    private class OnMyBusStateCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.c1:
                case R.id.c3: {
                    guanceceng.setVisibility(View.VISIBLE);
                    get_on_layout.setVisibility(View.GONE);
                    break;
                }
                case R.id.c2: {
                    guanceceng.setVisibility(View.GONE);
                    get_on_layout.setVisibility(View.VISIBLE);
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
                        bus_position2.clearCheck();
                    break;
                case R.id.p2:
                    if (p2.isChecked())
                        bus_position2.clearCheck();
                    break;
                case R.id.p3:
                    if (p3.isChecked())
                        bus_position2.clearCheck();
                    break;
                case R.id.p4:
                    if (p4.isChecked())
                        bus_position2.clearCheck();
                    break;
            }
        }
    }

    private class OnMyBusPosition2CheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int position) {
            switch (position) {
                case R.id.p5:
                    if (p5.isChecked())
                        bus_position1.clearCheck();
                    break;
                case R.id.p6:
                    if (p6.isChecked())
                        bus_position1.clearCheck();
                    break;
                case R.id.p7:
                    if (p7.isChecked())
                        bus_position1.clearCheck();
                    break;
                case R.id.p8:
                    if (p8.isChecked())
                        bus_position1.clearCheck();
                    break;
            }
        }
    }
    //endregion

    //百度地图定位结果处理类
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明


            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            radius = location.getRadius();    //获取定位精度，默认值为0.0f
            errorCode = location.getLocType();
            gps_time = location.getTime();

            //String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

            //输出GPS显示
            Animation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(500);
            alphaAnimation.setInterpolator(new LinearInterpolator());
            alphaAnimation.setRepeatCount(1);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            lamp.setAnimation(alphaAnimation);

            if (radius < 50 && (errorCode == 66 || errorCode == 61 || errorCode == 161)) {
                gpsTextView.setText("GPS:" + gps_time.substring(11) + "," + latitude + ", " + longitude + ", " + radius + "\n有效定位");
            } else {
                gpsTextView.setText("GPS:" + gps_time.substring(11) + "," + latitude + ", " + longitude + ", " + radius + "\n无效定位");
            }
            WriteGPS();
        }
    }

    //根据公交线路名设置线路方向和站点
    public void setBusLine(String line_name) {
        if ("".equals(line_name.trim())) {
            new android.support.v7.app.AlertDialog.Builder(this).setTitle("提示").setMessage("输入线路为空").setPositiveButton("确定", null).show();
            return;
        }
        //获取根据线路名从bus_data提取出线路信息
        boolean is_get_line = false;
        int x = 0;
        for (int i = 0; i < bus_data[0].length; ++i) {
            if (line_name.equals(bus_data[0][i].trim())) {
                //Log.d(bus_line_name, bus_data[1][i] + "\n" + bus_data[2][i] + "\n" + bus_data[3][i] + "\n" + bus_data[3][i]);
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

        String dir_0 = bus_data[1][x].trim();
        String dir_1 = bus_data[2][x].trim();
        String stops0 = bus_data[3][x].trim();
        String stops1 = bus_data[4][x].trim();

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
                stationAdapter = new ArrayAdapter<String>(bus.this, R.layout.custom_spiner_text_item1, stop_array[i]);
                stationAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item1);
                station.setAdapter(stationAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
