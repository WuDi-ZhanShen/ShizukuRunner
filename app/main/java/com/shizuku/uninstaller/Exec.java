package com.shizuku.uninstaller;

import android.app.Activity;
import android.app.Service;
import android.app.UiModeManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
    boolean br=false;

    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<Exec> mOuter;

        public MyHandler(Exec activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            mOuter.get().t2.append(msg.what == 1 ? Html.fromHtml(msg.obj + "<br>") : String.valueOf(msg.obj));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("执行中");
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        getWindow().getAttributes().alpha = 0.85f;
        setContentView(R.layout.exec);
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
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
            p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((cmd+"\nexit\n").getBytes());
            out.flush();
            out.close();
            h2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {
                            if (t2.length() > 1000||br) break;
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
            h3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {
                            if (t2.length() > 1000||br) break;
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = inline.equals("") ? null : "<font color='red'>" + inline + "</>";
                            mHandler.sendMessage(msg);
                        }
                        mReader.close();
                    } catch (Exception ignored) {
                    }
                }
            });
            h3.start();
            p.waitFor();
            String exitValue = String.valueOf(p.exitValue());
            t1.post(new Runnable() {
                @Override
                public void run() {
                    t1.setText(String.format("返回值：%s", exitValue));
                    setTitle("执行完毕");
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        try {
            br=true;
            if (Build.VERSION.SDK_INT >= 26) {
                p.destroyForcibly();
            } else {
                p.destroy();
            }
            h1.interrupt();
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}