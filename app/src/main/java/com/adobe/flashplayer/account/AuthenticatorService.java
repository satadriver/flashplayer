package com.adobe.flashplayer.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class AuthenticatorService extends Service {
    private final String TAG = "[ljg]AuthenticatorService";

    ////mAuthenticator目的是作为账号认证
    private Authenticator mAuthenticator = null;

    public AuthenticatorService() {
        Log.e(TAG,"AuthenticatorService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator =  new Authenticator(this);
        Log.e(TAG,"onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind");
        return mAuthenticator.getIBinder();
    }
}


/*
1 AuthenticationService类
AuthenticationService是一个继承Service的服务，目的是提供跨进程调用，供系统同步框架调用。
固定的Action为android.accounts.AccountAuthenticator

2 Authenticator是一个继承自AbstractAccountAuthenticator的类，AbstractAccountAuthenticator是一个虚类，
它定义处理手机“设置”里“账号与同步”中Account的添加、删除和验证等功能的基本接口，并实现了一些基本功能。
AbstractAccountAuthenticator里面有个继承于IAccountAuthenticator.Stub的内部类，
以用来对AbstractAccountAuthenticator的远程接口调用进行包装。
我们可以通过AbstractAccountAuthenticator的getIBinder（）方法，返回内部类的IBinder形式，
以便对此类进行远程调用，如上面代码onBind方法中的调用。
其中比较重要需要重载的方法是addAccount()：
//这个addAccount()在用户进入设置-账户-添加账户的时候触发的，这里面把自己设置账户的页面的信息封装给bundle，然后传出去即可。
//如果返回null表示不做任何触发


3 sync机制的使用和账号管理很类似，也是基于binder机制的跨进程通信。首先它需要一个Service，这个服务提供一个Action给系统以便系统能找到它；
然后就是继承和实现AbstractThreadedSyncAdapter，此类中包含实现了ISyncAdapter.Stub内部类，
这个内部类封装了远程接口调用，这个类getSyncAdapterBinder()方法，返回内部类的IBinder形式，
以便对AbstractThreadedSyncAdapte进行远程调用；
在manifest中需要对Service注册，而且指定meta-data，这个meta-data是一个xml文件，
在SampleSyncAdapter实例中，它的名字是syncadapter.xml，这个文件指定了账号和被监听的contentprovider

*/
