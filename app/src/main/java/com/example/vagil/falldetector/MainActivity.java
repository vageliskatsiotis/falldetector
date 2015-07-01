package com.example.vag.falldetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private DecimalFormat df = new DecimalFormat("#.###");
    private String boundary_key;
    private String phoneNumber_key;

    private TextView last_x_textView;
    private TextView last_y_textView;
    private TextView last_z_textView;
    private TextView x_textView;
    private TextView y_textView;
    private TextView z_textView;
    private TextView current_change_amount_textView;
    private TextView current_boundary_textView;

    private Sensor sensor;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;

    private double[] last_gravity = new double[3];
    private double[] gravity = new double[3];
    private boolean firstChange = true;
    private String phoneNumber ;
    private int warningBoundary = 750;
    private double changeAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boundary_key = getString(R.string.key_boundary);
        phoneNumber_key = getString(R.string.key_phone_number);
        initialUI();
        initialSensor();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "750"));
        phoneNumber = sharedPreferences.getString(phoneNumber_key, "");

        if (phoneNumber.equals(""))
            showSetPhoneNumberWarning();
    }
// Ρουτίνα έναρξης Interface
    public void initialUI() {
        last_x_textView = (TextView)findViewById(R.id.last_x_value);
        last_y_textView = (TextView)findViewById(R.id.last_y_value);
        last_z_textView = (TextView)findViewById(R.id.last_z_value);
        x_textView = (TextView)findViewById(R.id.x_value);
        y_textView = (TextView)findViewById(R.id.y_value);
        z_textView = (TextView)findViewById(R.id.z_value);
        current_change_amount_textView = (TextView)findViewById(R.id.current_change_amount);
        current_boundary_textView = (TextView)findViewById(R.id.current_boundary);
    }
// Ρουτίνα εκκίνησης γυροσκοπίου
    public void initialSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//    Ρουτίνα αποστολής μηνύματος όταν ξεπερνά το όριο
    @Override
    public void onSensorChanged(SensorEvent event) {
        last_gravity[0] = gravity[0];
        last_gravity[1] = gravity[1];
        last_gravity[2] = gravity[2];

        gravity[0] = event.values[0];
        gravity[1] = event.values[1];
        gravity[2] = event.values[2];

        changeAmount = Math.pow((gravity[0]-last_gravity[0]), 2) +
                Math.pow((gravity[1]-last_gravity[1]), 2) +
                Math.pow((gravity[2]-last_gravity[2]), 2);

        updateSensorView();

        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "750"));
        if (!firstChange && changeAmount >= warningBoundary) {
            phoneNumber = sharedPreferences.getString(phoneNumber_key, "");
            sendSMS(phoneNumber);
        }
        firstChange = false;
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }
// Ρουτίνα ενημέρωσης συντεταγμένων
    private void updateSensorView() {
        last_x_textView.setText("Last X = "+last_gravity[0]);
        last_y_textView.setText("Last Y = "+last_gravity[1]);
        last_z_textView.setText("Last Z = "+last_gravity[2]);
        x_textView.setText("X = "+gravity[0]);
        y_textView.setText("Y = "+gravity[1]);
        z_textView.setText("Z = "+gravity[2]);
        current_change_amount_textView.setText("Current change amount = "+df.format(changeAmount));
        current_boundary_textView.setText("Current boundary = "+warningBoundary);
    }
// Ρουτίνα αποστολής μηνύματος
    public void sendSMS(String phoneNumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "DANGER!!", null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// Ρουτίνα προειδοποίησης για μη καταχωρημένο νούμερο τηλεφώνου
    public void showSetPhoneNumberWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.set_phone_name_warning))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(((Dialog) dialog).getContext(), SettingsActivity.class));
                        dialog.cancel();
                    }
                }).show();
    }
}
