package com.ivoriechat.android.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.google.gson.JsonObject;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class WebsocketService extends Service {
    private final IBinder mBinder = new WebsocketServiceBinder();
    private static final String TAG = "WebsocketService";
    private Looper mServiceLooper;
    private ServiceHandler mHandler;
    private String wsServerUrl = AppGeneral.WEBSOCKET_SERVER_URL + AppGeneral.WEB_MODULE_PATH + AppGeneral.VIDEO_CALL_WEBSOCKET_ENDPOINT;
    private Session chatSession;
    private BehaviorSubject<String> websocketServiceResult;

    public WebsocketService() {
    }

    @Override
    public void onCreate() {
        //Called by the system when the service is first created.

        /*
         * A service runs in the same process as the application in which it is declared and in the main thread of that application, by default.
         * So, if your service performs intensive or blocking operations while the user interacts with an activity from the same application, the service will slow down activity performance.
         * To avoid impacting application performance, you should start a new thread inside the service.
         */
        //Start up the thread running the service.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        // Starts the new Thread of execution.
        thread.start();
        // This method returns the Looper associated with this thread.
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        // The looper can then be used to create handler classes.
        mHandler = new ServiceHandler(mServiceLooper);

        websocketServiceResult = BehaviorSubject.create();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Create Websocket Endpoint.");
                final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
                final ClientManager client = ClientManager.createClient();
                try {
                    chatSession = client.connectToServer(new ClientEndpoint(), cec,  URI.create(wsServerUrl));
                    chatSession.addMessageHandler(new WebsocketMessageHandler());
                } catch (DeploymentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // IBinder implementation that provides the programming interface that clients can use to interact with the service.
    public class WebsocketServiceBinder extends Binder {
        public WebsocketService getService(){
            return WebsocketService.this;
        }
    }

    public BehaviorSubject<String> getObservable() {
        return websocketServiceResult;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            String message = (String) msg.obj;

            try {
                chatSession.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // Websocket Client Endpoint
    public class ClientEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig config) {

            Log.i(TAG, "ClientEndpoint has been opened.");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.WEBSOCKET_CONNECTION_CALLBACK);
            jsonObject.addProperty(AppGeneral.CONNECTION_STATUS, AppGeneral.OPEN);
            websocketServiceResult.onNext(jsonObject.toString());
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            Log.i(TAG, "ClientEndpoint has been closed.");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.WEBSOCKET_CONNECTION_CALLBACK);
            jsonObject.addProperty(AppGeneral.CONNECTION_STATUS, AppGeneral.CLOSE);
            websocketServiceResult.onNext(jsonObject.toString());
        }

        @Override
        public void onError(Session session, Throwable thr) {
            Log.i(TAG, "ClientEndpoint has been through some errors..");
            thr.printStackTrace();
            Log.i(TAG, thr.getLocalizedMessage());
        }
    }

    // Websocket Message Handler
    public class WebsocketMessageHandler implements MessageHandler.Whole<String> {
        @Override
        public void onMessage(String message) {
            Log.i(TAG, " Websocket Message received from server:" + message);
            websocketServiceResult.onNext(message);
        }
    }

    public void sendToWebsocketServer(String message) {
        android.os.Message msg = mHandler.obtainMessage();
        msg.obj = message;
        mHandler.sendMessage(msg);
    }
}
