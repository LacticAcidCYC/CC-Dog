package com.example.eric.lbstest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.RegexUtils;
import com.example.eric.lbstest.utils.ToastUtils;
import com.example.eric.lbstest.utils.VerifyCodeManager;
import com.example.eric.lbstest.views.CleanableEditText;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

/**
 * @desc 注册界面
 * 功能描述：一般会使用手机登录，通过获取手机验证码，跟服务器交互完成注册
 * Created by RSJ on 17/4/18.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "signUpActivity";

    //界面控件
    private CleanableEditText phoneEdit;

    private CleanableEditText passwordEdit;

    private CleanableEditText passwordVerifyEdit;

    private CleanableEditText verifyCodeEdit;

    private CleanableEditText nickNameEdit;

    private Button btn_send_verify_code;

    private Button btn_create_account;

    private VerifyCodeManager codeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ActivityCollector.addActivity(this);

        initViews();
        codeManager = new VerifyCodeManager(this, phoneEdit, btn_send_verify_code);
    }

    /**
     * 通用findViewById,减少重复的类型转换
     * 6666666666666666
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class.", ex);
            throw ex;
        }
    }

    private void initViews() {

        btn_send_verify_code = getView(R.id.btn_send_verify_code);
        btn_send_verify_code.setOnClickListener(this);

        phoneEdit = getView(R.id.et_phone);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);//

        verifyCodeEdit = getView(R.id.et_verifyCode);
        verifyCodeEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);//

        passwordEdit = getView(R.id.et_password);
        passwordEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        passwordVerifyEdit = getView(R.id.et_password_verify);
        passwordVerifyEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        nickNameEdit = getView(R.id.et_nickname);
        nickNameEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nickNameEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        nickNameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    commit();//提交
                    //finish();
                }
                return false;
            }
        });

        btn_create_account = getView(R.id.btn_create_account);
        btn_create_account.setOnClickListener(this);
    }

    private void commit() {
        String phone = phoneEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String passwordVerify = passwordVerifyEdit.getText().toString();
        String code = verifyCodeEdit.getText().toString();
        String nickName = nickNameEdit.getText().toString();

        if(!passwordVerify.equals(password)) {
            ToastUtils.showShort(SignUpActivity.this, "两次输入的密码不同，请重新输入！");
            passwordVerifyEdit.setText("");
            return;
        }

        if(checkInput(phone, password, code, nickName)) {
            //请求服务器注册账号
            //ToastUtils.showShort(SignUpActivity.this, "输入符合格式，现为您注册账号，请稍后...");
            final BmobUser user = new BmobUser();
            user.setMobilePhoneNumber(phone);
            user.setUsername(nickName);
            user.setPassword(password);

            BmobSMS.verifySmsCode(SignUpActivity.this, phone, code, new VerifySMSCodeListener() {
                @Override
                public void done(BmobException ex) {
                    if(ex==null){//短信验证码已验证成功
                        Log.i("smile", "验证通过");
                        //注意：不能用save方法进行注册
                        user.signUp(SignUpActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                ToastUtils.showShort(SignUpActivity.this, "注册成功");
                                //通过BmobUser.getCurrentUser(context)方法获取登录成功后的本地用户信息
                                SignUpActivity.this.finish();
                            }

                            @Override
                            public void onFailure(int code, String msg) {
                                ToastUtils.showShort(SignUpActivity.this, "注册失败：" + msg);
                            }
                        });
                    }else{
                        Log.i("smile", "验证失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                        ToastUtils.showShort(SignUpActivity.this, "验证码错误，请重新输入！");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                verifyCodeEdit.setText("");
                            }
                        });
                    }
                }
            });
        }
    }

    private boolean checkInput(String phone, String password, String code, String nickName) {
        if(TextUtils.isEmpty(phone)) {
            //电话号码若为空
            ToastUtils.showShort(SignUpActivity.this, R.string.tip_phone_can_not_be_empty);
        } else {
            if(!RegexUtils.checkMobile(phone)) {
                ToastUtils.showShort(SignUpActivity.this, R.string.tip_phone_regex_not_right);
            } else if(TextUtils.isEmpty(code)) {
                ToastUtils.showShort(SignUpActivity.this, R.string.tip_please_input_code);
            } else if(password.length() < 6 || password.length() > 32 ||
                    TextUtils.isEmpty(password)) {
                ToastUtils.showShort(SignUpActivity.this, R.string.tip_please_input_6_32_password);
            } else if(TextUtils.isEmpty(nickName)) {
                ToastUtils.showShort(SignUpActivity.this, R.string.tip_please_input_nickname);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;

            case R.id.btn_send_verify_code:
                //请求接口发送验证码
                codeManager.getVerifyCode(VerifyCodeManager.REGISTER);
                BmobSMS.requestSMSCode(SignUpActivity.this, phoneEdit.getText().toString(), "smsMode", new RequestSMSCodeListener() {
                    @Override
                    public void done(Integer smsId, BmobException ex) {
                        if(ex==null){//验证码发送成功
                            Log.i("smile", "短信id：" + smsId);//用于查询本次短信发送详情
                        }
                    }
                });
                break;

            case R.id.btn_create_account:
                //ToastUtils.showShort(SignUpActivity.this, "点击创建账号");
                commit();
                //finish();
                break;

            case R.id.tv_user_rule:
                ToastUtils.showShort(SignUpActivity.this, "查看用户协议");
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
