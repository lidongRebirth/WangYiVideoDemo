package com.myfittinglife.app.wangyivideodemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;


/**
 * 网易音视频SDK初始化
 * SDK参考网址：https://dev.yunxin.163.com/docs/interface/%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AFAndroid%E7%AB%AF/IM_Android/index.html
 * OKGo初始化
 */
public class NimApplication extends Application {


    private static final String KEY_USER_ACCOUNT = "account";           //账号
    private static final String KEY_USER_TOKEN = "token";               //服务器返回的密码
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        NIMClient.init(this, loginInfo(), options());               // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）为null不登录
        initOKGO();
    }

    // 如果返回值为 null，则全部使用默认参数。
    // 自定义SDK选项
    private SDKOptions options() {
        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
//        config.notificationEntrance = WelcomeActivity.class; // 点击通知栏跳转到该Activity     日后补充
        config.notificationSmallIconId = R.mipmap.ic_launcher;
        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.netease.nim.demo/raw/msg";
        options.statusBarNotificationConfig = config;
        //*app-key设置
        options.appKey = "c2f97b1b1064e0754410b2da63f06365";    //*-------------------在这里填入你的appkey

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用下面代码示例中的位置作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        String sdkPath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/nim";
        options.sdkStorageRootPath = sdkPath;

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        //*---------------------以下部分日后开发----------------
//         配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
//         该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
//                options.thumbnailSize = ${Screen.width} / 2;  //*注释
//         用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
//        options.userInfoProvider = new UserInfoProvider() {
//            @Override
//            public UserInfo getUserInfo(String account) {
//                return null;
//            }
//
//            @Override
//            public int getDefaultIconResId() {
//                return R.mipmap.ic_launcher;
//            }
//
//            @Override
//            public Bitmap getTeamIcon(String tid) {
//                return null;
//            }
//
//            @Override
//            public Bitmap getAvatarForMessageNotifier(String account) {
//                return null;
//            }
//
//            @Override
//            public String getDisplayNameForMessageNotifier(String account, String sessionId,
//                                                           SessionTypeEnum sessionType) {
//                return null;
//            }
//        };
        return options;
    }
    //---------------以下本项目没有做自动登录，想开发记得在登陆成功后给SharePreference中填入用户信息
    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        // 从本地读取上次登录成功时保存的用户登录信息
        String account = getUserAccount();
        String token = getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }
    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }
    //从SharePreference中获取用户的账号和密码
    private static String getString(String key) {
        return context.getSharedPreferences("logininfo", Context.MODE_PRIVATE).getString(key, null);
    }

    //---------OKGo相关配置---------
    private void initOKGO() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志

        //超时时间设置，默认60秒
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);   //全局的连接超时时间
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));  ////使用sp保持cookie，如果cookie不过期，则一直有效

        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(0);                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
//                .addCommonHeaders();                           //可添加公共头部分

    }


}
