package com.myfittinglife.app.wangyivideodemo;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myfittinglife.app.wangyivideodemo.constant.CallStateEnum;
import com.myfittinglife.app.wangyivideodemo.module.SimpleAVChatStateObserver;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallActivity extends AppCompatActivity {

    @BindView(R.id.ll_accept_switch_layout)     //挂断/转化摄像头布局
    LinearLayout ll_accept_switch_layout;
    @BindView(R.id.tv_hangup_call)              //挂断
    TextView tv_hangup_call;
    @BindView(R.id.tv_switch_call)              //转换摄像头
    TextView tv_switch_call;
    @BindView(R.id.tv_cancel_call)              //取消发起电话
    TextView tv_cancel_call;
    @BindView(R.id.tv_id)
    TextView tv_id;

    @BindView(R.id.small_size_preview)           //小画布
    LinearLayout smallSizePreviewLayout;
    @BindView(R.id.large_size_preview)           //大画布
    LinearLayout largeSizePreviewLayout;
    @BindView(R.id.notificationLayout)           //等待对方接听时，大的视频预览图          //*日后更改，先大图预览自己，接通后小图预览自己
    View largeSizePreviewCoverLayout;




    private static final String TAG = "CallActivity_IM";
//    private String largeAccount; // 显示在大图像的用户id
//    private String smallAccount; // 显示在小图像的用户id
    private CallStateEnum callingState;     //*呼叫状态,用来控制刷新界面
    private AVChatCameraCapturer mVideoCapturer;
    protected AVChatData avChatData;                //发起方在发起请求函数了赋值，挂断电话那里要用

    //render
    private AVChatSurfaceViewRenderer smallRender;  //画布，用于显示图像
    private AVChatSurfaceViewRenderer largeRender;

    private String hisaccount;          //要发起会话的账号




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
        hisaccount = getIntent().getStringExtra("hisaccount");                    //获取对方账号
        tv_id.setText(hisaccount);

        this.smallRender = new AVChatSurfaceViewRenderer(getApplicationContext());      //初始化视频画布，用于显示双方的视频
        this.largeRender = new AVChatSurfaceViewRenderer(getApplicationContext());
//        checkPermission();//权限申请
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, true);          //会话状态监听        ondestory中要删掉
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, true);    //挂断监听
        //打开Rtc模块
        AVChatManager.getInstance().enableRtc();
        AVChatManager.getInstance().enableVideo();
        outGoingCalling(hisaccount,AVChatType.VIDEO);                                               //发起会话

    }


    @OnClick({R.id.tv_hangup_call, R.id.tv_switch_call, R.id.tv_cancel_call})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel_call:   //取消发起会话    ，这里也需要挂断
                Toast.makeText(getApplicationContext(), "取消发起会话", Toast.LENGTH_SHORT).show();       //*此处要挂断
                AVChatManager.getInstance().disableVideo();
                AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: 挂断成功");
                        Toast.makeText(getApplicationContext(),"挂断成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.i(TAG, "onFailed: 挂断失败");
                        Toast.makeText(getApplicationContext(),"挂断失败",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
                finish();
                break;
            case R.id.tv_hangup_call:   //挂断电话
                Toast.makeText(getApplicationContext(), "挂断电话", Toast.LENGTH_SHORT).show();
//                AVChatManager.getInstance().disableVideo();
                AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: 挂断成功");
                        Toast.makeText(getApplicationContext(),"挂断成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.i(TAG, "onFailed: 挂断失败");
                        Toast.makeText(getApplicationContext(),"挂断失败",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
                finish();
                break;
            case R.id.tv_switch_call:   //切换摄像头
                Toast.makeText(getApplicationContext(), "切换摄像头", Toast.LENGTH_SHORT).show();
                mVideoCapturer.switchCamera();
                break;
            default:
                break;

        }
    }

    //*-----发起会话成功      ①
    public void outGoingCalling(String account,final AVChatType callTypeEnum){

        checkPermission();//权限申请

        Log.i(TAG, "outGoingCalling: 发起会话");
        //*可选通知参数
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        //附加字段
        notifyOption.extendMessage = "extra_data";
        //默认forceKeepCalling为true，开发者如果不需要离线持续呼叫功能可以将forceKeepCalling设为false        //*离线就不会呼叫
        notifyOption.forceKeepCalling = false;
        //打开Rtc模块
        AVChatManager.getInstance().enableRtc();
        AVChatManager.getInstance().enableVideo();
        this.callingState = (callTypeEnum == AVChatType.VIDEO ? CallStateEnum.VIDEO : CallStateEnum.AUDIO);
//        //设置自己需要的可选参数
//        AVChatManager.getInstance().setParameters(avChatParameters);
        //视频通话
        if (callTypeEnum == AVChatType.VIDEO) {
            //打开视频模块
            AVChatManager.getInstance().enableVideo();
            //创建视频采集模块并且设置到系统中
            if (mVideoCapturer == null) {
                mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
                AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            }
        }
        //发起呼叫-----------------发起后需要预览，则在此处设置本地画布--------------
        AVChatManager.getInstance().call2(account, callTypeEnum, notifyOption, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                avChatData = data;              //有用，挂断电话那里要用
                //发起会话成功才可以挂断取消
                Log.i(TAG, "onSuccess: 发起视频会话成功"+"对方账号："+data.getAccount()+"通话ID："+data.getChatId());
                Toast.makeText(getApplicationContext(),"发起会话成功"+data,Toast.LENGTH_SHORT).show();

                Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
                    @Override
                    public void onEvent(AVChatCalleeAckEvent ackInfo) {
                        if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                            // 对方正在忙
                            Log.i(TAG, "onEvent: 对方正在忙");
                            Toast.makeText(getApplicationContext(),"对方正在忙",Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                            // 对方拒绝接听
                            Log.i(TAG, "onEvent: 对方拒绝接听");
                            Toast.makeText(getApplicationContext(),"对方拒绝接听",Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                            // 对方同意接听
                        }
                    }
                };
                AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, true);//注册网络通话被叫方的响应

                // 设置画布，加入到自己的布局中，用于呈现视频图像
                AVChatManager.getInstance().startVideoPreview();    //开始视频预览    一定要开启预览，不然看不见自己
                AVChatManager.getInstance().setupLocalVideoRender(smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                addIntoLargeSizePreviewLayout(smallRender);         //先将自己展现为大图像

            }

            @Override
            public void onFailed(int code) {
                closeRtc();
                Log.i(TAG, "onFailed: 发起视频会话失败"+code);
                Toast.makeText(getApplicationContext(),"发起会话失败"+code,Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onException(Throwable exception) {
                closeRtc();
                Log.i(TAG, "onException: "+exception);
                finish();
            }
        });
    }
    //-----关闭音视频引擎
    public void closeRtc(){
        AVChatManager.getInstance().disableRtc();
        AVChatManager.getInstance().disableVideo();
    }
    //将自己显示在画布上
    private void addIntoSmallSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        smallSizePreviewLayout.removeAllViews();
        smallSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        smallSizePreviewLayout.setVisibility(View.VISIBLE);
    }

    //将对方显示在画布上
    private void addIntoLargeSizePreviewLayout(SurfaceView surfaceView) {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        largeSizePreviewLayout.removeAllViews();
        largeSizePreviewLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        largeSizePreviewCoverLayout.setVisibility(View.GONE);
    }
    //权限的申请
    private void checkPermission(){
        List<String> lackPermissions = AVChatManager.getInstance().checkPermission(CallActivity.this);
        if(lackPermissions.isEmpty()){
            Toast.makeText(getApplicationContext(),"权限已申请",Toast.LENGTH_SHORT).show();
        }else {
            String []permissions =lackPermissions.toArray(new String[lackPermissions.size()]);
            ActivityCompat.requestPermissions(CallActivity.this,permissions,1);
        }

    }

    //----------------------------------监听器-----各种状态监听器,记得一定要注册register-----------
    // 通话过程状态监听
    private SimpleAVChatStateObserver avChatStateObserver = new SimpleAVChatStateObserver(){
        @Override
        public void onUserJoined(String account) {      //在用户加入后才能绘制对方的视频
            super.onUserJoined(account);
            Log.i(TAG, "onUserJoined: "+account+"用户加入了视频");
            tv_cancel_call.setVisibility(View.GONE);
            ll_accept_switch_layout.setVisibility(View.VISIBLE);
            //绘制对方的视频图像
            addIntoSmallSizePreviewLayout(smallRender);     //将自己转到小界面
            AVChatManager.getInstance().setupRemoteVideoRender(account, largeRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            addIntoLargeSizePreviewLayout(largeRender);
        }
        @Override
        public void onCallEstablished() {
            super.onCallEstablished();
            Log.i(TAG, "onCallEstablished: 会话建立成功");
        }
    };
    // 通话过程中，收到对方挂断电话
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            Log.i(TAG, "onEvent: 对方已挂断");
            Toast.makeText(getApplicationContext(),"对方已挂断",Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    //*-------------------------------------监听器完---------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeRtc();
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, false);         //会话状态取消监听
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, false);   //挂断监听取消

    }
}
