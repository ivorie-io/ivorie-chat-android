package com.ivoriechat.android.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ivoriechat.android.database.IvorieDatabase;
import com.ivoriechat.android.database.VerifiedUser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RetrievalService extends Service {

    private final IBinder mBinder = new RetrievalServiceBinder();
    private Looper mServiceLooper;
    private ServiceHandler mHandler;

    private static final int FETCH_EARNING_USERS = 0;
    private static final int FETCH_PAYING_USERS = 1;

    private final static String TAG = "RetrievalService";

    @Override
    public void onCreate() {
        /*
         * A service runs in the same process as the application in which it is declared and in the main thread of that application, by default.
         * So, if your service performs intensive or blocking operations while the user interacts with an activity from the same application, the service will slow down activity performance.
         * To avoid impacting application performance, you should start a new thread inside the service.
         */
        //Start up the thread running the service.
        HandlerThread thread = new HandlerThread("RetrievalService", Process.THREAD_PRIORITY_BACKGROUND);
        // Starts the new Thread of execution.
        thread.start();
        // This method returns the Looper associated with this thread.
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        // The looper can then be used to create handler classes.
        mHandler = new ServiceHandler(mServiceLooper);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class RetrievalServiceBinder extends Binder {
        public RetrievalService getService() {
            return RetrievalService.this;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case FETCH_EARNING_USERS:
                    fetchEarningUsersInternal();
                    break;
                case FETCH_PAYING_USERS:
                default:
                    break;
            }
        }
    }

    // programming interface for RetrievalService client
    public void fetchEarningUsers() {
        Message message = mHandler.obtainMessage();
        message.arg1 = FETCH_EARNING_USERS;
        mHandler.sendMessage(message);
    }

    private void fetchEarningUsersInternal() {
        URL url = Utils.constructURL(AppGeneral.GET_MENTOR_COLLECTION_API);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateLongFormatTypeAdapter()) // to serialize Date to long using gson
                .serializeNulls().create();

        JsonObject json = new JsonObject();
        json.addProperty(AppGeneral.CHAT_TYPE, ChatTypeEnum.CHAT_TO_EARN.getChatType());

        MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(gson.toJson(json), MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        String responseString = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.code() == 200) {
                responseString = response.body().string();
                Log.i(TAG, responseString);
            } else {
                Log.i(TAG, "Unexpected code " + response);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Type collectionType = new TypeToken<ArrayList<VerifiedUser>>(){}.getType();
        List<VerifiedUser> earningUserList = gson.fromJson(responseString, collectionType);
        if (earningUserList != null && earningUserList.size() > 0) {
            IvorieDatabase db = IvorieDatabase.getDatabase(getApplicationContext());
            db.verifiedUserDAO().deleteAllRows();
            db.verifiedUserDAO().insertAll(earningUserList);
        }
    }

}
