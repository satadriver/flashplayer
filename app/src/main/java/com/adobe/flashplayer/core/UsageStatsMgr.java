package com.adobe.flashplayer.core;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.ConfigurationStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class UsageStatsMgr {
    private static final String TAG = "[ljg]UsageStatsMgr ";

    public static final int INTERVAL_BEST = 4; //根据提供的开始、结束时间决定时间间隔
    public static final int INTERVAL_DAILY = 0; //以天为时间间隔（最长7天）
    public static final int INTERVAL_MONTHLY = 2; //以月为时间间隔（最长6个月）
    public static final int INTERVAL_WEEKLY = 1; //以周为时间间隔（最长4个星期）
    public static final int INTERVAL_YEARLY = 3; //以年为时间间隔（最长2年）

    UsageStatsMgr() {
        throw new RuntimeException("Stub!");
    }

    public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) {
        throw new RuntimeException("Stub!");
    }

    public List<ConfigurationStats> queryConfigurations(int intervalType, long beginTime, long endTime) {
        throw new RuntimeException("Stub!");
    }

    public UsageEvents queryEvents(long beginTime, long endTime) {
        throw new RuntimeException("Stub!");
    }

    public Map<String, UsageStats> queryAndAggregateUsageStats(long beginTime, long endTime) {
        throw new RuntimeException("Stub!");
    }

    public boolean isAppInactive(String packageName) {
        throw new RuntimeException("Stub!");
    }



    private static void getHistoryApps(Context context) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, startTime, endTime);

        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            HashSet<String> set = new HashSet<>();
            for (UsageStats usageStats : usageStatsList) {
                set.add(usageStats.getPackageName());
            }

            if (!set.isEmpty()) {
                Log.e("size", set.size() + "");
            }
        }
    }

    private void getTopApp2(Context context) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);//usagestats
        long time = System.currentTimeMillis();
        List<UsageStats> usageStatsList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 2000, time);

        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            SortedMap<Long, UsageStats> usageStatsMap = new TreeMap<>();
            for (UsageStats usageStats : usageStatsList) {
                usageStatsMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!usageStatsMap.isEmpty()) {
                String topPackageName = usageStatsMap.get(usageStatsMap.lastKey()).getPackageName();
            }
        }
    }

    public static String getTopApp(Context context,int seconds) {
        String topActivity = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm != null) {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - seconds * 1000, now);
                Log.e(TAG, "Running app number in last 6 seconds : " + stats.size());

                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                    Log.e(TAG, "top running app is : "+topActivity);
                }

            }
        }
        return topActivity;
    }



    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isGrantedUsagePremission( Context context) {
        boolean granted;
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }




    public static void openAppUsage(Context context) {

        if (isGrantedUsagePremission(context) == false) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            getTopApp(context,3600);
            getHistoryApps(context);
        }else{
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }



}