package com.dd.test.puzzleview_android.activity.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.test.puzzleview_android.R;
import com.dd.test.puzzleview_android.activity.view.TopView;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private TopView topView;
    private Button picSelectBtn;
    private ImageView picShowImageView;
    private MyBroadCastReceiver broadcastReceiver;

    private static final int EXTERNAL_STORAGE = 101;
    private boolean hasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topView = (TopView) findViewById(R.id.top_view);
        picSelectBtn = (Button) findViewById(R.id.pic_select);
        picShowImageView = (ImageView) findViewById(R.id.pic_show);

        topView.setTitle("puzzle-android");
        topView.hide(TopView.LEFT);

        picSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPermission){
                    Intent intentToPuzzle = new Intent(MainActivity.this, PuzzlePickerActivity.class);
                    startActivity(intentToPuzzle);
                }
            }
        });

        checkPermission();
    }

    private void checkPermission() {
        //去寻找是否已经有了相机的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            //Toast.makeText(MainActivity.this,"您申请了动态权限",Toast.LENGTH_SHORT).show();
            //如果有了相机的权限有调用相机
            hasPermission = true;

        }else{
            //否则去请求相机权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},EXTERNAL_STORAGE);

        }
    }

    @Override
    protected void onStart() {
        if (broadcastReceiver == null) {
            broadcastReceiver = new MyBroadCastReceiver();
        }
        IntentFilter intentFilter = new IntentFilter("puzzle");
        registerReceiver(broadcastReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String picPath = intent.getStringExtra("picPath");
            if (picPath != null) {
                Glide.with(context)
                        .load(String.format("file://%s", picPath))
                        .crossFade()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(picShowImageView);

            }
        }
    }

//    private void checkPermission() {
//        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//        if (EasyPermissions.hasPermissions(this, perms)) {
//        } else {
//            EasyPermissions.requestPermissions(this, "获取SD卡权限", EXTERNAL_STORAGE, perms);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        // Forward results to EasyPermissions
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
//    }
//
//    @Override
//    public void onPermissionsGranted(int requestCode, List<String> list) {
//        // Some permissions have been granted
//        // 请求权限已经被授权
//        // ...
//    }
//
//    @Override
//    public void onPermissionsDenied(int requestCode, List<String> list) {
//        // Some permissions have been denied
//        // 请求权限被拒绝
//        // ...
//    }

}
