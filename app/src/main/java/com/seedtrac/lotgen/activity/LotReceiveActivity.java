package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.communicator.alertCommunicator;
import com.seedtrac.lotgen.parser.actlotlist.ActLotListResponse;
import com.seedtrac.lotgen.parser.binlist.BinListResponse;
import com.seedtrac.lotgen.parser.binlist.Datum;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoData;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
import com.seedtrac.lotgen.parser.lotrecsubmit.LotRecSubmitSuccess;
import com.seedtrac.lotgen.parser.whlist.Data;
import com.seedtrac.lotgen.parser.whlist.WhListResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

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

public class LotReceiveActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

    private User userData;
    private AutoCompleteTextView actLotNumber, dd_wh, dd_bin, dd_subbin;
    private TextView tvCrop, tvSpCodef, tvSpCodem, tvProductionPerson;
    private LinearLayout ll_lotinfo;
    private EditText etHarvestDate, etNumberOfBags, etLotNumber, etFarmerID;
    private Button btnSubmit;
    private List<String> lots=new ArrayList<>();
    private List<Data> whlist=new ArrayList<>();
    private Integer whId=0;
    private List<Datum> binlist=new ArrayList<>();
    private Integer binId=0;
    private String lotnumber, harvestdate, whname, binname;
    private Integer bagcount;
    private TextView tvFarmerName, tvFarmerVillage;
    private int trid=0,rowid=0;
    private TextInputLayout til_ddLot;
    private RadioButton rbPreprintedTags, rbPrintRollTags;
    String tagType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lot_receive);
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
        Utils.hideLogoutButton(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh lot data when returning to this activity
        // Ensures latest status from server when coming back from PrintBagsLabelActivity
        if (lotnumber != null && !lotnumber.isEmpty()) {
            getLotInfo(lotnumber);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {

        actLotNumber = findViewById(R.id.actLotNumber);
        til_ddLot = findViewById(R.id.til_ddLot);
        dd_wh = findViewById(R.id.dd_wh);
        dd_bin = findViewById(R.id.dd_bin);
        dd_subbin = findViewById(R.id.dd_subbin);
        etHarvestDate = findViewById(R.id.etHarvestDate);
        etNumberOfBags = findViewById(R.id.etNumberOfBags);
        etLotNumber = findViewById(R.id.etLotNumber);
        etFarmerID = findViewById(R.id.etFarmerID);
        ll_lotinfo = findViewById(R.id.ll_lotinfo);
        tvCrop = findViewById(R.id.tvCrop);
        tvSpCodef = findViewById(R.id.tvSpCodef);
        tvSpCodem = findViewById(R.id.tvSpCodem);
        tvProductionPerson = findViewById(R.id.tvProductionPerson);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerVillage = findViewById(R.id.tvFarmerVillage);
        rbPreprintedTags = findViewById(R.id.rbPreprintedTags);
        rbPrintRollTags = findViewById(R.id.rbPrintRollTags);

        // Set title
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Lot Receive");

        lotnumber = getIntent().getStringExtra("lotNumber");
        harvestdate = getIntent().getStringExtra("harvestdate");
        bagcount = getIntent().getIntExtra("bagcount", 0);
        whname = getIntent().getStringExtra("whname");
        binname = getIntent().getStringExtra("binname");
        whId = getIntent().getIntExtra("whid", 0);
        binId = getIntent().getIntExtra("binid", 0);
        trid = getIntent().getIntExtra("trid", 0);
        rowid = getIntent().getIntExtra("rowid", 0);
        String incomingTagType = getIntent().getStringExtra("tagType");

        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
        if (lotnumber.isEmpty()){
            ll_lotinfo.setVisibility(View.GONE);
        }else {
            ll_lotinfo.setVisibility(View.VISIBLE);
            actLotNumber.setText(lotnumber);
            actLotNumber.setEnabled(false);
            actLotNumber.setClickable(false);
            etLotNumber.setText(lotnumber);
            etLotNumber.setEnabled(false);
            dd_wh.setText(whname);
            dd_bin.setText(binname);
            etHarvestDate.setText(harvestdate);
            etNumberOfBags.setText(bagcount.toString());
            if (incomingTagType != null && incomingTagType.equalsIgnoreCase("Roll")) {
                rbPrintRollTags.setChecked(true);
                rbPreprintedTags.setChecked(false);
            } else if (incomingTagType != null && incomingTagType.equalsIgnoreCase("Preprinted")) {
                rbPreprintedTags.setChecked(true);
                rbPrintRollTags.setChecked(false);
            }
            getLotInfo(lotnumber);
        }

        etFarmerID.requestFocus();
        // Set the listener
        etFarmerID.setOnEditorActionListener(this);
    }

    private void init(){
        if (lotnumber.isEmpty()){
            etHarvestDate.setOnClickListener(v -> showDatePicker());
            //getLotList();
        }

        etLotNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()){
                    String lotNumber = s.toString();
                    if (lotNumber.length() == 6) {
                        getLotInfo(lotNumber);
                    }
                }
            }
        });

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
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    // Implement the onEditorAction method
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // Handle the "Enter" or "Done" action here
            // For example, you can perform validation or submit the form
            if (!etFarmerID.getText().toString().isEmpty()) {
                String farmerID = etFarmerID.getText().toString().trim();
                getLotList(farmerID);
            }
            return true;// Consume the event
        }
        return false;// Return false if you didn't handle the event
    }

    private String getLotValue(){
        if (til_ddLot.getVisibility() == View.VISIBLE) {
            return actLotNumber.getText().toString().trim();
        } else {
            return etLotNumber.getText().toString().trim();
        }
    }

    private void validateAndSubmit() {
//        String lot = actLotNumber.getText().toString().trim();
//
//         String lot = etLotNumber.getText().toString().trim();
        String lot = getLotValue();

        String harvestDate = etHarvestDate.getText().toString().trim();
        String bags = etNumberOfBags.getText().toString().trim();
        if (rbPreprintedTags.isChecked()){
            tagType="Preprinted";
        }else if (rbPrintRollTags.isChecked()){
            tagType="Roll";
        }

        Dialog dialog = new Dialog(LotReceiveActivity.this);
        dialog.setContentView(R.layout.submit_confirm_alert);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        /*TextView text = dialog.findViewById(R.id.tv_message);
        text.setText("Check the data entered before submission, once submitted it cannot be edited.\nAre you sure you want to submit?");*/
        dialog.findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            dialog.dismiss();

//            if (lot.isEmpty() || harvestDate.isEmpty() || bags.isEmpty() || lot.length()!=6 || whId==0 || binId==0){
//                Utils.showAlert(LotReceiveActivity.this, "Please fill all the fields");
//                return;
//            }
            if (lot.isEmpty() || harvestDate.isEmpty() || bags.isEmpty() || lot.length()!=6 || whId==0 || binId==0){

                String message =
                        "Please fill all fields\n\n" +
                                "Lot : " + lot + "\n" +
                                "Harvest Date : " + harvestDate + "\n" +
                                "Bags : " + bags + "\n" +
                                "Lot Length : " + lot.length() + "\n" +
                                "WH ID : " + whId + "\n" +
                                "BIN ID : " + binId + "\n" +
                                "Tag Type : " + tagType;

                Utils.showAlert(LotReceiveActivity.this, message);
                return;
            }

            if (trid>0){
                updateLot(lot, harvestDate, bags, tagType);
            }else {
                submitForm(lot, harvestDate, bags, tagType);
            }

        });
        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void updateLot(String lot, String harvestDate, String bags, String tagType) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lot+"="+harvestDate+"="+bags+"="+whId+"="+binId+"="+trid);
        Call<LotRecSubmitSuccess> call =apiInterface.updateReceiveForm(userData.getMobile1(), userData.getScode(), lot, harvestDate, bags, String.valueOf(trid),whId.toString(), binId.toString(), rowid, tagType);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<LotRecSubmitSuccess> call, @NonNull Response<LotRecSubmitSuccess> response) {
                if (response.isSuccessful()) {
                    LotRecSubmitSuccess lotRecSubmitSuccess = response.body();
                    System.out.print("Response : " + lotRecSubmitSuccess);
                    if (lotRecSubmitSuccess != null) {
                        if (lotRecSubmitSuccess.getStatus()) {
                            Log.e("TagType Check", "updateLot - tagType: " + tagType);
                            if (tagType != null && tagType.equalsIgnoreCase("Roll")){
                                Toast.makeText(LotReceiveActivity.this, lotRecSubmitSuccess.getMsg(), Toast.LENGTH_SHORT).show();
                                // ✅ NEW FLOW: Go to BagsActivationSetupPrintRoll first for setup
                                Intent intent = new Intent(LotReceiveActivity.this, BagsActivationSetupActivityPrintRoll.class);
                                intent.putExtra("lotNumber", lot);
                                intent.putExtra("harvestdate", harvestDate);
                                intent.putExtra("bagcount", bagcount);
                                intent.putExtra("whname", whname);
                                intent.putExtra("binname", binname);
                                intent.putExtra("whid", whId);
                                intent.putExtra("binid", binId);
                                intent.putExtra("trid", trid);
                                intent.putExtra("rowid", rowid);
                                intent.putExtra("sourceActivity", "LotReceiveActivity");
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(LotReceiveActivity.this, lotRecSubmitSuccess.getMsg(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LotReceiveActivity.this, LotReceiveListActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Utils.showAlert(LotReceiveActivity.this, lotRecSubmitSuccess.getMsg());
                            //Toast.makeText(BagsActivationSetupActivity.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LotReceiveActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LotRecSubmitSuccess> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitForm(String lot, String harvestDate, String bags, String tagType) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lot+"="+harvestDate+"="+bags+"="+whId+"="+binId+"="+trid);
        Call<LotRecSubmitSuccess> call =apiInterface.submitReceiveForm(userData.getMobile1(), userData.getScode(), lot, harvestDate, bags, String.valueOf(trid),whId.toString(), binId.toString(), tagType);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<LotRecSubmitSuccess> call, @NonNull Response<LotRecSubmitSuccess> response) {
                if (response.isSuccessful()) {
                    LotRecSubmitSuccess lotRecSubmitSuccess = response.body();
                    System.out.print("Response : " + lotRecSubmitSuccess);
                    if (lotRecSubmitSuccess != null) {
                        if (lotRecSubmitSuccess.getStatus()) {
                            Log.e("TagType Check", "submitForm - tagType: " + tagType);
                            Toast.makeText(LotReceiveActivity.this, lotRecSubmitSuccess.getMsg(), Toast.LENGTH_SHORT).show();
                            if (tagType != null && tagType.equalsIgnoreCase("Roll")){
                                // ✅ NEW FLOW: Go to BagsActivationSetupPrintRoll first for setup
                                Intent intent = new Intent(LotReceiveActivity.this, BagsActivationSetupActivityPrintRoll.class);
                                intent.putExtra("lotNumber", lot);
                                intent.putExtra("harvestdate", harvestDate);
                                intent.putExtra("bagcount", bagcount);
                                intent.putExtra("whname", whname);
                                intent.putExtra("binname", binname);
                                intent.putExtra("whid", whId);
                                intent.putExtra("binid", binId);
                                intent.putExtra("trid", trid);
                                intent.putExtra("rowid", rowid);
                                intent.putExtra("sourceActivity", "LotReceiveActivity");
                                startActivity(intent);
                                finish();
                            }else {
                                Intent intent = new Intent(LotReceiveActivity.this, LotReceiveListActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Utils.showAlert(LotReceiveActivity.this, lotRecSubmitSuccess.getMsg());
                            //Toast.makeText(BagsActivationSetupActivity.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LotReceiveActivity.this,msg);
                            //Toast.makeText(BagsActivationSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LotRecSubmitSuccess> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(LotReceiveActivity.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            // ✅ FIX: Check if data list is not null and not empty before accessing
                            if (lotInfoResponse.getData() != null && !lotInfoResponse.getData().isEmpty()) {
                                LotInfoData lotInfoData = lotInfoResponse.getData().get(0);
                                ll_lotinfo.setVisibility(View.VISIBLE);
                                tvCrop.setText(lotInfoData.getCropname());
                                tvSpCodef.setText(lotInfoData.getSpcodef());
                                tvSpCodem.setText(lotInfoData.getSpcodem());
                                tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                                tvFarmerName.setText(lotInfoData.getFarmername());
                                tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                                getWhList();
                            } else {
                                ll_lotinfo.setVisibility(View.GONE);
                                Utils.showAlert(LotReceiveActivity.this, "No lot information found");
                                progressDialog.cancel();
                            }
                        } else {
                            ll_lotinfo.setVisibility(View.GONE);
                            Utils.showAlert(LotReceiveActivity.this, lotInfoResponse.getMsg());
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
                            Utils.showAlert(LotReceiveActivity.this,msg);
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
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
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
                            Toast.makeText(LotReceiveActivity.this, binListResponse.getMsg(), Toast.LENGTH_SHORT).show();
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
                            Utils.showAlert(LotReceiveActivity.this, binListResponse.getMsg());
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
                            Utils.showAlert(LotReceiveActivity.this,msg);
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
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLotList(String farmerID) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode());
        Call<ActLotListResponse> call =apiInterface.getRecLotList(userData.getMobile1(), userData.getScode(), farmerID);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ActLotListResponse> call, @NonNull Response<ActLotListResponse> response) {
                if (response.isSuccessful()) {
                    ActLotListResponse actLotListResponse = response.body();
                    System.out.print("Response : " + actLotListResponse);
                    if (actLotListResponse != null) {
                        if (actLotListResponse.getStatus()) {
                            Toast.makeText(LotReceiveActivity.this, actLotListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            lots = actLotListResponse.getData();
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, lots);
                            actLotNumber.setAdapter(adapter);
                            //Show dropdown on click (even without typing)
                            actLotNumber.setOnClickListener(v -> actLotNumber.showDropDown());
                            //getWhList();
                            if (lots.size()>1){
                                til_ddLot.setVisibility(View.VISIBLE);
                                etLotNumber.setVisibility(View.GONE);
                            }else{
                                til_ddLot.setVisibility(View.GONE);
                                etLotNumber.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Utils.showAlert(LotReceiveActivity.this, actLotListResponse.getMsg());
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
                            Utils.showAlert(LotReceiveActivity.this,msg);
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
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
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
                            Toast.makeText(LotReceiveActivity.this, whListResponse.getMsg(), Toast.LENGTH_SHORT).show();
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
                            Utils.showAlert(LotReceiveActivity.this, whListResponse.getMsg());
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
                            Utils.showAlert(LotReceiveActivity.this,msg);
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
                Utils.showAlert(LotReceiveActivity.this,"RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
}