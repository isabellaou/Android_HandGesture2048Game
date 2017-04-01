package ca.uwaterloo.lab3_203_37;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;



/**
 * Created by Keith_Laptop on 2016/12/20.
 */
public class AccelerometerEventListener implements SensorEventListener {

    //Setup filter constant here
    private final float FILTER_CONSTANT = 4.0f;

    //FSM: setup FSM states and Signatures here
    enum myState{WAIT, RISE_A, FALL_A, FALL_B, RISE_B, DETERMINED};
    myState state1 = myState.WAIT; //state for left and right state machine; initialize it to be wait
    myState state2 = myState.WAIT; //state for up and down state machine; initialize it to be wait

    enum mySig{SIG_A, SIG_B, SIG_X};
    mySig signatureX = mySig.SIG_X; //signature for x-axis; initialize it to be X type
    mySig signatureY = mySig.SIG_X; //signature for y-axis; initialize it to be X type

    //FSM: setup threshold constants here
    final float[] THRES_A = {0.5f, 2.0f, -0.4f}; //threshold constant for right gesture
    final float[] THRES_B = {-0.5f, -2.0f, 0.4f}; //threshold constant for left gesture

    final float[] THRES_C = {0.5f, 2.0f, -0.4f}; //threshold constant for up gesture
    final float[] THRES_D = {-0.5f, -2.0f, 0.4f}; //threshold constant for down gesture

    //FSM: Setup FSM sample counter here
    final int SAMPLEDEFAULT = 30;
    int sampleCounter = SAMPLEDEFAULT;





    //The corresponding textview and linegraphview on the layout
    private TextView instanceOutput;

    //100 history readings
    private float[][] historyReading = new float[100][3];

    //FIFO 100-element rotation method
    private void insertHistoryReading(float[] values){

        for(int i = 1; i < 100; i++){
            historyReading[i - 1][0] = historyReading[i][0];
            historyReading[i - 1][1] = historyReading[i][1];
            historyReading[i - 1][2] = historyReading[i][2];
        }

        //UPDATE THIS SECTION for LPF Implementation
        historyReading[99][0] += (values[0] - historyReading[99][0]) / FILTER_CONSTANT;
        historyReading[99][1] += (values[1] - historyReading[99][1]) / FILTER_CONSTANT;
        historyReading[99][2] += (values[2] - historyReading[99][2]) / FILTER_CONSTANT;

        //After filtering the data, call FSM for signature analysis.  This is NOT a good OOD practice, but will serve the purpose for now.
        callFSM();

        //Make sure that by the 30th sample, the FSM result is generated.  If not, it is a bad signature.
        if(sampleCounter <= 0){
            //check the state for both axis
            //if both state are determined, go check the signature
            if(state1 == myState.DETERMINED && state2 == myState.DETERMINED){
                //check the signature for both x and y axis
                //use the signature to determine which gesture it is
                if(signatureX == mySig.SIG_B && signatureY == mySig.SIG_X)
                    instanceOutput.setText("LEFT");
                else if(signatureX == mySig.SIG_A && signatureY == mySig.SIG_X)
                    instanceOutput.setText("RIGHT");
                else if(signatureY == mySig.SIG_A && signatureX == mySig.SIG_X){
                    instanceOutput.setText("UP");
                }
                else if(signatureY == mySig.SIG_B && signatureX == mySig.SIG_X){
                    instanceOutput.setText("Down");
                }
                else
                    instanceOutput.setText("Undetermined");
            }
            //if one of the state are undetermined
            //set both states into wait and output undetermined
            else{
                state1 = myState.WAIT;
                state2 = myState.WAIT;
                instanceOutput.setText("Undetermined");
            }

            //set all the variables into default value
            sampleCounter = SAMPLEDEFAULT;
            state1 = myState.WAIT;
            state2 = myState.WAIT;

        }

    }



