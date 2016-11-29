package com.example.administrator.howold;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mDetectBtn;
    private Button mGetImageBtn;
    private TextView mTipTextView;
    private ImageView mPhotoImageView;
    private FrameLayout mWaittingFLayout;
    private Bitmap mPhotoImage;
    private Paint mPaint;

    private String mCurrentPhotoStr;//当前图片的路径
    private static final int PICK_CODE = 0x110;
    private static final int MSG_SUCCESS = 0X111;
    private static final int MSG_ERROR = 0X112;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    mWaittingFLayout.setVisibility(View.GONE);
                    JSONObject rs = (JSONObject) msg.obj;
                    prepareRsBitmap(rs);
                    mPhotoImageView.setImageBitmap(mPhotoImage);
                    break;
                case MSG_ERROR:
                    mWaittingFLayout.setVisibility(View.GONE);
                    String errorMsg = (String) msg.obj;
                    if (TextUtils.isEmpty(errorMsg)) {
                        mTipTextView.setText("Error.");
                    } else {
                        mTipTextView.setText(errorMsg);
                    }

                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void prepareRsBitmap(JSONObject rs) {

        Bitmap bitmap = Bitmap.createBitmap(mPhotoImage.getWidth(), mPhotoImage.getHeight(), mPhotoImage.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mPhotoImage, 0, 0, null);
        try {
            JSONArray faces = rs.getJSONArray("face");
            int faceCount = faces.length();
            mTipTextView.setText("find" + faceCount);
            for (int i = 0; i < faceCount; i++) {
                //拿到单独face对象
                JSONObject face = faces.getJSONObject(i);
                JSONObject posObj = face.getJSONObject("position");

                float x = (float) posObj.getJSONObject("center").getDouble("x");
                float y = (float) posObj.getJSONObject("center").getDouble("y");

                float w = (float) posObj.getDouble("width");
                float h = (float) posObj.getDouble("height");

                x = x / 100 * bitmap.getWidth();
                y = y / 100 * bitmap.getHeight();

                w = w / 100 * bitmap.getWidth();
                h = h / 100 * bitmap.getHeight();

                mPaint.setColor(0xffffffff);
                mPaint.setStrokeWidth(3);

                //画box
                canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2, mPaint);
                canvas.drawLine(x + w / 2, y - h / 2, x + w / 2, y + h / 2, mPaint);
                canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2, mPaint);

                //得到年龄和性别
                int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");

                //转化textView为Bitmap
                Bitmap ageBitmap = buildAgeBitmap(age, "Male".equals(gender));

                //缩放
                int ageWidth = ageBitmap.getWidth();
                int ageHeight = ageBitmap.getHeight();
                if (bitmap.getWidth() < mPhotoImageView.getWidth() && bitmap.getHeight() < mPhotoImageView.getHeight()) {
                    float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhotoImageView.getWidth(),
                            bitmap.getHeight() * 1.0f / mPhotoImageView.getHeight());
                    ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int) (ageWidth * ratio),
                            (int) (ageHeight * ratio), false);
                }
                canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth() / 2,
                        y - h / 2 - ageBitmap.getHeight(), null);

                mPhotoImage = bitmap;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Bitmap buildAgeBitmap(int age, boolean isMale) {

        TextView tv = (TextView) mWaittingFLayout.findViewById(R.id.tv_age_and_gender);
        tv.setText(age + "");
        if (isMale) {
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().
                    getDrawable(R.drawable.male), null, null, null);

        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().
                    getDrawable(R.drawable.female), null, null, null);
        }
        tv.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
        tv.destroyDrawingCache();

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();

        mPaint = new Paint();

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
                mWaittingFLayout.setVisibility(View.VISIBLE);

                if (mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals("")){
                    resizePhoto();
                }else {
                    mPhotoImage = BitmapFactory.decodeResource(getResources(),R.drawable.t4);
                }

                FaceppDetect.detect(mPhotoImage, new FaceppDetect.CallBack() {
                    @Override
                    public void success(JSONObject result) {
                        Message message = new Message();
                        message.what = MSG_SUCCESS;
                        message.obj = result;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void error(FaceppParseException exception) {
                        Message message = new Message();
                        message.what = MSG_ERROR;
                        message.obj = exception.getErrorMessage();
                        mHandler.sendMessage(message);
                    }
                });
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
