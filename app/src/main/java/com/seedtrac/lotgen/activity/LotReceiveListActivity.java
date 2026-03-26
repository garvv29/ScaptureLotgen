package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.ActivationPendingListAdapter;
import com.seedtrac.lotgen.adapter.LotReceiveListAdapter;
import com.seedtrac.lotgen.parser.activationtrlist.ActivationTrListResponse;
import com.seedtrac.lotgen.parser.activationtrlist.Data;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.recpendinglotlist.Datum;
import com.seedtrac.lotgen.parser.recpendinglotlist.RecPendingLotListResponse;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
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

public class LotReceiveListActivity extends AppCompatActivity {
    private RecyclerView rvPendingList;
    private LotReceiveListAdapter adapter;
    private List<Datum> pendingList;
    private Button btnAddNew,btnSubmit;
    private TextView tvToolbarTitle;
    private User userData;
    private String trid="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lot_receive_list);
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
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        0, // 0 = white icons
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom behavior here
                //Toast.makeText(LotReceiveListActivity.this, "Back pressed!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LotReceiveListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLotList();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void setTheme() {
        // Set title
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Lot Receive");

        rvPendingList = findViewById(R.id.rvPendingList);
        btnAddNew = findViewById(R.id.btnAddNew);
        btnSubmit = findViewById(R.id.btnSubmit);

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);

        pendingList = new ArrayList<>();

        adapter = new LotReceiveListAdapter(LotReceiveListActivity.this, pendingList);
        rvPendingList.setHasFixedSize(true);
        rvPendingList.setLayoutManager(new LinearLayoutManager(this));
        rvPendingList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void init(){
        getLotList();
        btnAddNew.setOnClickListener(v -> {
            Intent intent = new Intent(LotReceiveListActivity.this, LotReceiveActivity.class);
            intent.putExtra("lotNumber", "");
            intent.putExtra("harvestdate", "");
            intent.putExtra("bagcount", "");
            intent.putExtra("whname", "");
            intent.putExtra("binname", "");
            intent.putExtra("trid", "");
            startActivity(intent);
        });

        btnSubmit.setOnClickListener(v -> {
            if(pendingList.isEmpty()){
                Utils.showAlert(LotReceiveListActivity.this, "No data to submit");
            }else {
                submitList();
            }
        });
    }

    private void submitList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<SubmitSuccessResponse> call =apiInterface.submitRecList(userData.getMobile1(), userData.getScode(),trid);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(LotReceiveListActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            pendingList.clear();
                            adapter.notifyDataSetChanged();
                            getLotList();
                        } else {
                            Utils.showAlert(LotReceiveListActivity.this, submitSuccessResponse.getMsg());
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("error_msg");
                            Utils.showAlert(LotReceiveListActivity.this, msg);
                            //Toast.makeText(BagActivationPendingListActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LotReceiveListActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagActivationPendingListActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLotList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<RecPendingLotListResponse> call =apiInterface.getLotRecPendingList(userData.getMobile1(), userData.getScode(),trid);
        call.enqueue(new Callback<RecPendingLotListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<RecPendingLotListResponse> call, @NonNull Response<RecPendingLotListResponse> response) {
                if (response.isSuccessful()) {
                    RecPendingLotListResponse recPendingLotListResponse = response.body();
                    System.out.print("Response : "+recPendingLotListResponse);
                    if (recPendingLotListResponse != null) {
                        if (recPendingLotListResponse.getStatus()){
                            pendingList = recPendingLotListResponse.getData();
                            trid = pendingList.get(0).getTrid().toString();
                            adapter = new LotReceiveListAdapter(LotReceiveListActivity.this, pendingList);
                            rvPendingList.setHasFixedSize(true);
                            rvPendingList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvPendingList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }else {
                            Utils.showAlert(LotReceiveListActivity.this, recPendingLotListResponse.getMsg());
                            //Toast.makeText(BagActivationPendingListActivity.this, activationTrListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LotReceiveListActivity.this, msg);
                            //Toast.makeText(BagActivationPendingListActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RecPendingLotListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                //Utils.showAlert(LotReceiveListActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagActivationPendingListActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}