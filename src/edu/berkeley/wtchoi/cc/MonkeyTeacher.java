package edu.berkeley.wtchoi.cc;

import edu.berkeley.wtchoi.cc.driver.ICommand;
import edu.berkeley.wtchoi.cc.driver.IDriver;
import edu.berkeley.wtchoi.cc.driver.PushCommand;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo.PointFactory;

import edu.berkeley.wtchoi.cc.learning.TeacherP;

import edu.berkeley.wtchoi.cc.util.datatype.CList;
import edu.berkeley.wtchoi.cc.util.datatype.CSet;
import edu.berkeley.wtchoi.cc.util.datatype.CVector;
import edu.berkeley.wtchoi.cc.util.datatype.Pair;

import java.util.TreeMap;


/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */

public class MonkeyTeacher implements TeacherP<ICommand, ViewState, AppModel> {

    private IDriver controller;
    private TreeMap<CList<ICommand>, CSet<ICommand>> paletteTable;
    private CSet<ICommand> defaultPalette;

    private PointFactory<ICommand> pointFactory;

    public MonkeyTeacher(IDriver imp) {
        controller = imp;
        pointFactory = TouchFactory.getInstance();
        paletteTable = new TreeMap<CList<ICommand>,CSet<ICommand>>();
        
        defaultPalette = new CSet<ICommand>();
        defaultPalette.add(PushCommand.getMenu());
    }

    public Pair<CList<ICommand>, CList<ViewState>> getCounterExample(AppModel model) {
        // TODO: implement getCounterExample query
        return null;
    }

    //This implementation only put last palette to palette table.
    public CList<ViewState> checkMembership(CList<ICommand> input) {
        if (input == null) return null;
        if (input.size() == 0) return null;

        CVector<ViewState> output = new CVector<ViewState>(input.size());
        CSet<ICommand> palette = null;

        controller.restartApp();
        for (ICommand t : input) {
            if (!controller.go(t))
                return null;

            ViewInfo mv = controller.getView();
            System.out.println(mv);
            palette = mv.getRepresentativePoints(pointFactory);
            palette.addAll(defaultPalette);
            if (palette == null)
                return null;

            ViewState state = new ViewState(palette);
            output.add(state);
        }
        paletteTable.put(input, palette);

        System.out.println("New Palette found");
        System.out.println(input);
        System.out.println(palette);
        
        return output;
    }

    public CSet<ICommand> getPalette(CList<ICommand> input) {
        CSet<ICommand> palette = paletteTable.get(input);

        if (palette == null) {// If null, run application to reach desired state and acquire palette
            System.out.println("Palette Miss!");
            controller.restartApp();
            boolean result = controller.go(input);
            if (result) {
                ViewInfo view = controller.getView();
                if (view == null) return null;

                palette = view.getRepresentativePoints(pointFactory);
                palette.addAll(defaultPalette);
                paletteTable.put(input, palette);
            } else {
                return null;
            }
        }

        return palette;
    }

    public boolean init() {
        //1. Initiate connection with application
        if (!controller.connectToDevice()) return false;
        if (!controller.initiateApp()) return false;

        //2. Warming palette table for initial state
        ViewInfo view = controller.getView();
        if (view == null) return false;

        CSet<ICommand> initialPalette = view.getRepresentativePoints(pointFactory);
        initialPalette.addAll(defaultPalette);
        paletteTable.put(new CVector<ICommand>(), initialPalette);

        return true;
    }
}