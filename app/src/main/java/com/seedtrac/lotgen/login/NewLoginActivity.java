package com.seedtrac.lotgen.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.parser.login.LoginResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewLoginActivity extends AppCompatActivity {

    EditText etMobile, etPassword;
    CheckBox cbRemember;
    Button btnSubmit;
    TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setTheme();
        init();
    }

    private void setTheme() {
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void init(){
        btnSubmit.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (mobile.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: API call or validation logic
                Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
                checkLogin(mobile,password);
            }
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private void checkLogin(String mobile, String password) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        Log.e("Params:", mobile+"="+password);
        Call<LoginResponse> call =apiInterface.loginAuth(mobile, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    System.out.print("Response : "+loginResponse);
                    if (loginResponse != null) {
                        if (loginResponse.getStatus()){
                            User userData = loginResponse.getUser();
                            SharedPreferences.getInstance(NewLoginActivity.this).storeObject(SharedPreferences.KEY_LOGIN_OBJ, userData);
                            Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(NewLoginActivity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("error_msg");
                            Toast.makeText(NewLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Toast.makeText(NewLoginActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}