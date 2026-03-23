package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.adapter.LoadingListAdapter;
import com.seedtrac.lotgen.parser.loadinglist.Data;
import com.seedtrac.lotgen.parser.loadinglist.LoadingListResponse;
import com.seedtrac.lotgen.parser.loadingtrinfo.LoadingTrInfoResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingScanningActivity extends AppCompatActivity implements LoadingListAdapter.LoadingListListener{
    TextView tvTransportName, tvVehicleNumber, tvLRNumber, tvDriverName, tvMobile, tvDestination, tvDispatchDate, tvToolbarTitle;
    Button btnAdd, btnRemove;
    RecyclerView rvItemList;
    List<Data> loadinList = new ArrayList<>();
    LoadingListAdapter adapter;
    private String trid="0";
    private EditText etBarcode;
    private String action="add";
    private User userData;
    private com.seedtrac.lotgen.parser.loadingtrinfo.Data trInfoData;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_scanning);
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
                Intent intent = new Intent(LoadingScanningActivity.this, LoadingTransactionsListActivity.class);
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
        // Your logic here
        etBarcode.requestFocus();
        // Set the listener
        //etBarcode.setOnEditorActionListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Handle focus loss, if needed
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void setTheme() {
        tvTransportName = findViewById(R.id.tvTransportName);
        tvVehicleNumber = findViewById(R.id.tvVehicleNumber);
        tvLRNumber = findViewById(R.id.tvLrNumber);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvMobile = findViewById(R.id.tvMobile);
        tvDestination = findViewById(R.id.tvDestination);
        tvDispatchDate = findViewById(R.id.tvDispatchDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        btnSubmit = findViewById(R.id.btnSubmit);
        etBarcode = findViewById(R.id.etBarcode);
        rvItemList = findViewById(R.id.rvItemList);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);

        tvToolbarTitle.setText("Bags Loading");

        trid = getIntent().getStringExtra("trid");

        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);

        // Setup RecyclerView
        adapter = new LoadingListAdapter(LoadingScanningActivity.this, loadinList);
        rvItemList.setHasFixedSize(true);
        rvItemList.setLayoutManager(new LinearLayoutManager(this));
        rvItemList.setAdapter(adapter);
        // Add divider (vertical)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvItemList.getContext(), DividerItemDecoration.VERTICAL);
        rvItemList.addItemDecoration(dividerItemDecoration);
        adapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etBarcode.getWindowToken(), 0);
    }

    private void init(){
        getTrInfo();

        etBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String scanResult = etBarcode.getText().toString().trim();

                if (!scanResult.isEmpty()) {
                    if (scanResult.length()==9 || scanResult.length() == 11) {
                        storeBagCode(scanResult);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            etBarcode.setText("");
                            etBarcode.requestFocus();
                        }, 200);

                    }
                }
            }
        });

        btnAdd.setOnClickListener(v -> {
            String bagCode = etBarcode.getText().toString().trim();
            action = "add";
            if (!bagCode.isEmpty()) {
                if (bagCode.length()==8 || bagCode.length()==11 || bagCode.length()==9){
                    checkScannedBarcode(bagCode);
                    //storeBagCode(bagCode);
                }else {
                    Utils.showAlert(LoadingScanningActivity.this, "Invalid Bag Code");
                    //Toast.makeText(LoadingScanningActivity.this, "Invalid Bag Code", Toast.LENGTH_SHORT).show();
                }
            } else {
                startScanner();
            }
        });

        btnRemove.setOnClickListener(v -> {
            String bagCode = etBarcode.getText().toString().trim();
            action = "remove";
            if (!bagCode.isEmpty()) {
                if (bagCode.length()==8 || bagCode.length()==11){
                    checkScannedBarcode(bagCode);
                    //storeBagCode(bagCode);
                }else {
                    Utils.showAlert(LoadingScanningActivity.this, "Invalid Bag Code");
                    //Toast.makeText(LoadingScanningActivity.this, "Invalid Bag Code", Toast.LENGTH_SHORT).show();
                }
            } else {
                startScanner();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadinList.isEmpty()){
                    Utils.showAlert(LoadingScanningActivity.this, "Please add bags");
                    //Toast.makeText(LoadingScanningActivity.this, "Please add bags", Toast.LENGTH_SHORT).show();
                }else{
                    final Dialog dialog = new Dialog(LoadingScanningActivity.this);
                    dialog.setContentView(R.layout.submit_confirm_alert_loading_scanning);
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    RadioGroup rgVehiclePickup = dialog.findViewById(R.id.rgVehiclePickup);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                    btnCancel.setOnClickListener(view -> dialog.cancel());

                    btnSubmit.setOnClickListener(v1 -> {
                        int selectedId = rgVehiclePickup.getCheckedRadioButtonId();
                        String vehiclePickup = selectedId == R.id.rbYes ? "yes" : "no";
                        
                        dialog.dismiss();
                        finalSubmit(vehiclePickup);
                    });
                    dialog.show();
                }
            }
        });
    }

    private void finalSubmit(String vehiclePickup) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+trid+"="+vehiclePickup);
        Call<SubmitSuccessResponse> call =apiInterface.loadingFinalSubmit(userData.getScode(), trid, vehiclePickup);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(LoadingScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoadingScanningActivity.this, LoadingTransactionsListActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(LoadingScanningActivity.this, submitSuccessResponse.getMsg());
                            //Toast.makeText(LoadingScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingScanningActivity.this, msg);
                            //Toast.makeText(LoadingScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTrInfo() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+trid);
        Call<LoadingTrInfoResponse> call =apiInterface.getTransInfo(userData.getScode(), trid);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<LoadingTrInfoResponse> call, @NonNull Response<LoadingTrInfoResponse> response) {
                if (response.isSuccessful()) {
                    LoadingTrInfoResponse loadingTrInfoResponse = response.body();
                    System.out.print("Response : " + loadingTrInfoResponse);
                    if (loadingTrInfoResponse != null) {
                        if (loadingTrInfoResponse.getStatus()) {
                            trInfoData = loadingTrInfoResponse.getData();
                            tvLRNumber.setText(trInfoData.getLrno());
                            tvDriverName.setText(trInfoData.getDrivername());
                            tvMobile.setText(trInfoData.getDriverno());
                            tvDispatchDate.setText(trInfoData.getDispdate());
                            tvTransportName.setText(trInfoData.getTrname());
                            tvVehicleNumber.setText(trInfoData.getVehno());
                            tvDestination.setText(trInfoData.getDestination());
                            getLoadingList(trid);
                        } else {
                            Utils.showAlert(LoadingScanningActivity.this, loadingTrInfoResponse.getMsg());
                            //Toast.makeText(LoadingScanningActivity.this, loadingTrInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingScanningActivity.this, msg);
                            //Toast.makeText(LoadingScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoadingTrInfoResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLoadingList(String trid) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+trid);
        Call<LoadingListResponse> call =apiInterface.getLoadingList(userData.getScode(), trid);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<LoadingListResponse> call, @NonNull Response<LoadingListResponse> response) {
                if (response.isSuccessful()) {
                    LoadingListResponse loadingListResponse = response.body();
                    System.out.print("Response : " + loadingListResponse);
                    if (loadingListResponse != null) {
                        if (loadingListResponse.getStatus()) {
                            Toast.makeText(LoadingScanningActivity.this, loadingListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            loadinList.clear();
                            loadinList = loadingListResponse.getData();
                            adapter = new LoadingListAdapter(LoadingScanningActivity.this, loadinList);
                            rvItemList.setHasFixedSize(true);
                            rvItemList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvItemList.setAdapter(adapter);
                            // Add divider (vertical)
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvItemList.getContext(), DividerItemDecoration.VERTICAL);
                            rvItemList.addItemDecoration(dividerItemDecoration);
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showAlert(LoadingScanningActivity.this, loadingListResponse.getMsg());
                            //Toast.makeText(LoadingScanningActivity.this, loadingListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingScanningActivity.this, msg);
                            //Toast.makeText(LoadingScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoadingListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR/Barcode");
        options.setOrientationLocked(true);
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    if (result.getContents().length()==8 || result.getContents().length()==11 ||result.getContents().length()==9){
                        storeBagCode(result.getContents());
                    }else {
                        Utils.showAlert(LoadingScanningActivity.this, "Invalid Bag Code");
                        //Toast.makeText(LoadingScanningActivity.this, "Invalid Bag Code", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void storeBagCode(String bagCode) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+trid+"="+bagCode+"="+action+"loading");
        Call<SubmitSuccessResponse> call =apiInterface.storeBagCode(userData.getMobile1(), userData.getScode(), trid, bagCode, action, "loading","");
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(LoadingScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            etBarcode.setText("");
                            etBarcode.requestFocus();
                            getLoadingList(trid);
                        } else {
                            etBarcode.requestFocus();
                            Utils.showAlert(LoadingScanningActivity.this, submitSuccessResponse.getMsg());
                            //Toast.makeText(LoadingScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    etBarcode.requestFocus();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingScanningActivity.this, msg);
                            //Toast.makeText(LoadingScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                etBarcode.requestFocus();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkScannedBarcode(String bagCode) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+trid+"="+bagCode+"="+action);
        Call<SubmitSuccessResponse> call =apiInterface.checkBagCode(userData.getScode(), trid, bagCode);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            //checkScannedBarcode(bagCode);
                            storeBagCode(bagCode);
                        } else {
                            Utils.showAlert(LoadingScanningActivity.this, submitSuccessResponse.getMsg());
                            //Toast.makeText(LoadingScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingScanningActivity.this, msg);
                            //Toast.makeText(LoadingScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClicked(Data data) {

    }

    /*@Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean isEnterKey = (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        boolean isDoneAction = (actionId == EditorInfo.IME_ACTION_DONE);

        if (isEnterKey || isDoneAction) {

            String scanResult = etBarcode.getText().toString().trim();

            if (!scanResult.isEmpty()) {

                if (scanResult.length() == 8 || scanResult.length() == 11) {

                    storeBagCode(scanResult);

                    // Auto clear field after a short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        etBarcode.setText("");
                        etBarcode.requestFocus();
                    }, 200); // delay 200ms

                } else {
                    Utils.showAlert(LoadingScanningActivity.this, "Invalid Bag Code");
                    etBarcode.setSelection(etBarcode.getText().length()); // keep input
                }
            }

            return true;
        }
        return false;
    }*/
}