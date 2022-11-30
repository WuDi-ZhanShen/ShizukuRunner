package com.shizuku.uninstaller;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    boolean b, c;
    Button B, C;
    int m;
    ListView d, e;
    EditText e1;
    ImageView iv;
    SharedPreferences sp;
//shizuku监听授权结果
    private final Shizuku.OnRequestPermissionResultListener RL = this::onRequestPermissionsResult;


    private void onRequestPermissionsResult(int i, int i1) {
        check();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //根据系统深色模式自动切换软件的深色/亮色主题
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        sp = getSharedPreferences("data",0);
        //如果是初次开启，则展示help界面
        if (sp.getBoolean("first",true)) {
            showHelp();
            sp.edit().putBoolean("first",false).apply();
        }
        //读取用户设置“是否隐藏后台”，并进行隐藏后台
        ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(sp.getBoolean("hide",true));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //横屏时不铺满屏幕，限定一下窗口宽度
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());
        B = findViewById(R.id.b);
        C = findViewById(R.id.c);
        iv= findViewById(R.id.iv);

        //设置猫猫图案的长按事件为展示帮助界面
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showHelp();
                return false;
            }
        });
        Shizuku.addRequestPermissionResultListener(RL);

        //m用于保存shizuku状态显示按钮的初始颜色（int类型哦），为的是适配安卓12的莫奈取色，方便以后恢复颜色时用
        m = B.getCurrentTextColor();

        //检查Shizuk是否运行，并申请Shizuku权限
        check();
        d = findViewById(R.id.list);
        e = findViewById(R.id.lista);

        //为两列listView适配每个item的具体样式和总item数
        initlist();
    }

    private void showHelp() {
        //展示帮助界面
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.help, null);
        ((TextView)v.findViewById(R.id.t3)).setText(Html.fromHtml("&nbsp;&nbsp;本应用<u><b><big>不会</big></b></u>收集您的任何信息，且完全不包含任何联网功能。<br>&nbsp;&nbsp;使用本应用需要您的设备已安装并激活Shizuku。<br>&nbsp;&nbsp;在后续的使用中，您可以<u><b><big>长按</big></b></u>主界面标题上的猫猫图案(如下图所示)来打开此帮助界面。" ));
        ((TextView)v.findViewById(R.id.t4)).setText(Html.fromHtml("&nbsp;&nbsp;--您可以点击某一个命令栏目来编辑该栏目；编辑完成并保存后，您可以点击运行按钮来运行刚才保存的命令。<br><br>&nbsp;&nbsp;--您也可以<u><b><big>单击</big></b></u>标题上的猫猫图案来切换APP为一次性运行命令的模式。<br><br>&nbsp;&nbsp;--点击主界面标题上的两个显示Shizuku状态的按钮中的任意一个，均可<u><b><big>刷新Shiuzku状态</big></b></u>。当然，关闭再打开本APP也是不错的刷新方法。<br><br>&nbsp;&nbsp;--如果您的设备上使用了“以root权限启动Shizuku”，那么本APP在执行命令时也将具有root权限。假如您不希望<u><b><big>以如此高的权限执行命令</big></b></u>(大材小用)，您可以勾选命令编辑界面的“将root权限降至Shell”来让APP仅使用Shell权限执行命令。<br><br>&nbsp;&nbsp;--您可以点击本界面下方的设置按钮探索更多功能哦！"));
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("使用帮助")
                .setView(v)
                .setNegativeButton("OK", null)
                .setNeutralButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.getWindow().getAttributes().alpha = 0.85f;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);

                        View v = View.inflate(MainActivity.this, R.layout.set, null);
                        Switch S = v.findViewById(R.id.s);

                        S.setChecked(sp.getBoolean("hide",true));
                        S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                sp.edit().putBoolean("hide",b).apply();
                                ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(b);
                            }
                        });
                        Switch S1 = v.findViewById(R.id.s1);

                        S1.setChecked(sp.getBoolean("20",false));
                        S1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                sp.edit().putBoolean("20",b).apply();
                                Toast.makeText(MainActivity.this, "重启APP后生效", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.setView(v);
                        dialog.show();
                    }
                })
                .create().show();

    }

    private void check() {

        //本函数用于检查shizuku状态，b代表shizuk是否运行，c代表shizuku是否授权
        b = true;
        c = false;
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(0);
            else c = true;
        } catch (Exception e) {
            if (checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED)
                c = true;
            if (e.getClass() == IllegalStateException.class) {
                b = false;
                Toast.makeText(this, "Shizuku未运行", Toast.LENGTH_SHORT).show();
            }
        }
        B.setText(b ? "Shizuku\n已运行" : "Shizuku\n未运行");
        B.setTextColor(b ? m : 0x77ff0000);
        C.setText(c ? "Shizuku\n已授权" : "Shizuku\n未授权");
        C.setTextColor(c ? m : 0x77ff0000);
    }

    @Override
    protected void onDestroy() {
        //在APP退出时，取消注册Shizuku授权结果监听，这是Shizuku的要求
        Shizuku.removeRequestPermissionResultListener(RL);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //在点击返回键时直接退出APP，因为APP比较轻量，没必要双击返回退出或者设置什么退出限制
        finish();
    }

    public void ch(View view) {
        //本函数绑定了主界面两个显示Shizuk状态的按钮的点击事件
        check();
    }

    public void ex(View view) {
        //单击猫猫头像的点击事件，让list变不可见，让EditText可见。

        flipAnimation(view);
        d.setVisibility(View.INVISIBLE);
        e.setVisibility(View.INVISIBLE);
        d.setAdapter(new adapter(this, new int[]{}));
        e.setAdapter(new adapter(this, new int[]{}));
        findViewById(R.id.l1).setVisibility(View.VISIBLE);
        e1 = findViewById(R.id.e);
        e1.setEnabled(true);
        e1.requestFocus();
        e1.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(e1, 0);
            }
        }, 200);
        e1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    exe(v);
                }
                return false;
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipAnimation(view);
                d.setVisibility(View.VISIBLE);
                e.setVisibility(View.VISIBLE);
                e1.setEnabled(false);
                initlist();
                findViewById(R.id.l1).setVisibility(View.GONE);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ex(view);
                    }
                });
            }
        });
    }


    private void flipAnimation(View view) {
        //flipAnimation是一个轻量级的翻转动画，很有趣哦
        ObjectAnimator a2 = ObjectAnimator.ofFloat(view, "rotationY", 90f, 0f);
        a2.setDuration(300).setInterpolator(new LinearInterpolator());
        a2.start();

    }


    public void exe(View view) {

        //EditText右边的执行按钮，点击后的事件
        if (e1.getText().length() > 0)
            startActivity(new Intent(this, Exec.class).putExtra("content", e1.getText().toString()));
    }


    public void initlist() {
        //根据用户设置，选择展示10个格子或者更多格子
        int[] e1 = sp.getBoolean("20",false)?new int[]{5, 6, 7, 8, 9, 15, 16, 17, 18, 19,25,26,27,28,29,35,36,37,38,39,45,46,47,48,49}:new int[]{5, 6, 7, 8, 9};
        int[] d1 = sp.getBoolean("20",false)?new int[]{0, 1, 2, 3, 4, 10, 11, 12, 13, 14,20,21,22,23,24,30,31,32,33,34,40,41,42,43,44}:new int[]{0, 1, 2, 3, 4};
        e.setAdapter(new adapter(this, e1));
        d.setAdapter(new adapter(this, d1));

        //加一点动画，非常的丝滑~~
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
}
