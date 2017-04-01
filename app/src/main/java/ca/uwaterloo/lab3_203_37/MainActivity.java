package ca.uwaterloo.lab3_203_37;


import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    RelativeLayout rl;
    TextView tvAccelerometer;

    //1000 px is the largest dimension on this phone,
    //so don't change it if possible
    final int GAMEBOARD_DIMENSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Relative layout Declaration
        rl = (RelativeLayout)findViewById(R.id.activity_main);
        //set gameboard dimension
        rl.getLayoutParams().width = GAMEBOARD_DIMENSION;
        rl.getLayoutParams().height = GAMEBOARD_DIMENSION;
        rl.setBackgroundResource(R.drawable.gameboard);

        //TextView for reporting Accelerometer Readings ONLY
        tvAccelerometer = new TextView(getApplicationContext());
        tvAccelerometer.setText("Accelerometer Instantaneous Readings");
        tvAccelerometer.setTextSize(25); //you can change the text size if you want
        tvAccelerometer.setTextColor(Color.BLACK);

        //Timer and GameLoop
        Timer myGameLoop = new Timer();
        GameLoopTask myGameLoopTimer = new GameLoopTask(this, rl, getApplicationContext());
        myGameLoop.schedule(myGameLoopTimer, 50, 50); //50ms periodic timer - 20fps


        //Register only the Gravity-Compensated Accelerometer Readings
        SensorManager sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        final AccelerometerEventListener accListener = new AccelerometerEventListener(tvAccelerometer, myGameLoopTimer);
        sensorManager.registerListener(accListener, Accelerometer,SensorManager.SENSOR_DELAY_GAME);

        //Adding the views
        rl.addView(tvAccelerometer);

    }

}