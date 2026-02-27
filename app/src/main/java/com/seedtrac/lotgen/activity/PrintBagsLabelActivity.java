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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private String action="";
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
    private String weightData, actualWeight;
    private Handler handler = new Handler();
    private static final int MAX_RETRIES = 3;
    private int currentRetryCount = 0;
    private Set<BluetoothDevice> pairedDevices;
    private static final int BT_PERMISSION_REQUEST = 101;
    private int trid,rowid;
    private TextView tvFarmerName, tvFarmerVillage;

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
                // Custom back press handling
                Intent intent = new Intent(PrintBagsLabelActivity.this, LotReceiveListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setTheme();
        init();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, BT_PERMISSION_REQUEST
                );
            } else {
                bluetoothGranted();
            }

        } else {
            // Android 11 and below
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        BT_PERMISSION_REQUEST
                );
            } else {
                bluetoothGranted();
            }
        }

        btnAdd.setOnClickListener(v -> {
            if(socket!=null){
                if (socket.isConnected()){
                    getWeighFromBlutooth();
                }else {
                    Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
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

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrintBagsLabelActivity.this, LotReceiveListActivity.class);
                startActivity(intent);
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
                storeBagCode(bagCode, actualWeight);
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
                weightData = new String(buffer, 0, bytes);
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
                            lotInfoData = lotInfoResponse.getData().get(0);
                            tvLotNumber.setText(lotInfoData.getLotno());
                            tvHarvestDate.setText(lotInfoData.getHarvestdate());
                            tvBags.setText(lotInfoData.getBags().toString());
                            tvQty.setText(lotInfoData.getQty().toString());
                            tvCrop.setText(lotInfoData.getCropname());
                            tvSpCode.setText(lotInfoData.getSpcodef() + " X " + lotInfoData.getSpcodem());
                            tvProductionPerson.setText(lotInfoData.getProductionpersonnel());
                            tvFarmerName.setText(lotInfoData.getFarmername());
                            tvFarmerVillage.setText(lotInfoData.getProductionlocation());
                            getActBarcodeList(lotInfoData.getLotno());
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

        /*if(socket!=null){
            if (socket.isConnected()){
                tv_message.setVisibility(View.GONE);
            }else {
                tv_message.setVisibility(View.VISIBLE);
            }
        }else{
            tv_message.setVisibility(View.VISIBLE);
        }*/
        String[] actualWt = weightData.split(" ");
        actualWeight = String.format("%.3f", Double.parseDouble(actualWt[0]));
        tv_weight.setText(actualWeight);

        tv_cropName.setText(lotInfoData.getCropname());
        tv_varietyName.setText(lotInfoData.getSpcodef()+"\n"+lotInfoData.getSpcodem());
        tv_harvestDate.setText(lotInfoData.getHarvestdate());
        tv_lotNo.setText(lotInfoData.getLotno());

        btnSubmit.setOnClickListener(view -> {
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
                if (response.isSuccessful()) {
                    SubmitSuccessResponse submitSuccessResponse = response.body();
                    System.out.print("Response : " + submitSuccessResponse);
                    if (submitSuccessResponse != null) {
                        if (submitSuccessResponse.getStatus()) {
                            Toast.makeText(PrintBagsLabelActivity.this, submitSuccessResponse.getMsg(), Toast.LENGTH_SHORT).show();
                            etBagCode.setText("");
                            getActBarcodeList(lotInfoData.getLotno());
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

        String printerIp = "172.168.4.11";
        int port = 9100;

        String tspl = String.format(
                "SIZE 50 mm,210 mm\n" +
                        "GAP 0,0\n" +
                        "DIRECTION 1\n" +
                        "CLS\n" +
                        "SPEED 3\n" +
                        "DENSITY 15\n" +
                        "SETENERGY 15\n" +

                        "TEXT 0,124,\"3\",0,1,1,\"------------X------------\"\n" +

                        "TEXT 130,150,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,210,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,270,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 70,330,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 20,390,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,450,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,510,\"3\",0,2,2,\"%s\"\n" +
                        "QRCODE 80,560,L,9,A,0,M2,S7,\"%s\"\n" +

                        "TEXT 40,1300,\"3\",0,1,1,\"------------------------------\"\n" +

                        "TEXT 130,1800,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,1860,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,1920,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 70,1980,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 20,2040,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,2100,\"3\",0,2,2,\"%s\"\n" +
                        "TEXT 75,2160,\"3\",0,2,2,\"%s\"\n" +
                        "QRCODE 80,2220,L,9,A,0,M2,S7,\"%s\"\n" +

                        "PRINT 1,1\n",

                // -------- First Label --------
                data.crop,
                data.variety1,
                data.variety2,
                data.date,
                data.lotNo,
                data.weight,
                data.qrcode,

                // -------- Second Label --------
                data.crop,
                data.variety1,
                data.variety2,
                data.date,
                data.lotNo,
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

                runOnUiThread(() ->
                        Toast.makeText(this, "Label Printed!", Toast.LENGTH_LONG).show()
                );
                getActBarcodeList(lotnumber);

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Print Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

}