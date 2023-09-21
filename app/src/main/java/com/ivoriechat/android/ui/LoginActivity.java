package com.ivoriechat.android.ui;

import android.os.Bundle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ivoriechat.android.R;
import com.ivoriechat.android.entities.ValidationCodeResponse;
import com.ivoriechat.android.utils.AppGeneral;
import com.ivoriechat.android.authentication.AccountGeneral;
import com.ivoriechat.android.utils.Utils;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AccountAuthenticatorActivity implements AppCompatCallback {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTaskRevised mAuthTask = null;

    private String mPhoneNumber;
    private String mCountryCallingCode;
    private String mValidationCode;

    // UI references
    private EditText mPhoneNumberView;
    private EditText mValidationCodeView;
    private Button mObtainValidationCodeButton;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    private AutoCompleteTextView mCountryCallingCodeSelect;
    private ArrayAdapter mCountryCallingCodeAdapter;
    private Map<String,String> COUNTRY_CALLING_CODE_MAP; // a map from readable country calling code to actual calling code itself

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private String mAccountType;
    private Account mAccount;
    private static final String TAG = "LoginActivity";
    private CheckBox agreeCheckbox;
    private Boolean termsAndConditionsAgreed = false;

    private Boolean codeSentSuccess = false;

    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_login);

        mAccountType = AppGeneral.ACCOUNT_TYPE;

        mAccountManager = AccountManager.get(getApplicationContext());
        Account[] mAccountList = mAccountManager.getAccountsByType(AppGeneral.ACCOUNT_TYPE);
        if(mAccountList.length > 0){
            if(mAccountList.length == 1) {
                mAccount = mAccountList[0];
                // mEncryptedPassword = mAccountManager.getPassword(mAccount);
                mPhoneNumber = mAccount.name;
                // Log.i(TAG, "mEncryptedPassword=" + mEncryptedPassword);
                Log.i(TAG, "mPhoneNumber=" + mPhoneNumber);
            } else {
                SharedPreferences pref = getSharedPreferences(AppGeneral.PREF_NAME, Context.MODE_PRIVATE);
                mPhoneNumber = pref.getString(AppGeneral.USER_MOBILE_PHONE_NUMBER, null);
                for(Account account: mAccountList) {
                    if(TextUtils.equals(account.name, mPhoneNumber)) {
                        mAccount = account;
                        // mEncryptedPassword = mAccountManager.getPassword(mAccount);
                    }
                }
            }
        }

        if (getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
            mPhoneNumber = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        }

        // Set up the login form.
        mPhoneNumberView = findViewById(R.id.phone_number);

        mValidationCodeView = findViewById(R.id.validation_code);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                });

        mObtainValidationCodeButton = findViewById(R.id.obation_code_button);
        mObtainValidationCodeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        obtainValidationCode();
                    }
                });

        if(mPhoneNumber != null){
            mPhoneNumberView.setText(mPhoneNumber);
        }

        agreeCheckbox = findViewById(R.id.agreeCheckBox);
        agreeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                termsAndConditionsAgreed = true;
            } else {
                termsAndConditionsAgreed = false;
            }
        });

        String[] countryCodeArray = getResources().getStringArray(R.array.country_calling_code);
        mCountryCallingCodeAdapter = new ArrayAdapter<>(this, R.layout.exposed_dropdown_menu, countryCodeArray);
        mCountryCallingCodeSelect = findViewById(R.id.country_calling_code_select);
        mCountryCallingCodeSelect.setAdapter(mCountryCallingCodeAdapter);
    }

    public void onTermsAndConditions(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TermsAndConditionsActivity.class);
        startActivity(intent);
    }

    public void onPrivacyPolicies(View view) {
        Intent intent = new Intent();
        intent.setClass(this, PrivacyAgreementActivity.class);
        startActivity(intent);
    }


    /**
     * Validate input fields before sending request for one-time password
     * @return
     */
    private Boolean validateFieldsForOTP() {
        // Reset errors.
        mCountryCallingCodeSelect.setError(null);
        mPhoneNumberView.setError(null);
        mValidationCodeView.setError(null);

        // Store phone number at the time of this obtaining validation code attempt.
        mPhoneNumber = mPhoneNumberView.getText().toString();
        mCountryCallingCode = prepareCountryCallingCode(mCountryCallingCodeSelect.getText().toString());

        boolean valid = false;
        View focusView = null;

        if (TextUtils.isEmpty(mCountryCallingCodeSelect.getText().toString())) {
            mCountryCallingCodeSelect.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mCountryCallingCodeSelect;
            valid = false;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_input_field_empty));
            focusView = mPhoneNumberView;
            valid = false;
        } else {
            Pattern p = Pattern.compile("\\d{11}");
            Matcher m = p.matcher(mPhoneNumber);
            if(!m.matches()){
                mPhoneNumberView.setError(getString(R.string.error_invalid_phone_number));
                focusView = mPhoneNumberView;
                valid = false;
            }
        }

        if (!valid) {
            // There was an error; don't attempt to get the code and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        return valid;
    }

    private Boolean validateFields() {
        boolean valid = false;
        View focusView = null;

        // Reset errors.
        mPhoneNumberView.setError(null);
        mValidationCodeView.setError(null);

        // Store values at the time of the login attempt.
        mCountryCallingCode = prepareCountryCallingCode(mCountryCallingCodeSelect.getText().toString());
        mPhoneNumber = mPhoneNumberView.getText().toString();
        mValidationCode = mValidationCodeView.getText().toString();

        // Check for a valid country calling code
        if (TextUtils.isEmpty(mCountryCallingCode)) {
            mCountryCallingCodeSelect.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mCountryCallingCodeSelect;
            valid = false;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_phone_number_empty));
            focusView = mPhoneNumberView;
            valid = false;
        } else {
            Pattern p = Pattern.compile("\\d{8,14}");
            Matcher m = p.matcher(mPhoneNumber);
            if(!m.matches()){
                mPhoneNumberView.setError(getString(R.string.error_invalid_phone_number));
                focusView = mPhoneNumberView;
                valid = false;
            }
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(mValidationCode)) {
            mValidationCodeView.setError(getString(R.string.error_validation_code_empty));
            focusView = mValidationCodeView;
            valid = false;
        } else if (mValidationCode.length() < 4) {
            mValidationCodeView.setError(getString(R.string.error_validation_code_too_short));
            focusView = mValidationCodeView;
            valid = false;
        }

        if(!valid) {
            focusView.requestFocus();
        }

        return valid;
    }

    /**
     * Strip the prefix country abstraction name and the plus sign
     */
    private String prepareCountryCallingCode(String callingCode) {
        if (callingCode == null)
            return null;

        String sanitizedCallingCode = callingCode.split("\\+")[1];
        return sanitizedCallingCode;
    }

    private void obtainValidationCode() {
        Boolean valid = validateFieldsForOTP();
        if (valid) {
            new ObtainValidationCodeTask().execute();
            mObtainValidationCodeButton.setEnabled(false);
            countDown();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (!termsAndConditionsAgreed) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_body_layout),
                    R.string.check_terms_and_conditions_hint, Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        Boolean valid = validateFields();

        if (valid) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            // hide the Android Soft Keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLoginStatusView.getWindowToken(), 0);

            mAuthTask = new UserLoginTaskRevised(getApplicationContext());
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mLoginStatusView.setVisibility(View.VISIBLE);
        mLoginStatusView.animate().setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginStatusView.setVisibility(show ? View.VISIBLE
                                : View.GONE);
                    }
                });

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE
                                : View.VISIBLE);
                    }
                });
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTaskRevised extends AsyncTask<Void, Void, String>{
        private Context myContext;
        private SharedPreferences pref;
        private SharedPreferences.Editor editor;
        private UserLoginTaskRevised(Context ctx){
            this.myContext = ctx;
        }

        @Override
        protected String doInBackground(Void... voids) {
            URL url = Utils.constructURL(AppGeneral.LOGIN_WITH_SMS_API);

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            JsonObject json = new JsonObject();
            json.addProperty(AppGeneral.COUNTRY_CALLING_CODE, mCountryCallingCode);
            json.addProperty(AppGeneral.USER_MOBILE_PHONE_NUMBER, mPhoneNumber);
            json.addProperty(AppGeneral.VALIDATION_CODE, mValidationCode);

            String responseString = null;

            MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(gson.toJson(json), MEDIA_TYPE_JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        //Runs on the UI thread after doInBackground. The specified result is the value returned by doInBackground.
        @Override
        protected void onPostExecute(String authFeedback){
            Bundle data = new Bundle();
            // Integer ownerIDinDB = null;
            String authToken = null;
            Integer userId = null; // USER_ID

            //Create a json object from the response string
            JSONObject jObject = null;

            if(authFeedback == null){
                // the server has returned an empty inputstream
                mPhoneNumberView.setError(getString(R.string.error_server_no_response));
                mPhoneNumberView.requestFocus();
                // simply show and hide the relevant UI components.
                showProgress(false);
            } else {
                try {
                    jObject = new JSONObject(authFeedback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try{
                    if(jObject != null){
                        // Fetch the response header "loginSuccess"
                        // Check if it is true
                        if (jObject.getBoolean(AppGeneral.LOGIN_SUCCESS)){
                            authToken = jObject.getString(AppGeneral.TOKEN);
                            userId = jObject.getInt(AppGeneral.USER_ID);
                            AccountGeneral.TokenValid = true;
                        } else if (!jObject.getBoolean(AppGeneral.USER_EXIST)){
                            authToken = AppGeneral.USER_NOT_EXIST;
                        } else if (!jObject.getBoolean(AppGeneral.SMS_CODE_CORRECT)){
                            authToken = AppGeneral.INVALIDE_VALIDATION_CODE;
                        }
                    }
                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if(!authToken.isEmpty()){
                if(authToken.equals(AppGeneral.INVALIDE_VALIDATION_CODE)){
                    mValidationCodeView.setError(getString(R.string.error_validation_code_incorrect));
                    mValidationCodeView.setText("");
                    mValidationCodeView.requestFocus();
                    // simply show and hide the relevant UI components.
                    showProgress(false);
                } else {
                    // mAccountManager.addAccountExplicitly()

                    pref = myContext.getSharedPreferences(AppGeneral.PREF_NAME, Context.MODE_PRIVATE);
                    editor = pref.edit();
                    // editor.putString(AppGeneral.OWNER_ID, ownerIDinDB);
                    // editor.putInt(AppGeneral.OWNER_ID, ownerIDinDB);
                    editor.putInt(AppGeneral.USER_ID, userId);
                    editor.putString(AppGeneral.USER_MOBILE_PHONE_NUMBER, mPhoneNumber);

                    // Commit the editing changes
                    editor.apply();

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, mPhoneNumber);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                    // long tokenExpireTime = System.currentTimeMillis() + 30000;
                    // data.putLong(AbstractAccountAuthenticator.KEY_CUSTOM_TOKEN_EXPIRY, tokenExpireTime);
                    Account account = new Account(mPhoneNumber, mAccountType);
                    // Log.i("before problem: ", mAccountType);
                    mAccountManager.addAccountExplicitly(account, authToken, null);
                    // Above is where the problem is located.
                    mAccountManager.setPassword(account, authToken);
                    // Adds an auth token to the AccountManager cache for an account.
                    mAccountManager.setAuthToken(account, AppGeneral.AUTHTOKENTYPE, authToken);
                    System.out.println("Authtoken has been set:" + authToken);
                    // Sets one userdata key for an account. Intended by use for the authenticator to stash state for itself, not directly by application
                    // mAccountManager.setUserData(account, AccountGeneral.OWNER_ID_IN_DB, Integer.toString(ownerIDinDB));
                    //Start the chat service after login successfully
                    // startChatService();
                    setAccountAuthenticatorResult(data);
                    setResult(RESULT_OK);
                    finish();
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(myContext, MainActivity.class);
                    startActivity(intent);
                }
            }
        }

    }

    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Log.i("LoginActivity", "New intent with flags "+intent.getFlags());
    }

    private class ObtainValidationCodeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            URL url=null;
            try {
                url = new URL(AppGeneral.SERVER_SECURE_PROTOCOL + AppGeneral.SERVER_DOMAIN_NAME + ":" + AppGeneral.SERVER_PORT_NUMBER + AppGeneral.WEB_MODULE_PATH
                        + AppGeneral.VALIDATION_CODE_API);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            JsonObject json = new JsonObject();
            json.addProperty(AppGeneral.USER_MOBILE_PHONE_NUMBER, gson.toJson(mPhoneNumber));
            json.addProperty(AppGeneral.COUNTRY_CALLING_CODE, gson.toJson(mCountryCallingCode));

            String responseString = null;

            MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(gson.toJson(json), MEDIA_TYPE_JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String serverFeedback) {
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            if(serverFeedback != null) {
                ValidationCodeResponse response = gson.fromJson(serverFeedback, ValidationCodeResponse.class);
                codeSentSuccess = response.getValidation_code_sent();
            }

            Snackbar snackbar;
            if (codeSentSuccess) {
                snackbar = Snackbar.make(findViewById(R.id.main_body_layout),
                        R.string.obtain_code_success, Snackbar.LENGTH_LONG);
            } else {
                snackbar = Snackbar.make(findViewById(R.id.main_body_layout),
                        R.string.obtain_code_failure, Snackbar.LENGTH_LONG);
            }
            snackbar.show();
        }
    }

    private void countDown() {
        long endTime = System.currentTimeMillis() + 60000;
        //runs without a timer by reposting this handler at the end of the runnable
        Handler timerHandler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = endTime - System.currentTimeMillis();
                int seconds = (int) (millis / 1000);
                // seconds = seconds % 60;
                mObtainValidationCodeButton.setText(Integer.toString(seconds) + "秒后重新获取");
                mObtainValidationCodeButton.setTextColor(getResources().getColor(R.color.text_entered));
                timerHandler.postDelayed(this, 500);
                if (millis <= 0) {
                    timerHandler.removeCallbacks(this);
                    mObtainValidationCodeButton.setEnabled(true);
                    mObtainValidationCodeButton.setText(R.string.obtain_validation_code);
                    mObtainValidationCodeButton.setTextColor(getResources().getColor(R.color.text_label));
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }
}
