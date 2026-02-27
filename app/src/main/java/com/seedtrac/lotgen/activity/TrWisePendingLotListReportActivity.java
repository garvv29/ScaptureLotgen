package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.TrWiseLotPendingReportAdapter;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.pendinglotreport.Datum;
import com.seedtrac.lotgen.parser.pendinglotreport.PendingLotListReportResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrWisePendingLotListReportActivity extends AppCompatActivity {

    private String type;
    private User userData;

    RecyclerView rvBags;
    TrWiseLotPendingReportAdapter bagsAdapter;
    private List<Datum> barcodeList=new ArrayList<>();
    private String reportType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tr_wise_pending_lot_list_report);
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back press handling
                Intent intent = new Intent(TrWisePendingLotListReportActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Pending Lots");

        type = getIntent().getStringExtra("type");

        TextView tvTitle = findViewById(R.id.tvTitle);

        rvBags = findViewById(R.id.rvBarcodes);

        if (type.equalsIgnoreCase("pending_activation")){
            tvTitle.setText("Bags Activation Pending Lots");
            reportType = "actpending";
        }else if (type.equalsIgnoreCase("pending_loading")){
            tvTitle.setText("Loading Pending Lots");
            reportType = "loadpending";
        }else {
            tvTitle.setText("Receive Pending Lot List");
            reportType="recpending";
        }

        // Setup RecyclerView
        //bagList = new ArrayList<>();
        bagsAdapter = new TrWiseLotPendingReportAdapter(barcodeList);
        rvBags.setLayoutManager(new LinearLayoutManager(this));
        rvBags.setAdapter(bagsAdapter);
        // Add divider (vertical)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBags.getContext(), DividerItemDecoration.VERTICAL);
        rvBags.addItemDecoration(dividerItemDecoration);

        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
    }

    private void init(){
        getLotList();
    }

    private void getLotList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<PendingLotListReportResponse> call =apiInterface.getPendingLotList(userData.getMobile1(),userData.getScode(),reportType);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<PendingLotListReportResponse> call, @NonNull Response<PendingLotListReportResponse> response) {
                if (response.isSuccessful()) {
                    PendingLotListReportResponse pendingLotListReportResponse = response.body();
                    System.out.print("Response : " + pendingLotListReportResponse);
                    if (pendingLotListReportResponse != null) {
                        if (pendingLotListReportResponse.getStatus()) {
                            Toast.makeText(TrWisePendingLotListReportActivity.this, pendingLotListReportResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            barcodeList.clear();
                            barcodeList = pendingLotListReportResponse.getData();
                            bagsAdapter = new TrWiseLotPendingReportAdapter(barcodeList);
                            rvBags.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvBags.setAdapter(bagsAdapter);
                            // Add divider (vertical)
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(TrWisePendingLotListReportActivity.this, DividerItemDecoration.VERTICAL);
                            rvBags.addItemDecoration(dividerItemDecoration);
                            bagsAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showAlert(TrWisePendingLotListReportActivity.this, pendingLotListReportResponse.getMsg());
                            //Toast.makeText(BagsActivationScanningActivity.this, actBarcodeListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(TrWisePendingLotListReportActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PendingLotListReportResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(TrWisePendingLotListReportActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}