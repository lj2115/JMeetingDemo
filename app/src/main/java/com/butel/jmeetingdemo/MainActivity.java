package com.butel.jmeetingdemo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.crash.Crash;
import cn.redcdn.jmeetingsdk.JMeetingAgent;
import cn.redcdn.jmeetingsdk.MeetingAgentContext;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.jmeetingsdk.MeetingItem;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private static final String LOGTAG = "JMeetingDemo";
    private EditText et_account;
    private EditText et_name;
    private EditText et_meeting_number;
    private Button btn_init;
    private Button btn_release;
    private Button btn_create_meeting;
    private Button btn_join_meeting;

    private LinearLayout ll_im;
    private Button btn_init_im;
    private Button btn_release_im;
    private EditText et_account_receiver;
    private EditText et_name_receiver;
    private Button btn_send_meeting_invitation;
    private Drawable background;
    private JMeetingAgent jMeetingAgent;
//    private BimClient bimClient;
    private String account_sender;//发送者视讯号
    private String name_sender;//发送者视昵称
    private String account_receiver;//接收者者视讯号
    private String name_receiver;//接收者视昵称
    private List<String> list = new ArrayList<>();
    private boolean initOKFlag = false;
    private boolean imInitOkFlag = false;
    private boolean sendIMFlag = false;
    private String meetingNumber;
    private String accessToken;
    private long lastClickBackTime = 0;
    public static String LogRootDir; //日志、配置文件根目录

    private String APPKEY = NpsConfigConstant.AUTH_APPKEY;//appKy(统一使用上海内网的)
    private String PASSWORD = NpsConfigConstant.AUTH_PASSWORD;//密码
    private String NPS;//主NPS地址
    private String SLAVE_NPS;//从NPS地址
    private String PERSONAL_CENTER_URL;//用户统一认证服务器地址

    //大网或者内网环境设置开关 true:表示大网(外网)环境  false:表示内网环境
    private boolean RELEASE_CONFIG_FLAG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置日志的路径并开始写日志
        LogRootDir = this.getPackageName() + "/main";
        LogcatFileManager.getInstance().setLogDir(LogRootDir);
        LogcatFileManager.getInstance().start(this.getPackageName());
        //设置产生的Crash文件路径
        Crash crash = new Crash();
        crash.setDir(LogRootDir);
        crash.init(this, this.getPackageName());
        String phoneVersion = android.os.Build.VERSION.RELEASE;
        CustomLog.i(LOGTAG, "MainActivity onCreate begin " + phoneVersion);
        //设置NPS、appKy和统一认证服务器地址
        setConfigs();
        //构建JMeetingAgent
        newJMeetingAgent();

        //初始化界面
        initView();
    }

    private void setConfigs() {
        CustomLog.i(LOGTAG, "setConfigs()");
        if (RELEASE_CONFIG_FLAG){
            CustomLog.i(LOGTAG, "配置大网环境NPS和统一认证服务器");
            NPS = NpsConfigConstant.NPS_RELEASE;
            SLAVE_NPS = NpsConfigConstant.SLAVE_NPS_RELEASE;
            PERSONAL_CENTER_URL = NpsConfigConstant.PERSONAL_CENTER_URL_RELEASE;
        }else {
            CustomLog.i(LOGTAG, "配置内网环境NPS和统一认证服务器");
            NPS = NpsConfigConstant.NPS_DEV;
            SLAVE_NPS = NpsConfigConstant.SLAVE_NPS_DEV;
            PERSONAL_CENTER_URL = NpsConfigConstant.PERSONAL_CENTER_URL_DEV;
        }
    }

    private void newJMeetingAgent() {
        if (null == jMeetingAgent) {
            //构建JMeetingAgent
            jMeetingAgent = new JMeetingAgent(getApplicationContext().getPackageName()) {
                //初始化回调
                @Override
                protected void onInit(String valueDes, int valueCode) {
                    CustomLog.d(LOGTAG, "onInit() valueDes = " + valueDes + ", valueCode = " + valueCode);
                    if (valueCode == 0){
                        initOKFlag = true;
                        Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                        //获取Token
                        accessToken = jMeetingAgent.getAccessToken();
                    }else {
                        initOKFlag = false;
                        Toast.makeText(MainActivity.this, "初始化失败(" + valueCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                //创建会议回调
                @Override
                protected void onCreatMeeting(int i, MeetingInfo meetingInfo, MeetingAgentContext meetingAgentContext) {
                    //获取创建会议时产生的会议号
                    meetingNumber = meetingInfo.meetingId;
                    CustomLog.d(LOGTAG, "onCreatMeeting(), meetingId =" + meetingNumber);
//                  Toast.makeText(MainActivity.this, "会议号：" + meetingNumber, Toast.LENGTH_LONG).show();
                    //用该会议号直接加入会议
                    join_Meeting(meetingNumber);
//                    //如果是发送IM会议要求消息，那么自己在加入会议后，还需要发出会议邀请
//                    if (sendIMFlag){
//                        Log.i(LOGTAG, "create meeting succeed, then send meeting invitation");
//                        sendMeetingInvitation(meetingNumber);
//                        sendIMFlag = false;
//                    }
                }

                //加入会议回调
                @Override
                protected void onJoinMeeting(String meetingId, int valueCode) {
                    CustomLog.d(LOGTAG, "onJoinMeeting(), meetingId = " + meetingId + ", valueCode = " + valueCode);
                    if (valueCode == 0){
                        Toast.makeText(MainActivity.this, "进入会议", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "加入会议失败(" + valueCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                protected void onIncomingCall(String s, int i) {

                }

                @Override
                protected void onNowMeetings(List<MeetingItem> list, int i, MeetingAgentContext meetingAgentContext) {

                }

                @Override
                protected void onQuitMeeting(String s, int i) {

                }

                @Override
                protected void onEvent(int eventCode, Object eventContent) {
                    CustomLog.d(LOGTAG, "onEvent(), eventCode =" + eventCode);
//                    if (eventCode == 1004){
//                        //退出会议了
//                        sendIMFlag = false;
//                    }
                }
            };
        }
    }


    private void initView() {
        CustomLog.i(LOGTAG, "initView()");

        //获取视讯号和名称控件
        et_account = (EditText) findViewById(R.id.et_account);
        et_name = (EditText) findViewById(R.id.et_name);

        //获取初始化和release按钮控件
        btn_init = (Button) findViewById(R.id.btn_init);
        btn_release = (Button) findViewById(R.id.btn_release);
        btn_init.setOnClickListener(this);
        btn_release.setOnClickListener(this);
        btn_release.setEnabled(false);

        //获取创建会议按钮控件
        btn_create_meeting = (Button) findViewById(R.id.btn_create_meeting);
        btn_create_meeting.setOnClickListener(this);

        //获取输入会议号控件
        et_meeting_number = (EditText) findViewById(R.id.et_meeting_number);
        et_meeting_number.addTextChangedListener(this);

        //获取加入会议按钮控件,刚开时设置不可点击
        btn_join_meeting = (Button) findViewById(R.id.btn_join_meeting);
        btn_join_meeting.setEnabled(false);
        background = btn_join_meeting.getBackground();

        //获取初始化IM和releaseIM按钮控件(注：暂时屏蔽掉IM模块)
        ll_im = (LinearLayout) findViewById(R.id.ll_im);
        ll_im.setVisibility(View.GONE);
        btn_init_im = (Button) findViewById(R.id.btn_init_IM);
        btn_release_im = (Button) findViewById(R.id.btn_release_IM);
        btn_init_im.setOnClickListener(this);
        btn_release_im.setOnClickListener(this);
        btn_init_im.setEnabled(false);
        btn_release_im.setEnabled(false);

        //获取接收者视讯号和名称控件
        et_account_receiver = (EditText) findViewById(R.id.et_account_receiver);
        et_name_receiver = (EditText) findViewById(R.id.et_name_receiver);

        //获取发送IM消息按钮
        btn_send_meeting_invitation = (Button) findViewById(R.id.btn_send_meeting_invitation);
        btn_send_meeting_invitation.setOnClickListener(this);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //初始化JMeetingAgent
            case R.id.btn_init:
                //获取输入的视讯号和名称
                account_sender = et_account.getText().toString();
                name_sender = et_name.getText().toString();
                if (TextUtils.isEmpty(account_sender) || TextUtils.isEmpty(name_sender)){
                    Toast.makeText(MainActivity.this, "请输入视讯号或昵称", Toast.LENGTH_SHORT).show();
                }else {
                    //设置初始化按钮不可点击，设置Release按钮可点击
                    btn_init.setEnabled(false);
                    btn_release.setEnabled(true);

                    //初始化jMeetingAgent
                    init(MainActivity.this, account_sender, name_sender, APPKEY, PASSWORD);
                }

                break;

            //Release JMeetingAgent
            case R.id.btn_release:
                //设置Release按钮不可点击，设置初始化按钮可点击
                btn_init.setEnabled(true);
                btn_release.setEnabled(false);
                btn_init_im.setEnabled(false);

                //release创建的JMeetingAgent对象
                Release();

                break;

            //创建会议
            case R.id.btn_create_meeting:

                MeetingAgentContext agentContext = new MeetingAgentContext();
                agentContext.setContextId("MainActivity");

                //初始化成功后才可以创建会议
                if (!initOKFlag){
                    Toast.makeText(MainActivity.this, "请先进行初始化操作", Toast.LENGTH_SHORT).show();
                }else {
                    //创建会议
                    createMeeting(list, name_sender + "会诊室", 1, "", agentContext);
                }

                break;

            //加入会议
            case R.id.btn_join_meeting:
                //获取输入的会议号
                String meetingNumberInput = et_meeting_number.getText().toString();

                //初始化成功后才能加入会议
                if (!initOKFlag){
                    Toast.makeText(MainActivity.this, "请先进行初始化操作", Toast.LENGTH_SHORT).show();
                }else {
                    join_Meeting(meetingNumberInput);
                }

                break;

//            //初始化IM
//            case R.id.btn_init_IM:
//
//                btn_release_im.setEnabled(true);
//                btn_init_im.setEnabled(false);
//
//                //初始化IM
//                initIM(MainActivity.this, IM_NPS, IM_SLAVE_NPS, account_sender, name_sender, AUTH_APPKEY, accessToken);
//
//                break;
//
//            //release IM
//            case R.id.btn_release_IM:
//
//                btn_release_im.setEnabled(false);
//                btn_init_im.setEnabled(initOKFlag);
//                //release IM
//                releaseIM();
//                break;
//
//            //发送会议邀请
//            case R.id.btn_send_meeting_invitation:
//                //获取输入的接收者的视讯号和名称
//                account_receiver = et_account_receiver.getText().toString();
//                name_receiver = et_name_receiver.getText().toString();
//                if (TextUtils.isEmpty(account_receiver) || TextUtils.isEmpty(name_receiver)){
//                    Toast.makeText(MainActivity.this, "请先输入接收者的视讯号或昵称", Toast.LENGTH_SHORT).show();
//                }else {
//                    if (!imInitOkFlag){
//                        Toast.makeText(MainActivity.this, "请先初始化IM", Toast.LENGTH_SHORT).show();
//                    }else {
//                        //初始化IM后，点击发送会议邀请，首先需要创建一个会议，自己入会后再发出会议邀请
//
//                        Log.i(LOGTAG, "send meeting invitation, create a meeting first!");
//
//                        sendIMFlag = true;
//
//                        MeetingAgentContext context = new MeetingAgentContext();
//                        context.setContextId("MainActivity");
//                        //创建会议
//                        createMeeting(list, name_sender + "会诊室", 1, "", context);
//                    }
//                }
//
//                break;

            default:
                break;
        }

    }


    /**
     *  1、JMeetingAgent的初始化(和Release配合使用)
     *   初始化接口，异步接口，初始化结果由onInit异步回调接口返回。
     *
     * @param context  上下文
     * @param account  视讯号
     * @param name     昵称
     *
     * @return 同步返回结果，0代表成功   非0代表失败
     */
    private int init(Context context, String account, String name) {
        int result = jMeetingAgent.init(context, account, name);
        CustomLog.i(LOGTAG, "init JMeetingAgent, account =" + account + ", name =" + name + ", result =" + result);

        return result;
    }

    /**
     * 1、JMeetingAgent的初始化(和Release配合使用)
     *   初始化接口，异步接口，初始化结果由onInit异步回调接口返回。
     *
     * @param context      上下文
     * @param account      视讯号
     * @param name         昵称
     * @param auth_appKey  appKey   默认：c8791b3ca34d4e688ccb870fe279c135
     * @param auth_pwd     密码     默认：123456
     *
     * @return 同步返回结果，0代表成功   非0代表失败
     */
    private int init(Context context, String account, String name, String auth_appKey, String auth_pwd) {
        //初始化之前先设置NPS地址
        jMeetingAgent.setNps(NPS, SLAVE_NPS);
        //设置统一认证服务器地址
        jMeetingAgent.setUserCenterUrl(PERSONAL_CENTER_URL);

        int result = jMeetingAgent.init(context, account, name, auth_appKey, auth_pwd);
        CustomLog.i(LOGTAG, "init JMeetingAgent, account =" + account + ", name =" + name + ", appKey ="
                + auth_appKey + ", pwd =" + auth_pwd + ", result =" + result);

        return result;
    }

    /**
     *  2、Release构建的JMeetingAgent对象
     */
    private void Release() {
        CustomLog.i(LOGTAG, "Release()");
        if (jMeetingAgent != null){
            jMeetingAgent.release();
            initOKFlag = false;
        }
    }

    /**
     *  3、创建即时会议，
     *   异步接口，结果通过onCreatMeeting异步回调接口返回
     *
     * @param list 被邀请参会人员列表，当集合为空的时候默认就是自己
     * @param topic 会议人员昵称和主题
     * @param meetingType 会议类型
     * @param beginDateTime 会议开始时间(可以为空)
     * @param context MeetingAgentContext上下文
     *
     * @return 同步返回接口， 0代表成功， 非0代表失败
     */
    private int createMeeting(List<String> list, String topic, int meetingType, String beginDateTime, MeetingAgentContext context) {
        int ret = jMeetingAgent.createMeeting(list, topic, meetingType, beginDateTime, context);
        CustomLog.i(LOGTAG, "createMeeting, list size =" +list.size() + ", topic =" + topic + ", meetingType =" + meetingType +
                ", beginDateTime =" + beginDateTime + ", agentContext Id =" + context.getContextId() + ", ret =" + ret);

        return ret;
    }


    /**
     / 4、加入会议  异步接口，结果通过onJoinMeeting异步回调接口返回
     *
     * @param meetingNumber 会议号
     *
     * @return 通过返回结果 -101：未初始化    0：成功  -1：失败
     */
    private int join_Meeting(String meetingNumber) {
        jMeetingAgent.setMeetingAdapter(false);

        int ret = jMeetingAgent.joinMeeting(meetingNumber);
        CustomLog.i(LOGTAG, "join_Meeting, meetingNumber =" + meetingNumber + ", ret =" + ret);

        return ret;
    }


//    /**
//     *  5、初始化IM
//     * @param context        上下文
//     * @param npsUrlMaster   IM主NPS地址
//     * @param npsUrlSlave    IM从NPS地址
//     * @param loginNube      登录者视讯号
//     * @param loginName      登录者昵称
//     * @param appkey         申请的appKey
//     * @param token          登录返回的token
//     */
//    private void initIM(Context context, String npsUrlMaster, String npsUrlSlave, String loginNube, String loginName, String appkey, String token) {
//        CustomLog.i(LOGTAG, "initIM(), npsUrlMaster =" + npsUrlMaster + ", npsUrlSlave =" + npsUrlSlave +
//                ", loginNube =" + loginNube + ", loginName =" + loginName + ", appKey =" + appkey + ", token =" + token);
//
//        bimClient.init(context, npsUrlMaster, npsUrlSlave, loginNube, loginName, appkey, token, new InitCallback() {
//            @Override
//            public void onSuccess() {
//                CustomLog.d(LOGTAG, "initIM(), onSuccess");
//                Toast.makeText(MainActivity.this, "初始化IM成功", Toast.LENGTH_SHORT).show();
//                imInitOkFlag = true;
//                //注册消息接收回调监听
//                registerMessageInvitation();
//            }
//
//            @Override
//            public void onFailed(int errorCode, String errorMsg) {
//                CustomLog.e(LOGTAG, "initIM(), onFailed, errorCode =" + errorCode + ", errorMsg =" + errorMsg);
//                Toast.makeText(MainActivity.this, "初始化IM失败(errorCode =" + errorCode + ")", Toast.LENGTH_SHORT).show();
//                imInitOkFlag = false;
//            }
//        });
//
//    }
//
//
//    /**
//     *  6、release IM
//     */
//    private void releaseIM() {
//        CustomLog.i(LOGTAG, "releaseIM()");
//
//        if (bimClient != null){
//            bimClient.logout();
//            imInitOkFlag = false;
//        }
//
//    }
//
//
//    /**
//     * 7、创建会议成功后，再发出会议邀请
//     *
//     * @param meetingNumber 会议号
//     */
//    private void sendMeetingInvitation(String meetingNumber) {
//        CustomLog.i(LOGTAG, "sendMeetingInvitation(), receiverNube =" + account_receiver + ", meetingId =" + meetingNumber);
//
//        bimClient.sendMeetingInvitation(account_receiver, meetingNumber, new InviteOperateCallback() {
//            @Override
//            public void onSuccess() {
//                CustomLog.d(LOGTAG, "sendMeetingInvitation(), onSuccess");
//                Toast.makeText(MainActivity.this, "发送会议邀请成功", Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onFailed(int errorCode, String errorMsg) {
//                CustomLog.d(LOGTAG, "sendMeetingInvitation(), onFailed, errorCode =" + errorCode + ", errorMsg =" + errorMsg);
//                Toast.makeText(MainActivity.this, "发送会议邀请失败(errorCode =" + errorCode + ")", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }
//
//
//    /**
//     * 8、设置IM消息接收回调的监听
//     */
//    private void registerMessageInvitation() {
//        CustomLog.i(LOGTAG, "registerMessageInvitation()");
//        bimClient.observeMeetingInvitation(new MeetingInvitationMsgCallback() {
//            //有新的会议邀请回调
//            @Override
//            public void onNewInvitationArrive(String inviterNube, String inviterName, String meetingId) {
//                CustomLog.d(LOGTAG, "onNewInvitationArrive(), inviterNube =" + inviterNube + ", inviterName =" +
//                        inviterName + ", meetingId =" + meetingId);
//
//                //这个时候自己可以弹出会议邀请来电对话框
//                showInComingCall(inviterNube, inviterName, meetingId);
//            }
//        });
//    }
//
//
//    /**
//     * 9、收到会议邀请后，弹出会议邀请来电对话框
//     *
//     * @param inviterNube  邀请者视讯号
//     * @param inviterName  邀请者昵称
//     * @param meetingId    会议号
//     */
//    private void showInComingCall(String inviterNube, String inviterName, String meetingId) {
//        int ret = jMeetingAgent.incomingCall(inviterNube, inviterName, meetingId, "");
//        CustomLog.i(LOGTAG, "showInComingCall(), ret =" + ret);
//    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        //获取输入框输入的会议号，八位数字才可以设置加入会议按钮能点击
        String number = et_meeting_number.getText().toString();
        if (number.length() == 8){
            btn_join_meeting.setBackgroundResource(R.drawable.btn_bg_pressed_shape);
            btn_join_meeting.setEnabled(true);
            btn_join_meeting.setOnClickListener(this);
        }else {
            btn_join_meeting.setBackgroundDrawable(background);
            btn_join_meeting.setEnabled(false);
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            long nowClickBackTime = System.currentTimeMillis();
            long delayTime = nowClickBackTime - lastClickBackTime;

            if (lastClickBackTime > 0 && delayTime < 2000){
                finish();
            }else {
                Toast.makeText(MainActivity.this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
                lastClickBackTime = nowClickBackTime;
            }
        }
        return false;
    }

}


