package edu.berkeley.wtchoi.cc.driver;

import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 4:29 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IDriver {
    public boolean connectToDevice();

    public boolean initiateApp();

    //public boolean resetData();

    public boolean restartApp();

    public boolean go(List<? extends ICommand> input);

    public boolean go(ICommand input);

    public ViewInfo getCurrentView();

    public void shutdown();
}
