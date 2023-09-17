package com.ivoriechat.android.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ivoriechat.android.ui.LoginActivity;
import com.ivoriechat.android.ui.MainActivity;
import com.ivoriechat.android.utils.AppGeneral;

import java.io.IOException;

public class IvorieAccountManagerCallback implements AccountManagerCallback<Bundle> {
    private Context mContext;
    private static String TAG = "AccountManagerCallback";
    public IvorieAccountManagerCallback(Context context){
        mContext = context;
    }
    @Override
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        Bundle result;
        Intent intent;
        if (accountManagerFuture.isDone()){
            Log.i("AccountManagerCallback:", "accountManagerFuture.isDone()");
            /*
            * Returns true if this task completed. Completion may be due to normal termination, an exception, or cancellation --
            * in all of these cases, this method will return true.
            * */
            try {
                result = accountManagerFuture.getResult();
                Log.i("AccountManagerCallback:", "result" + result);
                if(result.containsKey(AccountManager.KEY_INTENT)){
                    intent = result.getParcelable(AccountManager.KEY_INTENT);
                    // startActivityForResult(intent, AUTHENTICATOR_ACTIVITY);
                    mContext.startActivity(intent);
                }else {
                    String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                    if(!authToken.isEmpty()){
                        if(authToken.equals(AppGeneral.TOKEN_NOT_VALID)){
                            Log.i(TAG, "authToken not valid");
                            AuthenUtils.invalidateAuthToken(mContext);
                            Intent newtask = new Intent();
                            newtask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            newtask.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            newtask.setClass(mContext, LoginActivity.class);
                            mContext.startActivity(newtask);
                        } else {
                            Log.i(TAG, "authToken=" + authToken);
                            Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME), result.getString(AccountManager.KEY_ACCOUNT_TYPE));
                            AccountManager mAccountManager = AccountManager.get(mContext.getApplicationContext());
                            mAccountManager.setAuthToken(account, AccountGeneral.AUTHTOKENTYPE, authToken);

                            Intent newtask = new Intent();
                            newtask.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            newtask.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            newtask.setClass(mContext, MainActivity.class);
                            mContext.startActivity(newtask);
                        }

                    }
                }
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }
        }

    }
}
