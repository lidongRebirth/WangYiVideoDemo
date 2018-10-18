package com.myfittinglife.app.wangyivideodemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.myfittinglife.app.wangyivideodemo.bean.AlertBean;
import com.myfittinglife.app.wangyivideodemo.bean.LoginBean;
import com.myfittinglife.app.wangyivideodemo.utils.CheckSumBuilder;
import com.myfittinglife.app.wangyivideodemo.utils.JsonCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 注册登录请看此文档：必看！！！
 * https://dev.yunxin.163.com/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/%E6%9C%8D%E5%8A%A1%E7%AB%AFAPI%E6%96%87%E6%A1%A3/%E7%BD%91%E6%98%93%E4%BA%91%E9%80%9A%E4%BF%A1ID
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btn_sign)                //注册
            Button btn_sign;
    @BindView(R.id.btn_login)               //登录
            Button btn_login;
    @BindView(R.id.btn_alert_password)      //更改密码
            Button btn_alert_password;
    @BindView(R.id.et_id)                   //账号
            EditText et_id;
    @BindView(R.id.et_password)             //密码
            EditText et_password;
    @BindView(R.id.et_store_password)       //更改后的密码
            EditText et_store_password;

    private static final String TAG = "LoginActivity_test";
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        context = getApplicationContext();
    }


    @OnClick({R.id.btn_sign, R.id.btn_login, R.id.btn_alert_password})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign:                                     //注册
                if (!TextUtils.isEmpty(et_id.getText().toString())) {
                    registerOKGoJson();
                }
                break;
            case R.id.btn_login:                                    //登录
                if (!TextUtils.isEmpty(et_id.getText().toString())&&!TextUtils.isEmpty(et_password.getText().toString())) {
                    login();
                }
                break;
            case R.id.btn_alert_password:                           //更改密码
                if (!TextUtils.isEmpty(et_id.getText().toString())) {
                    alertPasswordOKGoJson();
                }
                break;
            default:
                break;
        }
    }

    //注册    200操作成功 403非法操作或没有权限 414参数错误 416频率控制 431http重复请求 500服务器内部错误
    public void registerOKGoJson() {

        String appKey = "c2f97b1b1064e0754410b2da63f06365";         //*填入自己的appkey
        String appSecret = "f9e40c216540";                          //*填入自己的appSecret
        String nonce = "12345";                                     //随机数，最大长度128个字符，可随意填写
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        String checkSum = CheckSumBuilder.getCheckSum(appSecret, nonce, curTime);   //参考 计算CheckSum的java代码

        OkGo.<LoginBean>post("https://api.netease.im/nimserver/user/create.action")
                .tag(this)
                .headers("AppKey", appKey)
                .headers("Nonce", nonce)
                .headers("CurTime", curTime)
                .headers("CheckSum", checkSum)
                .headers("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .params("accid", et_id.getText().toString())            //最大长度32字符，必须保证一个APP内唯一（只允许字母、数字、半角下划线_、@、半角点以及半角-组成，不区分大小写，会统一小写处理，请注意以此接口返回结果中的accid为准
                .execute(new JsonCallBack<LoginBean>(LoginBean.class, context) {
                    @Override
                    public void onSuccess(Response<LoginBean> response) {
                        if (response != null && response.body() != null) {
                            if (response.body().getCode() == 200) {
                                Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "onSuccess: 注册成功" + response.body().getInfo().toString());
                                et_store_password.setText(response.body().getInfo().getToken());
                            } else {
                                Toast.makeText(context, "注册失败:" + response.body().getCode(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }


                });
    }

    //登录
    public void login() {
        LoginInfo info = new LoginInfo(et_id.getText().toString(), et_password.getText().toString());       //用户登录信息
        RequestCallback<LoginInfo> callback = new RequestCallback<LoginInfo>() {                            //回调接口事件
            @Override
            public void onSuccess(LoginInfo param) {
                Log.i(TAG, "onSuccess: " + param.getAccount() + param.getAppKey() + param.getToken());
                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("mine_account", et_id.getText().toString());
                startActivity(intent);

            }

            @Override
            public void onFailed(int code) {
                Log.i(TAG, "登录失败，错误码为：" + code);
            }

            @Override
            public void onException(Throwable exception) {

            }
        };
        NIMClient.getService(AuthService.class).login(info)                                                 //登录
                .setCallback(callback);

    }

    //更改密码
    public void alertPasswordOKGoJson() {

        String appKey = "c2f97b1b1064e0754410b2da63f06365";                     //*
        String appSecret = "f9e40c216540";                                      //*
        String nonce = "12345";
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        String checkSum = CheckSumBuilder.getCheckSum(appSecret, nonce, curTime);//参考 计算CheckSum的java代码

        OkGo.<AlertBean>post("https://api.netease.im/nimserver/user/refreshToken.action")
                .tag(this)
                .headers("AppKey", appKey)
                .headers("Nonce", nonce)
                .headers("CurTime", curTime)
                .headers("CheckSum", checkSum)
                .headers("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .params("accid", et_id.getText().toString())
                .execute(new JsonCallBack<AlertBean>(AlertBean.class, context) {
                    @Override
                    public void onSuccess(Response<AlertBean> response) {
                        if (response != null && response.body() != null) {
                            Log.i(TAG, "更改成功：" + response.body().getInfo().toString());
                            Toast.makeText(context, "更改成功", Toast.LENGTH_SHORT).show();
                            et_store_password.setText(response.body().getInfo().getToken());
                        }
                    }

                    @Override
                    public void onError(Response<AlertBean> response) {
                        super.onError(response);
                        if (response != null && response.body() != null)
                            Log.i(TAG, "更改失败：" + response.body().getCode() + response.body().getInfo().toString());
                        Toast.makeText(context, "更改失败：" + response.body().getCode() + response.body().getInfo().toString(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
