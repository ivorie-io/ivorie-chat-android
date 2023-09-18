package com.ivoriechat.android.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ivoriechat.android.R;
import com.ivoriechat.android.databinding.ActivityMainBinding;
import com.ivoriechat.android.utils.RetrievalService;

public class MainActivity extends AppCompatActivity implements EarningUsersFragment.OnRetrievalServiceListener {

    private ActivityMainBinding binding;

    private boolean mServiceBound = false;
    private RetrievalService mRetrievalService;
    private RetrievalServiceConnection mRetrievalServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_earning_users, R.id.navigation_paying_users, R.id.navigation_vault)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Intent mServiceIntent = new Intent(this, RetrievalService.class);
        mRetrievalServiceConnection = new RetrievalServiceConnection();
        bindService(mServiceIntent, mRetrievalServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy (){
        super.onDestroy();

        if(mServiceBound) {
            unbindService(mRetrievalServiceConnection);
        }
    }

    @Override
    public void onRetrieveVerifiedUsers() {
        if(mServiceBound) {
            mRetrievalService.fetchEarningUsers();
        }
    }

    /*
     * A client binds to a service by calling bindService().
     * When it does, it must provide an implementation of ServiceConnection, which monitors the connection with the service.
     * */
    private class RetrievalServiceConnection implements ServiceConnection {

        //Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // ComponentName: The concrete component name of the service that has been connected.
            // IBinder: The IBinder of the Service's communication channel, which you can now make calls on.

            // We've bound to EventRetrievalService, cast the IBinder and get EventRetrievalService instance
            RetrievalService.RetrievalServiceBinder binder = (RetrievalService.RetrievalServiceBinder)iBinder;
            mRetrievalService = binder.getService();
            // Indicate that we've bound to EventRetrievalService
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRetrievalService = null;
            // Indicate that we are no longer bound to EventRetrievalService
            mServiceBound = false;
        }
    }

}