# API Workflow Guide - From API Call to Frontend Display

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Step-by-Step Workflow](#step-by-step-workflow)
3. [Key Components Explained](#key-components-explained)
4. [Code Examples](#code-examples)
5. [Data Flow Diagram](#data-flow-diagram)

---

## Architecture Overview

The Android app uses a **Retrofit + GSON + RecyclerView** pattern to fetch data from server and display it.

```
┌─────────────────┐
│   Frontend UI   │  (Activity/Fragment)
└────────┬────────┘
         │
    Displays data via
         │
┌────────▼────────┐
│  RecyclerView   │  (with Adapter)
│   + Adapter     │
└────────▲────────┘
         │
    Binds data to UI
         │
┌────────┴────────┐
│   Data Model    │  (Response class)
└────────▲────────┘
         │
   Deserialized from JSON
         │
┌────────┴────────┐
│   Retrofit      │  (HTTP Client)
│   + GSON        │
└────────▲────────┘
         │
  Makes HTTP call
         │
┌────────┴────────┐
│  Backend API    │  (Server)
└─────────────────┘
```

---

## Step-by-Step Workflow

### **Step 1: Define API Endpoint**
File: `ApiInterface.java`

```java
@GET("summeryrep.php")
Call<SpCodeWiseSummaryResponse> getSpCodeWiseSummaryReport(
    @Query("mobile1") String mobile1,
    @Query("scode") String scode,
    @Query("sdate") String sdate,
    @Query("edate") String edate
);
```

**What happens:**
- Defines an HTTP GET request to endpoint: `summeryrep.php`
- Accepts 4 query parameters (mobile1, scode, sdate, edate)
- Returns a `Call<SpCodeWiseSummaryResponse>` object
- `Call` = a request that can be executed

### **Step 2: Create Data Model (Response Class)**
File: `SpCodeWiseSummaryResponse.java`

```java
public class SpCodeWiseSummaryResponse {
    @SerializedName("status")
    private Boolean status;
    
    @SerializedName("msg")
    private String msg;
    
    @SerializedName("data")
    private List<SpCodeWiseSummaryData> data;
    
    public static class SpCodeWiseSummaryData {
        @SerializedName("crop")
        private String crop;
        
        @SerializedName("spcodef")
        private String spcodef;
        
        @SerializedName("spcodem")
        private String spcodem;
        
        @SerializedName("Bags")
        private String bags;
        
        @SerializedName("Qty")
        private Double qty;
    }
}
```

**What happens:**
- Defines structure of API response
- `@SerializedName` maps JSON keys to Java variables
- GSON uses this to convert JSON → Java objects
- Nested class for each data item in list

**Example API Response (JSON):**
```json
{
  "status": true,
  "msg": "Success",
  "data": [
    {
      "crop": "Chilli",
      "spcodef": "AN919",
      "spcodem": "AN920",
      "Bags": "2",
      "Qty": 0.00
    }
  ]
}
```

### **Step 3: Get Retrofit Client**
File: `RetrofitClient.java`

```java
public static ApiInterface getApiService() {
    return retrofit.create(ApiInterface.class);
}
```

**What happens:**
- Creates single instance of Retrofit API client
- `create()` generates implementation of ApiInterface
- Returns ready-to-use API service

### **Step 4: Make API Call**
File: `SpCodeWiseSummaryReportActivity.java`

```java
ApiInterface apiInterface = RetrofitClient.getApiService();

Call<SpCodeWiseSummaryResponse> call = 
    apiInterface.getSpCodeWiseSummaryReport(
        mobile1,      // User's mobile from SharedPreferences
        scode,        // Season code
        fromDate,     // Format: "dd-MM-yyyy"
        toDate        // Format: "dd-MM-yyyy"
    );
```

**What happens:**
- Gets API service instance
- Calls the endpoint method with parameters
- Creates a `Call` object (request prepared but NOT executed)
- Can be executed or cancelled

### **Step 5: Execute API Call with Callback**

```java
call.enqueue(new Callback<SpCodeWiseSummaryResponse>() {
    @Override
    public void onResponse(Call<SpCodeWiseSummaryResponse> call, 
                          Response<SpCodeWiseSummaryResponse> response) {
        // Called when response received (success or error status)
        if (response.isSuccessful() && response.body() != null) {
            SpCodeWiseSummaryResponse reportResponse = response.body();
            
            if (reportResponse.getStatus()) {
                // Success
                displayReportData(reportResponse.getData());
            } else {
                // API returned error message
                Utils.showAlert(SpCodeWiseSummaryReportActivity.this, 
                    reportResponse.getMsg());
            }
        } else {
            Utils.showAlert(SpCodeWiseSummaryReportActivity.this, 
                "Server error");
        }
    }

    @Override
    public void onFailure(Call<SpCodeWiseSummaryResponse> call, 
                         Throwable t) {
        // Called when network request failed
        Utils.showAlert(SpCodeWiseSummaryReportActivity.this, 
            "Network Error: " + t.getMessage());
    }
});
```

**What happens:**
1. **Async Execution**: Request runs on background thread
2. **Network Call**: Retrofit sends HTTP GET to server
3. **Response Received**: 
   - Server returns JSON data
   - Gson automatically deserializes JSON to `SpCodeWiseSummaryResponse` object
4. **onResponse() called**:
   - `response.isSuccessful()` = HTTP 200-299 status
   - `response.body()` = Deserialized Java object (SpCodeWiseSummaryResponse)
   - Check `status` field for business logic success/failure
5. **onFailure() called** (if network error or exception)

### **Step 6: Convert Response to Adapter Format**

```java
private void displayReportData(List<SpCodeWiseSummaryResponse.SpCodeWiseSummaryData> apiData) {
    List<SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData> adapterData = new ArrayList<>();
    
    for (SpCodeWiseSummaryResponse.SpCodeWiseSummaryData item : apiData) {
        SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData data = 
            new SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData();
        
        data.setCropname(item.getCrop());
        data.setSpcodef(item.getSpcodef());
        data.setSpcodem(item.getSpcodem());
        data.setBags(item.getBags());
        data.setQty(item.getQty());
        
        adapterData.add(data);
    }
    
    // Set data to adapter
    adapter.setData(adapterData);
}
```

**What happens:**
- Converts API response model → Adapter model
- Some apps use same model for both (this app keeps them separate for flexibility)
- Prepares data in format adapter expects
- No network activity here - just data transformation

### **Step 7: Update RecyclerView with Data**
File: `SpCodeWiseSummaryReportAdapter.java`

```java
public void setData(List<SpCodeWiseSummaryData> dataList) {
    this.dataList = dataList;
    notifyDataSetChanged();  // Tells RecyclerView to refresh UI
}

@Override
public void onBindViewHolder(ViewHolder holder, int position) {
    SpCodeWiseSummaryData data = dataList.get(position);
    
    holder.tvSrNo.setText(String.valueOf(position + 1));
    holder.tvCrop.setText(data.getCropname());
    holder.tvSpCode.setText(data.getSpcodef() + " X " + data.getSpcodem());
    holder.tvQty.setText(String.format("%.2f", data.getQty()));
    holder.tvBags.setText(data.getBags());
}
```

**What happens:**
1. **setData()**: Sets data list in adapter
2. **notifyDataSetChanged()**: Tells RecyclerView data changed
3. **RecyclerView refreshes**: Calls `onBindViewHolder()` for visible items
4. **onBindViewHolder()**: Binds each data item to a view row
5. **UI Updates**: Views display the data

### **Step 8: Display in UI**
File: `activity_spcode_wise_summary_report.xml`

```xml
<!-- Table Header (Blue Bar) -->
<LinearLayout
    android:background="@drawable/bg_add_button"
    android:orientation="horizontal">
    <TextView android:text="#" ... />
    <TextView android:text="Crop" ... />
    <TextView android:text="SP Code" ... />
    <TextView android:text="Qty" ... />
    <TextView android:text="Bags" ... />
</LinearLayout>

<!-- Data Rows -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rvReportData"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1" />
```

**What happens:**
- RecyclerView renders list items using `spcode_wise_summary_report_item.xml` layout
- Each row shows crop, SP code, quantity, bags
- Scrollable if data exceeds screen height

---

## Key Components Explained

### **1. Retrofit**
**What is it?** HTTP client library for Android

**Key points:**
- Simplifies network requests
- Runs on background thread (async)
- Converts Java objects ↔ JSON
- Base URL: `https://lotgen.vnrseeds.in/lgapp/`

```java
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://lotgen.vnrseeds.in/lgapp/")
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

### **2. GSON**
**What is it?** JSON serialization/deserialization library

**Key points:**
- Converts JSON string → Java object (deserialization)
- Converts Java object → JSON string (serialization)
- Uses `@SerializedName` to map JSON keys to Java fields

```java
// JSON from API
String json = "{\"status\": true, \"msg\": \"Success\", \"data\": [...]}";

// GSON converts to Java object
Gson gson = new Gson();
SpCodeWiseSummaryResponse response = gson.fromJson(json, SpCodeWiseSummaryResponse.class);
```

### **3. Callback Pattern**
**What is it?** Async pattern to handle delayed responses

**Why?** Network calls take time (1-5 seconds). Can't block UI thread.

```
User clicks "Get Report"
    ↓
Request sent to background thread
    ↓
UI remains responsive (user can interact)
    ↓
Server responds (1-5 seconds later)
    ↓
onResponse() or onFailure() called on main thread
    ↓
UI updated with data
```

### **4. RecyclerView**
**What is it?** Efficient list display widget

**Key points:**
- Recycles views = better performance
- Adapter manages data → view binding
- `ViewHolder` pattern caches view references

```
RecyclerView
├─ Adapter (manages data)
│  ├─ onBindViewHolder() - binds data to view
│  ├─ onCreateViewHolder() - creates view instance
│  └─ getItemCount() - returns list size
├─ ViewHolder (holds view references)
│  ├─ tvCrop (TextView)
│  ├─ tvQty (TextView)
│  └─ tvBags (TextView)
└─ Data List
   ├─ Item 1: {crop: "Chilli", qty: 100, bags: 2}
   ├─ Item 2: {crop: "Tomato", qty: 50, bags: 1}
   └─ Item 3: {crop: "Onion", qty: 200, bags: 4}
```

---

## Code Examples

### **Complete Flow Example: From Click to Display**

```java
// 1. User clicks "Get Report" button
btnSearch.setOnClickListener(v -> {
    String fromDate = etFromDate.getText().toString();
    String toDate = etToDate.getText().toString();
    
    // 2. Show progress
    progressDialog.show();
    
    // 3. Get API service
    ApiInterface apiInterface = RetrofitClient.getApiService();
    
    // 4. Get user data from SharedPreferences
    UserSessionManager sessionManager = UserSessionManager.getInstance();
    UserData userData = sessionManager.getUserData();
    
    // 5. Make API call
    Call<SpCodeWiseSummaryResponse> call = 
        apiInterface.getSpCodeWiseSummaryReport(
            userData.getMobile1(),
            userData.getScode(),
            fromDate,
            toDate
        );
    
    // 6. Execute async call
    call.enqueue(new Callback<SpCodeWiseSummaryResponse>() {
        @Override
        public void onResponse(Call<SpCodeWiseSummaryResponse> call,
                              Response<SpCodeWiseSummaryResponse> response) {
            // Hide progress
            progressDialog.dismiss();
            
            // 7. Check if response successful
            if (response.isSuccessful() && response.body() != null) {
                SpCodeWiseSummaryResponse reportResponse = response.body();
                
                // 8. Check business logic status
                if (reportResponse.getStatus()) {
                    // 9. Display data
                    displayReportData(reportResponse.getData());
                } else {
                    // Show error message from API
                    Utils.showAlert(SpCodeWiseSummaryReportActivity.this,
                        reportResponse.getMsg());
                }
            } else {
                Utils.showAlert(SpCodeWiseSummaryReportActivity.this,
                    "Server Error");
            }
        }
        
        @Override
        public void onFailure(Call<SpCodeWiseSummaryResponse> call,
                             Throwable t) {
            // Hide progress
            progressDialog.dismiss();
            
            // 10. Show network error
            Utils.showAlert(SpCodeWiseSummaryReportActivity.this,
                "Network Error: " + t.getMessage());
        }
    });
});

// 11. Convert and display data
private void displayReportData(List<SpCodeWiseSummaryResponse.SpCodeWiseSummaryData> data) {
    List<SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData> adapterData = new ArrayList<>();
    
    for (SpCodeWiseSummaryResponse.SpCodeWiseSummaryData item : data) {
        SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData row = new SpCodeWiseSummaryReportAdapter.SpCodeWiseSummaryData();
        row.setCropname(item.getCrop());
        row.setSpcodef(item.getSpcodef());
        row.setSpcodem(item.getSpcodem());
        row.setBags(item.getBags());
        row.setQty(item.getQty());
        adapterData.add(row);
    }
    
    // 12. Set data to adapter (triggers UI update)
    adapter.setData(adapterData);
}
```

---

## Data Flow Diagram

### **Timeline of Events**

```
TIME    THREAD          ACTION
────────────────────────────────────────────────────
T0      Main            User clicks "Get Report" button

T1      Main            Show ProgressDialog (spinning circle)
                        Prepare API call parameters

T2      Main            .enqueue() called (non-blocking)
                        Returns immediately

T3      Background      HTTP GET request sent to server
                        https://lotgen.vnrseeds.in/lgapp/summeryrep.php
                        ?mobile1=9876543210&scode=2024&sdate=17-03-2026&edate=24-03-2026

T4      Background      Waiting for server response...
                        (Network latency: 1-5 seconds)

T5      Background      Response received as JSON:
                        {
                          "status": true,
                          "msg": "Success",
                          "data": [
                            {
                              "crop": "Chilli",
                              "spcodef": "AN919",
                              "spcodem": "AN920",
                              "Bags": "2",
                              "Qty": 0.00
                            }
                          ]
                        }

T6      Background      GSON deserializes JSON → SpCodeWiseSummaryResponse object

T7      Main            onResponse() callback called on main thread
                        Access: response.body() = deserialized object

T8      Main            Check response.isSuccessful() = true
                        Check reportResponse.getStatus() = true

T9      Main            Hide ProgressDialog
                        Convert data to adapter format

T10     Main            adapter.setData(newData)
                        notifyDataSetChanged()

T11     Main/UI         RecyclerView refreshes
                        onBindViewHolder() called for each item
                        TextViews updated with values

T12     Main/UI         RecyclerView displays on screen
                        User sees the report table
```

### **Data Transformation Flow**

```
┌──────────────────────────────────────────┐
│ JSON String (from server)                │
│ {                                        │
│   "status": true,                        │
│   "msg": "Success",                      │
│   "data": [{"crop": "Chilli", ...}]    │
│ }                                        │
└────────────────┬─────────────────────────┘
                 │
                 │ GSON deserialization
                 ▼
┌──────────────────────────────────────────┐
│ Java Object: SpCodeWiseSummaryResponse   │
│ - status: true                           │
│ - msg: "Success"                         │
│ - data: List<SpCodeWiseSummaryData>     │
│   ├─ SpCodeWiseSummaryData              │
│   │  ├─ crop: "Chilli"                  │
│   │  ├─ spcodef: "AN919"                │
│   │  ├─ spcodem: "AN920"                │
│   │  ├─ bags: "2"                       │
│   │  └─ qty: 0.00                       │
└────────────────┬─────────────────────────┘
                 │
                 │ Conversion to adapter model
                 ▼
┌──────────────────────────────────────────┐
│ Adapter Data List                        │
│ List<SpCodeWiseSummaryData>              │
│ ├─ SpCodeWiseSummaryData                │
│ │  ├─ cropname: "Chilli"                │
│ │  ├─ spcodef: "AN919"                  │
│ │  ├─ spcodem: "AN920"                  │
│ │  ├─ bags: "2"                         │
│ │  └─ qty: 0.00                         │
└────────────────┬─────────────────────────┘
                 │
                 │ RecyclerView binding
                 ▼
┌──────────────────────────────────────────┐
│ UI Display - Table Row                   │
│ ┌──────────────────────────────────────┐ │
│ │ # │ Crop    │ SP Code      │ Qty │ B │ │
│ ├──────────────────────────────────────┤ │
│ │ 1 │ Chilli  │ AN919 X AN20 │0.00│ 2│ │
│ └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

---

## Error Handling Flow

```
API Call Made
    │
    ├─► Network Error
    │   └─► onFailure() called
    │       └─► Show: "Network Error: [message]"
    │
    ├─► Server Responds with HTTP 500 (Server Error)
    │   └─► response.isSuccessful() = false
    │       └─► Show: "Server Error"
    │
    ├─► Server Responds with HTTP 200 (Success)
    │   └─► response.isSuccessful() = true
    │       ├─► response.body() is null
    │       │   └─► Show: "Server Error"
    │       │
    │       └─► response.body() is not null
    │           ├─► status = false
    │           │   └─► Show: API msg (e.g., "No data found")
    │           │
    │           └─► status = true
    │               └─► displayReportData(data)
    │                   └─► Update RecyclerView
    │                       └─► User sees table
```

---

## Summary

**Simple Flow:**

1. **User Action** → Click button
2. **API Call** → Prepare request with parameters
3. **Network Request** → Send HTTP GET to server
4. **Response** → Server sends JSON data
5. **Deserialization** → GSON converts JSON to Java objects
6. **Data Conversion** → Convert to adapter format
7. **UI Update** → RecyclerView displays data
8. **User Sees** → Beautiful table on screen

**Key Points:**
- ✅ Async execution = UI stays responsive
- ✅ Callback pattern = handles delayed responses
- ✅ GSON = automatic JSON ↔ Java conversion
- ✅ RecyclerView = efficient list display
- ✅ Error handling = network + business logic errors
