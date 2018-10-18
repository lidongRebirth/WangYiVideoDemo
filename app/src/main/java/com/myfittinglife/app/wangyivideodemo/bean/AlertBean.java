package com.myfittinglife.app.wangyivideodemo.bean;


/**
 * 更改密码返回的参数
 */
public class AlertBean {

    /**
     * code : 200
     * info : {"token":"xxx","accid":"xx"}
     */

    private int code;
    private InfoBean info;

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

    public static class InfoBean {
        /**
         * token : xxx
         * accid : xx
         */

        private String token;
        private String accid;

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

        @Override
        public String toString() {
            return "InfoBean{" +
                    "token='" + token + '\'' +
                    ", accid='" + accid + '\'' +
                    '}';
        }
    }
}
