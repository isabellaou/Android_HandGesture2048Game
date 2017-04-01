package ca.uwaterloo.lab3_203_37;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.TimerTask;

public class GameLoopTask extends TimerTask {
    //fields declaration
    private Activity myActivity;
    private Context myContext;
    private RelativeLayout myRl;

    //constructor of GameLoopTask
    public GameLoopTask (Activity MyActivity, RelativeLayout MyRL, Context MyContext){
        this.myActivity = MyActivity;
        this.myContext = MyContext;
        this.myRl = MyRL;
        createBlock();
    }

    public void run(){
        myActivity.runOnUiThread(
                new Runnable(){
                    public void run(){
                        //This line is just for testing, i think in the following steps
                        //you need to replace it for other things.
                        Log.d("Second Count:","1");
                    }
                }
        );
    }

    public void createBlock() {
        GameBlock newBlock = new GameBlock(myContext, 650, 650);
        myRl.addView(newBlock);
    }

}
