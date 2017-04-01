package ca.uwaterloo.lab3_203_37;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;



public class GameLoopTask extends TimerTask{
    private Activity myActivity;
    private Context myContext;
    private RelativeLayout MyRL;
    private GameBlock newBlock;
    public enum gameDirection{UP,DOWN,LEFT,RIGHT,NO_MOVEMENT}
    public LinkedList<GameBlock> myGBList;

    private int random_x;
    private int random_y;

    private Random myRandomGen = new Random();

    //Coordinates of 4 corners of the game board: top left = -70, -70, bottom left = -70, 675
    //top right = 675, -70, bottom right = 675, 675
    //public static final int LEFT_BOUNDARY = -70;
    //public static final int UP_BOUNDARY = -70;
    //public static final int SLOT_ISOLATION = 248; // SLOT_ISOLATION = (675+70)/3

    public static final int LEFT_BOUNDARY = -75;
    public static final int UP_BOUNDARY = -75;
    public static final int RIGHT_BOUNDARY = 675;
    public static final int DOWN_BOUNDARY = 675;
    public static final int SLOT_ISOLATION = 250;
    public int [][] num= new int[4][4];

    private boolean check_empty = true;
    private LinkedList<Integer> temp_list = new LinkedList<>();
    private boolean Win = false;
    private boolean [][] filledBlock= new boolean[4][4];//all false;false --empty;true is full.
    private int [] replace = new int [100];



    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public GameLoopTask(Activity myActivity, RelativeLayout MyRL, Context myContext) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                filledBlock[i][j]=false;
                num[i][j]=0;
            }
        }
        this.myActivity = myActivity;
        this.MyRL = MyRL;
        this.myContext = myContext;
        myGBList = new LinkedList<GameBlock>();
        createBlock();
        for(int i=0;i<100;i++){
            replace[i]=0;
        }
    }

    public static int convertlocation(int a){
        a=(a-LEFT_BOUNDARY)/SLOT_ISOLATION;
        return a;
    }
    public static int convertNum(int a){
        a = LEFT_BOUNDARY+a*SLOT_ISOLATION;
        return a;
    }

    public boolean isOccupied(int curr_X,int curr_Y) {
        for(GameBlock each_block : myGBList) {
            if(each_block.current()[0] == curr_X && each_block.current()[1] == curr_Y) {
                Log.d("Game Loop occupied: ", "Found!");
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    //this method creates a new block
    private void createBlock(){
        boolean filled = true;
        random_x = myRandomGen.nextInt(4)*SLOT_ISOLATION + LEFT_BOUNDARY;
        random_y = myRandomGen.nextInt(4)*SLOT_ISOLATION + UP_BOUNDARY;
        for(int a = 0;a <4; a++){
            for(int b = 0; b < 4; b++){
                if(!filledBlock[a][b]) {
                    filled = false;
                }
            }
        }
        //make sure the new block is in the empty block
        if(!filled) {
            while (filledBlock[convertlocation(random_x)][convertlocation(random_y)]) {
                random_x = convertNum(myRandomGen.nextInt(4));
                random_y =  convertNum(myRandomGen.nextInt(4));
            }
            newBlock = new GameBlock(myContext, MyRL, random_x, random_y, this);
            MyRL.addView(newBlock);
            myGBList.add(newBlock);
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void setDirection(gameDirection newDirection){
        temp_list.clear();
        check_empty = false;
        for(int i=0;i<100;i++){//initialize replace array
            replace[i]=0;
        }
        //Log.d("The Direction is ", currentGameDirection.toString());

        for(GameBlock each_block : myGBList) {
            each_block.setDestination(newDirection);
        }
        for(int a = 0; a <4; a++){//reset num and full block
            for(int b = 0; b < 4; b++){
                filledBlock[a][b] = false;
                num[a][b] = 0;
            }
        }
        for(GameBlock bg : myGBList) {
            if(bg.current()[0]!=bg.target()[0]||bg.current()[1]!=bg.target()[1]){
                check_empty = true;//the block is empty
            }
            for (int a = 0; a < 4; a++) {
                for (int b = 0; b < 4; b++) {
                    if (bg.target()[0] ==  convertNum(a) && bg.target()[1] == convertNum(b)) {
                        filledBlock[a][b] = true;//the block have some thing in it
                        num[a][b] = bg.get_number();//save the number of the block in to a list
                    }
                }
            }
        }
    }

    //check if the user wins or loses
    public boolean DefOrVic(){
        boolean victory = true;
        for(int j = 0; j <4; j++){
            for(int i = 0; i < 3; i++){
                //check there is no block number is 0 and no blocks around it got the same number
                if(num[i+1][j]==0||num[i][j]==0||num[i][j] ==num[i+1][j]||num[j][i]==num[j][i+1])
                {
                    victory = false;
                }
            }
        }
        return victory;
    }

    @Override
    public void run(){
        this.myActivity.runOnUiThread(
                new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                    public void run() {
                        boolean motionDone = true;
                        temp_list.clear();
                        for(GameBlock bg : myGBList){
                            // make sure everything happen it moved to target
                            if(bg.current()[0]!=bg.target()[0]||bg.current()[1]!=bg.target()[1]){
                                motionDone = false;
                            }
                        }
                        for(GameBlock bg : myGBList){
                            bg.move();
                            //check if the block is moved to the target location
                            if(bg.current()[0]==bg.target()[0]&&bg.current()[1]==bg.target()[1]){
                                for(GameBlock gb1:myGBList){
                                    if(gb1.target()[0]==bg.target()[0]&&
                                            gb1.target()[1]==bg.target()[1]&& bg != gb1){
                                        if(bg.needDoubled){
                                            if(replace[myGBList.indexOf(bg)]<1){//get the number of the block need to change in mygblist
                                                temp_list.add(myGBList.indexOf(bg));
                                            }
                                        }
                                        //after the motion is finished, double the value
                                        if(replace[myGBList.indexOf(bg)]<1 && motionDone){
                                            bg.blockShownValue *= 2;//double the number in the block
                                           //if the value gets to 64, then user wins
                                            if(bg.blockShownValue==256){
                                                Win = true;
                                            }
                                            num[convertlocation(bg.target()[0])][convertlocation(bg.target()[1])] =bg.blockShownValue;
                                            replace[myGBList.indexOf(bg)]++;
                                        }
                                    }
                                }
                            }
                        }

                        if(motionDone){
                            //when the motion is finished and the location is empty, then create a new block in the location
                            if(check_empty){
                                createBlock();//create block and save the number
                                num[convertlocation(myGBList.getLast().current()[0])][convertlocation(myGBList.getLast().current()[1])] =myGBList.getLast().get_number();
                                check_empty = false;//reset check empty
                            }
                            int delete = 0;
                            for(int x:temp_list){
                                //delete the block and everything by finding in the temp list
                                MyRL.removeView(myGBList.get(x-delete).remove());
                                myGBList.remove(x-delete);
                                delete++;
                            }
                        }
                       //set the textview for WIN or LOSS
                        if(Win||DefOrVic()){
                            TextView finish = new TextView(myContext);
                            MyRL.addView(finish);
                            finish.bringToFront();
                            finish.setTextColor(Color.CYAN);
                            finish.setTextSize(40);
                            finish.setX(0);
                            finish.setY(0);
                            if(Win){
                                finish.setText("VICTORY!");
                            }
                            else if(DefOrVic()){
                                finish.setText("DEFEATED!");
                            }
                        }
                    }
                }
        );
    }
}