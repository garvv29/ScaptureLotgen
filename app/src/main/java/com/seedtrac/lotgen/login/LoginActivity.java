package com.seedtrac.lotgen.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
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
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etMobile, etPassword;
    private Button btnStart;
    private TextView tvForgotPassword;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        // Change background color
        window.setStatusBarColor(getResources().getColor(R.color.light_blue));

        // Change icon color (white or dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        0, // 0 = white icons
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            // Below Android 11
            getWindow().getDecorView().setSystemUiVisibility(0); // 0 = white icons
        }

        setTheme();
        init();
    }

    private void setTheme() {
        // Initialize views
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnStart = findViewById(R.id.btnSubmit);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void init(){
        // Login button click
        btnStart.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            //Utils.showAlert(this,"Login Clicked");

            // Dummy check (replace with API call or DB validation)
            if (!mobile.isEmpty() && !password.isEmpty()) {
                //Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                checkLogin(mobile,password);
                // Go to next screen (example: HomeActivity)
                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();*/
            } else {
                Utils.showAlert(this,"Enter login details");
                //Toast.makeText(this, "Enter login details", Toast.LENGTH_SHORT).show();
            }
        });

        // Forgot Password click
        tvForgotPassword.setOnClickListener(v ->{
                Utils.showAlert(this,"Forgot Password Clicked");
                //Toast.makeText(this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
            }
        );
    }

    private void checkLogin(String mobile, String password) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", mobile+"="+password);
        Call<LoginResponse> call =apiInterface.loginAuth(mobile, password);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    System.out.print("Response : " + loginResponse);
                    if (loginResponse != null) {
                        if (loginResponse.getStatus()) {
                            User userData = loginResponse.getUser();
                            SharedPreferences.getInstance(LoginActivity.this).storeObject(SharedPreferences.KEY_LOGIN_OBJ, userData);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(LoginActivity.this, loginResponse.getMsg());
                            //Toast.makeText(LoginActivity.this, loginResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("error_msg");
                            Utils.showAlert(LoginActivity.this,msg);
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(LoginActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(LoginActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}