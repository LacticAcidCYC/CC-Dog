package com.example.eric.lbstest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.RegexUtils;
import com.example.eric.lbstest.views.CleanableEditText;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

/**
 * @desc 登录界面
 * Created by RSJ on 17/4/18.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "loginActivity";

    //登录界面控件
    private CleanableEditText accountEditText;

    private CleanableEditText pwdEditText;

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);

        initViews();
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
        accountEditText = getView(R.id.et_phone_number);
        accountEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        accountEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        pwdEditText = getView(R.id.et_password);
        pwdEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        pwdEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
        pwdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        pwdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO) {
                    clickLogin();
                }
                return false;
            }
        });
    }

    private void clickLogin() {
        String account = accountEditText.getText().toString();
        String pwd= pwdEditText.getText().toString();
        if(checkInput(account, pwd)) {
            //请求服务器登录
            Toast.makeText(LoginActivity.this, "Please wait for logging...", Toast.LENGTH_LONG).show();
            BmobUser.loginByAccount(LoginActivity.this, account, pwd, new LogInListener<BmobUser>() {

                @Override
                public void done(BmobUser bmobUser, BmobException e) {
                    if(bmobUser!=null){
                        Log.i("smile","Success");
                        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                        startActivity(intent);
                    }
                }
            });

        }
    }

    private boolean checkInput(String account, String pwd) {
        //账号或密码为空
        if(account == null || account.trim().equals("")) {
            Toast.makeText(LoginActivity.this, R.string.tip_account_empty, Toast.LENGTH_LONG).show();
        } else {
            // 账号不匹配手机号格式（11位数字且以1开头）
            if(!RegexUtils.checkMobile(account)) {
                Toast.makeText(LoginActivity.this, R.string.tip_account_regex_not_right, Toast.LENGTH_LONG).show();
            } else if(pwd == null || pwd.trim().equals("")) {
                Toast.makeText(LoginActivity.this, R.string.tip_password_can_not_be_empty, Toast.LENGTH_LONG).show();
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_cancel:
                Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(MainIntent);
                break;
            case R.id.btn_login:
                clickLogin();
                break;
            case R.id.iv_qq:
                Toast.makeText(LoginActivity.this, "Using QQ", Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_sina:
                Toast.makeText(LoginActivity.this, "Using Weibo", Toast.LENGTH_LONG).show();
                break;
            case R.id.iv_wechat:
                Toast.makeText(LoginActivity.this, "Using Weixin", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_create_account:
                //Toast.makeText(LoginActivity.this, "创建新用户", Toast.LENGTH_LONG).show();
                enterRegister();
                break;
            case R.id.tv_forget_password:
                //Toast.makeText(LoginActivity.this, "忘记密码", Toast.LENGTH_LONG).show();
                enterForgetPwd();
                break;
        }
    }

    private void enterRegister() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void enterForgetPwd() {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}










