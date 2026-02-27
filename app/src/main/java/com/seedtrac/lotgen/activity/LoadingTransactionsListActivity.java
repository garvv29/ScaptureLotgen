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
import android.window.OnBackInvokedDispatcher;

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
import com.seedtrac.lotgen.adapter.LoadingPendingListAdapter;
import com.seedtrac.lotgen.model.LoadingTrList;
import com.seedtrac.lotgen.parser.activationtrlist.ActivationTrListResponse;
import com.seedtrac.lotgen.parser.loadingtrpendinglist.Data;
import com.seedtrac.lotgen.parser.loadingtrpendinglist.LoadingTrPendingListResponse;
import com.seedtrac.lotgen.parser.login.User;
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

public class LoadingTransactionsListActivity extends AppCompatActivity {
    private RecyclerView rvPendingList;
    private LoadingPendingListAdapter adapter;
    private Button btnAddNew;
    private TextView tvToolbarTitle;
    private User userData;
    private List<Data> pendingList= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_transactions_list);
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
                Intent intent = new Intent(LoadingTransactionsListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        rvPendingList = findViewById(R.id.rvPendingList);
        btnAddNew = findViewById(R.id.btnAddNew);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);

        tvToolbarTitle.setText("Pending Bag Loading List");

        adapter = new LoadingPendingListAdapter(this, pendingList);

        rvPendingList.setLayoutManager(new LinearLayoutManager(this));
        rvPendingList.setAdapter(adapter);

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
    }

    private void init(){
        getPendingList();
        btnAddNew.setOnClickListener(
                view -> {
                    Intent intent = new Intent(LoadingTransactionsListActivity.this, LoadingSetupActivity.class);
                    startActivity(intent);
                }
        );
    }

    private void getPendingList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<LoadingTrPendingListResponse> call =apiInterface.getLoadingPendingList(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<LoadingTrPendingListResponse> call, @NonNull Response<LoadingTrPendingListResponse> response) {
                if (response.isSuccessful()) {
                    LoadingTrPendingListResponse loadingTrPendingListResponse = response.body();
                    System.out.print("Response : " + loadingTrPendingListResponse);
                    if (loadingTrPendingListResponse != null) {
                        if (loadingTrPendingListResponse.getStatus()) {
                            pendingList = loadingTrPendingListResponse.getData();
                            adapter = new LoadingPendingListAdapter(getApplicationContext(),pendingList);
                            rvPendingList.setHasFixedSize(true);
                            rvPendingList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvPendingList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showAlert(LoadingTransactionsListActivity.this, loadingTrPendingListResponse.getMsg());
                            //Toast.makeText(LoadingTransactionsListActivity.this, loadingTrPendingListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("error_msg");
                            Utils.showAlert(LoadingTransactionsListActivity.this, msg);
                            //Toast.makeText(LoadingTransactionsListActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoadingTrPendingListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LoadingTransactionsListActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingTransactionsListActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}