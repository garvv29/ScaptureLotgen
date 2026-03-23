package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.seedtrac.lotgen.R;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.seedtrac.lotgen.parser.activationsubmit.ActivationSubmitResponse;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingSetupActivity extends AppCompatActivity {
    EditText etTrName, etVehicleNumber, etLRNumber, etDriverName, etMobile, etDispatchDate;
    AutoCompleteTextView spDestination;
    Button btnSubmit;

    private User userData;
    private AutoCompleteTextView dd_destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_setup);
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
                Intent intent = new Intent(LoadingSetupActivity.this, LoadingTransactionsListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        etTrName = findViewById(R.id.etTrName);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        etLRNumber = findViewById(R.id.etLRNumber);
        etDriverName = findViewById(R.id.etDriverName);
        etMobile = findViewById(R.id.etMobileNumber);
        etDispatchDate = findViewById(R.id.etDispatchDate);
        spDestination = findViewById(R.id.actDestination);
        dd_destination = findViewById(R.id.dd_destination);
        btnSubmit = findViewById(R.id.btnSubmit);

        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Bags Loading Setup");

        // Auto-fill current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        etDispatchDate.setText(currentDate);

        // Destination Dropdown Data
        String[] destinations = {"Deorjhal Plant", "Boriya Plant", "Hyderabad Plant"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, destinations);
        spDestination.setAdapter(adapter);

        // Show dropdown on click (even without typing)
        spDestination.setOnClickListener(v -> spDestination.showDropDown());

        List<String> palntList = new ArrayList<>();
        palntList.add("Deorjhal Plant");
        palntList.add("Boriya Plant");
        palntList.add("Hyderabad Plant");
        ArrayAdapter<String> remarksAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, palntList);
        dd_destination.setAdapter(remarksAdapter);
        dd_destination.setOnClickListener(v -> dd_destination.showDropDown());

        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
    }

    private void init(){
        btnSubmit.setOnClickListener(v -> {
            String lrNumber = etLRNumber.getText().toString().trim();
            String trName = etTrName.getText().toString().trim();
            String vehicleNumber = etVehicleNumber.getText().toString().trim();
            String driverName = etDriverName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            //String destination = spDestination.getText().toString().trim();
            String destination = dd_destination.getText().toString().trim();
            String dispatchDate = etDispatchDate.getText().toString().trim();

            if (trName.isEmpty() || vehicleNumber.isEmpty() || lrNumber.isEmpty() || driverName.isEmpty() || mobile.isEmpty() || destination.isEmpty()) {
                Utils.showAlert(LoadingSetupActivity.this, "Please fill all fields");
                return;
            }
            if (mobile.length() != 10) {
                Utils.showAlert(LoadingSetupActivity.this, "Invalid Mobile Number");
                return;
            }
            Dialog dialog = new Dialog(LoadingSetupActivity.this);
            dialog.setContentView(R.layout.submit_confirm_alert);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    submitForm(trName, vehicleNumber, lrNumber, driverName, mobile, destination, dispatchDate);
                }
            });
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
            dialog.setCancelable(false);
            dialog.show();
        });
    }

    private void submitForm(String trName, String vehicleNumber, String lrNumber, String driverName, String mobile, String destination, String dispatchDate) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+trName+"="+vehicleNumber+"="+lrNumber+"="+driverName+"="+mobile+"="+destination+"="+dispatchDate);
        Call<ActivationSubmitResponse> call =apiInterface.submitLoadingSetupForm(userData.getMobile1(), userData.getScode(), trName, vehicleNumber, lrNumber, driverName, mobile, destination, dispatchDate);
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<ActivationSubmitResponse> call, @NonNull Response<ActivationSubmitResponse> response) {
                if (response.isSuccessful()) {
                    ActivationSubmitResponse activationSubmitResponse = response.body();
                    System.out.print("Response : " + activationSubmitResponse);
                    if (activationSubmitResponse != null) {
                        if (activationSubmitResponse.getStatus()) {
                            Toast.makeText(LoadingSetupActivity.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoadingScanningActivity.class);
                            String trid = String.valueOf(activationSubmitResponse.getUser().getTrid());
                            intent.putExtra("trid", trid);
                            startActivity(intent);
                        } else {
                            Utils.showAlert(LoadingSetupActivity.this, activationSubmitResponse.getMsg());
                            //Toast.makeText(LoadingSetupActivity.this, activationSubmitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(LoadingSetupActivity.this, msg);
                            //Toast.makeText(LoadingSetupActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(LoadingSetupActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(LoadingSetupActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}