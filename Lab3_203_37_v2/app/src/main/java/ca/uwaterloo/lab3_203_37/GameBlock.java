package ca.uwaterloo.lab3_203_37;

import android.content.Context;
import android.widget.ImageView;

public class GameBlock extends ImageView{

    private final float IMAGE_SCALE = 0.6f;
    private int myCoordX;
    private int myCoordY;
    //Coordinates of 4 corners of the game board: top left = -130, -130, bottom left = -130, 650
    //top right = 650, -130, bottom right = 650, 650
    public GameBlock (Context gbCTX ,int coordX, int coordY) {
        super(gbCTX);
        myCoordX = coordX;
        myCoordY = coordY;
        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);
        this.setX(coordX);
        this.setY(coordY);
    }
}
