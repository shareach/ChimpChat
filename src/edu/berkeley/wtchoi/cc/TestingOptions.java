package edu.berkeley.wtchoi.cc;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/10/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestingOptions implements Serializable{
    private static final long serialVersionUID = -7186309675577891256L;

    //time interval between supervisor tick in millie seconds.
    public int tick_interval = 200;

    //supervisor tick count
    public int tick_count = 5;

    //supervisor snooze refill. It should be smaller then tick_count;
    public int tick_snooze = 3;

    //stable count. to prevent premature state transition
    public int stable_count = 1;
}
