package com.virtugos.uberapp.driver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.virtugos.uberapp.driver.utills.AndyConstants;

import java.util.List;

public class ActivityStartReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals(AndyConstants.ACTION_START_MAP_ACTIVITY)) {

            if (isForeground(context)) {
                return;
            }

            Intent mapIntent = new Intent(context, MapActivity.class);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent
                    .FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mapIntent);


        }

    }


    public boolean isForeground(Context context){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

}
