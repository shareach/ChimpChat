package edu.berkeley.wtchoi.cc.driver.drone;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 6:34 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ViewTransformer {

    public static ViewInfo fromView(View v){
        LinkedList<ViewInfo> ch;
        try{
            ViewGroup vg = (ViewGroup)v;
            ch = new LinkedList<ViewInfo>();
            for(int i =0;i<vg.getChildCount(); i++){
                ch.add(fromView(vg.getChildAt(i)) );
            }
        }
        catch(Exception e){
            ch = null;
        }

        ViewInfo vi = new ViewInfo(v.getLeft(), v.getTop(), v.getWidth(),v.getHeight(), ch);
        vi.setScroll(v.getScrollX(),v.getScrollY());

        vi.setId(v.getId());
        if(v instanceof  EditText){
            EditText et = (EditText) v;
            vi.setIsEditText(true);
            vi.setTextContent(et.getText().toString());
        }
        vi.setFocus(v.hasFocus());

        int[] location = new int[2];
        v.getLocationOnScreen(location);
        vi.setAbsolute(location[0], location[1]);


        //Determine which widget is able to click.
        //refer for detailed information, refer java.android.view.View.onTouchEvent()
        vi.setVisible(v.isEnabled() && v.isClickable());

        return vi;
    }
}
