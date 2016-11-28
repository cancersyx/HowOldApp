package com.example.administrator.howold;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by ${zsf} on 2016/11/28.
 * 工具类
 */
public class FaceppDetect {

    public interface CallBack {
        void success(JSONObject result);

        void error(FaceppParseException exception);
    }

    public static void detect(final Bitmap bitmap, final CallBack callBack) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //请求
                    HttpRequests requests = new HttpRequests(Constant.Key, Constant.SECRET, true, true);
                    Bitmap bmSmall = Bitmap.createBitmap
                            (bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] arrays = byteArrayOutputStream.toByteArray();
                    PostParameters parameters = new PostParameters();
                    parameters.setImg(arrays);
                    JSONObject object = requests.detectionDetect(parameters);
                    //打印个Log
                    Log.e("zsf",object.toString());
                    if (callBack != null) {
                        callBack.success(object);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();
                    if (callBack != null) {
                        callBack.error(e);
                    }
                }
            }
        }).start();

    }
}
