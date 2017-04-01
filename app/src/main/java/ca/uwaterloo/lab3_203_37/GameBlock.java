package ca.uwaterloo.lab3_203_37;

        import android.content.Context;
        import android.graphics.Color;
        import android.os.Build;
        import android.support.annotation.RequiresApi;
        import android.util.Log;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import java.util.Random;


public class GameBlock extends GameBlockTemplate {
    private final float ACC = 4.0f;// the accelerometer of the block
    private final float IMAGE_SCALE = 0.5f; //the size of the block -- change it to 0.65f when using Nosheen's phone

    private int myCoordX;
    private int myCoordY;
    private int targetX;
    private int targetY;

    public TextView tv;

    public int blockShownValue;
    private int offset_x = 160;
    private int offset_y = 110;
    private int Velocity;
    private GameLoopTask.gameDirection myDir =GameLoopTask.gameDirection.NO_MOVEMENT;
    public RelativeLayout gbrl;
    private GameLoopTask myglt;
    public boolean canMove;
    private int [] block_num = new int [4];
    public boolean needDoubled = false;

    //@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public GameBlock(Context gbContext, RelativeLayout RL, int coord_X, int coord_Y,GameLoopTask task ){
        super(gbContext);//for the context reference
        this.setImageResource(R.drawable.gameblock);
        this.gbrl = RL;
        this.myglt = task;
        myCoordX = coord_X;// the (x,y) value
        myCoordY = coord_Y;
        targetX = myCoordX;//the target(x,y)
        targetY = myCoordY;
        Velocity = 0;// velocity of the block
        myDir = GameLoopTask.gameDirection.NO_MOVEMENT;
        block_num = new int[4];

        setX(myCoordX);
        setY(myCoordY);
        setScaleX(IMAGE_SCALE);// the image size
        setScaleY(IMAGE_SCALE);
        Random myRandom = new Random();
        blockShownValue = (myRandom.nextInt(2)+1)*2;
        tv = new TextView(gbContext);
        gbrl.addView(tv);
        tv.setX(myCoordX + offset_x);
        tv.setY(myCoordY + offset_y);
        tv.setText(String.format("%d",blockShownValue));
        tv.setTextSize(40f);
        tv.setTextColor(Color.BLACK);

