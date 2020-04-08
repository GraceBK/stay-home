package com.grace.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetProfileActivity extends AppCompatActivity {

    EditText firstname;
    EditText lastname;
    TextView birthday;
    EditText lieunaissance;
    EditText address;
    EditText town;
    EditText zipcode;
    Button btnBirthday;
    Button btnSave;
    private SimpleDateFormat dateFormat;
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        getSupportActionBar().setElevation(0);


        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        initViewById();
        setDataField();
        actionView();
    }

    private void actionView() {

        btnBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDataInPreference();
            }
        });
    }

    private boolean isValideDate(String date) {
        String regex = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    private void saveDataInPreference() {

        if (TextUtils.isEmpty(firstname.getText())) {
            firstname.setHint("First name is required!");
            firstname.setError("First name is required!");
        }

        if (TextUtils.isEmpty(lastname.getText())) {
            lastname.setHint("First name is required!");
            lastname.setError("First name is required!");
        }

        if (!isValideDate(birthday.getText().toString())) {
            birthday.setTextColor(getResources().getColor(R.color.red));
            birthday.setText("Birthday is required!");
        }

        if (TextUtils.isEmpty(lieunaissance.getText())) {
            lieunaissance.setHint("First name is required!");
            lieunaissance.setError("First name is required!");
        }

        if (TextUtils.isEmpty(address.getText())) {
            address.setHint("First name is required!");
            address.setError("First name is required!");
        }

        if (TextUtils.isEmpty(town.getText())) {
            town.setHint("First name is required!");
            town.setError("First name is required!");
        }

        if (TextUtils.isEmpty(zipcode.getText())) {
            zipcode.setHint("First name is required!");
            zipcode.setError("First name is required!");
        }

        if (!TextUtils.isEmpty(firstname.getText()) && !TextUtils.isEmpty(lastname.getText())
                && !TextUtils.isEmpty(lieunaissance.getText()) && !TextUtils.isEmpty(address.getText())
                && !TextUtils.isEmpty(town.getText()) && !TextUtils.isEmpty(zipcode.getText())
                && isValideDate(birthday.getText().toString())) {
            Toast.makeText(this, "Enregistre avec succes", Toast.LENGTH_LONG).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_key_firstname), firstname.getText().toString());
            editor.putString(getString(R.string.pref_key_lastname), lastname.getText().toString());
            editor.putString(getString(R.string.pref_key_birthday), birthday.getText().toString());
            editor.putString(getString(R.string.pref_key_lieunaissance), lieunaissance.getText().toString());
            editor.putString(getString(R.string.pref_key_address), address.getText().toString());
            editor.putString(getString(R.string.pref_key_town), town.getText().toString());
            editor.putString(getString(R.string.pref_key_zipcode), zipcode.getText().toString());
            editor.apply();
        } else {
            Toast.makeText(this, "Remplir tout les champs", Toast.LENGTH_LONG).show();
        }
    }

    private void setDataField() {

        String _firstname = sharedPreferences.getString(getString(R.string.pref_key_firstname), "");
        String _lastname = sharedPreferences.getString(getString(R.string.pref_key_lastname), "");
        String _birthday = sharedPreferences.getString(getString(R.string.pref_key_birthday), "");
        String _lieunaissance = sharedPreferences.getString(getString(R.string.pref_key_lieunaissance), "");
        String _address = sharedPreferences.getString(getString(R.string.pref_key_address), "");
        String _town = sharedPreferences.getString(getString(R.string.pref_key_town), "");
        String _zipcode = sharedPreferences.getString(getString(R.string.pref_key_zipcode), "");

        if (!_firstname.isEmpty()) {
            firstname.setText(_firstname);
        }

        if (!_lastname.isEmpty()) {
            lastname.setText(_lastname);
        }

        if (!_birthday.isEmpty()) {
            birthday.setText(_birthday);
        }

        if (!_lieunaissance.isEmpty()) {
            lieunaissance.setText(_lieunaissance);
        }

        if (!_address.isEmpty()) {
            address.setText(_address);
        }

        if (!_town.isEmpty()) {
            town.setText(_town);
        }

        if (!_zipcode.isEmpty()) {
            zipcode.setText(_zipcode);
        }



        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);

        Calendar newCalendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(i, i1, i2);
                birthday.setTextColor(getResources().getColor(R.color.black));
                birthday.setText(dateFormat.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
    }

    private void initViewById() {
        firstname = findViewById(R.id.field_firstname);
        lastname = findViewById(R.id.field_lastname);
        lieunaissance = findViewById(R.id.field_lieunaissance);
        address = findViewById(R.id.field_address);
        town = findViewById(R.id.field_town);
        zipcode = findViewById(R.id.field_zipcode);
        birthday = findViewById(R.id.field_birthday);
        btnBirthday = findViewById(R.id.birthday);
        btnSave = findViewById(R.id.save_info);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
