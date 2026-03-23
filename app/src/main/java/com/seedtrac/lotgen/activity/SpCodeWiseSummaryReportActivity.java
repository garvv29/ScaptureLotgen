package com.seedtrac.lotgen.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seedtrac.lotgen.MainActivity;
import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.adapter.SpCodeWiseSummaryReportAdapter;
import com.seedtrac.lotgen.parser.login.User;
import com.seedtrac.lotgen.retrofit.ApiInterface;
import com.seedtrac.lotgen.retrofit.RetrofitClient;
import com.seedtrac.lotgen.sessionmanager.SharedPreferences;
import com.seedtrac.lotgen.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kotlin.io.TextStreamsKt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpCodeWiseSummaryReportActivity extends AppCompatActivity {

    private EditText etFromDate, etToDate;
    private Button btnSearch;
    private RecyclerView rvReportData;
    private SpCodeWiseSummaryReportAdapter adapter;
    private List<SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData> reportDataList;
    private User userData;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spcode_wise_summary_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.light_blue));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SpCodeWiseSummaryReportActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Utils.hideLogoutButton(this);
        setTheme();
        init();
    }

    @SuppressLint("SetTextI18n")
    private void setTheme() {
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Reports");

        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        btnSearch = findViewById(R.id.btnSearch);
        rvReportData = findViewById(R.id.rvReportData);

        calendar = Calendar.getInstance();

        // Set default dates (today)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        etFromDate.setText(sdf.format(calendar.getTime()));
        etToDate.setText(sdf.format(calendar.getTime()));

        userData = (User) SharedPreferences.getInstance(this).getObject(SharedPreferences.KEY_LOGIN_OBJ, User.class);

        // Setup RecyclerView
        reportDataList = new ArrayList<>();
        adapter = new SpCodeWiseSummaryReportAdapter(reportDataList);
        rvReportData.setLayoutManager(new LinearLayoutManager(this));
        rvReportData.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvReportData.getContext(), DividerItemDecoration.VERTICAL);
        rvReportData.addItemDecoration(dividerItemDecoration);

        // Date picker for From Date
        etFromDate.setOnClickListener(v -> showDatePickerDialog(etFromDate));

        // Date picker for To Date
        etToDate.setOnClickListener(v -> showDatePickerDialog(etToDate));

        // Search button
        btnSearch.setOnClickListener(v -> {
            String fromDate = etFromDate.getText().toString().trim();
            String toDate = etToDate.getText().toString().trim();

            if (fromDate.isEmpty() || toDate.isEmpty()) {
                Utils.showAlert(SpCodeWiseSummaryReportActivity.this, "Please select both dates");
            } else {
                fetchReport(fromDate, toDate);
            }
        });
    }

    private void init() {
        // Initial data fetch with today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String today = sdf.format(calendar.getTime());
        fetchReport(today, today);
    }

    private void showDatePickerDialog(EditText editText) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            calendar.set(selectedYear, selectedMonth, selectedDay);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            editText.setText(sdf.format(calendar.getTime()));
        }, year, month, day);

        datePickerDialog.show();
    }

    private void fetchReport(String fromDate, String toDate) {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Log.e("Params:", userData.getMobile1() + "=" + userData.getScode() + "=" + fromDate + "=" + toDate);

        // Note: Add the API method in ApiInterface
        // Call<SpCodeWiseSummaryResponse> call = apiInterface.getSpCodeWiseSummaryReport(userData.getMobile1(), userData.getScode(), fromDate, toDate);

        // For now, showing sample implementation
        // Replace with actual API call when endpoint is ready
        Toast.makeText(this, "API endpoint needs to be created in ApiInterface", Toast.LENGTH_SHORT).show();
        progressDialog.cancel();
    }

    @SuppressLint({"NotifyDataSetChanged"})
    private void displayReportData(List<SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData> data) {
        reportDataList.clear();
        reportDataList.addAll(data);
        adapter.notifyDataSetChanged();
    }
}
