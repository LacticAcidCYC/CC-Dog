package com.example.eric.lbstest;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.eric.lbstest.utils.ActivityCollector;

public class RegisterOrSignInActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_signIn;

    private Button btn_signUp;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_or_sign_in);
        ActivityCollector.addActivity(this);

        Toolbar toolbar = getView(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        initViews();
    }

    public final <E extends View> E getView(int id) {
        try {
            return (E) findViewById(id);
        } catch (ClassCastException ex) {
            throw ex;
        }
    }

    private void initViews() {
        btn_signIn = getView(R.id.btn_signIn);
        btn_signIn.setOnClickListener(this);
        btn_signUp = getView(R.id.btn_signUp);
        btn_signUp.setOnClickListener(this);

        fab = getView(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                finish();
                break;
            case R.id.btn_signIn:
                Intent signInIntent = new Intent(this, LoginActivity.class);
                startActivity(signInIntent);
                break;
            case R.id.btn_signUp:
                Intent signUpiIntent = new Intent(this, SignUpActivity.class);
                startActivity(signUpiIntent);
                break;
            case R.id.fab:
                Snackbar.make(fab, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
