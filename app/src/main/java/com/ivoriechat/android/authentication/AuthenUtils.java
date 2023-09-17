package com.ivoriechat.android.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ivoriechat.android.utils.AppGeneral;

import java.io.IOException;

public class AuthenUtils {

    // attempt to retrieve AuthToken from AccountManager
    public static String peekAuthToken(Context context){
        String token = null;
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());

        Account[] mAccountList = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account mAccount = null;
        if(mAccountList.length == 1){
            mAccount = mAccountList[0];

    /*
    Gets an auth token from the AccountManager's cache. If no auth token is cached for this account, null will be returned --
    a new auth token will not be generated, and the server will not be contacted. Intended for use by the authenticator, not directly by applications.
     */
            token = mAccountManager.peekAuthToken(mAccount, AccountGeneral.AUTHTOKENTYPE);
            Log.i("AuthenUtils:", "token" + token);
        } else if (mAccountList.length > 1) {
            SharedPreferences pref = context.getSharedPreferences(AppGeneral.PREF_NAME, Context.MODE_PRIVATE);
            String mPhoneNumber = pref.getString(AppGeneral.USER_MOBILE_PHONE_NUMBER, null);
            Account account = new Account(mPhoneNumber, AccountGeneral.ACCOUNT_TYPE);
            token = mAccountManager.peekAuthToken(account, AccountGeneral.AUTHTOKENTYPE);
            Log.i("AuthenUtils:", "mAccountList" + account.name + " " + account.toString());
            Log.i("AuthenUtils:", "token" + token);
        }

        return token;
    }


    public static String blockingGetAuthToken(Context context) {
        String token = null;
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());

        Account[] mAccountList = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account mAccount = null;
        if(mAccountList.length > 0){
            mAccount = mAccountList[0];
        }
        try {
            token = mAccountManager.blockingGetAuthToken(mAccount, AccountGeneral.AUTHTOKENTYPE, false);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
        return token;
    }

    public static void invalidateAuthToken(Context context) {
        String token = null;
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());
        Account[] mAccountList = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account mAccount = null;
        if(mAccountList.length == 1){
            mAccount = mAccountList[0];
            token = mAccountManager.peekAuthToken(mAccount, AccountGeneral.AUTHTOKENTYPE);
            mAccountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, token);
            Log.i("AuthenUtils:", "token " + token + " has been invalidated");
        } else if (mAccountList.length > 1) {
            SharedPreferences pref = context.getSharedPreferences(AppGeneral.PREF_NAME, Context.MODE_PRIVATE);
            String mPhoneNumber = pref.getString(AppGeneral.USER_MOBILE_PHONE_NUMBER, null);
            Account account = new Account(mPhoneNumber, AccountGeneral.ACCOUNT_TYPE);
            token = mAccountManager.peekAuthToken(account, AccountGeneral.AUTHTOKENTYPE);
            mAccountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, token);
            Log.i("AuthenUtils:", "token " + token + " has been invalidated");
        }

    }

    public static Boolean checkIfEnteredAccountInfoBefore(Context context) {
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());
        Account[] mAccountList = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        if(mAccountList.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void removeAccount(Context context) {
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());
        Account[] mAccountList = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account mAccount = null;
        if(mAccountList.length > 0){
            SharedPreferences pref = context.getSharedPreferences(AppGeneral.PREF_NAME, Context.MODE_PRIVATE);
            String mPhoneNumber = pref.getString(AppGeneral.USER_MOBILE_PHONE_NUMBER, null);
            mAccount = new Account(mPhoneNumber, AccountGeneral.ACCOUNT_TYPE);
            mAccountManager.removeAccount(mAccount, null, null, null);
        }
    }
}
