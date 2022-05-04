package com.shizuku.uninstaller;

import android.app.Activity;
import android.app.Service;
import android.app.UiModeManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    boolean  b, c;
    Button B, C;
    int m;

    private final Shizuku.OnRequestPermissionResultListener RL = this::onRequestPermissionsResult;

    private void onRequestPermissionsResult(int i, int i1) {
        check();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());
        B = findViewById(R.id.b);
        C = findViewById(R.id.c);
        Shizuku.addRequestPermissionResultListener(RL);
        m = B.getCurrentTextColor();
        check();
        ListView d = findViewById(R.id.list);
        ListView e = findViewById(R.id.lista);
        e.setAdapter(new adapter(this, new int[]{5, 6, 7, 8, 9}));
        d.setAdapter(new adapter(this, new int[]{0, 1, 2, 3, 4}));
        TranslateAnimation animation = new TranslateAnimation(-50f, 0f, -30f, 0f);
        animation.setDuration(500);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        d.setLayoutAnimation(controller);
        animation = new TranslateAnimation(50f, 0f, -30f, 0f);
        animation.setDuration(500);
        controller = new LayoutAnimationController(animation, 0.1f);
        e.setLayoutAnimation(controller);
    }

    private void check() {
        b = true;
        c = false;
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(0);
            else c=true;
        } catch (Exception e) {
            if (checkSelfPermission("moe.shizuku.manager.permission.API_V23")==PackageManager.PERMISSION_GRANTED)
                c=true;
            if (e.getClass() == IllegalStateException.class) {
                b = false;
                Toast.makeText(this, "shizuku未运行", Toast.LENGTH_SHORT).show();
            }
        }
        B.setText(b ? "shizuku\n已运行" : "shizuku\n未运行");
        B.setTextColor(b ? m : 0x77ff0000);
        C.setText(c ? "shizuku\n已授权" : "shizuku\n未授权");
        C.setTextColor(c ? m : 0x77ff0000);
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(RL);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void ch(View view) {
        check();
    }
}
