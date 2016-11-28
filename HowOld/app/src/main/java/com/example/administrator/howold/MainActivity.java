package com.example.administrator.howold;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mDetectBtn;
    private Button mGetImageBtn;
    private TextView mTipTextView;
    private ImageView mPhotoImageView;
    private FrameLayout mWaittingFLayout;
    private Bitmap mPhotoImage;

    private String mCurrentPhotoStr;//当前图片的路径
    private static final int PICK_CODE = 0x110;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
    }

    private void initEvents() {
        mGetImageBtn.setOnClickListener(this);
        mDetectBtn.setOnClickListener(this);
    }

    private void initViews() {
        mDetectBtn = (Button) findViewById(R.id.btn_detect);
        mGetImageBtn = (Button) findViewById(R.id.btn_get_image);
        mTipTextView = (TextView) findViewById(R.id.tv_tip);
        mPhotoImageView = (ImageView) findViewById(R.id.iv_photo);
        mWaittingFLayout = (FrameLayout) findViewById(R.id.fl_waitting);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_image:
                //得到图片
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);
                break;
            case R.id.btn_detect:

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurrentPhotoStr = cursor.getString(idx);
                cursor.close();
                //获取图片，先压缩
                //不大于3兆
                resizePhoto();
                mPhotoImageView.setImageBitmap(mPhotoImage);
                mTipTextView.setText("Click DETECT ==>");

            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 压缩图片
     */
    private void resizePhoto() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoStr, options);
        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0f / 1024f);
        options.inSampleSize = (int) Math.ceil(ratio);
        options.inJustDecodeBounds = false;
        mPhotoImage = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
    }
}
