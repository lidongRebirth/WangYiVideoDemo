package com.myfittinglife.app.wangyivideodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.myfittinglife.app.wangyivideodemo.receiver.PhoneCallStateObserver;
import com.myfittinglife.app.wangyivideodemo.utils.AVChatProfile;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 登录成功后的界面，在此界面向指定账户号发起视频请求，并在此处监听视频请求
 */
public class MainActivity extends AppCompatActivity {


    @BindView(R.id.et_his_account)
    EditText his_account;
    @BindView(R.id.btn_call)            //发起电话
    Button btn_call;
    private static final String TAG = "MainActivity_IM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: ");
        ButterKnife.bind(this);
        registerAVChatIncomingCallObserver(true);                   //监听来电
    }

    @OnClick({R.id.btn_call})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_call:                                                             //发起会话后进入CallActivity活动，在此界面内进行发起操作
                Log.i(TAG, "onClick: 发起会话");
                Intent intent = new Intent(this,CallActivity.class);
                intent.putExtra("hisaccount", his_account.getText().toString());
                //此处尽量传入更多的值，自己账号，对方账号，
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData data) {
                Log.i(TAG, "Extra Message->" + "对方账号："+data.getAccount()+"通话ID："+data.getChatId());
//                largeAccount = data.getAccount();   //对方账号赋值
                //*如果选择继续原来的通话，挂断当前来电，最好能够先发送一个正忙的指令给对方
                if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
                        || AVChatProfile.getInstance().isAVChatting()
                        || AVChatManager.getInstance().getCurrentChatId() != 0) {
                    Log.i(TAG, "reject incoming call data =" + data.toString() + " as local phone is not idle");
                    AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);
                    //*正在会话时挂断后面来的电话
                    AVChatManager.getInstance().hangUp2(data.getChatId(), new AVChatCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: 挂断来电");
                        }

                        @Override
                        public void onFailed(int code) {
                            Log.i(TAG, "onFailed: 挂断来电失败"+code);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                    return;
                }else {
                    Intent intent = new Intent(getApplicationContext(), AcceptActivity.class);
                    intent.putExtra("AVChatData", data);
                    startActivity(intent);
                    Log.i(TAG, "onEvent: 收到的账号："+data.getAccount());
                }

            }
        }, register);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: ");
        super.onBackPressed();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        registerAVChatIncomingCallObserver(false);              //取消监听来电
//        NIMClient.getService(AuthService.class).logout();       //登出

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }
}
