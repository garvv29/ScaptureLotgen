package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.parser.activationsubmit.ActivationSubmitResponse;
import com.seedtrac.lotgen.parser.actlotlist.ActLotListResponse;
import com.seedtrac.lotgen.parser.actremarklist.ActRemarksListResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoData;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import android.app.DatePickerDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.*;
import android.window.OnBackInvokedDispatcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class BagsActivationSetupActivityPrintRoll extends AppCompatActivity {
    AutoCompleteTextView actLotNumber,actProdGrade,actRemarks;
    EditText etHarvestDate, etNumberOfBags, etTotalWeight, etTareWeight, etRemarks, etMoisture;
    TextInputLayout til_remarks;
    RadioGroup rgGOT, rgMoisture;
    Button btnSubmit;
    private User userData;
    private List<String> lots= new ArrayList<>();
    private LinearLayout ll_lotinfo;
    private TextView tvCrop;
    private TextView tvSpCodef;
    private TextView tvProductionPerson;
    private TextView tvHarvestDate;
    private TextView tvSpCodem;
    private TextView tvFarmerName, tvFarmerVillage;
    private String selProdGrade="";
    private String selectedCropName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bags_activation_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupStatusBar();
        initViews();
        setupLotNumberDropdown();
        setupListeners();
        Utils.hideLogoutButton(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back navigation based on source activity
                String sourceActivity = getIntent().getStringExtra("sourceActivity");
                Intent intent;
                
                if ("LotReceiveActivity".equals(sourceActivity)) {
                    intent = new Intent(BagsActivationSetupActivityPrintRoll.this, LotReceiveActivity.class);
                } else {
                    intent = new Intent(BagsActivationSetupActivityPrintRoll.this, BagActivationPendingListActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        // Initialize
        actLotNumber = findViewById(R.id.actLotNumber);
        actProdGrade = findViewById(R.id.actProdGrade);
        actRemarks = findViewById(R.id.actRemarks);
        etHarvestDate = findViewById(R.id.etHarvestDate);
        etNumberOfBags = findViewById(R.id.etNumberOfBags);
        etTotalWeight = findViewById(R.id.etTotalWeight);
        etTareWeight = findViewById(R.id.etTareWeight);
        etMoisture = findViewById(R.id.etMoisture);
        etRemarks = findViewById(R.id.etRemarks);
        til_remarks = findViewById(R.id.til_remarks);
        rgGOT = findViewById(R.id.rgGOT);
        //rgMoisture = findViewById(R.id.rgMoisture);
        btnSubmit = findViewById(R.id.btnSubmit);
        ll_lotinfo = findViewById(R.id.ll_lotinfo);
        tvCrop = findViewById(R.id.tvCrop);
        tvSpCodef = findViewById(R.id.tvSpCodef);
        tvSpCodem = findViewById(R.id.tvSpCodem);
        tvProductionPerson = findViewById(R.id.tvProductionPerson);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerVillage = findViewById(R.id.tvFarmerVillage);

        // Set title
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Bags Activation Setup");

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
    }

    private void setupStatusBar() {
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
    }

    private void setupLotNumberDropdown() {
        List<String> prodGradeList = new ArrayList<>();
        prodGradeList.add("A");
        prodGradeList.add("B");
        prodGradeList.add("C");
        prodGradeList.add("D");
        prodGradeList.add("GOT");
        prodGradeList.add("NA");
        ArrayAdapter<String> remarksAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, prodGradeList);
        actProdGrade.setAdapter(remarksAdapter);
        actProdGrade.setOnClickListener(v -> actProdGrade.showDropDown());

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
                            Toast.makeText(BagsActivationSetupActivityPrintRoll.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            lots = actLotListResponse.getData();
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, lots);
                            actLotNumber.setAdapter(adapter);
                            // Show dropdown on click (even without typing)
                            actLotNumber.setOnClickListener(v -> actLotNumber.showDropDown());
                            getRemarksList();
                        } else {
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, actLotListResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this,msg);
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(BagsActivationSetupActivityPrintRoll.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRemarksList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+selectedCropName);
        Log.e("CropName", "Crop name passed to API: " + selectedCropName);
        Call<ActRemarksListResponse> call =apiInterface.getRemarksList(userData.getScode(), selectedCropName);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ActRemarksListResponse> call, @NonNull Response<ActRemarksListResponse> response) {
                if (response.isSuccessful()) {
                    ActRemarksListResponse actRemarksListResponse = response.body();
                    System.out.print("Response : " + actRemarksListResponse);
                    if (actRemarksListResponse != null) {
                        if (actRemarksListResponse.getStatus()) {
                            List<String> remarkList = actRemarksListResponse.getData();
                            ArrayAdapter<String> remarksAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, remarkList);
                            actRemarks.setAdapter(remarksAdapter);
                            actRemarks.setOnClickListener(v -> actRemarks.showDropDown());
                        } else {
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, actRemarksListResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this,msg);
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActRemarksListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(BagsActivationSetupActivityPrintRoll.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Date Picker
        etHarvestDate.setOnClickListener(v -> showDatePicker());

        // Submit button listener
        btnSubmit.setOnClickListener(v -> validateAndSubmit());

        actLotNumber.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            //Toast.makeText(this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            getLotInfo(selectedItem);
        });

        actRemarks.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            if (selectedItem.equalsIgnoreCase("Other")){
                til_remarks.setVisibility(View.VISIBLE);
            }else {
                til_remarks.setVisibility(View.GONE);
            }
        });

        actProdGrade.setOnItemClickListener((parent, view, position, id) -> {
            selProdGrade = parent.getItemAtPosition(position).toString();
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        // Get today's date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Calculate the max date (2 days before today)
        Calendar maxDate = (Calendar) calendar.clone();
        maxDate.add(Calendar.DAY_OF_MONTH, -3);

        // Calculate the min date (30 days before max date)
        Calendar minDate = (Calendar) maxDate.clone();
        minDate.add(Calendar.DAY_OF_MONTH, -30);

        @SuppressLint("SetTextI18n")
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) ->
                        etHarvestDate.setText(dayOfMonth + "-" + (month1 + 1) + "-" + year1),
                year, month, day
        );

        // Apply min and max date restrictions
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void validateAndSubmit() {
        String lot = actLotNumber.getText().toString().trim();
        String harvestDate = etHarvestDate.getText().toString().trim();
        String bags = etNumberOfBags.getText().toString().trim();
        String qty = etTotalWeight.getText().toString().trim();
        String tare = etTareWeight.getText().toString().trim();
        String remarks = actRemarks.getText().toString().trim();
        String moisture = etMoisture.getText().toString().trim();
        if (actRemarks.getText().toString().trim().equalsIgnoreCase("Other")){
            remarks=etRemarks.getText().toString().trim();
        }
        String gotStatus = ((RadioButton) findViewById(rgGOT.getCheckedRadioButtonId())).getText().toString();
        //String moisture = ((RadioButton) findViewById(rgMoisture.getCheckedRadioButtonId())).getText().toString();
        if (lot.isEmpty() || bags.isEmpty() || rgGOT.getCheckedRadioButtonId() == -1 || selProdGrade.isEmpty() || moisture.isEmpty()) {
            Utils.showAlert(this, "Please fill all fields");
            //Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Double.parseDouble(moisture)>=4 && Double.parseDouble(moisture)<=13) {
            Dialog dialog = new Dialog(BagsActivationSetupActivityPrintRoll.this);
            dialog.setContentView(R.layout.submit_confirm_alert);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            /*TextView text = dialog.findViewById(R.id.tv_message);
            text.setText("Check the data entered before submission, once submitted it cannot be edited.\nAre you sure you want to submit?");*/
            String finalRemarks = remarks;
            dialog.findViewById(R.id.btnSubmit).setOnClickListener(v -> submitForm(lot, harvestDate, bags, qty, tare, gotStatus, moisture, finalRemarks));
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
            dialog.setCancelable(false);
            dialog.show();
        } else {
            if (Double.parseDouble(moisture) > 99) {
                Utils.showAlert(this, "Moisture value cannot be greater than 99");
                return;
            }
            Dialog dialog = new Dialog(BagsActivationSetupActivityPrintRoll.this);
            dialog.setContentView(R.layout.submit_confirm_alert);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView text = dialog.findViewById(R.id.tv_message);
            text.setText("You have entred moisture value which is out of range.\nCheck the data entered before submission, once submitted it cannot be edited.\nAre you sure you want to submit?");
            String finalRemarks = remarks;
            dialog.findViewById(R.id.btnSubmit).setOnClickListener(v -> submitForm(lot, harvestDate, bags, qty, tare, gotStatus, moisture, finalRemarks));
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
            dialog.setCancelable(false);
            dialog.show();
        }


    }

    private void submitForm(String lot, String harvestDate, String bags, String qty, String tare, String gotStatus, String moisture, String remarks) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lot+"="+harvestDate+"="+bags+"="+qty+"="+tare+"="+gotStatus+"="+moisture);
        Call<ActivationSubmitResponse> call =apiInterface.submitActivationForm(userData.getMobile1(), userData.getScode(), lot, harvestDate, bags, qty, tare, gotStatus, moisture, remarks, selProdGrade);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ActivationSubmitResponse> call, @NonNull Response<ActivationSubmitResponse> response) {
                if (response.isSuccessful()) {
                    ActivationSubmitResponse activationSubmitResponse = response.body();
                    System.out.print("Response : " + activationSubmitResponse);
                    if (activationSubmitResponse != null) {
                        if (activationSubmitResponse.getStatus()) {
                            // Save activation flag
                            String activationKey = "lot_activated_" + lot;
                            SharedPreferences.getInstance(BagsActivationSetupActivityPrintRoll.this).storeObject(activationKey, "true");
                            
                            Toast.makeText(BagsActivationSetupActivityPrintRoll.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), PrintBagsLabelActivity.class);
                            intent.putExtra("lotNumber", lot);
                            intent.putExtra("harvestdate", harvestDate);
                            intent.putExtra("trid", activationSubmitResponse.getUser().getTrid());
                            intent.putExtra("bagcount", bags);
                            startActivity(intent);
                        } else {
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, activationSubmitResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, msg);
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivationSubmitResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(BagsActivationSetupActivityPrintRoll.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            LotInfoData lotInfoData = lotInfoResponse.getData().get(0);
                            selectedCropName = lotInfoData.getCropname();
                            tvCrop.setText(lotInfoData.getCropname());
                            tvSpCodef.setText(lotInfoData.getSpcodef());
                            tvSpCodem.setText(lotInfoData.getSpcodem());
                            tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                            tvHarvestDate.setText(lotInfoData.getHarvestdate());
                            etNumberOfBags.setText(lotInfoData.getBags().toString());
                            tvFarmerName.setText(lotInfoData.getFarmername());
                            tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                            getRemarksList();
                        } else {
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, lotInfoResponse.getMsg());
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, msg);
                            //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivityPrintRoll.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}