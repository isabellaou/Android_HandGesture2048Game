package ca.uwaterloo.lab3_203_37;


import android.content.Context;
import android.widget.ImageView;

/**
 * Created by I on 2017-03-07.
 */

public abstract class GameBlockTemplate extends ImageView {
    public GameBlockTemplate(Context context) {
        super(context);
    }
    public abstract void setDestination(GameLoopTask.gameDirection newdir);
    public abstract void move();

}
