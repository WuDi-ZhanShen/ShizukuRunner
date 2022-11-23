package com.shizuku.uninstaller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class adapter extends BaseAdapter {
    private final int[] data;
    private final Context mContext;

    public adapter(Context mContext, int[] data) {
        super();
        this.mContext = mContext;
        this.data = data;
    }

    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.r, null);
            holder = new ViewHolder();
            holder.texta = convertView.findViewById(R.id.a);
            holder.textb = convertView.findViewById(R.id.b);
            holder.imageButton = convertView.findViewById(R.id.c);
            holder.layout = convertView.findViewById(R.id.l);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SharedPreferences b = mContext.getSharedPreferences(String.valueOf(data[position]), 0);
        init(holder, b);
        return convertView;
    }

    static class ViewHolder {
        TextView texta;
        TextView textb;
        ImageButton imageButton;
        LinearLayout layout;
    }

    void init(ViewHolder holder, SharedPreferences b) {

        boolean existc = b.getString("content", null) == null || b.getString("content", null).length() == 0;
        boolean existn = b.getString("name", null) == null || b.getString("name", null).length() == 0;

        View.OnClickListener voc = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v = View.inflate(mContext, R.layout.dialog, null);
                final CheckBox cb = v.findViewById(R.id.cb);
                cb.setChecked(b.getBoolean("shell", false));
                final EditText editText = v.findViewById(R.id.e);
                editText.setText(b.getString("content", null));
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            b.edit().putString("content", editText.getText().toString()).putBoolean("shell", cb.isChecked()).apply();
                            init(holder, b);
                        }
                        return false;
                    }
                });
                final EditText editText1 = v.findViewById(R.id.a);
                editText1.setText(b.getString("name", null));
                editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            b.edit().putString("name", editText1.getText().toString()).putBoolean("shell", cb.isChecked()).apply();
                            init(holder, b);
                        }
                        return false;
                    }
                });
                editText.requestFocus();
                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
                    }
                }, 200);
                new AlertDialog.Builder(mContext).setTitle("编辑命令").setView(v).setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        b.edit().putString("content", editText.getText().toString()).putString("name", editText1.getText().toString()).putBoolean("shell", cb.isChecked()).apply();
                        init(holder, b);
                    }
                }).show();
            }
        };

        holder.imageButton.setImageResource(existc ? R.drawable.plus : R.drawable.run);
        holder.imageButton.setOnClickListener(!existc ? new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, Exec.class).putExtra("content", b.getBoolean("shell", false) ? "whoami|grep root &> /dev/null && echo '提示:已将root降权至shell' 1>&2;" + mContext.getApplicationInfo().nativeLibraryDir + "/libchid.so 2000 " + b.getString("content", " ") + " || " + b.getString("content", " ") : b.getString("content", " ")));
            }
        } : voc);
        holder.texta.setText(existn ? "空" : b.getString("name", "空"));
        holder.texta.setTextColor(existc ? mContext.getResources().getColor(R.color.b) : mContext.getResources().getColor(R.color.a));
        holder.textb.setText(existc ? "空" : b.getString("content", "空"));
        holder.layout.setOnClickListener(voc);
    }

}
