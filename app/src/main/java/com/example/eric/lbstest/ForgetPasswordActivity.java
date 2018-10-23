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
import cn.bmob.v3.listener.ResetPasswordByCodeListener;

/**
 * @desc 忘记密码
 * Created by RSJ on 17/4/19.
 */
public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "forgetPasswordActivity";

    //界面控件
    private CleanableEditText phoneEdit;

    private CleanableEditText pwdEdit;

    private CleanableEditText pwdVerifyEdit;

    private CleanableEditText verifycodeEdit;

    private Button btn_send_verify_code;

    private Button btn_reset_pwd;

    private VerifyCodeManager codeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
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
            return (E)findViewById(id);
        } catch (ClassCastException ex) {
            Log.e(TAG, "Could not cast View to concrete class", ex);
            throw ex;
        }
    }

    private void initViews() {
        btn_send_verify_code = getView(R.id.btn_send_verify_code);
        btn_send_verify_code.setOnClickListener(this);
        btn_reset_pwd = getView(R.id.btn_reset_pwd);
        btn_reset_pwd.setOnClickListener(this);

        phoneEdit = getView(R.id.et_phone);
        phoneEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        
        verifycodeEdit = getView(R.id.et_verifyCode);
        verifycodeEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        pwdEdit = getView(R.id.et_password);
        pwdEdit.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        pwdVerifyEdit = getView(R.id.et_password_verify);
        pwdVerifyEdit.setImeOptions(EditorInfo.IME_ACTION_GO);
        pwdVerifyEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        pwdVerifyEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    commit();
                    //finish();
                }
                return false;
            }
        });
    }

    private void commit() {
        String phone = phoneEdit.getText().toString();
        String verifyCode = verifycodeEdit.getText().toString();
        String pwd = pwdEdit.getText().toString();
        String pwdVerify = pwdVerifyEdit.getText().toString();
        Log.i("pwd", pwd);
        Log.i("pwdVerify", pwdVerify);
        if(!pwdVerify.equals(pwd)) {
            ToastUtils.showShort(ForgetPasswordActivity.this, "两次输入的密码不同，请重新输入！");
            pwdVerifyEdit.setText("");
            return;
        }
        if(checkInput(phone, verifyCode, pwd)) {
            //请求服务器重置密码
            //ToastUtils.showShort(ForgetPasswordActivity.this, "输入符合格式，现为您重置密码，请稍后...");
            BmobUser.resetPasswordBySMSCode(ForgetPasswordActivity.this, verifyCode, pwd, new ResetPasswordByCodeListener() {
                @Override
                public void done(BmobException ex) {
                    if(ex==null){
                        Log.i("smile", "密码重置成功");
                        ToastUtils.showShort(ForgetPasswordActivity.this, "密码重置成功！");
                        finish();
                    }else{
                        Log.i("smile", "重置失败：code ="+ex.getErrorCode()+",msg = "+ex.getLocalizedMessage());
                        ToastUtils.showShort(ForgetPasswordActivity.this, "验证码输入错误，密码重置失败！");
                    }
                }
            });
        }
    }

    private boolean checkInput(String phone, String verifyCode, String pwd) {
        if(TextUtils.isEmpty(phone)) {//电话号码为空
            ToastUtils.showShort(ForgetPasswordActivity.this, R.string.tip_phone_can_not_be_empty);
        } else {
            if(!RegexUtils.checkMobile(phone)) {//电话号码格式不符
                ToastUtils.showShort(ForgetPasswordActivity.this, R.string.tip_phone_regex_not_right);
            } else if(TextUtils.isEmpty(verifyCode)) {//验证码不正确
                ToastUtils.showShort(ForgetPasswordActivity.this, R.string.tip_please_input_code);
            } else if(pwd.length() < 6 || pwd.length() > 32) {//密码格式不符
                ToastUtils.showShort(ForgetPasswordActivity.this, R.string.tip_please_input_6_32_password);
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
                codeManager.getVerifyCode(VerifyCodeManager.RESET_PWD);
                BmobSMS.requestSMSCode(ForgetPasswordActivity.this, phoneEdit.getText().toString(), "smsMode", new RequestSMSCodeListener() {
                    @Override
                    public void done(Integer smsId, BmobException ex) {
                        if(ex==null){//验证码发送成功
                            Log.i("smile", "短信id：" + smsId);//用于查询本次短信发送详情
                        }
                    }
                });
                break;
            case R.id.btn_reset_pwd:
                commit();
                //finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}






















