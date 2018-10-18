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
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


//要做监听挂断、接收会话，挂断电话

public class AcceptActivity extends AppCompatActivity {

    @BindView(R.id.tv_hangup_call)
    TextView tv_hangup_call;
    @BindView(R.id.tv_accept_call)
    TextView tv_accept_call;
    @BindView(R.id.tv_switch_call)
    TextView tv_switch_call;
    @BindView(R.id.tv_num)
    TextView tv_num;


    @BindView(R.id.small_size_preview)           //小画布
    LinearLayout smallSizePreviewLayout;
    @BindView(R.id.large_size_preview)           //大画布
    LinearLayout largeSizePreviewLayout;
    @BindView(R.id.notificationLayout)           //等待对方接听时，大的视频预览图          //*日后更改，先大图预览自己，接通后小图预览自己
    View largeSizePreviewCoverLayout;

    @BindView(R.id.ll_info_layout)
    LinearLayout ll_info_layout;



    private static final String TAG = "AcceptActivity_IM";

    private AVChatCameraCapturer mVideoCapturer;        //提供操作相机的接口
    private CallStateEnum callingState;                 //*呼叫状态,用来控制刷新界面
    private boolean destroyRTC = false;

    private AVChatData avChatData;                      //用于接听挂断电话
    private AVChatType avChatType;

    //render
    private AVChatSurfaceViewRenderer smallRender;      //画布，用于显示图像
    private AVChatSurfaceViewRenderer largeRender;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);
        ButterKnife.bind(this);
        avChatData = (AVChatData) getIntent().getSerializableExtra("AVChatData");       //获取对方信息
        tv_num.setText(avChatData.getAccount());        //账号
        avChatType = avChatData.getChatType();
        this.callingState = (avChatType == AVChatType.VIDEO ? CallStateEnum.VIDEO : CallStateEnum.AUDIO);


        this.smallRender = new AVChatSurfaceViewRenderer(getApplicationContext());      //初始化视频画布，用于显示双方的视频
        this.largeRender = new AVChatSurfaceViewRenderer(getApplicationContext());
        Log.i(TAG, "onCreate: ");
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, true);          //会话状态监听        ondestory中要删掉
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, true);    //挂断监听
    }

    @OnClick({R.id.tv_hangup_call,R.id.tv_accept_call,R.id.tv_switch_call})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_accept_call:       //接听电话
                Toast.makeText(getApplicationContext(), "接听电话", Toast.LENGTH_SHORT).show();
                tv_accept_call.setVisibility(View.GONE);
                tv_switch_call.setVisibility(View.VISIBLE);
                receiveInComingCall();      //接听来电
                break;
            case R.id.tv_hangup_call:
                Toast.makeText(getApplicationContext(), "挂断电话", Toast.LENGTH_SHORT).show();
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
                        finish();//*改

                    }
                    @Override
                    public void onException(Throwable exception) {

                    }
                });
                finish();
                break;
            case R.id.tv_switch_call:
                Toast.makeText(getApplicationContext(), "切换摄像头", Toast.LENGTH_SHORT).show();
                mVideoCapturer.switchCamera();
                break;
             default:
                 break;
        }
    }
    //------
    //*-----------接听来电------------------------③
    private void receiveInComingCall() {
        //接听，告知服务器，以便通知其他端

        AVChatManager.getInstance().enableRtc();
        AVChatManager.getInstance().enableVideo();          //*打开视频模块

        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        if (callingState == CallStateEnum.VIDEO_CONNECTING) {
            Log.i(TAG, "receiveInComingCall: 接到来电"+callingState);
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().startVideoPreview();

        }
        // 音视频权限检查
        checkPermission();
        //*---------接听来电-----------------
        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "accept success成功接听来电");

                ll_info_layout.setVisibility(View.GONE);    //*
                AVChatManager.getInstance().startVideoPreview();    //开始视频预览    一定要开启预览，不然看不见自己
                AVChatManager.getInstance().setupLocalVideoRender(smallRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                addIntoSmallSizePreviewLayout(smallRender);     //*自己图像
            }
            @Override
            public void onFailed(int code) {
                if (code == -1) {
                    Toast.makeText(getApplicationContext(), "本地音视频启动失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "建立连接失败", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "accept onFailed->" + code);
                handleAcceptFailed();
            }
            @Override
            public void onException(Throwable exception) {
                Log.i(TAG, "accept exception->" + exception);
                handleAcceptFailed();
            }
        });
    }

    private void handleAcceptFailed() {
        if (destroyRTC) {
            return;
        }
        if (callingState == CallStateEnum.VIDEO_CONNECTING) {
            AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().disableVideo();
        }
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
    }
    //*---------
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

    //----------------------------------监听器-----各种状态监听器,记得一定要注册register，完后取消注册--------------------
    // 通话过程状态监听
    private SimpleAVChatStateObserver avChatStateObserver = new SimpleAVChatStateObserver(){
        @Override
        public void onUserJoined(String account) {      //在用户加入后才能绘制对方的视频
            super.onUserJoined(account);
            Log.i(TAG, "onUserJoined: "+account+"用户加入了视频");

            //绘制对方的视频图像
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

    //权限的申请
    private void checkPermission(){
        List<String> lackPermissions = AVChatManager.getInstance().checkPermission(AcceptActivity.this);
        if(lackPermissions.isEmpty()){
            Toast.makeText(getApplicationContext(),"权限已申请",Toast.LENGTH_SHORT).show();
        }else {
            String []permissions =lackPermissions.toArray(new String[lackPermissions.size()]);
            ActivityCompat.requestPermissions(AcceptActivity.this,permissions,1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //不写会卡死
        AVChatManager.getInstance().disableRtc();
        AVChatManager.getInstance().disableVideo();
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, false);         //会话状态取消监听
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, false);   //挂断监听取消

    }
}
