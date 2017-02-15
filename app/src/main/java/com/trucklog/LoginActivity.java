package com.trucklog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.trucklog.Interfaces.AsyncTaskResponse;
import com.trucklog.Models.UserModel;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.ShowProgressDialog;
import com.trucklog.Utils.TokenManage;
import com.trucklog.Utils.VerificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.trucklog.Utils.Constants.JSON;
import static com.trucklog.Utils.Constants.REQUEST_PERMISSION;

public class LoginActivity extends AppCompatActivity {
    EditText txt_email, txt_password;
    Button bt_login;
    UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txt_email = (EditText)findViewById(R.id.txt_login_email);
        txt_password = (EditText)findViewById(R.id.txt_login_password);
        bt_login = (Button)findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLoginRequest();
            }
        });

        requestPermission();
    }

    private void requestPermission(){
        String[] perms = {"android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkAllGrantedOrNot(perms))
                requestPermissions(perms, REQUEST_PERMISSION);
        }
    }

    private boolean checkAllGrantedOrNot(String[] perms){
        boolean ret_val = true;
        for(String permission : perms) {
            if (! hasPermission(this, permission)) {ret_val = false; }
        }
        return ret_val;
    }

    public boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION){
            for(int grantResult:grantResults){
                if(grantResult == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "Without Permission, App doesn't work correctly", Toast.LENGTH_SHORT).show();
                    requestPermission();
                    return;
                }
            }
        }
    }

    public void sendLoginRequest(){
        if(VerificationUtils.isEmptyText(txt_email) || VerificationUtils.isEmptyText(txt_password))
            return;
        if(VerificationUtils.isValidEmail(txt_email.getText().toString()) == false){
            txt_email.setError("invalid email address");
            return;
        }
        try {
            ShowProgressDialog.showProgressDialog(LoginActivity.this, "Log in");
            LoginService service = new LoginService(createJsonObject(txt_email.getText().toString(), txt_password.getText().toString()), new AsyncTaskResponse() {
                @Override
                public void response(Object o) {

                    ShowProgressDialog.hideProgressDialog();
                    try {
                        JSONObject result_object = new JSONObject((String) o);
                        if(result_object.getInt("success") == 1){
                            String token = result_object.getString("token");
                            TokenManage.setToken(LoginActivity.this.getApplicationContext(), token);
                            userModel = new UserModel(result_object.getJSONObject("user"));
                            gotoNextScreen();
                        }
                        else{
                            new AlertDialog.Builder(LoginActivity.this).setMessage(result_object.getString("message")).setTitle("Error")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
            service.execute();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void gotoNextScreen(){
        Intent i  = new Intent(LoginActivity.this, HomeActivity.class);
        i.putExtra("user",userModel);
        startActivity(i);
        finish();
    }

    private JSONObject createJsonObject(String email, String password) throws JSONException{
        JSONObject request_content = new JSONObject();
        JSONObject request_data = new JSONObject();
        request_content.put("email", email);
        request_content.put("password", password);
        request_data.put("pitch", "login");
        request_data.put("data", request_content);
        return request_data;
    }

    public class LoginService extends AsyncTask{
        AsyncTaskResponse response;
        JSONObject request_object;
        OkHttpClient okhttp;
        public LoginService(JSONObject request_object, AsyncTaskResponse response){
            this.response = response;
            this.request_object = request_object;
            okhttp = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            RequestBody body = RequestBody.create(JSON, request_object.toString());
            Request request = new Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Accept","application/json")
                    .url(Constants.SERVER_URL)
                    .post(body)
                    .build();
            try {
                Response response = okhttp.newCall(request).execute();
                return response.body().string();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            response.response(o);
        }
    }
}
