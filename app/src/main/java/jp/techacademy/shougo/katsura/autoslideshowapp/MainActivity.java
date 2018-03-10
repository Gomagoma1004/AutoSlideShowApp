package jp.techacademy.shougo.katsura.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer mTimer;

    Uri imageUri;

    Handler mHandler = new Handler();

    Button mNextButton;
    Button mBackButton;
    Button mStartAndStopButton;

    ImageView mImageView;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNextButton = (Button) findViewById(R.id.Next);
        mBackButton = (Button) findViewById(R.id.Back);
        mStartAndStopButton = (Button) findViewById(R.id.StartAndStop);

        mImageView = (ImageView) findViewById(R.id.Image);

        mStartAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimer == null) {
                    mTimer = new Timer();
                    mStartAndStopButton.setText("Stop");
                    mNextButton.setEnabled(false);
                    mBackButton.setEnabled(false);
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (cursor.moveToNext()) {
                                        doCursor();
                                    } else {
                                        cursor.moveToFirst();
                                        doCursor();
                                    }
                                }
                            });
                        }
                    }, 2000, 2000);
                } else if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                    mNextButton.setEnabled(true);
                    mBackButton.setEnabled(true);
                    mStartAndStopButton.setText("Start");
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToNext()) {
                    doCursor();

                } else {
                    cursor.moveToFirst();
                    doCursor();
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToPrevious()) {
                    doCursor();

                } else {
                    cursor.moveToLast();
                    doCursor();
                }
            }
        });


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }


    }

    public void doCursor(){
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        mImageView.setImageURI(imageUri);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                showAlertDialog();
                break;
        }
    }

    private void showAlertDialog() {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("許可されませんでした");
        alertDialogBuilder.setMessage("許可されないとこのアプリは使用できません");

        // 肯定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("それな",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("UI_PARTS", "肯定ボタン");
                    }
                });

        // 否定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setNegativeButton("それま？",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("UI_PARTS", "否定ボタン");
                    }
                });

        // AlertDialogを作成して表示する
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
            doCursor();
            Log.d("ANDROID", "URI : " + imageUri.toString());
        }

    }
}
