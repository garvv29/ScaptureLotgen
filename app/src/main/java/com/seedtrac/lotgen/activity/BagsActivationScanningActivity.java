package com.seedtrac.lotgen.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.BagsAdapter;
import com.seedtrac.lotgen.parser.actbarcodelist.ActBarcodeListResponse;
import com.seedtrac.lotgen.parser.actbarcodelist.Data;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoData;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;
import com.seedtrac.lotgen.parser.gssubbinlist.GsSubBinListResponse;
import com.seedtrac.lotgen.parser.gssubbinlist.GsSubBinData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BagsActivationScanningActivity extends AppCompatActivity {
    TextView tvLotNumber, tvHarvestDate, tvBags, tvQty, tvGotStatus, tvMoisture, tvRemarks;
    Button btnAdd,btnRemove;
    RecyclerView rvBags;
    ArrayList<String> bagList;
    BagsAdapter bagsAdapter;
    EditText etBagCode;
    private TextView tvToolbarTitle, tvFarmerName, tvFarmerVillage;
    String lotNumber = "";
    private User userData;
    private String action="";
    private LotInfoData lotInfoData;
    private MaterialButton btnSubmit;
    private TextView tvCrop;
    private TextView tvSpCode;
    private TextView tvProductionPerson;
    private List<Data> barcodeList=new ArrayList<>();

    private Dialog dialog;
    private TextView tv_weight;
    private Button bt_con;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;
    BluetoothSocket socket = null;
    private BluetoothAdapter bluetoothAdapter;
    private String weightData="0.000 kg", actualWeight;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final int MAX_RETRIES = 3;
    private int currentRetryCount = 0;
    private Set<BluetoothDevice> pairedDevices;
    private static final int BT_PERMISSION_REQUEST = 101;
    private boolean isPreprinted = false; // Flag to track if it's preprinted case

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bags_activation_scanning);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Save flow stage: Currently in BagsActivationScanning
        lotNumber = getIntent().getStringExtra("lotNumber");
        if (lotNumber != null) {
            String flowStageKey = "flow_stage_" + lotNumber;
            SharedPreferences.getInstance(this).storeObject(flowStageKey, "BagsActivationScanning");
        }

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
                Intent intent = new Intent(BagsActivationScanningActivity.this, BagActivationPendingListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();
        Utils.hideLogoutButton(this);
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        tvLotNumber = findViewById(R.id.tvLotNumber);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvBags = findViewById(R.id.tvBagCount);
        tvQty = findViewById(R.id.tvTotalQty);
        tvGotStatus = findViewById(R.id.tvGOT);
        tvMoisture = findViewById(R.id.tvMoisture);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        btnSubmit = findViewById(R.id.btnSubmit);
        rvBags = findViewById(R.id.rvBarcodes);
        etBagCode = findViewById(R.id.etBarcode);

        tvCrop = findViewById(R.id.tvCrop);
        tvSpCode = findViewById(R.id.tvSpCode);
        tvProductionPerson = findViewById(R.id.tvProductionPerson);
        tvRemarks = findViewById(R.id.tvRemarks);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Bags Activation");

        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerVillage = findViewById(R.id.tvFarmerVillage);
        bt_con = findViewById(R.id.bt_con);

        // Get data from previous screen
        lotNumber = getIntent().getStringExtra("lotNumber");
        isPreprinted = getIntent().getBooleanExtra("isPreprinted", false);

        // Setup RecyclerView
        bagList = new ArrayList<>();
        bagsAdapter = new BagsAdapter(barcodeList);
        rvBags.setLayoutManager(new LinearLayoutManager(this));
        rvBags.setAdapter(bagsAdapter);
        // Add divider (vertical)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBags.getContext(), DividerItemDecoration.VERTICAL);
        rvBags.addItemDecoration(dividerItemDecoration);

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);
    }

    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    private void init(){
        getLotInfo();
        btnAdd.setOnClickListener(v -> {
            //if (socket!=null && socket.isConnected()){
                String bagCode = etBagCode.getText().toString().trim();
                action = "add";
                if (!bagCode.isEmpty()) {
                    if (bagCode.length()==8 || bagCode.length()==11 || bagCode.length()==9){
                        Dialog dialog = new Dialog(BagsActivationScanningActivity.this);
                        dialog.setContentView(R.layout.getweight_popup);
                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        EditText weight = dialog.findViewById(R.id.etweight);
                        tv_weight = dialog.findViewById(R.id.tv_weight);
                        Button btnCancel = dialog.findViewById(R.id.btnCancel);
                        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                        btnCancel.setOnClickListener(view -> dialog.cancel());

                        String[] actualWt = weightData.split(" ");
                        actualWeight = String.format("%.3f", Double.parseDouble(actualWt[0]));
                        tv_weight.setText(actualWeight);

                        btnSubmit.setOnClickListener(v1 -> {
                            dialog.cancel();
                            if (weight.getText().toString().trim().isEmpty() && actualWeight.isEmpty()){
                                Utils.showAlert(BagsActivationScanningActivity.this, "Please enter bag weight");

                                return;
                            }
                            if (weight.getText().toString().trim().isEmpty()){
                                actualWeight = tv_weight.getText().toString().trim();
                            }else {
                                actualWeight=weight.getText().toString();
                            }
                            storeBagCode(bagCode, actualWeight);
                        });
                        dialog.show();
                    }else {
                        Utils.showAlert(BagsActivationScanningActivity.this, "Invalid Bag Code");
                        //Toast.makeText(BagsActivationScanningActivity.this, "Invalid Bag Code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startScanner();
                }
            /*}else {
                Utils.showAlert(BagsActivationScanningActivity.this,"Bluetooth not connected...");
            }*/

        });

        btnRemove.setOnClickListener(v -> {
            String bagCode = etBagCode.getText().toString().trim();
            action = "remove";
            if (!bagCode.isEmpty()) {
                if (bagCode.length()==8 || bagCode.length()==11){
                    storeBagCode(bagCode,"");
                }else {
                    Utils.showAlert(BagsActivationScanningActivity.this, "Invalid Bag Code");
                }
            } else {
                startScanner();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (barcodeList.isEmpty()){
                    Utils.showAlert(BagsActivationScanningActivity.this, "Please add bags");
                    //Toast.makeText(BagsActivationScanningActivity.this, "Please add bags", Toast.LENGTH_SHORT).show();
                }/*else if (barcodeList.size()>lotInfoData.getBags()){
                    Utils.showAlert(BagsActivationScanningActivity.this, "Scanned bags are not matched with setup bags. If you want to continue with this bags, then edit setup bags and try to submit again");
                }else*/{
                    // Show Guard Sample popup instead of confirmation dialog (for preprinted case)
                    if (isPreprinted) {
                        showGuardSamplePopupForActivation();
                    } else {
                        // For non-preprinted case, show regular confirmation
                        final Dialog dialog = new Dialog(BagsActivationScanningActivity.this);
                        dialog.setContentView(R.layout.submit_confirm_alert);
                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                        Button btnCancel = dialog.findViewById(R.id.btnCancel);
                        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                        btnCancel.setOnClickListener(view -> dialog.cancel());

                        btnSubmit.setOnClickListener(v1 -> {
                            finalSubmit();
                        });
                        dialog.show();
                    }
                }
            }
        });

        bt_con.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (socket!=null && socket.isConnected()){
                    bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
                    bt_con.setBackgroundColor(getResources().getColor(R.color.light_grey));
                    bt_con.setText("Offline");
                    closeConnection(socket);
                }else {
                    establishBluetooth();
                }
            }
        });
    }

    private void bluetoothGranted() {
        Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
        // 👉 Start Bluetooth scan / connect printer here
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BT_PERMISSION_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                bluetoothGranted();
            } else {
                Toast.makeText(this,
                        "Bluetooth permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final Runnable updateTextViewRunnable = new Runnable() {
        @Override
        public void run() {
            // Check if the dialog is showing and the TextView is available
            if (dialog != null && dialog.isShowing() && tv_weight != null) {
                // Update the text inside the dialog's TextView
                if (weightData != null && !tv_weight.getText().toString().equals(weightData)){
                    runOnUiThread(new Runnable() {
                        @SuppressLint({"DefaultLocal", "DefaultLocale"})
                        @Override
                        public void run() {
                            //Your UI code here
                            String[] actualWt = weightData.split(" ");
                            actualWeight = String.format("%.3f", Double.parseDouble(actualWt[0]));
                            tv_weight.setText(String.format("%.3f", Double.parseDouble(actualWt[0])));
                        }
                    });
                }
            }
            // Repeat this runnable code block every 1 second
            handler.postDelayed(this, 1000); // 1000ms = 1 second
        }
    };

    /*@Override
    protected void onStart() {
        super.onStart();
        // Any Bluetooth setup you want to start when activity becomes visible
        establishBluetooth();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any remaining resources
        handler.removeCallbacks(updateTextViewRunnable);
        if (socket!=null && socket.isConnected()){
            closeConnection(socket);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateTextViewRunnable);
        if (socket!=null && socket.isConnected()){
            closeConnection(socket);
        }
    }

    @SuppressLint("SetTextI18n")
    private void establishBluetooth(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            // Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 or later (API level 31+)
                if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                    return;
                }
            } else {
                //For Android versions lower than 12 (Android 10 or 11)
                if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
                    //No need to request permission, it’s automatically granted at install.
                }
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //BluetoothWeightActivity.this.startActivityForResult(new Intent(BluetoothWeightActivity.this.m_Activity, DeviceListActivity.class), 1);
        }

        assert bluetoothAdapter != null;
        ArrayList<String> deviceNames = new ArrayList<>();
        final ArrayList<BluetoothDevice> devices = new ArrayList<>();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //If the device matches your weighing scale, connect to it
                deviceNames.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
            // Display devices in a dialog
            runOnUiThread(() -> {
                bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
                bt_con.setBackgroundColor(getResources().getColor(R.color.light_grey));
                bt_con.setText("Offline");
                AlertDialog.Builder builder = new AlertDialog.Builder(BagsActivationScanningActivity.this);
                builder.setTitle("Select a device to connect");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(BagsActivationScanningActivity.this, android.R.layout.select_dialog_singlechoice, deviceNames);
                builder.setAdapter(arrayAdapter, (dialog, which) -> {
                    BluetoothDevice selectedDevice = devices.get(which);
                    dialog.dismiss();
                    new Thread(() -> connectToDevice(selectedDevice)).start(); // Connect to the selected device in a background thread
                });
                builder.show();
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void connectToDevice(BluetoothDevice device) {
        try {
            // UUID is specific to your weighing scale's Bluetooth service
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 or later (API level 31+)
                if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                    return;
                }
            } else {
                //For Android versions lower than 12 (Android 10 or 11)
                if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
                    //No need to request permission, it’s automatically granted at install.
                }
            }
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tv_btname.setText(device.getName());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Android 12 or later (API level 31+)
                        if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                            return;
                        }
                    } else {
                        //For Android versions lower than 12 (Android 10 or 11)
                        if (ActivityCompat.checkSelfPermission(BagsActivationScanningActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(BagsActivationScanningActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
                            //No need to request permission, it’s automatically granted at install.
                        }
                    }
                    bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btcon, 0, 0, 0);
                    bt_con.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    bt_con.setText("Connected");
                    Toast.makeText(BagsActivationScanningActivity.this, "Connected to : "+device.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            //bt_con.setImageResource(R.drawable.ic_btcon);
            //bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btcon, 0, 0, 0);
            //Manage the connection in a separate thread
            manageConnectedSocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BluetoothConnection", "Socket connection failed: " + e.getMessage());
            //bt_con.setImageResource(R.drawable.ic_bt);
            //bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
                    bt_con.setBackgroundColor(getResources().getColor(R.color.light_grey));
                    bt_con.setText("Offline");
                }
            });

            if (e.getMessage() != null) {
                if (e.getMessage().contains("read failed")) {
                    Log.e("BluetoothConnection", "Connection timed out or device is out of range.");
                } else if (e.getMessage().contains("host is down")) {
                    Log.e("BluetoothConnection", "Bluetooth device is unreachable or powered off.");
                } else if (e.getMessage().contains("socket closed")) {
                    Log.e("BluetoothConnection", "Socket was closed unexpectedly.");
                }
                // Handle other specific exceptions if needed
                handleConnectionFailure();
            }

        }
    }

    private void handleConnectionFailure() {
        if (currentRetryCount < MAX_RETRIES) {
            currentRetryCount++;
            Log.d("Bluetooth", "Retrying connection... Attempt " + currentRetryCount);
            try {
                Thread.sleep(2000); // Wait 2 seconds before retrying
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            establishBluetooth(); // Retry connection
        } else {
            Log.e("Bluetooth", "Max retries reached. Connection failed.");
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        InputStream inputStream;
        try {
            //tn_bluetooth.setVisibility(View.INVISIBLE);
            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            if (inputStream.read(buffer)==-1){
                establishBluetooth();
            }
            while ((bytes = inputStream.read(buffer)) != -1) {
                weightData = new String(buffer, 0, bytes).trim();
                //Parse and use the weight data
                Log.d("Weight Data", weightData);
            }
        } catch (IOException e) {
            establishBluetooth();
            e.printStackTrace();
        }
    }

    private void closeConnection(BluetoothSocket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalSubmit() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        
        Log.d("SCANNING_SUBMIT", "SCode: " + userData.getScode() + ", TRId: " + (lotInfoData != null ? lotInfoData.getTrid() : "NULL"));
        
        Log.e("Params:", userData.getScode()+"="+lotInfoData.getTrid());
        Call<SubmitSuccessResponse> call =apiInterface.actSetupFinalSubmit(userData.getScode(), lotInfoData.getTrid().toString());
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Log.d("SCANNING_SUBMIT", "Activation complete");
                            Toast.makeText(BagsActivationScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                            
                            // Activation successful, proceed to MainActivity
                            Intent intent = new Intent(BagsActivationScanningActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(BagsActivationScanningActivity.this, submitSuccessResponse.getMsg());
                            progressDialog.cancel();
                        }
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationScanningActivity.this, msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                // Silent fail on network errors - don't show technical messages
                Log.e("SCANNING_ERROR", "Network Error: " + t.getClass().getSimpleName());
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
                    //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    if (result.getContents().length()==8 || result.getContents().length()==11){
                        if (action.equalsIgnoreCase("add")){
                            Dialog dialog = new Dialog(BagsActivationScanningActivity.this);
                            dialog.setContentView(R.layout.getweight_popup);
                            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            EditText weight = dialog.findViewById(R.id.etweight);
                            TextInputLayout tilweight = dialog.findViewById(R.id.tilweight);
                            tv_weight = dialog.findViewById(R.id.tv_weight);
                            Button btnCancel = dialog.findViewById(R.id.btnCancel);
                            Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

                            btnCancel.setOnClickListener(view -> dialog.cancel());

                            String[] actualWt = weightData.split(" ");
                            actualWeight = String.format("%.3f", Double.parseDouble(actualWt[0]));
                            tv_weight.setText(actualWeight);

                            btnSubmit.setOnClickListener(v1 -> {
                                if (weight.getText().toString().trim().isEmpty() && actualWeight.isEmpty()){
                                    Utils.showAlert(BagsActivationScanningActivity.this, "Please enter bag weight");
                                    dialog.cancel();
                                    return;
                                }
                                if (weight.getText().toString().trim().isEmpty()){
                                    actualWeight = tv_weight.getText().toString().trim();
                                }else {
                                    actualWeight=weight.getText().toString();
                                }
                                storeBagCode(result.getContents(), actualWeight);
                            });
                            dialog.show();
                        }else {
                            storeBagCode(result.getContents(), "");
                        }
                    }else {
                        Utils.showAlert(BagsActivationScanningActivity.this, "Invalid Bag Code");
                    }
                }
            });

    private void storeBagCode(String bagCode, String weight) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lotInfoData.getTrid()+"="+bagCode+"="+action+"=activation"+"="+weight);
        Call<SubmitSuccessResponse> call =apiInterface.storeBagCode(userData.getMobile1(), userData.getScode(), lotInfoData.getTrid().toString(), bagCode, action, "activation", weight);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(BagsActivationScanningActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            etBagCode.setText("");
                            getActBarcodeList(lotInfoData.getTrid());
                        } else {
                            Utils.showAlert(BagsActivationScanningActivity.this, submitSuccessResponse.getMsg());
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
                            Utils.showAlert(BagsActivationScanningActivity.this, msg);
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
                Utils.showAlert(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLotInfo() {
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
                            // FIX: Check if data list is not null and not empty before accessing
                            if (lotInfoResponse.getData() != null && !lotInfoResponse.getData().isEmpty()) {
                                lotInfoData = lotInfoResponse.getData().get(0);
                                tvLotNumber.setText(lotInfoData.getLotno());
                                tvHarvestDate.setText(lotInfoData.getHarvestdate());
                                tvBags.setText(lotInfoData.getBags().toString());
                                tvQty.setText(lotInfoData.getQty().toString());
                                tvGotStatus.setText(lotInfoData.getGotstatus());
                                tvMoisture.setText(lotInfoData.getMoisture());
                                tvCrop.setText(lotInfoData.getCropname());
                                tvSpCode.setText(lotInfoData.getSpcodef() + " X " + lotInfoData.getSpcodem());
                                tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                                tvRemarks.setText(lotInfoData.getRemarks());
                                tvFarmerName.setText(lotInfoData.getFarmername());
                                tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                                getActBarcodeList(lotInfoData.getTrid());
                            } else {
                                Utils.showAlert(BagsActivationScanningActivity.this, "No lot information found");
                                progressDialog.cancel();
                            }
                        } else {
                            Utils.showAlert(BagsActivationScanningActivity.this, lotInfoResponse.getMsg());
                            //Toast.makeText(BagsActivationScanningActivity.this, lotInfoResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.cancel();
                    }
                } else {
                    progressDialog.cancel();
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Utils.showAlert(BagsActivationScanningActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Utils.showAlert(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getActBarcodeList(Integer trid) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+lotInfoData.getTrid());
        Call<ActBarcodeListResponse> call =apiInterface.getActBarList(userData.getScode(), trid.toString());
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<ActBarcodeListResponse> call, @NonNull Response<ActBarcodeListResponse> response) {
                if (response.isSuccessful()) {
                    ActBarcodeListResponse actBarcodeListResponse = response.body();
                    System.out.print("Response : " + actBarcodeListResponse);
                    if (actBarcodeListResponse != null) {
                        if (actBarcodeListResponse.getStatus()) {
                            Toast.makeText(BagsActivationScanningActivity.this, actBarcodeListResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            barcodeList.clear();
                            barcodeList = actBarcodeListResponse.getData();
                            Double totalQty = 0.0;
                            for(Data barcode : barcodeList){
                                totalQty = totalQty+Double.parseDouble(barcode.getWeight());
                            }
                            tvQty.setText(totalQty.toString());
                            tvBags.setText(barcodeList.size() +"/"+lotInfoData.getBags().toString());
                            bagsAdapter = new BagsAdapter(barcodeList);
                            rvBags.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvBags.setAdapter(bagsAdapter);
                            // Add divider (vertical)
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBags.getContext(), DividerItemDecoration.VERTICAL);
                            rvBags.addItemDecoration(dividerItemDecoration);
                            bagsAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showAlert(BagsActivationScanningActivity.this, actBarcodeListResponse.getMsg());
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
                            Utils.showAlert(BagsActivationScanningActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActBarcodeListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showGuardSamplePopup() {
        final Dialog dialog = new Dialog(BagsActivationScanningActivity.this);
        dialog.setContentView(R.layout.dialog_guard_sample_popup);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RadioGroup rgGuardSample = dialog.findViewById(R.id.rgGuardSample);
        LinearLayout guardSampleDetailsContainer = dialog.findViewById(R.id.guardSampleDetailsContainer);
        
        TextInputEditText etLotNumber = dialog.findViewById(R.id.etLotNumber);
        TextInputEditText etBarcodeScan = dialog.findViewById(R.id.etBarcodeScan);
        AutoCompleteTextView dd_wh_popup = dialog.findViewById(R.id.dd_wh_popup);
        AutoCompleteTextView dd_bin_popup = dialog.findViewById(R.id.dd_bin_popup);
        RadioGroup rgFarmerHandover = dialog.findViewById(R.id.rgFarmerHandover);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btnSubmit);

        // Set lot number
        etLotNumber.setText(lotNumber);

        // Handle Guard Sample Yes/No selection
        rgGuardSample.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGuardSampleYes) {
                // Show the details container
                guardSampleDetailsContainer.setVisibility(View.VISIBLE);
            } else {
                // Hide the details container
                guardSampleDetailsContainer.setVisibility(View.GONE);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            int guardSampleId = rgGuardSample.getCheckedRadioButtonId();
            
            if (guardSampleId == -1) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please select Guard Sample Yes or No");
                return;
            }

            // Proceed regardless of Yes/No selection
            dialog.dismiss();
            showConfirmationDialogBeforeFinalSubmit();
        });

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showGuardSamplePopupForActivation() {
        final Dialog dialog = new Dialog(BagsActivationScanningActivity.this);
        dialog.setContentView(R.layout.dialog_guard_sample_popup);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        RadioGroup rgGuardSample = dialog.findViewById(R.id.rgGuardSample);
        LinearLayout guardSampleDetailsContainer = dialog.findViewById(R.id.guardSampleDetailsContainer);
        
        TextInputEditText etLotNumber = dialog.findViewById(R.id.etLotNumber);
        TextInputEditText etBarcodeScan = dialog.findViewById(R.id.etBarcodeScan);
        AutoCompleteTextView dd_wh_popup = dialog.findViewById(R.id.dd_wh_popup);
        AutoCompleteTextView dd_bin_popup = dialog.findViewById(R.id.dd_bin_popup);
        AutoCompleteTextView dd_subbin_popup = dialog.findViewById(R.id.dd_subbin_popup);
        RadioGroup rgFarmerHandover = dialog.findViewById(R.id.rgFarmerHandover);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btnSubmit);

        // Variables to store selected IDs
        final Integer[] selectedWhId = {null};
        final Integer[] selectedBinId = {null};
        final Integer[] selectedSubbinId = {null};

        // Barcode validation state
        final boolean[] isBarcodeValid = {false};

        // Debounce handler for barcode validation
        final Handler barcodeHandler = new Handler();
        final Runnable[] barcodeRunnable = {null};

        // Set lot number
        etLotNumber.setText(lotNumber);

        // Auto-validate barcode with debounce (same as PrintBagsLabelActivity)
        etBarcodeScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String barcode = s.toString().trim();
                Log.d("SCANNING_BARCODE_INPUT", "Text changed: '" + barcode + "' (length=" + barcode.length() + ")");
                
                // Reset barcode validity when user modifies input
                if (!barcode.isEmpty()) {
                    isBarcodeValid[0] = false;
                    updateSubmitButtonStateForActivation(btnSubmit, isBarcodeValid[0], selectedWhId[0], selectedBinId[0], selectedSubbinId[0]);
                }

                // Validate immediately when correct length is detected (no debounce)
                if (barcode.length() == 8 || barcode.length() == 9 || barcode.length() == 11) {
                    Log.d("SCANNING_BARCODE_INPUT", "Valid length detected! Calling validation API immediately...");
                    validateGuardSampleBarcodeForActivation(barcode, isBarcodeValid, btnSubmit, selectedWhId, selectedBinId, selectedSubbinId);
                } else if (!barcode.isEmpty()) {
                    Log.d("SCANNING_BARCODE_INPUT", "Invalid length (" + barcode.length() + "), waiting for more input");
                }
            }
        });

        // Handle Guard Sample Yes/No selection
        rgGuardSample.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGuardSampleYes) {
                // Show the details container
                guardSampleDetailsContainer.setVisibility(View.VISIBLE);
                
                // Load WH List only when Yes is selected
                List<com.seedtrac.lotgen.parser.gswhlist.GsData> whList = new ArrayList<>();
                dd_wh_popup.setOnClickListener(v -> dd_wh_popup.showDropDown());
                dd_bin_popup.setOnClickListener(v -> dd_bin_popup.showDropDown());
                dd_subbin_popup.setOnClickListener(view -> dd_subbin_popup.showDropDown());

                // Fetch WH list with ID storage
                getWhListForActivationPopup(dd_wh_popup, dd_bin_popup, whList, dialog, selectedWhId, selectedBinId, selectedSubbinId);
                
                // Update button state based on Yes validation
                updateSubmitButtonStateForActivation(btnSubmit, isBarcodeValid[0], selectedWhId[0], selectedBinId[0], selectedSubbinId[0]);
            } else {
                // Hide the details container
                guardSampleDetailsContainer.setVisibility(View.GONE);
                
                // Enable submit button for No selection (no validation needed)
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1.0f);
            }
        });

        btnCancel.setOnClickListener(v -> {
            // Cleanup debounce handler
            if (barcodeRunnable[0] != null) {
                barcodeHandler.removeCallbacks(barcodeRunnable[0]);
            }
            dialog.dismiss();
        });

        btnSubmit.setOnClickListener(v -> {
            int guardSampleId = rgGuardSample.getCheckedRadioButtonId();
            
            if (guardSampleId == -1) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please select Guard Sample Yes or No");
                return;
            }

            // If No is selected, proceed with activation without Guard Sample validation
            if (guardSampleId == R.id.rbGuardSampleNo) {
                // Cleanup debounce handler
                if (barcodeRunnable[0] != null) {
                    barcodeHandler.removeCallbacks(barcodeRunnable[0]);
                }
                dialog.dismiss();
                showConfirmationDialogBeforeFinalSubmit();
                return;
            }

            // If Yes is selected, validate all Guard Sample fields
            String barcode = etBarcodeScan.getText().toString().trim();
            String selectedWh = dd_wh_popup.getText().toString().trim();
            String selectedBin = dd_bin_popup.getText().toString().trim();
            String selectedSubbin = dd_subbin_popup.getText().toString().trim();

            if (barcode.isEmpty() || selectedWh.isEmpty() || selectedBin.isEmpty()) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please fill all required fields");
                return;
            }

            if (!isBarcodeValid[0]) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please ensure barcode is valid");
                return;
            }

            if (selectedWhId[0] == null || selectedBinId[0] == null) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please select valid Warehouse and Bin");
                return;
            }

            // Get selected farmer handover value
            int farmerHandoverId = rgFarmerHandover.getCheckedRadioButtonId();
            if (farmerHandoverId == -1) {
                Utils.showAlert(BagsActivationScanningActivity.this, "Please select Farmer Handover");
                return;
            }

            RadioButton rbFarmerHandover = dialog.findViewById(farmerHandoverId);
            String farmerHandover = rbFarmerHandover.getText().toString();

            // Cleanup debounce handler before API call
            if (barcodeRunnable[0] != null) {
                barcodeHandler.removeCallbacks(barcodeRunnable[0]);
            }

            // Call updateGuardSampleDetails API
            updateGuardSampleDetailsForActivation(barcode, lotNumber, selectedWhId[0], selectedBinId[0], selectedSubbinId[0], farmerHandover, dialog);
        });

        dialog.show();
    }

    // Helper method to update submit button state for activation popup
    @SuppressLint("SetTextI18n")
    private void updateSubmitButtonStateForActivation(MaterialButton btnSubmit, boolean isBarcodeValid, Integer whId, Integer binId, Integer subbinId) {
        boolean allFieldsValid = isBarcodeValid && whId != null && binId != null && subbinId != null;
        btnSubmit.setEnabled(allFieldsValid);
        btnSubmit.setAlpha(allFieldsValid ? 1.0f : 0.5f);
    }

    // Validate barcode with debounce for activation popup
    private void validateGuardSampleBarcodeForActivation(String barcode, boolean[] isBarcodeValid, MaterialButton btnSubmit, Integer[] selectedWhId, Integer[] selectedBinId, Integer[] selectedSubbinId) {
        Log.e("SCANNING_BARCODE", "============ VALIDATION STARTED ============");
        Log.e("SCANNING_BARCODE", "Barcode: " + barcode + " | Length: " + barcode.length());
        Log.e("SCANNING_BARCODE", "UserData: Mobile=" + (userData != null ? userData.getMobile1() : "NULL") + ", SCode=" + (userData != null ? userData.getScode() : "NULL"));
        
        if (userData == null) {
            Log.e("SCANNING_BARCODE", "ERROR: userData is NULL!");
            Toast.makeText(BagsActivationScanningActivity.this, "Error: User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        Log.e("SCANNING_BARCODE", "Calling checkGsBarcode API with mobile=" + userData.getMobile1() + ", scode=" + userData.getScode());
        
        Call<SubmitSuccessResponse> call = apiInterface.checkGsBarcode(userData.getMobile1(), userData.getScode(), barcode);
        Log.e("SCANNING_BARCODE", "API call enqueued, waiting for response...");
        call.enqueue(new Callback<SubmitSuccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitResponse = response.body();
                    if (submitResponse != null && submitResponse.getStatus()) {
                        isBarcodeValid[0] = true;
                        Log.d("SCANNING_BARCODE", "Valid: " + submitResponse.getMsg());
                        Toast.makeText(BagsActivationScanningActivity.this, "✓ Barcode valid", Toast.LENGTH_SHORT).show();
                    } else {
                        isBarcodeValid[0] = false;
                        Log.d("SCANNING_BARCODE", "Invalid: " + (submitResponse != null ? submitResponse.getMsg() : "Unknown"));
                        Toast.makeText(BagsActivationScanningActivity.this, "✗ Invalid barcode", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isBarcodeValid[0] = false;
                    Log.d("SCANNING_BARCODE", "Validation failed - HTTP " + response.code());
                }

                // Update button state based on validation result
                updateSubmitButtonStateForActivation(btnSubmit, isBarcodeValid[0], selectedWhId[0], selectedBinId[0], selectedSubbinId[0]);
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                // Silent fail on network errors during validation
                Log.d("SCANNING_BARCODE", "Network error: " + t.getClass().getSimpleName());
            }
        });
    }

    private void getWhListForActivationPopup(AutoCompleteTextView dd_wh, AutoCompleteTextView dd_bin, 
                                    List<com.seedtrac.lotgen.parser.gswhlist.GsData> whList, Dialog dialog,
                                    Integer[] selectedWhId, Integer[] selectedBinId, Integer[] selectedSubbinId) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Call<com.seedtrac.lotgen.parser.gswhlist.WhListResponse> call = apiInterface.getGsWhList(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<com.seedtrac.lotgen.parser.gswhlist.WhListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<com.seedtrac.lotgen.parser.gswhlist.WhListResponse> call, @NonNull Response<com.seedtrac.lotgen.parser.gswhlist.WhListResponse> response) {
                if (response.isSuccessful()) {
                    com.seedtrac.lotgen.parser.gswhlist.WhListResponse whListResponse = response.body();
                    if (whListResponse != null && whListResponse.getStatus()) {
                        List<com.seedtrac.lotgen.parser.gswhlist.GsData> dataList = whListResponse.getData();
                        whList.addAll(dataList);
                        List<String> whNames = new ArrayList<>();
                        for (com.seedtrac.lotgen.parser.gswhlist.GsData w : dataList) {
                        if (w.getWhname() != null) {
                            whNames.add(w.getWhname());
                        }

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BagsActivationScanningActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, whNames);
                        dd_wh.setAdapter(adapter);

                        // WH selection listener with ID storage
                        dd_wh.setOnItemClickListener((parent, view, position, id) -> {
                            com.seedtrac.lotgen.parser.gswhlist.GsData selectedWhData = dataList.get(position);
                            Integer whId = selectedWhData.getWhid();
                            selectedWhId[0] = whId;  // Store WH ID
                            selectedBinId[0] = null; // Reset BIN and SUBBIN when WH changes
                            selectedSubbinId[0] = null;
                            dd_bin.setText("");      // Clear BIN dropdown
                            AutoCompleteTextView dd_subbin_popup = dialog.findViewById(R.id.dd_subbin_popup);
                            getBinListForActivationPopup(whId, dd_bin, dd_subbin_popup, selectedBinId, selectedSubbinId);
                        });

                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Utils.showAlert(BagsActivationScanningActivity.this, "Failed to load WH list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.seedtrac.lotgen.parser.gswhlist.WhListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Utils.showAlert(BagsActivationScanningActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void getBinListForActivationPopup(Integer whId, AutoCompleteTextView dd_bin, AutoCompleteTextView dd_subbin, Integer[] selectedBinId, Integer[] selectedSubbinId) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Call<com.seedtrac.lotgen.parser.gsbinlist.BinListResponse> call = apiInterface.getGsBinList(userData.getMobile1(), userData.getScode(), whId);
        call.enqueue(new Callback<com.seedtrac.lotgen.parser.gsbinlist.BinListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<com.seedtrac.lotgen.parser.gsbinlist.BinListResponse> call, @NonNull Response<com.seedtrac.lotgen.parser.gsbinlist.BinListResponse> response) {
                if (response.isSuccessful()) {
                    com.seedtrac.lotgen.parser.gsbinlist.BinListResponse binListResponse = response.body();
                    if (binListResponse != null && binListResponse.getStatus()) {
                        List<com.seedtrac.lotgen.parser.gsbinlist.GsDatum> binList = binListResponse.getData();
                        List<String> binNames = new ArrayList<>();
                        for (com.seedtrac.lotgen.parser.gsbinlist.GsDatum b : binList) {
                        if (b.getBinname() != null) {
                            binNames.add(b.getBinname());
                        }

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BagsActivationScanningActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, binNames);
                        dd_bin.setAdapter(adapter);

                        // BIN selection listener with ID storage and SUBBIN loading
                        dd_bin.setOnItemClickListener((parent, view, position, id) -> {
                            com.seedtrac.lotgen.parser.gsbinlist.GsDatum selectedBinData = binList.get(position);
                            Integer binId = selectedBinData.getBinid();
                            selectedBinId[0] = binId;  // Store BIN ID
                            selectedSubbinId[0] = null; // Reset SUBBIN when BIN changes
                            dd_subbin.setText("");
                            getSubBinListForActivationPopup(whId, binId, dd_subbin, selectedSubbinId);
                        });
                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Utils.showAlert(BagsActivationScanningActivity.this, "Failed to load BIN list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.seedtrac.lotgen.parser.gsbinlist.BinListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Utils.showAlert(BagsActivationScanningActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void getSubBinListForActivationPopup(Integer whId, Integer binId, AutoCompleteTextView dd_subbin, Integer[] selectedSubbinId) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Call<GsSubBinListResponse> call = apiInterface.getGsSubbinList(userData.getMobile1(), userData.getScode(), whId, binId);
        call.enqueue(new Callback<GsSubBinListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<GsSubBinListResponse> call, @NonNull Response<GsSubBinListResponse> response) {
                if (response.isSuccessful()) {
                    GsSubBinListResponse subbinListResponse = response.body();
                    if (subbinListResponse != null) {
                        List<GsSubBinData> subbinList = subbinListResponse.getData();
                        if (subbinList != null && !subbinList.isEmpty()) {
                            List<String> subbinNames = new ArrayList<>();
                            for (GsSubBinData b : subbinList) {
                                if (b.getOutercontainer() != null) {
                                    subbinNames.add(b.getOutercontainer());
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(BagsActivationScanningActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, subbinNames);
                            dd_subbin.setAdapter(adapter);

                            // SUBBIN selection listener with ID storage
                            dd_subbin.setOnItemClickListener((parent, view, position, id) -> {
                                GsSubBinData selectedSubbinData = subbinList.get(position);
                                Integer subbinId = selectedSubbinData.getBinid();
                                selectedSubbinId[0] = subbinId;  // Store SUBBIN ID
                                Log.d("SCANNING_SUBBIN", "Selected SUBBIN ID: " + subbinId);
                            });
                        } else {
                            Utils.showAlert(BagsActivationScanningActivity.this, "No SubBin data available");
                        }
                    } else {
                        Utils.showAlert(BagsActivationScanningActivity.this, "Empty response from server");
                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Utils.showAlert(BagsActivationScanningActivity.this, "Failed to load Sub-Bin list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GsSubBinListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Utils.showAlert(BagsActivationScanningActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void updateGuardSampleDetailsForActivation(String qrcode, String lotno, Integer whid, Integer binid, Integer subbinid, String farmerHandover, Dialog dialog) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving guard sample details...");
        progressDialog.show();

        Log.d("SCANNING_UPDATE_GS", "QR: " + qrcode + " | Lot: " + lotno + " | WH: " + whid + " | BIN: " + binid + " | SUBBIN: " + subbinid + " | FarmerHandover: " + farmerHandover);

        Call<SubmitSuccessResponse> call = apiInterface.updateGuardSampleDetails(
            userData.getMobile1(),
            userData.getScode(),
            qrcode,
            lotno,
            String.valueOf(whid),
            String.valueOf(binid),
            String.valueOf(subbinid),
            farmerHandover
        );

        call.enqueue(new Callback<SubmitSuccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                progressDialog.cancel();
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitResponse = response.body();
                    if (submitResponse != null && submitResponse.getStatus()) {
                        Toast.makeText(BagsActivationScanningActivity.this, submitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                        Log.d("SCANNING_UPDATE_GS", "Success: " + submitResponse.getMsg());
                        
                        // Dismiss dialog and proceed with confirmation
                        dialog.dismiss();
                        showConfirmationDialogBeforeFinalSubmit();
                    } else {
                        Utils.showAlert(BagsActivationScanningActivity.this, submitResponse != null ? submitResponse.getMsg() : "Failed to save guard sample details");
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = TextStreamsKt.readText(response.errorBody().charStream());
                            try {
                                JSONObject jsonObj = new JSONObject(errorBodyStr);
                                String msg = jsonObj.optString("msg", "Server Error: " + response.code());
                                Utils.showAlert(BagsActivationScanningActivity.this, msg);
                            } catch (JSONException je) {
                                Utils.showAlert(BagsActivationScanningActivity.this, "Server Error: " + response.code());
                            }
                        } catch (Exception e) {
                            Log.e("SCANNING_UPDATE_GS", "Error parsing response: " + e.getMessage());
                        }
                    } else {
                        Utils.showAlert(BagsActivationScanningActivity.this, "Server error: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("SCANNING_UPDATE_GS", "Network error: " + t.getMessage());
                Utils.showAlert(BagsActivationScanningActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void showConfirmationDialogBeforeFinalSubmit() {
        final Dialog confirmDialog = new Dialog(BagsActivationScanningActivity.this);
        confirmDialog.setContentView(R.layout.submit_confirm_alert);
        Objects.requireNonNull(confirmDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button btnCancel = confirmDialog.findViewById(R.id.btnCancel);
        Button btnSubmit = confirmDialog.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(view -> confirmDialog.cancel());

        btnSubmit.setOnClickListener(v1 -> {
            confirmDialog.dismiss();
            finalSubmit();
        });
        confirmDialog.show();
    }



}





