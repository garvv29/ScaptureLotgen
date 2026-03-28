package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.GsSlocBarcodeAdapter;
import com.seedtrac.lotgen.communicator.alertCommunicator;
import com.seedtrac.lotgen.model.ScannedBarcode;
import com.seedtrac.lotgen.parser.binlist.BinListResponse;
import com.seedtrac.lotgen.parser.binlist.Datum;
import com.seedtrac.lotgen.parser.gsbarcodeinfo.GsBarcodeInfoResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
import com.seedtrac.lotgen.parser.whlist.Data;
import com.seedtrac.lotgen.parser.whlist.WhListResponse;
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

public class GsSLOCShiftingActivity extends AppCompatActivity {
    private AutoCompleteTextView actLotNumber, dd_wh, dd_bin, dd_subbin;
    private TextView tvCrop, tvSpCodef, tvSpCodem, tvProductionPerson,tvLotNo, tvGsSLOC, tvBarcode;
    private LinearLayout ll_lotinfo, ll_table_header;
    private EditText etBarcode;
    private Button btnSubmit;
    private RecyclerView rv_scanned_barcodes;
    private GsSlocBarcodeAdapter scannedBarcodeAdapter;
    private List<ScannedBarcode> scannedBarcodes = new ArrayList<>();
    private List<String> lots=new ArrayList<>();
    private List<Data> whlist=new ArrayList<>();
    private Integer whId=0;
    private List<Datum> binlist=new ArrayList<>();
    private Integer binId=0;
    private List<com.seedtrac.lotgen.parser.subbinlist.Datum1> subbinlist=new ArrayList<>();
    private Integer subbinId=0;
    private String lotnumber, harvestdate, whname, binname;
    private Integer bagcount;
    private TextView tvFarmerName, tvFarmerVillage,tvBags, tvTotalQty, tvHarvestDate;
    private TextInputLayout til_ddLot;
    private User userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gs_slocshifting);
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

        setTheme();
        init();

    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        Utils.getInstance(this);
        dd_wh = findViewById(R.id.dd_wh);
        dd_bin = findViewById(R.id.dd_bin);
        dd_subbin = findViewById(R.id.dd_subbin);
        ll_lotinfo = findViewById(R.id.ll_lotinfo);
        ll_table_header = findViewById(R.id.ll_table_header);
        rv_scanned_barcodes = findViewById(R.id.rv_scanned_barcodes);
        tvLotNo = findViewById(R.id.tvLotNo);
        tvBarcode = findViewById(R.id.tvBarcode);
        tvGsSLOC = findViewById(R.id.tvGsSLOC);
        tvCrop = findViewById(R.id.tvCrop);
        tvSpCodef = findViewById(R.id.tvSpCodef);
        tvSpCodem = findViewById(R.id.tvSpCodem);
        tvProductionPerson = findViewById(R.id.tvProductionPerson);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerVillage = findViewById(R.id.tvFarmerVillage);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvBags = findViewById(R.id.tvBags);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        etBarcode = findViewById(R.id.etBarcode);

        // Setup RecyclerView
        rv_scanned_barcodes.setLayoutManager(new LinearLayoutManager(this));
        scannedBarcodeAdapter = new GsSlocBarcodeAdapter(scannedBarcodes);
        rv_scanned_barcodes.setAdapter(scannedBarcodeAdapter);

        // Set title
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("GS SLOC Shifting");
    }

    private void init(){
        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
        getWhList();
        dd_wh.setOnItemClickListener((parent, view, position, id) -> {
            Data selectedWh = whlist.get(position);
            whId = selectedWh.getWhid();
            getBinList();
        });
        dd_bin.setOnItemClickListener((parent, view, position, id) -> {
            Datum selectedBin = binlist.get(position);
            binId = selectedBin.getBinid();
            subbinId = 0;  // Reset subbin when bin changes
            dd_subbin.setText("");
            getSubbinList();
        });
        dd_subbin.setOnItemClickListener((parent, view, position, id) -> {
            com.seedtrac.lotgen.parser.subbinlist.Datum1 selectedSubbin = subbinlist.get(position);
            subbinId = selectedSubbin.getSubbinid();
        });

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
                    if (scanResult.length()==8 || scanResult.length() == 11) {
                        getBarcodeInfo(scanResult);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            etBarcode.setText("");
                            etBarcode.requestFocus();
                        }, 200);
                    }
                }
            }
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void getBarcodeInfo(String scanResult) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+scanResult);
        Call<GsBarcodeInfoResponse> call =apiInterface.getGsBarcodeInfo(userData.getMobile1(), userData.getScode(), scanResult);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<GsBarcodeInfoResponse> call, @NonNull Response<GsBarcodeInfoResponse> response) {
                if (response.isSuccessful()) {
                    GsBarcodeInfoResponse lotInfoResponse = response.body();
                    System.out.print("Response : " + lotInfoResponse);
                    if (lotInfoResponse != null) {
                        if (lotInfoResponse.getStatus()) {
                            Toast.makeText(GsSLOCShiftingActivity.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            com.seedtrac.lotgen.parser.gsbarcodeinfo.User lotInfoData = lotInfoResponse.getUser().get(0);
                            
                            // Check if barcode already exists in the list
                            boolean exists = scannedBarcodes.stream()
                                    .anyMatch(b -> b.getBarcode().equals(scanResult));
                            
                            if (!exists) {
                                // Add barcode to the list
                                ScannedBarcode barcode = new ScannedBarcode(
                                        scannedBarcodes.size() + 1,
                                        scanResult,
                                        lotInfoData.getLotno()
                                );
                                scannedBarcodes.add(barcode);
                                updateTableVisibility();
                                scannedBarcodeAdapter.notifyDataSetChanged();
                                
                                // Disable WH and BIN after first barcode is added
                                if (scannedBarcodes.size() == 1) {
                                    dd_wh.setEnabled(false);
                                    dd_bin.setEnabled(false);
                                    Toast.makeText(GsSLOCShiftingActivity.this, "WH and Bin are now locked", Toast.LENGTH_SHORT).show();
                                }
                                
                                Toast.makeText(GsSLOCShiftingActivity.this, "Barcode added to list", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GsSLOCShiftingActivity.this, "Barcode already exists in the list", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.cancel();
                        } else {
                            ll_lotinfo.setVisibility(View.GONE);
                            Utils.showAlert(GsSLOCShiftingActivity.this, lotInfoResponse.getMsg());
                            progressDialog.cancel();
                        }
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GsSLOCShiftingActivity.this,msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GsBarcodeInfoResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GsSLOCShiftingActivity.this,"RetrofitError : " + t.getMessage());
            }
        });
    }

    private void validateAndSubmit() {
        Dialog dialog = new Dialog(GsSLOCShiftingActivity.this);
        dialog.setContentView(R.layout.submit_confirm_alert);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            if (scannedBarcodes.isEmpty() || whId==0 || binId==0 ){
                Utils.showAlert(GsSLOCShiftingActivity.this, "Please scan at least one barcode and select WH, Bin");
                return;
            }

            Utils.getInstance().showSubmitConfirmAlert(GsSLOCShiftingActivity.this, "Do you want to submit this transaction?", new alertCommunicator() {
                @Override
                public void onClickPositiveBtn() {
                    submitForm();
                }

                @Override
                public void onClickNegativeBtn() {

                }
            });

        });
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void submitForm() {
        // Build comma-separated barcode list
        StringBuilder barcodeList = new StringBuilder();
        for (int i = 0; i < scannedBarcodes.size(); i++) {
            if (i > 0) barcodeList.append(",");
            barcodeList.append(scannedBarcodes.get(i).getBarcode());
        }

        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:",userData.getMobile1()+"="+ userData.getScode()+"="+ barcodeList.toString()+"="+whId.toString()+"="+ binId.toString()+"="+subbinId.toString());
        Call<SubmitSuccessResponse> call =apiInterface.updateGsSLOCDetails(userData.getMobile1(), userData.getScode(), barcodeList.toString(), whId.toString(), binId.toString(), subbinId.toString());
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(GsSLOCShiftingActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GsSLOCShiftingActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(GsSLOCShiftingActivity.this, submitSuccessResponse.getMsg());
                            //Toast.makeText(BagsActivationScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GsSLOCShiftingActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(GsSLOCShiftingActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getWhList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<WhListResponse> call =apiInterface.getWhList(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<WhListResponse> call, @NonNull Response<WhListResponse> response) {
                if (response.isSuccessful()) {
                    WhListResponse whListResponse = response.body();
                    System.out.print("Response : " + whListResponse);
                    if (whListResponse != null) {
                        if (whListResponse.getStatus()) {
                            Toast.makeText(GsSLOCShiftingActivity.this, whListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            whlist = whListResponse.getData();
                            List<String> whList = new ArrayList<>();
                            for (Data list : whlist) {
                                whList.add(list.getWhname());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, whList);
                            dd_wh.setAdapter(adapter);
                            // Show dropdown on click (even without typing)
                            dd_wh.setOnClickListener(v -> dd_wh.showDropDown());
                        } else {
                            Utils.showAlert(GsSLOCShiftingActivity.this, whListResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivity.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GsSLOCShiftingActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WhListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GsSLOCShiftingActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBinList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<BinListResponse> call =apiInterface.getBinList(userData.getMobile1(), userData.getScode(),whId);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<BinListResponse> call, @NonNull Response<BinListResponse> response) {
                if (response.isSuccessful()) {
                    BinListResponse binListResponse = response.body();
                    System.out.print("Response : " + binListResponse);
                    if (binListResponse != null) {
                        if (binListResponse.getStatus()) {
                            Toast.makeText(GsSLOCShiftingActivity.this, binListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            binlist = binListResponse.getData();
                            List<String> binList = new ArrayList<>();
                            for (Datum list : binlist) {
                                binList.add(list.getBinname());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, binList);
                            dd_bin.setAdapter(adapter);
                            // Show dropdown on click (even without typing)
                            dd_bin.setOnClickListener(v -> dd_bin.showDropDown());
                            // Load subbin list when bin is loaded
                            if (!binlist.isEmpty()) {
                                getSubbinList();
                            }
                        } else {
                            Utils.showAlert(GsSLOCShiftingActivity.this, binListResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivity.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GsSLOCShiftingActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BinListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GsSLOCShiftingActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSubbinList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+whId+"="+binId);
        Call<com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse> call = apiInterface.getSubbinList(userData.getMobile1(), userData.getScode(), whId, binId);
        call.enqueue(new Callback<com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse> call, @NonNull Response<com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse> response) {
                if (response.isSuccessful()) {
                    com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse subbinListResponse = response.body();
                    if (subbinListResponse != null && subbinListResponse.getStatus()) {
                        subbinlist = subbinListResponse.getData();
                        List<String> subbinNamesList = new ArrayList<>();
                        for (com.seedtrac.lotgen.parser.subbinlist.Datum1 subbin : subbinlist) {
                            subbinNamesList.add(subbin.getSubbinname());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(GsSLOCShiftingActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, subbinNamesList);
                        dd_subbin.setAdapter(adapter);
                        dd_subbin.setOnClickListener(v -> dd_subbin.showDropDown());
                        progressDialog.cancel();
                    } else {
                        progressDialog.cancel();
                        Utils.showAlert(GsSLOCShiftingActivity.this, "Failed to load SubBin list");
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GsSLOCShiftingActivity.this, msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.seedtrac.lotgen.parser.subbinlist.SubBinListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GsSLOCShiftingActivity.this, "RetrofitError : " + t.getMessage());
            }
        });
    }

    private void updateTableVisibility() {
        if (scannedBarcodes.isEmpty()) {
            ll_table_header.setVisibility(View.GONE);
            rv_scanned_barcodes.setVisibility(View.GONE);
            // Re-enable WH and BIN if list is emptied
            dd_wh.setEnabled(true);
            dd_bin.setEnabled(true);
        } else {
            ll_table_header.setVisibility(View.VISIBLE);
            rv_scanned_barcodes.setVisibility(View.VISIBLE);
        }
    }

}