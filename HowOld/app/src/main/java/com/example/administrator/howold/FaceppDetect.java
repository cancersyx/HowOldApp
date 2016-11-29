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

    /**
     * 我们使用了匿名内部类，所以我们的参数设置为final类型
     * @param bitmap
     * @param callBack
     */
    public static void detect(final Bitmap bitmap, final CallBack callBack) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("zsf","ddddddddddddd");

                try {
                    //请求
                    Log.d("zsf","在try里面");
//                    HttpRequests requests = new HttpRequests(Constant.Key, Constant.SECRET, false, true);
                    HttpRequests requests = new HttpRequests(Constant.Key,Constant.SECRET,true,false);
                    //Bitmap转换成二进制码
                    Bitmap bmSmall = Bitmap.createBitmap
                            (bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] arrays = byteArrayOutputStream.toByteArray();
                    PostParameters parameters = new PostParameters();
                    parameters.setImg(arrays);
                    JSONObject object = requests.detectionDetect(parameters);
                    //打印个Log
                    Log.d("zsf", object.toString());
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
