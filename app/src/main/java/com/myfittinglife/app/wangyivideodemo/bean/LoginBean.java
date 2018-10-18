package com.myfittinglife.app.wangyivideodemo.bean;

/**
 * 登录和注册返回的参数
 * 请自行查看文档注册登录返回的json数据类型
 *https://dev.yunxin.163.com/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/%E6%9C%8D%E5%8A%A1%E7%AB%AFAPI%E6%96%87%E6%A1%A3/%E7%BD%91%E6%98%93%E4%BA%91%E9%80%9A%E4%BF%A1ID
 */
public class LoginBean {
    /**
     * code : 200
     * info : {"token":"xx","accid":"xx","name":"xx"}
     */

    private int code;
    private InfoBean info;

    public static class InfoBean {
        /**
         * token : xx
         * accid : xx
         * name : xx
         */

        private String token;
        private String accid;
        private String name;


        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAccid() {
            return accid;
        }

        public void setAccid(String accid) {
            this.accid = accid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
    }



}
