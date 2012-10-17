package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.*;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TouchFactory implements PointFactory<ICommand> {
    private static TouchFactory instance;

    public Collection<ICommand> get(int x, int y, ViewInfo v) {
        Collection<ICommand> c = new LinkedList<ICommand>();
        if(v.isEditText()){
            c.add(new EnterCommand(x, y, v.getTextContent(), "random string", v.hasFocus()));
        }
        else{
            c.add(new TouchCommand(x, y));
            c.add(new LongTouchCommand(x, y));
        }
        return c;
    }

    static TouchFactory getInstance() {
        if(instance == null){
            instance = new TouchFactory();
        }
        return instance;
    }
}
