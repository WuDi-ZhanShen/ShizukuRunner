package com.shizuku.uninstaller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
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
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent(mContext, Exec.class).putExtra("content", b.getString("content", " ")));
            }
        });
        holder.texta.setText(b.getString("name", "空"));
        holder.textb.setText(b.getString("content", "空"));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(mContext);
                editText.setText(b.getString("content", null));
                editText.setSingleLine(true);
                editText.requestFocus();
                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
                    }
                }, 200);
                new AlertDialog.Builder(mContext).setTitle("设置命令内容").setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        b.edit().putString("content", editText.getText().toString()).apply();
                        holder.textb.setText(editText.getText().toString());
                    }
                }).show();
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final EditText editText = new EditText(mContext);
                editText.setSingleLine(true);
                editText.setText(b.getString("name", null));
                editText.requestFocus();
                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
                    }
                }, 200);
                new AlertDialog.Builder(mContext).setTitle("设置标题").setView(editText).setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        b.edit().putString("name", editText.getText().toString()).apply();
                        holder.texta.setText(editText.getText().toString());
                    }
                }).show();
                return false;
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView texta;
        TextView textb;
        ImageButton imageButton;
        LinearLayout layout;
    }

}
