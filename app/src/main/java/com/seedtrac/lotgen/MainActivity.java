package com.seedtrac.lotgen;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.seedtrac.lotgen.activity.BagActivationPendingListActivity;
import com.seedtrac.lotgen.activity.BagsActivationSetupActivity;
import com.seedtrac.lotgen.activity.GaurdSampleActivity;
import com.seedtrac.lotgen.activity.GsSLOCShiftingActivity;
import com.seedtrac.lotgen.activity.LoadingSetupActivity;
import com.seedtrac.lotgen.activity.LoadingTransactionsListActivity;
import com.seedtrac.lotgen.activity.LotReceiveActivity;
import com.seedtrac.lotgen.activity.LotReceiveListActivity;
import com.seedtrac.lotgen.activity.SpCodeWiseSummaryReportActivity;
import com.seedtrac.lotgen.activity.TrWisePendingLotListReportActivity;
import com.seedtrac.lotgen.adapter.LoadingPendingListAdapter;
import com.seedtrac.lotgen.adapter.LotReceiveListAdapter;
import com.seedtrac.lotgen.parser.dashboarddata.DashboardDataResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.recpendinglotlist.RecPendingLotListResponse;
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

public class MainActivity extends AppCompatActivity {
    private LinearLayout btnBagActivation, btnLoading, btnRecieve, btnGsSampling, btnGsSlocshifting, btnReport;
    private TextView tv_userName,tv_pendingActivations,tv_pendingLoading,tv_pendingRecieve,tvTotActivated,tvTotDisp;
    private TextView tvToolbarTitle;
    private User userData;
    private ImageView ivRecieve,ivBagActivation,ivLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setTheme();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data whenever user returns to MainActivity
        getDashboardData();
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
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
        btnBagActivation = findViewById(R.id.btnBagActivation);
        btnLoading = findViewById(R.id.btnLoading);
        btnRecieve = findViewById(R.id.btnRecieve);
        btnGsSampling = findViewById(R.id.btnGsSampling);
        btnGsSlocshifting = findViewById(R.id.btnGsSlocshifting);
        btnReport = findViewById(R.id.btnReport);
        tv_userName = findViewById(R.id.tv_userName);
        tv_pendingActivations = findViewById(R.id.tv_pendingActivations);
        tv_pendingLoading = findViewById(R.id.tv_pendingLoading);
        tv_pendingRecieve = findViewById(R.id.tv_pendingRecieve);
        tvTotActivated = findViewById(R.id.tvTotActivated);
        tvTotDisp = findViewById(R.id.tvTotDisp);

        ivRecieve = findViewById(R.id.ivRecieve);
        ivBagActivation = findViewById(R.id.ivBagActivation);
        ivLoading = findViewById(R.id.ivLoading);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Home");

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
        tv_userName.setText("Welcome back, " + userData.getName());
        tv_pendingActivations.setText(userData.getPendingactivate().toString()+" Lots Pending");
        tv_pendingLoading.setText(userData.getPendingloading().toString()+" Lots Pending");
    }

    private void init(){
        getDashboardData();
        btnBagActivation.setOnClickListener(v ->{
                Intent intent = new Intent(MainActivity.this, BagActivationPendingListActivity.class);
                startActivity(intent);
        });

        btnLoading.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoadingTransactionsListActivity.class);
            startActivity(intent);
        });

        btnRecieve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LotReceiveListActivity.class);
            startActivity(intent);
        });

        tv_pendingActivations.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrWisePendingLotListReportActivity.class);
            intent.putExtra("type","pending_activation");
            startActivity(intent);
        });

        tv_pendingLoading.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrWisePendingLotListReportActivity.class);
            intent.putExtra("type","pending_loading");
            startActivity(intent);
        });

        tv_pendingRecieve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrWisePendingLotListReportActivity.class);
            intent.putExtra("type","pending_receive");
            startActivity(intent);
        });

        btnGsSampling.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GaurdSampleActivity.class);
            startActivity(intent);
        });

        btnGsSlocshifting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GsSLOCShiftingActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SpCodeWiseSummaryReportActivity.class);
            startActivity(intent);
        });
    }

    private void getDashboardData() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<DashboardDataResponse> call =apiInterface.getDashboardData(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<DashboardDataResponse> call, @NonNull Response<DashboardDataResponse> response) {
                if (response.isSuccessful()) {
                    DashboardDataResponse dashboardDataResponse = response.body();
                    System.out.print("Response : " + dashboardDataResponse);
                    if (dashboardDataResponse != null) {
                        if (dashboardDataResponse.getStatus()) {
                            tv_pendingRecieve.setText(dashboardDataResponse.getData().getRecpending().toString());
                            tv_pendingActivations.setText(dashboardDataResponse.getData().getActpending().toString());
                            tv_pendingLoading.setText(dashboardDataResponse.getData().getLoadpending().toString());
                            tvTotActivated.setText(dashboardDataResponse.getData().getTotactivated().toString());
                            tvTotDisp.setText(dashboardDataResponse.getData().getTotdisp().toString());
                        } else {
                            Utils.showAlert(MainActivity.this, dashboardDataResponse.getMsg());
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("error_msg");
                            Utils.showAlert(MainActivity.this, msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardDataResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(MainActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(MainActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}