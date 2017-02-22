package com.trucklog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.NewLoadManage;
import com.trucklog.Utils.TokenManage;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoadService extends Service {
    boolean  is_HomeActivity_Available;
    public static final String EXTRA_MESSENGER="com.trucklog.SERVICE_CREATED_MESSAGE";
    private static LoadService instance;
    Messenger notify_messenger;
    Thread socket_thread;

    public static LoadService getInstance(){
        return instance;
    }

    private Socket mSocket;{
        try {
            mSocket = IO.socket("https://urbantucking.com");///socket.io
        } catch (URISyntaxException e) {}
    }

    private Emitter.Listener onNewLoad = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            NewLoadManage.setNewLoad(LoadService.this, Integer.parseInt(args[0].toString()));
            displayNotificationLoadisChanged(Integer.parseInt(args[0].toString()));
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            String token = TokenManage.getToken(LoadService.this);
            mSocket.emit("auth", token);
        }

    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mSocket.emit("message", "test");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("**Error Socket**","Error");
        }
    };

    private Emitter.Listener onPong = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
    /**
     * variables for asyncTask
     *
     */

    private boolean isRunning = false;
//    private Handler handler_Loaddata = new Handler();
//    private Runnable runnable_Loaddata = new Runnable() {
//        @Override
//        public void run() {
//            String token = TokenManage.getToken(LoadService.this.getApplicationContext());
//            handler_Loaddata.postDelayed(runnable_Loaddata, 60000);
//        }
//    };




    private Runnable socket_manage_runnable = new Runnable() {
        @Override
        public void run() {
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
//            mSocket.on("auth", onAuth);
            mSocket.on("noti", onNewLoad);
            mSocket.on(Socket.EVENT_MESSAGE, onMessage);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on(Socket.EVENT_PONG, onPong);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.connect();
        }
    };

    public void setIs_HomeActivity_Available(boolean value){
        is_HomeActivity_Available = value;
    }

    public void displayNotificationLoadisChanged(int id){
        if(is_HomeActivity_Available == true) {
            Message notifiy_message = Message.obtain();
            notifiy_message.arg1 = Constants.MESSAGE_NOTIFITY;
            notifiy_message.arg2 = id;
            try {
                notify_messenger.send(notifiy_message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            makeNotification();
        }
    }

    private void makeNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                        .setContentTitle("New load assigned. Check it, please")
                        .setContentText("Truck Logo")
                        .setAutoCancel(true);
        Intent resultIntent = new Intent(this, LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.isRunning = false;
        instance = LoadService.this;
        socket_thread = new Thread(socket_manage_runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(this.isRunning == false){
            this.isRunning = true;
            notify_messenger = (Messenger)intent.getExtras().get(EXTRA_MESSENGER);
            socket_thread.start();
            is_HomeActivity_Available = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocket.disconnect();
                mSocket.off("connect",onConnect);
                mSocket.off("noti", onNewLoad);
            }
        }).start();
    }




    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }






}