        needDoubled = false;
        canMove = true;
    }

    //Allowing blocks to report is own location
    public int[] getCoordinate(){
        int[] thisCoord = new int[2];
        thisCoord[0] = myCoordX;
        thisCoord[1] = myCoordY;
        return thisCoord;
    }

    //method returns the target coordination
    public  int[] target(){
        int[] targetcoord = new int[2];
        targetcoord[0] = targetX;
        targetcoord[1] = targetY;
        return targetcoord;
    }

    //method returns the current coordination
    public int[] current(){
        int[] curr = new int[2];
        curr[0] = myCoordX;
        curr[1] = myCoordY;
        return curr;
    }

    //send the reference of the relative layout with delete everything
    //remove the block from the game board
    public GameBlock remove(){
        gbrl.removeView(tv);
        myglt = null;
        myDir = null;
        return this;
    }
    // get the number shown on the block
    public  int get_number(){
        return  blockShownValue;
    }

    //remove the 0 from the array of a 4 number int array
    //0 representing the empty slot
    //this method removes the empty slots to easier compare the value shown on each block
    public int[] remove_emptySlot (){
        int temp_a = 0;
        for (int a : block_num) {
            if (a != 0)
                temp_a++;
        }
        //create an array without the empty slots
        int[] temp_b = new int[temp_a];
        int i = 0;
        //pass block number into the temporary array
        for (int a = 0; a < block_num.length; a++) {
            if (block_num[a] != 0) {
                temp_b[a - i] = block_num[a];
            } else i++;
        }
        return temp_b;
    }

    //this method merges the block with the same value
    public int merge_same_num(int occupiedNumber) {
        int[] temp_y = remove_emptySlot();
        //if there are four blocks in a row, then check the adjacent two blocks' values
        //if the values are the same, then set the needDoubled boolean to be true
        //occupiedNumber decreases 1 when find a pair of blocks with same value
        if (temp_y.length==4) {
            if(temp_y[0]==temp_y[1]){
                occupiedNumber--;
                if(temp_y[2]==temp_y[3]) {
                    occupiedNumber--;
                    needDoubled=true;
                }
            } else if(temp_y[1]==temp_y[2]){
                occupiedNumber--;
                needDoubled = true;
            } else if(temp_y[2]==temp_y[3]){
                occupiedNumber--;
                needDoubled=true;
            }
        }
        //if there are three blocks in a row, then check the adjacent two blocks' values
        //if the values are the same, then set the needDoubled boolean to be true
        if (temp_y.length==3) {
            if(temp_y[0]==temp_y[1]){
                occupiedNumber--;
                needDoubled = true;
            } else if(temp_y[1]==temp_y[2]){
                occupiedNumber--;
                needDoubled=true;
            }
        }
        //if there are two blocks in a row, then set the needDoubled boolean to be true
        if (temp_y.length==2) {
            if(temp_y[0]==temp_y[1]){
                occupiedNumber--;
                needDoubled=true;
            }
        }
        return occupiedNumber;
    }
    public void setDestination(GameLoopTask.gameDirection setDirection) {
        myDir = setDirection;

        int testCoord;
        //initialization of the variable
        int numOfOccupied = 0;
        needDoubled = false;
        switch(setDirection){
            case NO_MOVEMENT:
                break;
            case LEFT:
                testCoord = GameLoopTask.LEFT_BOUNDARY;
                block_num = new int [(GameLoopTask.convertlocation(myCoordX)) + 1]; //set a block number array
                block_num[GameLoopTask.convertlocation(myCoordX)] = blockShownValue; //send the numbers at this block
                while(testCoord != myCoordX){
                    //send the number on this direction
                    block_num[GameLoopTask.convertlocation(testCoord)] =
                            myglt.num[GameLoopTask.convertlocation(testCoord)]
                                    [GameLoopTask.convertlocation(myCoordY)];
                    //check if the location is occupied
                    if(myglt.isOccupied(testCoord, myCoordY)){
                        numOfOccupied++;
                    }
                    if(testCoord<GameLoopTask.RIGHT_BOUNDARY){
                        testCoord += GameLoopTask.SLOT_ISOLATION;//make sure it not out off range
                    }
                }
                //find the same number and merge; return the number of occupants after merging
                numOfOccupied = merge_same_num(numOfOccupied);
                targetX = GameLoopTask.LEFT_BOUNDARY + numOfOccupied * GameLoopTask.SLOT_ISOLATION;//set new target
                targetY = myCoordY;
                Log.d("Game Block Report: ", String.format("Target X Coord: %d", targetX));
                break;

            case RIGHT:
                testCoord = GameLoopTask.RIGHT_BOUNDARY;
                block_num = new int[4-(GameLoopTask.convertlocation(myCoordX))+1 ];
                block_num [4-(GameLoopTask.convertlocation(myCoordX))] = blockShownValue;
                while(testCoord != myCoordX){
                    block_num[4-(GameLoopTask.convertlocation(testCoord))] =
                            myglt.num[GameLoopTask.convertlocation(testCoord)]
                                    [GameLoopTask.convertlocation(myCoordY)];

                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if(myglt.isOccupied(testCoord, myCoordY)){
                        numOfOccupied++;
                    }
                    if (testCoord>GameLoopTask.LEFT_BOUNDARY) {
                        testCoord -= GameLoopTask.SLOT_ISOLATION;
                    }
                }
                //find the same number and merge; return the number of occupants after merging
                numOfOccupied = merge_same_num(numOfOccupied);
                targetX = GameLoopTask.RIGHT_BOUNDARY - numOfOccupied * GameLoopTask.SLOT_ISOLATION;
                targetY = myCoordY;
                Log.d("Game Block Report: ", String.format("Target X Coord: %d", targetX));
                break;
            case UP:
                testCoord = GameLoopTask.UP_BOUNDARY;
                block_num = new int [(GameLoopTask.convertlocation(myCoordY))+1];
                block_num[GameLoopTask.convertlocation(myCoordY)] = blockShownValue;
                while(testCoord != myCoordY){
                    block_num[GameLoopTask.convertlocation(testCoord)] =
                            myglt.num[GameLoopTask.convertlocation(myCoordX)]
                                    [GameLoopTask.convertlocation(testCoord)];

                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if(myglt.isOccupied(myCoordX, testCoord)){
                        numOfOccupied++;
                    }
                    if(testCoord<GameLoopTask.DOWN_BOUNDARY){
                        testCoord += GameLoopTask.SLOT_ISOLATION;
                    }
                }
                //find the same number and merge; return the number of occupants after merging
                numOfOccupied = merge_same_num(numOfOccupied);
                targetY = GameLoopTask.UP_BOUNDARY + numOfOccupied * GameLoopTask.SLOT_ISOLATION;
                targetX = myCoordX;
                Log.d("Game Block Report: ", String.format("Target Y Coord: %d", targetY));
                break;
            case DOWN:
                testCoord = GameLoopTask.DOWN_BOUNDARY;
                block_num = new int[4-(GameLoopTask.convertlocation(myCoordY)) +1];
                block_num [4-(GameLoopTask.convertlocation(myCoordY))] = blockShownValue;
                while(testCoord != myCoordY){
                    block_num[4-(GameLoopTask.convertlocation(testCoord))] =
                            myglt.num[GameLoopTask.convertlocation(myCoordX)]
                                    [GameLoopTask.convertlocation(testCoord)];
                    Log.d("Game Block Test Point", String.format("%d", testCoord));
                    if(myglt.isOccupied(myCoordX, testCoord)){
                        numOfOccupied++;
                    }
                    if (testCoord>GameLoopTask.UP_BOUNDARY) {
                        testCoord -= GameLoopTask.SLOT_ISOLATION;
                    }
                }
                //find the same number and merge; return the number of occupants after merging
                numOfOccupied = merge_same_num(numOfOccupied);
                targetY = GameLoopTask.DOWN_BOUNDARY - numOfOccupied * GameLoopTask.SLOT_ISOLATION;
                targetX = myCoordX;
                Log.d("Game Block Report: ", String.format("Target Y Coord: %d", targetY));
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void move(){
        //set the block number at the center of block
        if(blockShownValue>10){
            offset_x=120;
            if(blockShownValue>100){
                offset_x=100;
                tv.setTextSize(30);
            }
            if(blockShownValue>1000){
                offset_y = 120;
                offset_x=95;
                tv.setTextSize(20);
            }
        }
        tv.setText(String.format("%d", blockShownValue));
        bringToFront();
        tv.bringToFront();

        switch(myDir){
            case UP:
                if(myCoordY>targetY){
                    if((myCoordY-Velocity)<=targetY){
                        myCoordY = targetY;
                        Velocity = 0;
                        tv.setText(String.format("%d", blockShownValue));
                    }
                    else{
                        myCoordY -= Velocity;
                        Velocity += ACC;
                    }
                }
                break;
            case DOWN:
                if(myCoordY<targetY){
                    if((myCoordY+Velocity)>=targetY){
                        myCoordY = targetY;
                        Velocity = 0;
                        tv.setText(String.format("%d", blockShownValue));
                    }
                    else{
                        myCoordY += Velocity;
                        Velocity += ACC;
                    }
                }
                break;
            case LEFT:
                if(myCoordX>targetX){
                    if((myCoordX-Velocity)<=targetX){
                        myCoordX = targetX;
                        Velocity = 0;
                        tv.setText(String.format("%d", blockShownValue));
                    }
                    else{
                        myCoordX -= Velocity;
                        Velocity += ACC;
                    }
                }
                break;
            case RIGHT:
                if(myCoordX<targetX){
                    if((myCoordX+Velocity)>=targetX){
                        myCoordX = targetX;
                        Velocity = 0;
                        tv.setText(String.format("%d", blockShownValue));
                    }
                    else{
                        myCoordX += Velocity;
                        Velocity += ACC;
                    }
                }
                break;
            default:
                break;
        }
        setX(myCoordX);
        setY(myCoordY);
        tv.setX(myCoordX+offset_x);
        tv.setY(myCoordY+offset_y);
    }
}
