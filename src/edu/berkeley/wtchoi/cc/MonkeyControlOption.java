package edu.berkeley.wtchoi.cc;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 8:46 PM
 * To change this template use File | Settings | File Templates.
 */
class MonkeyControlOption{
    private String mainActivity;
    private String applicationPackage;
    private long   timeout = 5000;
    private String adb;

    private TestingOptions testingOptions = null;


    public void fillFromEnvironmentVariables(){
        adb = System.getenv("ADB_DIR");
        applicationPackage = System.getenv("PACKAGE");//("com.android.demo.notepad3")
        mainActivity = System.getenv("MAIN_ACTIVITY");//("com.android.demo.notepad3.Notepadv3")

        testingOptions = new TestingOptions();
        testingOptions.tick_count = getenv_as_int("TICK_COUNT",10);
        testingOptions.tick_interval = getenv_as_int("TICK_INTERVAL",100);
        testingOptions.tick_snooze = getenv_as_int("TICK_SNOOZE",5);
        testingOptions.stable_count = getenv_as_int("STABLE_COUNT",1);
    }

    //to check whether all basic information is there
    public boolean isComplete(){
        if(mainActivity == null) return false;
        if(applicationPackage == null) return false;
        if(adb == null) return false;
        return true;
    }

    public String getApplicationPackage(){
        return applicationPackage;
    }

    public String getRunComponent(){
        return applicationPackage + '/' + mainActivity;
    }

    public String getADB(){
        return adb;
    }

    public long getTimeout(){
        return timeout;
    }

    public void setMainActivity(String s){
        mainActivity = s;
    }

    public void setApplicationPackage(String s){
        applicationPackage = s;
    }

    public void setTimeout(long t){
        timeout = t;
    }

    public void setADB(String s){
        adb = s;
    }

    public TestingOptions getTestingOptions(){
        return testingOptions;
    }

    private static int getenv_as_int(String envvar, int default_value){
        String value = System.getenv(envvar);

        if(value == null){
            return default_value;
        }

        int decoded = Integer.getInteger(value);
        return decoded;
    }
}