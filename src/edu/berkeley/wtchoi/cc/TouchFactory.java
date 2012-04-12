package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.TouchCommand;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;
import edu.berkeley.wtchoi.cc.driver.ICommand;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TouchFactory implements PointFactory<ICommand> {
    private static TouchFactory instance;

    public ICommand get(int x, int y) {
        return new TouchCommand(x, y) ;
    }

    static TouchFactory getInstance() {
        if(instance == null){
            instance = new TouchFactory();
        }
        return instance;
    }
}
