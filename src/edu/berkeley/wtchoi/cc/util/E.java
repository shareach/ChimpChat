package edu.berkeley.wtchoi.cc.util;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/16/12
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */

//Utility class to ease execution control
public abstract class E {
    private static int miniSleepInterval = 200;

    public static boolean sleep(long sleepInterval){
        try{
            Thread.sleep(sleepInterval);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public static int getenv_as_int(String envvar, int default_value){
        String value = System.getenv(envvar);

        if(value == null){
            return default_value;
        }

        int decoded = Integer.getInteger(value);
        return decoded;
    }
}
