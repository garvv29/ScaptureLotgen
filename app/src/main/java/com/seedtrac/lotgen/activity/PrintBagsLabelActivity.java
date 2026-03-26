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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.BagsAdapter;
import com.seedtrac.lotgen.adapter.LabelPrintingBarcodeListAdapter;
import com.seedtrac.lotgen.model.LabelData;
import com.seedtrac.lotgen.parser.actbarcodelist.ActBarcodeListResponse;
import com.seedtrac.lotgen.parser.actbarcodelist.Data;
import com.seedtrac.lotgen.parser.labelprintingbarcodelist.Datum;
import com.seedtrac.lotgen.parser.labelprintingbarcodelist.LabelPrintingBarList;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoData;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
import com.seedtrac.lotgen.parser.printlabel.PrintLabelInfo;
import com.seedtrac.lotgen.parser.printlabel.Qrarray;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
import com.seedtrac.lotgen.parser.whlist.WhListResponse;
import com.seedtrac.lotgen.parser.binlist.BinListResponse;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrintBagsLabelActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

    TextView tvLotNumber, tvHarvestDate, tvBags, tvQty;
    Button btnAdd,btnRemove;
    RecyclerView rvBags;
    ArrayList<String> bagList;
    LabelPrintingBarcodeListAdapter bagsAdapter;
    EditText etBagCode;
    private TextView tvToolbarTitle;

    private Integer whId=0,binId=0;
    private String lotnumber, harvestdate, whname, binname;
    private Integer bagcount;

    private User userData;
    private String action="add";
    private LotInfoData lotInfoData;
    private MaterialButton btnSubmit;
    private TextView tvCrop;
    private TextView tvSpCode;
    private TextView tvProductionPerson;
    private List<Datum> barcodeList=new ArrayList<>();
    private Dialog dialog;
    private TextView tv_weight;
    private Button bt_con;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;
    BluetoothSocket socket = null;
    private BluetoothAdapter bluetoothAdapter;
    private String weightData, actualWeight="0.000";
    private Handler handler = new Handler();
    private static final int MAX_RETRIES = 3;
    private int currentRetryCount = 0;
    private Set<BluetoothDevice> pairedDevices;
    private static final int BT_PERMISSION_REQUEST = 101;
    private int trid,rowid;
    private TextView tvFarmerName, tvFarmerVillage;
    private boolean labelPrinted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_print_bags_label);
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
                // Custom back press handling based on source activity
                String sourceActivity = getIntent().getStringExtra("sourceActivity");
                Intent intent;

                if ("LotReceiveActivity".equals(sourceActivity)) {
                    intent = new Intent(PrintBagsLabelActivity.this, LotReceiveActivity.class);
                } else if ("ActivationPendingListActivity".equals(sourceActivity)) {
                    intent = new Intent(PrintBagsLabelActivity.this, BagActivationPendingListActivity.class);
                } else {
                    intent = new Intent(PrintBagsLabelActivity.this, LotReceiveListActivity.class);
                }
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
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        btnSubmit = findViewById(R.id.btnSubmit);
        rvBags = findViewById(R.id.rvBarcodes);
        etBagCode = findViewById(R.id.etBarcode);
        bt_con = findViewById(R.id.bt_con);

        tvCrop = findViewById(R.id.tvCrop);
        tvSpCode = findViewById(R.id.tvSpCode);
        tvProductionPerson = findViewById(R.id.tvProductionPerson);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerVillage = findViewById(R.id.tvFarmerVillage);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Receive Lot and Print Label");

        // Get data from previous screen
        lotnumber = getIntent().getStringExtra("lotNumber");

        harvestdate = getIntent().getStringExtra("harvestdate");
        bagcount = getIntent().getIntExtra("bagcount", 0);
        whname = getIntent().getStringExtra("whname");
        binname = getIntent().getStringExtra("binname");
        whId = getIntent().getIntExtra("whid", 0);
        binId = getIntent().getIntExtra("binid", 0);
        trid = getIntent().getIntExtra("trid", 0);
        rowid = getIntent().getIntExtra("rowid", 0);

        // Setup RecyclerView
        bagList = new ArrayList<>();
        bagsAdapter = new LabelPrintingBarcodeListAdapter(barcodeList);
        rvBags.setLayoutManager(new LinearLayoutManager(this));
        rvBags.setAdapter(bagsAdapter);
        // Add divider (vertical)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBags.getContext(), DividerItemDecoration.VERTICAL);
        rvBags.addItemDecoration(dividerItemDecoration);

        userData = (User) com.seedtrac.lotgen.sessionmanager.SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);

        etBagCode.requestFocus();
        // Set the listener
        etBagCode.setOnEditorActionListener(this);
    }

    private void init(){
        getLotInfo();

        // TESTING MODE: Bluetooth permission checks disabled
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        //     // Android 12+
        //     if (ContextCompat.checkSelfPermission(this,
        //             Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
        //             ContextCompat.checkSelfPermission(this,
        //                     Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        //
        //         ActivityCompat.requestPermissions(
        //                 this,
        //                 new String[]{
        //                         Manifest.permission.BLUETOOTH_CONNECT,
        //                         Manifest.permission.BLUETOOTH_SCAN
        //                 }, BT_PERMISSION_REQUEST
        //         );
        //     } else {
        //         bluetoothGranted();
        //     }
        //
        // } else {
        //     // Android 11 and below
        //     if (ContextCompat.checkSelfPermission(this,
        //             Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //
        //         ActivityCompat.requestPermissions(
        //                 this,
        //                 new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        //                 BT_PERMISSION_REQUEST
        //         );
        //     } else {
        //         bluetoothGranted();
        //     }
        // }

        btnAdd.setOnClickListener(v -> {
            // TESTING MODE: Bypass Bluetooth connection check, directly open weight dialog
            getWeighFromBlutooth();
        });

        bt_con.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                // TESTING MODE: Show test mode status instead of Bluetooth connection
                Toast.makeText(PrintBagsLabelActivity.this, "🧪 Bluetooth bypassed - TEST MODE ACTIVE", Toast.LENGTH_SHORT).show();
                bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
                bt_con.setBackgroundColor(getResources().getColor(R.color.light_green));
                bt_con.setText("TEST MODE");
            }
            // ===== Original Bluetooth code (commented) =====
            // if (socket!=null && socket.isConnected()){
            //     bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bt, 0, 0, 0);
            //     bt_con.setBackgroundColor(getResources().getColor(R.color.light_grey));
            //     bt_con.setText("Offline");
            //     closeConnection(socket);
            // }else {
            //     establishBluetooth();
            // }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(PrintBagsLabelActivity.this, LotReceiveListActivity.class);
                startActivity(intent);*/
                if (barcodeList.isEmpty()){
                    Utils.showAlert(PrintBagsLabelActivity.this, "Please add bags");
                    //Toast.makeText(BagsActivationScanningActivity.this, "Please add bags", Toast.LENGTH_SHORT).show();
                }
                else if (barcodeList.size() != lotInfoData.getBags()){
                    Utils.showAlert(PrintBagsLabelActivity.this, 
                        "Please print and scan all bags. Currently: " + barcodeList.size() + "/" + lotInfoData.getBags());
                }
                /*else if (barcodeList.size()>lotInfoData.getBags()){
                    Utils.showAlert(BagsActivationScanningActivity.this, "Scanned bags are not matched with setup bags. If you want to continue with this bags, then edit setup bags and try to submit again");
                }*/
                else {
                    String sourceActivity = getIntent().getStringExtra("sourceActivity");
                    if ("LotReceiveActivity".equals(sourceActivity) || "BagsActivationSetupPrintRoll".equals(sourceActivity)) {
                        String flowStageKey = "flow_stage_" + lotnumber;
                        SharedPreferences.getInstance(PrintBagsLabelActivity.this).storeObject(flowStageKey, "GuardSample");
                        
                        showGuardSamplePopup();
                    } else {
                        // Regular flow - direct submit
                        finalSubmit();
                    }
                }
            }
        });
    }

    private void finalSubmit() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
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
                            Toast.makeText(PrintBagsLabelActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            String sourceActivity = getIntent().getStringExtra("sourceActivity");
                            Intent intent;
                            
                            if ("LotReceiveActivity".equals(sourceActivity)) {
                                // Print Roll flow → Go to Bags Activation Setup
                                intent = new Intent(PrintBagsLabelActivity.this, BagsActivationSetupActivityPrintRoll.class);
                                intent.putExtra("lotNumber", lotnumber);
                                intent.putExtra("sourceActivity", "PrintBagsLabelActivity");
                            } else {
                                // Regular flow → Go to Home
                                intent = new Intent(PrintBagsLabelActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            Utils.showAlert(PrintBagsLabelActivity.this, submitSuccessResponse.getMsg());
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
                            Utils.showAlert(PrintBagsLabelActivity.this, msg);
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
                Utils.showAlert(PrintBagsLabelActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implement the onEditorAction method
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            // Handle the "Enter" or "Done" action here
            // For example, you can perform validation or submit the form
            if (!etBagCode.getText().toString().isEmpty()) {
                String bagCode = etBagCode.getText().toString().trim();
                // If a label was just printed, this scan confirms the label
                if (labelPrinted) {
                    labelPrinted = false;
                    //updateButtonState();
                    storeBagCode(bagCode, actualWeight);
                    Toast.makeText(this, "Label scanned successfully!", Toast.LENGTH_SHORT).show();
                    etBagCode.setText("");
                } else {
                    storeBagCode(bagCode, actualWeight);
                    etBagCode.setText("");
                }
            }
            return true;// Consume the event
        }
        return false;// Return false if you didn't handle the event
    }

    private void bluetoothGranted() {
        Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
        // 👉 Start Bluetooth scan / connect printer here
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                if (weightData != null && !tv_weight.getText().toString().equals(weightData)){/* Lines 357-367 omitted */}
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
                if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                    return;
                }
            } else {
                //For Android versions lower than 12 (Android 10 or 11)
                if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(PrintBagsLabelActivity.this);
                builder.setTitle("Select a device to connect");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PrintBagsLabelActivity.this, android.R.layout.select_dialog_singlechoice, deviceNames);
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
                if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                    return;
                }
            } else {
                //For Android versions lower than 12 (Android 10 or 11)
                if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
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
                        if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                            return;
                        }
                    } else {
                        //For Android versions lower than 12 (Android 10 or 11)
                        if (ActivityCompat.checkSelfPermission(PrintBagsLabelActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(PrintBagsLabelActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);
                            //No need to request permission, it’s automatically granted at install.
                        }
                    }
                    bt_con.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btcon, 0, 0, 0);
                    bt_con.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    bt_con.setText("Connected");
                    Toast.makeText(PrintBagsLabelActivity.this, "Connected to : "+device.getName(), Toast.LENGTH_SHORT).show();
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

    private void getLotInfo() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lotnumber);
        Call<LotInfoResponse> call =apiInterface.getLotInfo(userData.getMobile1(), userData.getScode(), lotnumber);
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
                                tvQty.setText(String.format("%.3f", lotInfoData.getQty()));
                                tvCrop.setText(lotInfoData.getCropname());
                                tvSpCode.setText(lotInfoData.getSpcodef() + " X " + lotInfoData.getSpcodem());
                                tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                                tvFarmerName.setText(lotInfoData.getFarmername());
                                tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                                getActBarcodeList(lotInfoData.getLotno());
                            } else {
                                Utils.showAlert(PrintBagsLabelActivity.this, "No lot information found");
                                progressDialog.cancel();
                            }
                        } else {
                            Utils.showAlert(PrintBagsLabelActivity.this, lotInfoResponse.getMsg());
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
                            Utils.showAlert(PrintBagsLabelActivity.this, msg);
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
                Utils.showAlert(PrintBagsLabelActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getActBarcodeList(String lotno) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getScode()+"="+lotInfoData.getTrid());
        Call<LabelPrintingBarList> call =apiInterface.getPrintBarList(userData.getScode(), lotno);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
            @Override
            public void onResponse(@NonNull Call<LabelPrintingBarList> call, @NonNull Response<LabelPrintingBarList> response) {
                if (response.isSuccessful()) {
                    LabelPrintingBarList labelPrintingBarList = response.body();
                    System.out.print("Response : " + labelPrintingBarList);
                    if (labelPrintingBarList != null) {
                        if (labelPrintingBarList.getStatus()) {
                            Toast.makeText(PrintBagsLabelActivity.this, labelPrintingBarList.getMsg(), Toast.LENGTH_SHORT).show();
                            barcodeList.clear();
                            barcodeList = labelPrintingBarList.getData();
                            tvBags.setText(barcodeList.size() +"/"+lotInfoData.getBags().toString());
                            Double totalQty = 0.0;
                            for (Datum data : barcodeList) {
                                totalQty = totalQty+Double.parseDouble(data.getWeight());
                            }
                            tvQty.setText(String.format("%.3f", Double.parseDouble(totalQty.toString())));
                            bagsAdapter = new LabelPrintingBarcodeListAdapter(barcodeList);
                            rvBags.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            rvBags.setAdapter(bagsAdapter);
                            //Add divider (vertical)
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBags.getContext(), DividerItemDecoration.VERTICAL);
                            rvBags.addItemDecoration(dividerItemDecoration);
                            bagsAdapter.notifyDataSetChanged();

                            // Set click listener for reprint functionality
                            bagsAdapter.setOnItemClickListener((datum, position) -> showReprintDialog(datum, position + 1));

                            // Update button state based on label printing and bag count
                            updateButtonState();
                        } else {
                            Utils.showAlert(PrintBagsLabelActivity.this, labelPrintingBarList.getMsg());
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
                            Utils.showAlert(PrintBagsLabelActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LabelPrintingBarList> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(PrintBagsLabelActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void getWeighFromBlutooth() {
        dialog = new Dialog(PrintBagsLabelActivity.this);
        dialog.setContentView(R.layout.blutooth_weight_and_print_popup);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        TextView tv_cropName = dialog.findViewById(R.id.tv_cropName);
        TextView tv_varietyName = dialog.findViewById(R.id.tv_varietyName);
        TextView tv_lotNo = dialog.findViewById(R.id.tv_lotNo);
        TextView tv_harvestDate = dialog.findViewById(R.id.tv_harvestDate);
        TextView tv_message = dialog.findViewById(R.id.tv_message);
        tv_weight = dialog.findViewById(R.id.tv_weight);

        dialog.setCanceledOnTouchOutside(false);

        // ========== TESTING MODE ==========
        // Allow manual weight entry by clicking on weight field
        tv_weight.setOnClickListener(v -> {
            // Create an EditText dialog for manual weight input
            EditText etManualWeight = new EditText(PrintBagsLabelActivity.this);
            etManualWeight.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etManualWeight.setText(tv_weight.getText().toString());
            etManualWeight.setSelection(etManualWeight.getText().length());

            AlertDialog.Builder builder = new AlertDialog.Builder(PrintBagsLabelActivity.this);
            builder.setTitle("Enter Weight (kg) - TEST MODE");
            builder.setView(etManualWeight);
            builder.setPositiveButton("OK", (dialog1, which) -> {
                String weight = etManualWeight.getText().toString().trim();
                if (!weight.isEmpty()) {
                    tv_weight.setText(weight);
                } else {
                    Toast.makeText(PrintBagsLabelActivity.this, "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // Show message appropriate for testing mode
        tv_weight.setText("0.000");
        tv_message.setVisibility(View.VISIBLE);
        tv_message.setText("🧪 TEST MODE - Click weight field to enter manually");
        tv_message.setTextColor(Color.parseColor("#FF9800"));

        // =========== Original Bluetooth code (commented) ===========
        // Get weight from Bluetooth
        // if (weightData != null && !weightData.isEmpty()) {
        //     String[] actualWt = weightData.split(" ");
        //     actualWeight = String.format("%.3f", Double.parseDouble(actualWt[0]));
        //     tv_weight.setText(actualWeight);
        //     tv_message.setVisibility(View.GONE);
        // } else {
        //     tv_message.setVisibility(View.VISIBLE);
        //     tv_message.setText("Waiting for weight data...");
        // }

        tv_cropName.setText(lotInfoData.getCropname());
        tv_varietyName.setText(lotInfoData.getSpcodef()+"\n"+lotInfoData.getSpcodem());
        tv_harvestDate.setText(lotInfoData.getHarvestdate());
        tv_lotNo.setText(lotInfoData.getLotno());

        btnSubmit.setOnClickListener(view -> {
            handler.removeCallbacks(updateTextViewRunnable);
            if (socket!=null && socket.isConnected()){
                closeConnection(socket);
            }
            submitAndPrint(tv_weight.getText().toString());
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());
    }

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
                progressDialog.cancel();
                Log.e("StoreBagCode", "Response Code: " + response.code() + " | isSuccessful: " + response.isSuccessful());
                Log.e("StoreBagCode", "Response Code: " + response.code());
                Log.e("StoreBagCode", "Raw Body: " + response.raw());
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    Log.e("StoreBagCode", "Response Body: " + submitSuccessResponse);

                    if (submitSuccessResponse != null) {
                        Log.e("StoreBagCode", "Status: " + submitSuccessResponse.getStatus() + " | Msg: " + submitSuccessResponse.getMsg());

                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(PrintBagsLabelActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            //etBagCode.setText("");
                            updateButtonState();
                            getActBarcodeList(lotInfoData.getLotno());
                        } else {
                            Utils.showAlert(PrintBagsLabelActivity.this, submitSuccessResponse.getMsg());
                            Log.e("StoreBagCode", "Status is FALSE: " + submitSuccessResponse.getMsg());
                        }
                    } else {
                        Log.e("StoreBagCode", "Response body is NULL!");
                        Utils.showAlert(PrintBagsLabelActivity.this, "Response body is null");
                    }
                } else {
                    Log.e("StoreBagCode", "Response NOT successful!");
                    if (response.errorBody() != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(TextStreamsKt.readText(response.errorBody().charStream()));
                            String msg = jsonObj.getString("msg");
                            Log.e("StoreBagCode", "Error Body: " + msg);
                            Utils.showAlert(PrintBagsLabelActivity.this, msg);
                        } catch (JSONException e) {
                            Log.e("StoreBagCode", "JSONException: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Log.e("StoreBagCode", "Throwable Full: ", t);
                Utils.showAlert(PrintBagsLabelActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitAndPrint(String weight) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Log.e("Params:", userData.getMobile1()+"="+userData.getScode()+"="+lotnumber);
        Call<PrintLabelInfo> call =apiInterface.getLabelPrintData(userData.getMobile1(), userData.getScode(), lotnumber, weight);
        call.enqueue(new Callback<>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<PrintLabelInfo> call, @NonNull Response<PrintLabelInfo> response) {
                if (response.isSuccessful()) {
                    PrintLabelInfo printLabelInfo = response.body();
                    System.out.print("Response : " + printLabelInfo);
                    if (printLabelInfo != null) {
                        if (printLabelInfo.getStatus()) {
                            List<Qrarray> qrcodeData = printLabelInfo.getQrarray();
                            LabelData labelData = new LabelData(
                                    qrcodeData.get(0).getCrop(),
                                    qrcodeData.get(0).getSpcodef(),
                                    qrcodeData.get(0).getSpcodem(),
                                    weight,
                                    qrcodeData.get(0).getHarvestdate(),
                                    qrcodeData.get(0).getLotno(),
                                    qrcodeData.get(0).getNob(),
                                    qrcodeData.get(0).getQrcode()
                            );

                            printTSCLabelWifi(labelData);

                        } else {
                            Utils.showAlert(PrintBagsLabelActivity.this, printLabelInfo.getMsg());
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
                            Utils.showAlert(PrintBagsLabelActivity.this, msg);
                            //Toast.makeText(BagsActivationScanningActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrintLabelInfo> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Log.e("Error", "RetrofitError : " + t.getMessage());
                Utils.showAlert(PrintBagsLabelActivity.this, "RetrofitError : " + t.getMessage());
                //Toast.makeText(BagsActivationScanningActivity.this, "RetrofitError : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void printTSCLabelWifi(LabelData data) {

        String printerIp = "172.168.7.24";
        int port = 9100;

        String tspl = String.format(
                "SIZE 50 mm,330 mm\n" +
                        "GAP 0,0\n" +
                        "DIRECTION 1\n" +
                        "CLS\n" +
                        "SPEED 3\n" +
                        "DENSITY 15\n" +
                        "SETENERGY 11\n" +

                        "TEXT 50,124,\"3\",0,1,1,\"------------X------------\"\n" +

                        // First Section (gap 60)
                        "TEXT 200,180,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 120,240,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 120,300,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 20,360,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 100,420,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 150,480,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 130,540,\"3\",0,2,2,\"%s\"\n" +
                        "QRCODE 80,600,L,9,A,0,M2,S5,\"%s\"\n" +

                        // Divider
                        "TEXT 20,1480,\"3\",0,1,1,\"------------------------------\"\n" +

                        // Second Section
                        "TEXT 200,2040,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 120,2090,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 120,2140,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 20,2190,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 100,2240,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 150,2300,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 130,2360,\"3\",0,2,2,\"%s\"\n" +
                        "QRCODE 80,2420,L,9,A,0,M2,S5,\"%s\"\n" +

                        "PRINT 1,1\n",
//                "SIZE 50 mm,330 mm\n" +
//                        "GAP 0,0\n" +
//                        "DIRECTION 1\n" +
//                        "CLS\n" +
//                        "SPEED 3\n" +
//                        "DENSITY 15\n" +
//                        "SETENERGY 11\n" +
//
//                        "TEXT 90,124,\"3\",0,1,1,\"------------X------------\"\n" +
//
//                        "TEXT 240,180,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 160,270,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 160,360,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 40,450,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 140,540,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 190,630,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 170,720,\"3\",0,3,3,\"%s\"\n" +
//                        "QRCODE 150,810,L,13,A,0,M2,S7,\"%s\"\n" +
//
//                        "TEXT 40,1950,\"3\",0,1,1,\"------------------------------\"\n" +
//
//                        "TEXT 240,2920,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 160,3010,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 160,3100,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 40,3190,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 140,3280,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 190,3370,\"3\",0,3,3,\"%s\"\n" +
//                        "TEXT 170,3460,\"3\",0,3,3,\"%s\"\n" +
//                        "QRCODE 150,3550,L,13,A,0,M2,S7,\"%s\"\n" +
//                        "PRINT 1,1\n",

                // -------- First Label --------
                data.crop,
                data.variety1,
                data.variety2,
                data.date,
                data.lotNo,
                data.Nob,
                data.weight,
                data.qrcode,

                // -------- Second Label --------
                data.crop,
                data.variety1,
                data.variety2,
                data.date,
                data.lotNo,
                data.Nob,
                data.weight,
                data.qrcode
        );

        sendToPrinter(tspl, printerIp, port);
    }

    private void sendToPrinter(String tspl, String ip, int port) {

        new Thread(() -> {
            try (Socket socket = new Socket(ip, port);
                 OutputStream outputStream = socket.getOutputStream()) {

                outputStream.write(tspl.getBytes("UTF-8"));
                outputStream.flush();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Label Printed! Please scan the printed label to continue.", Toast.LENGTH_LONG).show();
                    labelPrinted = true;
                    updateButtonState();
                    getActBarcodeList(lotnumber);
                    etBagCode.requestFocus();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Print Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                Log.e("PrintError","Error"+e.getMessage());
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void showGuardSamplePopup() {
        final Dialog dialog = new Dialog(PrintBagsLabelActivity.this);
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
        etLotNumber.setText(lotnumber);

        // Auto-validate barcode when scanned (same as BagsActivationScanningActivity)
        etBarcodeScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String barcode = s.toString().trim();
                // Auto-validate when barcode length is 8, 11, or 9 (same as BagsActivationScanningActivity)
                if (barcode.length() == 8 || barcode.length() == 11 || barcode.length() == 9) {
                    validateGuardSampleBarcode(barcode, dialog);
                }
            }
        });

        // Handle Guard Sample Yes/No selection
        rgGuardSample.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbGuardSampleYes) {
                // Show the details container
                guardSampleDetailsContainer.setVisibility(View.VISIBLE);
                
                // Load WH List only when Yes is selected
                List<com.seedtrac.lotgen.parser.whlist.Data> whList = new ArrayList<>();
                dd_wh_popup.setOnClickListener(v -> dd_wh_popup.showDropDown());
                dd_bin_popup.setOnClickListener(v -> dd_bin_popup.showDropDown());

                // Fetch WH list
                getWhListForPopup(dd_wh_popup, dd_bin_popup, whList, dialog);
            } else {
                // Hide the details container
                guardSampleDetailsContainer.setVisibility(View.GONE);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            int guardSampleId = rgGuardSample.getCheckedRadioButtonId();
            
            if (guardSampleId == -1) {
                Utils.showAlert(PrintBagsLabelActivity.this, "Please select Guard Sample Yes or No");
                return;
            }

            // If No is selected, just proceed without validation
            if (guardSampleId == R.id.rbGuardSampleNo) {
                // Proceed to print labels without Guard Sample details
                submitGuardSampleAndPrint(lotnumber, "", "No", dialog);
                return;
            }

            // If Yes is selected, validate all fields
            String barcode = etBarcodeScan.getText().toString().trim();
            String selectedWh = dd_wh_popup.getText().toString().trim();
            String selectedBin = dd_bin_popup.getText().toString().trim();

            // Get selected value from farmer handover radio group
            int farmerHandoverId = rgFarmerHandover.getCheckedRadioButtonId();

            if (barcode.isEmpty() || selectedWh.isEmpty() || selectedBin.isEmpty()) {
                Utils.showAlert(PrintBagsLabelActivity.this, "Please fill all fields");
                return;
            }

            if (farmerHandoverId == -1) {
                Utils.showAlert(PrintBagsLabelActivity.this, "Please select Farmer Handover");
                return;
            }

            // Get radio button text
            RadioButton rbFarmerHandover = dialog.findViewById(farmerHandoverId);
            String farmerHandover = rbFarmerHandover.getText().toString();

            // Submit guard sample and then submit print label
            submitGuardSampleAndPrint(lotnumber, barcode, farmerHandover, dialog);
        });

        dialog.show();
    }

    // New method: Validate barcode automatically when scanned
    private void validateGuardSampleBarcode(String barcode, Dialog dialog) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        
        Log.d("POPUP_VALIDATE", "Barcode: " + barcode + " (len=" + barcode.length() + ")");
        
        Call<SubmitSuccessResponse> call = apiInterface.checkGsBarcode(userData.getMobile1(), userData.getScode(), barcode);
        call.enqueue(new Callback<SubmitSuccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitResponse = response.body();
                    if (submitResponse != null && submitResponse.getStatus()) {
                        Log.d("POPUP_VALIDATE", "Barcode Valid");
                        Toast.makeText(PrintBagsLabelActivity.this, "Barcode validated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("POPUP_VALIDATE", "Barcode Invalid: " + submitResponse.getMsg());
                        Toast.makeText(PrintBagsLabelActivity.this, "Invalid barcode: " + submitResponse.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("POPUP_VALIDATE", "HTTP Error: " + response.code());
                    Toast.makeText(PrintBagsLabelActivity.this, "Barcode validation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                // Silently fail on network errors (barcode still being entered)
                // Don't show error toast for real-time validation
                Log.d("BarcodeValidation", "Validation check error: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            }
        });
    }

    private void getWhListForPopup(AutoCompleteTextView dd_wh, AutoCompleteTextView dd_bin, 
                                    List<com.seedtrac.lotgen.parser.whlist.Data> whList, Dialog dialog) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Call<WhListResponse> call = apiInterface.getWhList(userData.getMobile1(), userData.getScode());
        call.enqueue(new Callback<WhListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<WhListResponse> call, @NonNull Response<WhListResponse> response) {
                if (response.isSuccessful()) {
                    WhListResponse whListResponse = response.body();
                    if (whListResponse != null && whListResponse.getStatus()) {
                        List<com.seedtrac.lotgen.parser.whlist.Data> dataList = whListResponse.getData();
                        whList.addAll(dataList);
                        List<String> whNames = new ArrayList<>();
                        for (com.seedtrac.lotgen.parser.whlist.Data w : dataList) {
                            whNames.add(w.getWhname());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(PrintBagsLabelActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, whNames);
                        dd_wh.setAdapter(adapter);

                        // WH selection listener
                        dd_wh.setOnItemClickListener((parent, view, position, id) -> {
                            com.seedtrac.lotgen.parser.whlist.Data selectedWhData = dataList.get(position);
                            Integer whId = selectedWhData.getWhid();
                            getBinListForPopup(whId, dd_bin);
                        });
                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Utils.showAlert(PrintBagsLabelActivity.this, "Failed to load WH list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<WhListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Utils.showAlert(PrintBagsLabelActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void getBinListForPopup(Integer whId, AutoCompleteTextView dd_bin) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Call<BinListResponse> call = apiInterface.getBinList(userData.getMobile1(), userData.getScode(), whId);
        call.enqueue(new Callback<BinListResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<BinListResponse> call, @NonNull Response<BinListResponse> response) {
                if (response.isSuccessful()) {
                    BinListResponse binListResponse = response.body();
                    if (binListResponse != null && binListResponse.getStatus()) {
                        List<com.seedtrac.lotgen.parser.binlist.Datum> binList = binListResponse.getData();
                        List<String> binNames = new ArrayList<>();
                        for (com.seedtrac.lotgen.parser.binlist.Datum b : binList) {
                            binNames.add(b.getBinname());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(PrintBagsLabelActivity.this, 
                            android.R.layout.simple_dropdown_item_1line, binNames);
                        dd_bin.setAdapter(adapter);
                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Utils.showAlert(PrintBagsLabelActivity.this, "Failed to load BIN list");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BinListResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                Utils.showAlert(PrintBagsLabelActivity.this, "Error: " + t.getMessage());
            }
        });
    }

    private void submitGuardSampleAndPrint(String lot, String barcode, String farmerHandover, Dialog dialog) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        // LOG ALL PARAMS - POPUP SUBMISSION
        Log.e("POPUP_SUBMIT", "===== GUARD SAMPLE POPUP SUBMISSION =====");
        Log.e("POPUP_SUBMIT", "Lot Number: " + lot);
        Log.e("POPUP_SUBMIT", "Barcode: " + barcode);
        Log.e("POPUP_SUBMIT", "Farmer Handover: " + farmerHandover);
        Log.e("POPUP_SUBMIT", "Mobile: " + userData.getMobile1());
        Log.e("POPUP_SUBMIT", "SCode: " + userData.getScode());
        Log.e("POPUP_SUBMIT", "Calling API: checkGsBarcode (gsbarrcodechk.php)");
        Log.e("POPUP_SUBMIT", "API Params: mobile1=" + userData.getMobile1() + ", scode=" + userData.getScode() + ", qrcode=" + barcode);
        Log.e("POPUP_SUBMIT", "=====================================");

        Call<SubmitSuccessResponse> call = apiInterface.checkGsBarcode(userData.getMobile1(), userData.getScode(), barcode);
        call.enqueue(new Callback<SubmitSuccessResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitSuccessResponse> call, @NonNull Response<SubmitSuccessResponse> response) {
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitResponse = response.body();
                    if (submitResponse != null && submitResponse.getStatus()) {
                        dialog.dismiss();
                        progressDialog.cancel();
                        Log.d("POPUP_SUBMIT", "Barcode validated");
                        
                        String sourceActivity = getIntent().getStringExtra("sourceActivity");
                        if ("BagsActivationSetupPrintRoll".equals(sourceActivity)) {
                            // Print Roll flow - go to Scanning
                            Log.d("POPUP_ROUTING", "To BagsActivationScanningActivity");
                            Intent intent = new Intent(PrintBagsLabelActivity.this, BagsActivationScanningActivity.class);
                            intent.putExtra("lotNumber", lotnumber);
                            intent.putExtra("isPreprinted", false);
                            startActivity(intent);
                            finish();
                        } else if ("LotReceiveActivity".equals(sourceActivity)) {
                            // Legacy flow - finalSubmit
                            Log.d("POPUP_ROUTING", "Calling finalSubmit");
                            finalSubmit();
                        }
                    } else {
                        progressDialog.cancel();
                        Utils.showAlert(PrintBagsLabelActivity.this, submitResponse.getMsg());
                    }
                } else {
                    progressDialog.cancel();
                    Log.e("POPUP_RESPONSE", "HTTP Error - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitSuccessResponse> call, @NonNull Throwable t) {
                progressDialog.cancel();
                // Silent fail on network errors - don't show technical messages
                Log.e("POPUP_ERROR", "Network Error: " + t.getClass().getSimpleName());
            }
        });
    }

    private void updateButtonState() {
        if (labelPrinted) {
            // Disable print button if a label has been printed but not scanned
            btnAdd.setEnabled(false);
            btnAdd.setAlpha(0.5f);
        } else if (barcodeList.size() >= lotInfoData.getBags()) {
            // Disable if all bags are printed
            btnAdd.setEnabled(false);
            btnAdd.setAlpha(0.5f);
        } else {
            // Enable if there are more bags to print
            btnAdd.setEnabled(true);
            btnAdd.setAlpha(1.0f);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showReprintDialog(Datum datum, int nob) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrintBagsLabelActivity.this);
        builder.setTitle("Reprint Label - Bag #" + nob);

        StringBuilder details = new StringBuilder();
        details.append("Crop: ").append(lotInfoData.getCropname()).append("\n");
        details.append("Variety 1: ").append(lotInfoData.getSpcodef()).append("\n");
        details.append("Variety 2: ").append(lotInfoData.getSpcodem()).append("\n");
        details.append("Harvest Date: ").append(lotInfoData.getHarvestdate()).append("\n");
        details.append("Lot No: ").append(lotInfoData.getLotno()).append("\n");
        details.append("Bag No: ").append(nob).append("\n");
        details.append("Weight: ").append(String.format("%.3f", Double.parseDouble(datum.getWeight()))).append("\n");
        details.append("QR Code: ").append(datum.getQrcode()).append("\n");

        builder.setMessage(details.toString());

        builder.setPositiveButton("Reprint", (dialog, which) -> {
            reprintLabel(datum, nob);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    @SuppressLint("DefaultLocale")
    private void reprintLabel(Datum datum, int nob) {
        try {
            String weight = String.format("%.3f", Double.parseDouble(datum.getWeight()));
            // Get first 2 letters of crop name in capital
            String cropNameShort = lotInfoData.getCropname().substring(0, Math.min(2, lotInfoData.getCropname().length())).toUpperCase();
            // Format Nob as "position/total bags"
            String nobFormatted = nob + "/" + lotInfoData.getBags();

            LabelData labelData = new LabelData(
                    lotInfoData.getCropcode(),
                    lotInfoData.getSpcodef(),
                    lotInfoData.getSpcodem(),
                    weight,
                    lotInfoData.getHarvestdate(),
                    lotInfoData.getLotno(),
                    nobFormatted,
                    datum.getQrcode()
            );

            printTSCLabelWifi(labelData);
            Toast.makeText(this, "Reprinting label for bag #" + nob, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ReprintError", "Error reprinting label: " + e.getMessage());
            Toast.makeText(this, "Error reprinting label: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}