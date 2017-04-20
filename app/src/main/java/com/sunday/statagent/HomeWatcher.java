package com.sunday.statagent;

/**
 * Created by Sunday on 2016/1/26.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Home键监听封装
 *
 * @author way
 */
public class HomeWatcher {

    static final String TAG = "HomeWatcher";
    public Context mContext;
    public IntentFilter mFilter;
    public OnHomePressedListener mListener;
    public InnerRecevier mRecevier;
    public static HomeWatcher homeWatcher;

    // 回调接口
    public interface OnHomePressedListener {
        public void onHomePressed();

        public void onHomeLongPressed();
    }

    public static void initWatcher(final Context context) {
        homeWatcher = new HomeWatcher(context);
        homeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                LogUtil.i("onHomePressed!");
                SharedPreferences.Editor editor = context.getSharedPreferences(StatAgent.SP_NAME,Context.MODE_PRIVATE).edit();
                editor.putLong("exitTime", new Date().getTime());
                editor.commit();
            }

            @Override
            public void onHomeLongPressed() {
                LogUtil.i("onHomeLongPressed!");
                SharedPreferences.Editor editor = context.getSharedPreferences(StatAgent.SP_NAME,Context.MODE_PRIVATE).edit();
                editor.putLong("exitTime", new Date().getTime());
                editor.commit();
            }
        });
        homeWatcher.startWatch();
    }

    public static void stopWatcher(Context context) {
        if (null != homeWatcher){
            homeWatcher.stopWatch();
        }
    }

    public HomeWatcher(Context context) {
        mContext = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setOnHomePressedListener(OnHomePressedListener listener) {
        mListener = listener;
        mRecevier = new InnerRecevier();
    }

    /**
     * 开始监听，注册广播
     */
    public void startWatch() {
        if (mRecevier != null) {
            mContext.registerReceiver(mRecevier, mFilter);
        }
    }

    /**
     * 停止监听，注销广播
     */
    public void stopWatch() {
        if (mRecevier != null) {
            mContext.unregisterReceiver(mRecevier);
        }
    }

    /**
     * 广播接收者
     */
    class InnerRecevier extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
//                    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            // 短按home键
                            mListener.onHomePressed();
                        } else if (reason
                                .equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            // 长按home键
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
        }
    }
}