package com.example.wen.transitrecorder;

import android.os.Environment;
import android.util.Log;
import android.content.Context;

import java.io.*;
import java.util.logging.Logger;

import android.content.res.*;

import jxl.*;

/**
 * Created by WENS on 2018/4/14.
 */

public class ReadExcel {

    public static String[][] getExcelData(Context context, String filename) {
        //Log.d("读取excel类", "开始读取");
        try {
            String[][] data;
            //首先加载_transit文件夹下的线路表，如果不存在则使用assets里的表
            //创建文件夹及文件
            File filedir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "_transit");
            if (!filedir.exists()) {
                filedir.mkdirs();
            }
            File custom_xls_file = new File(filedir + "/" + filename);
            InputStream is;
            if (custom_xls_file.exists()) {
                is = new FileInputStream(custom_xls_file);
                if (filename.equals("bus.xls")) {
                    MainActivity.is_custom_bus = true;
                }
                if (filename.equals("subway.xls")) {
                    MainActivity.is_custom_subway = true;
                }
            } else {
                is = context.getResources().getAssets().open(filename);
            }
            Workbook book = Workbook.getWorkbook(is);
            Sheet sheet = book.getSheet(0);
            int Rows = sheet.getRows();
            //int Cols = sheet.getColumns();
            //Log.d("总行列：", Rows + "," + Cols);
            data = new String[5][Rows - 2];
            //读取线路名、起点站、终点站、上行站点、下行站点，共5列。
            int[] c_lst = {2, 3, 4, 5, 6};
            for (int i = 0; i < c_lst.length; ++i) {
                for (int j = 2; j < Rows; ++j) {
                    Cell cell = sheet.getCell(c_lst[i], j);
                    String result = cell.getContents();
                    //Log.d("列：" + c_lst[i] + ",行：" + j, result);
                    data[i][j - 2] = result;
                }
            }
            book.close();
            //Log.d("第一行", data[0][0] + "," + data[1][0] + "," + data[2][0] + "," + data[3][0] + "," + data[4][0]);
            return data;
        } catch (Exception e) {
            new android.support.v7.app.AlertDialog.Builder(context).setTitle("提示").setMessage("加载线路数据失败").setPositiveButton("确定", null).show();
            //System.out.println(e);
            //Log.e("读取错误", "读取失败");
        }
        return null;
    }
}
