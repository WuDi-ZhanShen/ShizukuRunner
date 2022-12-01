package com.shizuku.uninstaller;

import android.app.Activity;
import android.app.Service;
import android.app.UiModeManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import rikka.shizuku.Shizuku;

public class Exec extends Activity {

    TextView t1, t2;
    Process p;
    Thread h1, h2, h3;
    boolean br = false;


    //mHandler用于弱引用和主线程更新UI，为什么一定要这样搞呢，简单地说就是不这样就会报错、会内存泄漏。
    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<Exec> mOuter;

        public MyHandler(Exec activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            //msg.what 是1就是错误信息，是2是正常信息
            mOuter.get().t2.append(msg.what == 1 ? (SpannableString)msg.obj : String.valueOf(msg.obj));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("执行中");

        //根据系统深色模式动态改变深色主题
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);

        //半透明背景
        getWindow().getAttributes().alpha = 0.85f;
        setContentView(R.layout.exec);
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);

        //子线程执行命令，否则UI线程执行就会导致UI卡住动不了
        h1 = new Thread(new Runnable() {
            @Override
            public void run() {
                ShizukuExec(getIntent().getStringExtra("content"));
            }
        });
        h1.start();
    }

    public void ShizukuExec(String cmd) {
        try {

            //记录执行开始的时间
            long time = System.currentTimeMillis();

            //使用Shizuku执行命令
            p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((cmd + "\nexit\n").getBytes());
            out.flush();
            out.close();

            //开启新线程，实时读取命令输出
            h2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {

                            //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
                            if (t2.length() > 2000 || br) break;
                            Message msg = new Message();
                            msg.what = 0;
                            msg.obj = inline.equals("") ? "\n" : inline + "\n";
                            mHandler.sendMessage(msg);
                        }
                        mReader.close();
                    } catch (Exception ignored) {
                    }
                }
            });
            h2.start();

            //开启新线程，实时读取命令报错信息
            h3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {

                            //如果TextView的字符太多了（会使得软件非常卡顿），或者用户退出了执行界面（br为true），则停止读取
                            if (t2.length() > 2000 || br) break;
                            Message msg = new Message();
                            msg.what = 1;
                            if (inline.equals(""))
                                msg.obj = null;
                            else {
                                SpannableString ss = new SpannableString(inline+"\n");
                                ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                msg.obj = ss;
                            }
                            mHandler.sendMessage(msg);
                        }
                        mReader.close();
                    } catch (Exception ignored) {
                    }
                }
            });
            h3.start();

            //等待命令运行完毕
            p.waitFor();

            //获取命令返回值
            String exitValue = String.valueOf(p.exitValue());

            //显示命令返回值和命令执行时长
            t1.post(new Runnable() {
                @Override
                public void run() {
                    t1.setText(String.format("返回值：%s\n执行用时：%.2f秒", exitValue, (System.currentTimeMillis() - time) / 1000f));
                    setTitle("执行完毕");
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {

        //关闭所有输入输出流，销毁进程，防止内存泄漏等问题
        br = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        p.destroyForcibly();
                    } else {
                        p.destroy();
                    }
                    h1.interrupt();
                    h2.interrupt();
                    h3.interrupt();
                } catch (Exception ignored) {
                }
            }
        }, 1000);
        super.onDestroy();
    }
}
