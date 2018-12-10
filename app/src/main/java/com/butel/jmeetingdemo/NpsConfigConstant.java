package com.butel.jmeetingdemo;

/**
 * Created by YangLin on 2018/10/19
 * NPS、appKey和统一认证服务器地址配置
 */

public class NpsConfigConstant {

//    //上海内网appKey和密码
//    public static final String AUTH_APPKEY = "0ab4a7a30d3840d4aefcf36e68344370";
//    public static final String AUTH_PASSWORD = "867223";

    //党建云项目使用appKey和密码
    public static final String AUTH_APPKEY = "477eb384bf334c4192bcbc9b103e5096";
    public static final String AUTH_PASSWORD = "123456";

    //大网nps地址
    public static final String NPS_RELEASE = "http://xmeeting.butel.com/nps_x1/";
    public static final String SLAVE_NPS_RELEASE = "http://xmeeting.jihuiyi.cn/nps_x1/";
    //nps地址
    public static final String NPS_DEV = "http://10.11.171.19:10131/nps_x1/";
    public static final String SLAVE_NPS_DEV = "http://10.11.171.19:10131/nps_x1/";

    //大网NPS环境统一认证服务器地址
    public static final String PERSONAL_CENTER_URL_RELEASE = "http://103.25.23.99/BaikuUserCenterV2";
    //内网NPS环境统一认证服务器地址
    public static final String PERSONAL_CENTER_URL_DEV = "http://10.11.171.19:10004/BaikuUserCenterV2";
}
