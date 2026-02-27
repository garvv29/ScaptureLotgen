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

import com.google.android.material.textfield.TextInputLayout;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.communicator.alertCommunicator;
import com.seedtrac.lotgen.parser.actlotlist.ActLotListResponse;
import com.seedtrac.lotgen.parser.binlist.BinListResponse;
import com.seedtrac.lotgen.parser.binlist.Datum;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoData;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
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

public class GaurdSampleActivity extends AppCompatActivity {
    private AutoCompleteTextView actLotNumber, dd_wh, dd_bin, dd_subbin;
    private TextView tvCrop, tvSpCodef, tvSpCodem, tvProductionPerson;
    private LinearLayout ll_lotinfo;
    private EditText etBarcode;
    private Button btnSubmit;
    private List<String> lots=new ArrayList<>();
    private List<Data> whlist=new ArrayList<>();
    private Integer whId=0;
    private List<Datum> binlist=new ArrayList<>();
    private Integer binId=0;
    private String lotnumber, harvestdate, whname, binname;
    private Integer bagcount;
    private TextView tvFarmerName, tvFarmerVillage,tvBags, tvTotalQty, tvHarvestDate;
    private TextInputLayout til_ddLot;
    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gaurd_sample);
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
        actLotNumber = findViewById(R.id.actLotNumber);
        til_ddLot = findViewById(R.id.til_ddLot);
        dd_wh = findViewById(R.id.dd_wh);
        dd_bin = findViewById(R.id.dd_bin);
        dd_subbin = findViewById(R.id.dd_subbin);
        ll_lotinfo = findViewById(R.id.ll_lotinfo);
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

        // Set title
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Guard Sampling");
    }

    private void init(){
        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
        getLotList();
        actLotNumber.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            //Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            getLotInfo(selectedItem);
        });
        dd_wh.setOnItemClickListener((parent, view, position, id) -> {
            Data selectedWh = whlist.get(position);
            whId = selectedWh.getWhid();
            getBinList();
        });
        dd_bin.setOnItemClickListener((parent, view, position, id) -> {
            Datum selectedBin = binlist.get(position);
            binId = selectedBin.getBinid();
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
                        checkBarcode(scanResult);
                    }
                }
            }
        });

        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void checkBarcode(String scanResult) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+scanResult);
        Call<SubmitSuccessResponse> call =apiInterface.checkGsBarcode(userData.getMobile1(), userData.getScode(), scanResult);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            getWhList();
                        } else {
                            Utils.showAlert(GaurdSampleActivity.this, submitSuccessResponse.getMsg());
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
                            Utils.showAlert(GaurdSampleActivity.this, msg);
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
                Utils.showAlert(GaurdSampleActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSubmit() {
        String lot = actLotNumber.getText().toString().trim();
        //String lot = etLotNumber.getText().toString().trim();
        String barcode = etBarcode.getText().toString().trim();

        Dialog dialog = new Dialog(GaurdSampleActivity.this);
        dialog.setContentView(R.layout.submit_confirm_alert);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        /*TextView text = dialog.findViewById(R.id.tv_message);
        text.setText("Check the data entered before submission, once submitted it cannot be edited.\nAre you sure you want to submit?");*/
        dialog.findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            if (lot.isEmpty() || barcode.isEmpty() || lot.length()!=6 || whId==0 || binId==0){
                Utils.showAlert(GaurdSampleActivity.this, "Please fill all the fields");
                return;
            }

            Utils.getInstance().showSubmitConfirmAlert(GaurdSampleActivity.this, "Do you want to submit this transaction?", new alertCommunicator() {
                @Override
                public void onClickPositiveBtn() {
                    submitForm(lot, barcode);
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

    private void submitForm(String lot, String barcode) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+lot+"="+barcode);
        Call<SubmitSuccessResponse> call =apiInterface.updateGuardSampleDetails(userData.getMobile1(), userData.getScode(), barcode, lot, whId.toString(), binId.toString());
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(GaurdSampleActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GaurdSampleActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(GaurdSampleActivity.this, submitSuccessResponse.getMsg());
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
                            Utils.showAlert(GaurdSampleActivity.this, msg);
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
                Utils.showAlert(GaurdSampleActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLotInfo(String lotNumber) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lotNumber);
        Call<LotInfoResponse> call =apiInterface.getLotInfo(userData.getMobile1(), userData.getScode(), lotNumber);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<LotInfoResponse> call, @NonNull Response<LotInfoResponse> response) {
                if (response.isSuccessful()) {
                    LotInfoResponse lotInfoResponse = response.body();
                    System.out.print("Response : " + lotInfoResponse);
                    if (lotInfoResponse != null) {
                        if (lotInfoResponse.getStatus()) {
                            Toast.makeText(GaurdSampleActivity.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            LotInfoData lotInfoData = lotInfoResponse.getData().get(0);
                            ll_lotinfo.setVisibility(View.VISIBLE);
                            tvCrop.setText(lotInfoData.getCropname());
                            tvSpCodef.setText(lotInfoData.getSpcodef()+" X "+lotInfoData.getSpcodem());
                            tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                            tvFarmerName.setText(lotInfoData.getFarmername());
                            tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                            tvBags.setText(lotInfoData.getBags().toString());
                            tvTotalQty.setText(lotInfoData.getQty().toString());
                            tvHarvestDate.setText(lotInfoData.getHarvestdate());
                        } else {
                            ll_lotinfo.setVisibility(View.GONE);
                            Utils.showAlert(GaurdSampleActivity.this, lotInfoResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivity.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(GaurdSampleActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LotInfoResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GaurdSampleActivity.this,"RetrofitError : " + t.getMessage());
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
                            Toast.makeText(GaurdSampleActivity.this, binListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            binlist = binListResponse.getData();
                            List<String> binList = new ArrayList<>();
                            for (Datum list : binlist) {
                                binList.add(list.getBinname());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, binList);
                            dd_bin.setAdapter(adapter);
                            // Show dropdown on click (even without typing)
                            dd_bin.setOnClickListener(v -> dd_bin.showDropDown());
                        } else {
                            Utils.showAlert(GaurdSampleActivity.this, binListResponse.getMsg());
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
                            Utils.showAlert(GaurdSampleActivity.this,msg);
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
                Utils.showAlert(GaurdSampleActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLotList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<ActLotListResponse> call =apiInterface.getLotList(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ActLotListResponse> call, @NonNull Response<ActLotListResponse> response) {
                if (response.isSuccessful()) {
                    ActLotListResponse actLotListResponse = response.body();
                    System.out.print("Response : " + actLotListResponse);
                    if (actLotListResponse != null) {
                        if (actLotListResponse.getStatus()) {
                            Toast.makeText(GaurdSampleActivity.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            lots = actLotListResponse.getData();
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, lots);
                            actLotNumber.setAdapter(adapter);
                            //Show dropdown on click (even without typing)
                            actLotNumber.setOnClickListener(v -> actLotNumber.showDropDown());

                        } else {
                            Utils.showAlert(GaurdSampleActivity.this, actLotListResponse.getMsg());
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
                            Utils.showAlert(GaurdSampleActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActLotListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(GaurdSampleActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GaurdSampleActivity.this, whListResponse.getMsg(), Toast.LENGTH_SHORT).show();
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
                            Utils.showAlert(GaurdSampleActivity.this, whListResponse.getMsg());
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
                            Utils.showAlert(GaurdSampleActivity.this,msg);
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
                Utils.showAlert(GaurdSampleActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}