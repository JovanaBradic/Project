package com.academy.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class RequestTimeOffActivity extends AppCompatActivity {
    TextView dateFrom, dateTo, amountDays;
    EditText amountDaysField, note;
    public String dataFrom, dataTo, noteValue;
    DatePickerDialog.OnDateSetListener dateSetListenerFrom, dateSetListenerTo;
    Button minus, plus, submitRequest;
    View parentLayout;
    SharedPreferences sharedPreferences;
    Spinner spinner;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_time_off);

        dateFrom = findViewById(R.id.dateFrom);
        dateTo = findViewById(R.id.toDate);
        amountDays = findViewById(R.id.amountDays);
        amountDaysField = findViewById(R.id.amountDaysField);
        note = findViewById(R.id.noteField);
        submitRequest = findViewById(R.id.submitRequest);
        parentLayout = findViewById(android.R.id.content);

        // back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // back button end

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        if (!isConnected){
            AlertDialog.Builder builder = new AlertDialog.Builder(RequestTimeOffActivity.this);
            builder.setMessage("Check your internet connection and try again!");
            builder.setTitle("No internet connection");
            builder.setPositiveButton("Ok", (AlertDialog.OnClickListener) (dialog, which) -> {
                submitRequest.setEnabled(false);
            });
            builder.setCancelable(false);
            AlertDialog alertDialog = builder.show();
        }else{

            noteValue = note.getText().toString();

            if (!dateFrom.getText().equals("")){
                submitRequest.setEnabled(true);
            }

            //click on submit
            submitRequest.setOnClickListener(v -> {
                requestTimeOff();
                getCategoryId();
            });
            //click on submit end


            // from date
            AtomicLong lastClickTimeFrom = new AtomicLong();
            dateFrom.setOnClickListener(v -> {
                try {
                    if (SystemClock.elapsedRealtime() - lastClickTimeFrom.get() < 1000){
                        return;
                    }
                    lastClickTimeFrom.set(SystemClock.elapsedRealtime());
                    calendarFrom();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
            dateSetListenerFrom = (view, year, month, dayOfMonth) -> {
                month = month + 1;
                dataFrom = dayOfMonth + "." + month + "." + year + ".";
                Log.i("JOVANA", "Value date from:" + dataFrom);
                dateFrom.setText(dataFrom);
                dateTo.setText(dataFrom);
                amountDaysField.setText("1");
                enabledSubmitButton();
                calculateDate();
            };
            //from date end

            // to date
            AtomicLong lastClickTimeTo = new AtomicLong();
            dateTo.setOnClickListener(v -> {
                try {
                    if (SystemClock.elapsedRealtime() - lastClickTimeTo.get() < 1000){
                        return;
                    }
                    lastClickTimeTo.set(SystemClock.elapsedRealtime());
                    calendarTo();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            dateSetListenerTo = (view, year, month, dayOfMonth) -> {
                month = month + 1;
                dataTo = dayOfMonth + "." + month + "." + year + ".";
                dateTo.setText(dataTo);
                Log.i("JOVANA", "To date:" + dataTo);
                calculateDate();
            };
            // to date end

            // drop down categories
            minus = findViewById(R.id.minus);
            plus = findViewById(R.id.plus);

            spinner = findViewById(R.id.spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String nameOfCategory = parent.getItemAtPosition(position).toString();
                    Date date = new Date();
                    DateFormat originalFormat = new SimpleDateFormat("dd.MM.yyyy");
                    if(nameOfCategory.equals("Paid Time Off - Pto")){
                        date.setTime(date.getTime() + 1000*60*60*24*14);
                        String dateString = originalFormat.format(date);
                        dateFrom.setText(dateString);
                        dateTo.setText(dateString);
                    } else{
                        date.setTime(date.getTime());
                        String dateString = originalFormat.format(date);
                        dateFrom.setText(dateString);
                        dateTo.setText(dateString);
                    }
                    if(nameOfCategory.equals("Personal Excuse") ){
                        amountHoursField(1);
                        amountDaysField.setText("1");
                    }else{
                        amountHoursField(2);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            // drop down categories end

            // amount hours
            plus.setOnClickListener(v -> {
                String value = String.valueOf(amountDaysField.getText());
                Log.i("AMOUNT", "" + value);
                String newValue = String.valueOf(Integer.parseInt(value) + 1);
                amountDaysField.setText(newValue);
            });
            minus.setOnClickListener(v -> {
                String value = String.valueOf(amountDaysField.getText());
                Log.i("AMOUNT", "" + value);
                String newValue = String.valueOf(Integer.parseInt(value) - 1);
                amountDaysField.setText(newValue);
            });
            // amount hours end
        }
    }

    private void enabledSubmitButton(){
        submitRequest.setEnabled(!dateFrom.getText().equals("") && !amountDaysField.getText().toString().equals(""));
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.i("BRADIC", "hours: " + s.toString());
            if (s.length() >= 1){
                if (Integer.parseInt(s.toString()) >= 8){
                    plus.setEnabled(false);
                    minus.setEnabled(true);
                    submitRequest.setEnabled(false);
                }else if(Integer.parseInt(s.toString()) <= 1){
                    minus.setEnabled(false);
                    plus.setEnabled(true);
                    submitRequest.setEnabled(false);
                }else{
                    minus.setEnabled(true);
                    plus.setEnabled(true);
                    submitRequest.setEnabled(true);
                }
            }else{
                submitRequest.setEnabled(false);
                minus.setEnabled(false);
                plus.setEnabled(false);
            }

        }
        @Override
        public void afterTextChanged(Editable s) {
            enabledSubmitButton();
        }
    };

    public void amountHoursField(int key){
        if(key == 1){
            dateTo.setEnabled(false);
            amountDays.setText("Amount - Hours");
            amountDaysField.setHint("Amount hours");
            amountDaysField.setEnabled(true);
            int maxLength = 1;
            amountDaysField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            amountDaysField.addTextChangedListener(textWatcher);
        }else if(key == 2){
            amountDaysField.removeTextChangedListener(textWatcher);
            dateTo.setEnabled(true);
            amountDays.setText("Amount - Days");
            amountDaysField.setHint("Amount days");
            amountDaysField.setEnabled(false);
            int maxLength = 2;
            amountDaysField.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            calculateDate();
            minus.setEnabled(false);
            plus.setEnabled(false);
        }
    }
    public void calculateDate(){
        try{
            String from = dataFrom;
            String to = dataTo;

            Date date1;
            Date date2;

            SimpleDateFormat dates = new SimpleDateFormat("dd.MM.yyyy.");

            date1 = dates.parse(from);
            date2 = dates.parse(to);

            long difference = Math.abs(date1.getTime() - date2.getTime());

            Calendar fromCalendar = Calendar.getInstance();
            Calendar toCalendar = Calendar.getInstance();
            fromCalendar.setTime(date1);
            toCalendar.setTime(date2);


            int numberOfDays = 0;
            while (fromCalendar.before(toCalendar)) {
                if ((Calendar.SATURDAY != fromCalendar.get(Calendar.DAY_OF_WEEK))
                        &&(Calendar.SUNDAY != fromCalendar.get(Calendar.DAY_OF_WEEK))) {
                    numberOfDays++;
                }
                fromCalendar.add(Calendar.DATE,1);
            }
            String dayDifference = Long.toString(numberOfDays + 1);
            amountDaysField.setText(dayDifference);

        }catch (Exception e){
            Log.i("DATE", "Exception" + e.getMessage());
        }
    }

    // back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // back button end

    public void calendarFrom() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(RequestTimeOffActivity.this,
                R.style.DatePicker, dateSetListenerFrom, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        datePickerDialog.show();

        String nameCategory = spinner.getSelectedItem().toString();
        long minDate = System.currentTimeMillis() + (1000*60*60*24)*14;
        if(nameCategory.equals("Paid Time Off - Pto")){
            datePickerDialog.getDatePicker().setMinDate(minDate);
        }else{
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }

    }
    public void calendarTo() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(RequestTimeOffActivity.this,
                R.style.DatePicker, dateSetListenerTo, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        datePickerDialog.show();

        SimpleDateFormat dates = new SimpleDateFormat("dd.MM.yyyy.");
        String valueToDate = dateTo.getText().toString();
        Date dateTo = dates.parse(valueToDate);
        Log.i("BRADIC", "date to: " + dateTo.getTime());

        String nameCategory = spinner.getSelectedItem().toString();
        switch (nameCategory) {
            case "Personal Excuse":
                datePickerDialog.cancel();
                amountDaysField.setText("1");
                break;
            case "Absence Due To Bussiness":
            case "Sick Days":
            case "Work From Home":
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                break;
            case "Paid Time Off - Pto":
                datePickerDialog.getDatePicker().setMinDate(dateTo.getTime());
                datePickerDialog.getDatePicker().setMaxDate(dateTo.getTime() + ((1000 * 60 * 60 * 24) * 14));
                datePickerDialog.show();
                break;
            case "Patron Saint Day":
                datePickerDialog.cancel();
                break;
            default:
                dateTo = dates.parse(valueToDate);
                datePickerDialog.getDatePicker().setMinDate(dateTo.getTime());
                datePickerDialog.show();
                break;
        }

    }
    public void requestTimeOff(){

        Thread thread = new Thread(new Runnable() {
            HttpURLConnection connection = null;
            int responseCode;

            @Override
            public void run() {
                try {
                    sharedPreferences = RequestTimeOffActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", "");

                    URL url = new URL("http://api-charm.delsystems.academy/v1/time-off-requests");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Basic " + token);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    @SuppressLint("SimpleDateFormat")
                    DateFormat originalFormat = new SimpleDateFormat("dd.MM.yyyy");

                    String categoryName = spinner.getSelectedItem().toString();
                    if (categoryName.equals("Personal Excuse")){
                        @SuppressLint("SimpleDateFormat")
                        DateFormat newFormatFrom = new SimpleDateFormat("yyyy-MM-dd'T'23:00:00'Z'");
                        Date dateFrom = originalFormat.parse(dataFrom);
                        String newFormatedDateFrom = newFormatFrom.format(dateFrom);

                        int amount = Integer.parseInt(amountDaysField.getText().toString());

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("date_from", newFormatedDateFrom);
                        jsonObject.put("date_to", newFormatedDateFrom);
                        jsonObject.put("amount", amount);
                        jsonObject.put("note", noteValue);
                        jsonObject.put("category_id", getCategoryId());

                        DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                        stream.writeBytes(jsonObject.toString());
                        stream.flush();
                        stream.close();

                        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                        StringBuilder response = new StringBuilder();
                        String inputStr;
                        while ((inputStr = reader.readLine()) != null)
                            response.append(inputStr);

                        responseCode = connection.getResponseCode();

                        RequestTimeOffActivity.this.runOnUiThread(() -> {
                            setResult(1);
                            finish();
                        });

                    }else {
                        @SuppressLint("SimpleDateFormat")
                        DateFormat newFormatFrom = new SimpleDateFormat("yyyy-MM-dd'T'23:00:00'Z'");
                        Date dateFrom = originalFormat.parse(dataFrom);
                        String newFormatedDateFrom = newFormatFrom.format(dateFrom);


                        @SuppressLint("SimpleDateFormat")
                        DateFormat newFormatTo = new SimpleDateFormat("yyyy-MM-dd'T'23:00:00'Z'");
                        Date dateTo = originalFormat.parse(dataTo);
                        String newFormatedDateTo = newFormatTo.format(dateTo);

                        int amount = Integer.parseInt(amountDaysField.getText().toString());

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("date_from", newFormatedDateFrom);
                        jsonObject.put("date_to", newFormatedDateTo);
                        jsonObject.put("amount", amount);
                        jsonObject.put("note", noteValue);
                        jsonObject.put("category_id", getCategoryId());
                        Log.i("REQUEST", "Json is:" + jsonObject);

                        DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
                        stream.writeBytes(jsonObject.toString());
                        stream.flush();
                        stream.close();

                        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                        StringBuilder response = new StringBuilder();
                        String inputStr;
                        while ((inputStr = reader.readLine()) != null)
                            response.append(inputStr);

                        responseCode = connection.getResponseCode();

                        RequestTimeOffActivity.this.runOnUiThread(() -> {
                            setResult(1);
                            finish();
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.i("REQUEST", "Exception:" + e.getMessage());
                }
                finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        });
        thread.start();
    }

    public int getCategoryId(){
        String name = spinner.getSelectedItem().toString();
        int categoryId = 0;
        if (name.equals("Personal Excuse")){
            categoryId = 1;
        }else if (name.equals("Absence Due To Bussiness")){
            categoryId = 2;
        }else if (name.equals("Paid Time Off - Pto")){
            categoryId = 3;
        }else if (name.equals("Sick Days")){
            categoryId = 4;
        }else if (name.equals("Work From Home")){
            categoryId = 5;
        }else if (name.equals("Patron Saint Day")){
            categoryId = 6;
        }
        return categoryId;
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent();
        setResult(200, backIntent);
        super.onBackPressed();
    }

}