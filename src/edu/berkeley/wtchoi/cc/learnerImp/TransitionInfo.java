package edu.berkeley.wtchoi.cc.learnerImp;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/13/12
 * Time: 7:35 PM
 * To change this template use File | Settings | File Templates.
 */

public class TransitionInfo implements Serializable
{
    private boolean didNothing = false;

    public void setDidNothing (boolean f){
        didNothing = f;
    }

    public boolean didNothing(){
        return didNothing;
    }

    public boolean equalsTo(TransitionInfo target){
        return didNothing == target.didNothing;
    }
}
