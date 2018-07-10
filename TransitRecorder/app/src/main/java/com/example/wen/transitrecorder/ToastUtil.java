package com.example.wen.transitrecorder;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by wen on 2017/11/11.
 */

public class ToastUtil {

    private static Toast toast;
    private static Toast toast1;
    private static Toast toast2;

    /**
     * 显示Toast
     *
     * @param context 上下文
     * @param content 要显示的内容
     */
    public static void showRedToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
            View view = toast.getView();
            view.setBackgroundResource(android.R.color.holo_red_light);
            view.setPadding(20, 20, 20, 20);
            toast.setGravity(Gravity.BOTTOM, 0, 30);
            toast.setView(view);
        } else {
            View view = toast.getView();
            view.setBackgroundResource(android.R.color.holo_red_light);
            view.setPadding(20, 20, 20, 20);
            toast.setView(view);
            toast.setGravity(Gravity.BOTTOM, 0, 30);
            toast.setText(content);
        }
        toast.show();
    }

    public static void showGreenToast(Context context, String content) {
        if (toast1 == null) {
            toast1 = Toast.makeText(context, Html.fromHtml("<font color='#00ff00'>" + content + "</font>"), Toast.LENGTH_SHORT);
            toast1.setGravity(Gravity.BOTTOM, 0, -50);
        }
        toast1.show();
    }

    public static void showLongToast(Context context, String content) {
        if (toast2 == null) {
            toast2 = Toast.makeText(context, Html.fromHtml("<font color='#ffffff'>" + content + "</font>"), Toast.LENGTH_LONG);
            toast2.setGravity(Gravity.BOTTOM, 0, -50);
        }
        toast2.show();
    }
}