    public void callFSM(){

        //create a float with the value of the difference between the current and previous x axis readings
        float deltaA = historyReading[99][0] - historyReading[98][0];
        float deltaB = historyReading[99][1] - historyReading[98][1];

        //FSM1: left and right
        switch(state1){

            case WAIT:

                sampleCounter = SAMPLEDEFAULT;
                signatureX = mySig.SIG_X;

                //when Δax > Thres_A[0] or Δax < Thres_B[0], a hand gesture is taken
                //if it is positive, then it is possibly right hand gesture
                if(deltaA > THRES_A[0]){
                    state1 = myState.RISE_A;
                }
                //if it is negative, then it is possibly left hand gesture
                else if(deltaA < THRES_B[0]){
                    state1 = myState.FALL_B;
                }

                break;

            case RISE_A:

                //when the slope becomes negative, check the value of the max point
                //if it is larger than or equal to Thres_A[1]
                //it is possibly a right hand gesture
                if(deltaA <= 0){
                    if(historyReading[99][0] >= THRES_A[1]){
                        state1 = myState.FALL_A;
                    }
                    else{
                        state1 = myState.DETERMINED;
                    }
                }

                break;

            case FALL_A:

                //when the slope becomes positive, check the value of the min point
                //if it is less than or equal to Thres_A[2]
                //it is a right hand gesture
                if(deltaA >= 0){
                    if (historyReading[99][0] <= THRES_A[2]) {
                        signatureX = mySig.SIG_A;
                    }
                    state1 = myState.DETERMINED;
                }

                break;

            case FALL_B:

                //when the slope becomes positive, check the value of the min point
                //if it is less than or equal to Thres_B[1]
                //it is possibly a left hand gesture
                if(deltaA >= 0){
                    if(historyReading[99][0] <= THRES_B[1]){
                        state1 = myState.RISE_B;
                    }
                    else{
                        state1 = myState.DETERMINED;
                    }
                }

                break;

            case RISE_B:

                //when the slope becomes negative, check the value of the max point
                //if it is greater than or equal to Thres_B[2]
                //it is a left hand gesture
                if(deltaA <= 0){
                    if (historyReading[99][0] >= THRES_B[2]) {
                        signatureX = mySig.SIG_B;
                    }
                    state1 = myState.DETERMINED;
                }

                break;

            case DETERMINED:

                Log.d("FSM: ", "State DETERMINED " + signatureX.toString());

                break;

            default:
                state1 = myState.WAIT;
                break;

        }

        //up and down
        switch(state2){

            case WAIT:

                sampleCounter = SAMPLEDEFAULT;
                signatureY = mySig.SIG_X;

                //when Δay > Thres_C[0] or Δay < Thres_C[0], a hand gesture is taken
                //if it is positive, then it is possibly right hand gesture
                if(deltaB > THRES_C[0]){
                    state2 = myState.RISE_A;
                }
                else if(deltaB < THRES_D[0]){
                    state2 = myState.FALL_B;
                }

                break;

            case RISE_A:

                //when the slope becomes negative, check the value of the max point
                //if it is larger than or equal to Thres_C[1]
                //it is possibly a up hand gesture
                if(deltaB <= 0){
                    if(historyReading[99][1] >= THRES_C[1]){
                        state2 = myState.FALL_A;
                    }
                    else{
                        state2 = myState.DETERMINED;
                    }
                }

                break;

            case FALL_A:

                //when the slope becomes positive, check the value of the min point
                //if it is less than or equal to Thres_C[2]
                //it is a up hand gesture
                if(deltaB >= 0){
                    if (historyReading[99][1] <= THRES_C[2]) {
                        signatureY = mySig.SIG_A;
                    }
                    state2 = myState.DETERMINED;
                }

                break;

            case FALL_B:

                //when the slope becomes positive, check the value of the min point
                //if it is less than or equal to Thres_D[1]
                //it is possibly a down hand gesture
                if(deltaB >= 0){
                    if(historyReading[99][1] <= THRES_D[1]){
                        state2 = myState.RISE_B;
                    }
                    else{
                        state2 = myState.DETERMINED;
                    }
                }

                break;

            case RISE_B:

                //when the slope becomes negative, check the value of the max point
                //if it is greater than or equal to Thres_D[2]
                //it is a down hand gesture
                if(deltaB <= 0){
                    if (historyReading[99][1] >= THRES_D[2]) {
                        signatureY = mySig.SIG_B;
                    }
                    state2 = myState.DETERMINED;
                }

                break;

            case DETERMINED:

                Log.d("FSM: ", "State DETERMINED " + signatureX.toString());

                break;

            default:
                state2 = myState.WAIT;
                break;

        }

        sampleCounter--;

    }



    //constructor: get the references of the TV and LGV from the layout
    public AccelerometerEventListener(TextView outputView) {
        instanceOutput = outputView;
    }

    //Getter method for the history readings
    public float[][] getHistoryReading(){
        return historyReading;
    }




    //Required method for SensorEventListener
    public void onAccuracyChanged(Sensor s, int i) { }

    //Required method for sensorEventListener
    public void onSensorChanged(SensorEvent se) {

        //Check to see whether the event is from the linear acceleration
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            //insert the new values into the FIFO buffer
            insertHistoryReading(se.values);

            //Update information on the textview
            //Will use this TV for gesture display
            //instanceOutput.setText("The Accelerometer Reading is: \n"
            //        + String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]) + "\n");

            //Add the points to the LGV
            //Should update this one to display the filtered readings

        }
    }

}
