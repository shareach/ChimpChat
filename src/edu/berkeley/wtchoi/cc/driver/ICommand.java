package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 4:30 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ICommand extends Comparable<ICommand>{
    public void sendCommand(IChimpDevice target) throws RuntimeException;

    public Integer typeint();
    //This is for fast comparison between different implementation of ICommand interface
    //All different implementation should use different Integer number;
}