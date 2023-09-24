package com.ivoriechat.android.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ivoriechat.android.R;
import com.ivoriechat.android.authentication.AuthenUtils;
import com.ivoriechat.android.utils.AppGeneral;
import com.ivoriechat.android.utils.ImageUtils;
import com.ivoriechat.android.utils.RoundedCornersTransform;
import com.ivoriechat.android.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerificationActivity extends AppCompatActivity {

    private EditText mUserRealNameText;
    private String mUserRealName;

    private Button mSelectPortraitButton;
    private ImageView mPortraitImageView;
    private RadioGroup mUsageRadioGroup;
    private byte[] mPortraitRawData;

    private EditText mSkillsInputText;
    private ChipGroup mSkillChipGroup;
    private ArrayList<String> mExpertiseList = new ArrayList();
    private Integer mChipsTotalLength = 0;

    private ArrayAdapter mHourlyRateAdapter;

    private AutoCompleteTextView mHourlyRateSelect;
    private String mHourlyRate;

    private TextView mExpectedUsageHintText;
    private String mExpectedUsage;

    private EditText mSelfIntroductionText;
    private String mSelfIntroduction;

    private TextView mHourlyRateHintText;
    private TextView mSkillsHintText;

    private Button mSubmitButton;

    public static int SELECT_IMAGE_INTENT_ID = 1000;
    private static final String TAG = "VerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mUserRealNameText = findViewById(R.id.user_name_edit_text);

        mSelectPortraitButton = findViewById(R.id.select_portrait_button);
        mSelectPortraitButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image_source)), SELECT_IMAGE_INTENT_ID);
        });

        mPortraitImageView = findViewById(R.id.portrait_imageview);

        mExpectedUsageHintText = findViewById(R.id.expected_usage_notes);
        mHourlyRateHintText = findViewById(R.id.hourly_rate_hint_text);
        mSkillsHintText = findViewById(R.id.skills_hint_text);

        mUsageRadioGroup = findViewById(R.id.expected_usage_radio_group);
        mUsageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = findViewById(checkedId);
            String usage = radioButton.getText().toString();
            if (TextUtils.equals(usage, getText(R.string.chat_to_earn))) {
                mExpectedUsage = "chat2earn";
                mExpectedUsageHintText.setText(R.string.chat_to_earn_hint);
                mHourlyRateHintText.setText(R.string.hourly_rate_hint_for_chat_to_earn);
                mSkillsHintText.setText(R.string.skills_hint_for_chat_to_earn);
            } else if (TextUtils.equals(usage, getText(R.string.pay_to_chat))) {
                mExpectedUsage = "pay2chat";
                mExpectedUsageHintText.setText(R.string.pay_to_chat_hint);
                mHourlyRateHintText.setText(R.string.hourly_rate_hint_for_pay_to_chat);
                mSkillsHintText.setText(R.string.skills_hint_for_pay_to_chat);
            }
        });

        String[] hourlyRates = getResources().getStringArray(R.array.hourly_rate_in_usd);
        mHourlyRateAdapter = new ArrayAdapter<>(this, R.layout.exposed_dropdown_menu, hourlyRates);
        mHourlyRateSelect = findViewById(R.id.hourly_rate_select);
        mHourlyRateSelect.setAdapter(mHourlyRateAdapter);

        mSkillChipGroup = findViewById(R.id.skills_chip_group);
        mSkillsInputText = findViewById(R.id.skills_input_text);
        mSkillsInputText.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // convert the input text into a chip and add it to the chip group
                ChipDrawable chip = ChipDrawable.createFromResource(this, R.xml.standalone_chip);
                Editable inputText = mSkillsInputText.getText();
                String skill = inputText.subSequence(mChipsTotalLength, inputText.toString().length()).toString();
                chip.setText(skill);
                chip.setCloseIconVisible(false);
                chip.setChipBackgroundColorResource(R.color.ivorie_theme);
                chip.setTextColor(Color.WHITE);
                chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
                ImageSpan span = new ImageSpan(chip);
                inputText.setSpan(span, mChipsTotalLength, inputText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (!TextUtils.isEmpty(skill)) {
                    mExpertiseList.add(skill);
                    mChipsTotalLength = inputText.toString().length();
                }
                return true;
            }

            // If the event is a key-down event on the "del" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_DEL)) {
                // delete the last item from mExpertiseList
                if (mExpertiseList.size() > 0) {
                    mExpertiseList.remove(mExpertiseList.size()-1);
                }

                Integer totalSize = 0;
                for (String skill: mExpertiseList) {
                    totalSize = totalSize + skill.length();
                }
                mChipsTotalLength = totalSize;
                // false, indicating that this listener has not consumed the event, so that the last entered chip can be deleted by the delete operation
                return false;
            }

            return false;
        });

        mSelfIntroductionText = findViewById(R.id.self_description_edit_text);

        mSubmitButton = findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(v -> {
            onSubmit();
        });
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == SELECT_IMAGE_INTENT_ID) && (resultCode == Activity.RESULT_OK) && (data != null)){
            Uri returnUri = data.getData();
            // Retrieve a File's MIME Type
            String mimeType = getContentResolver().getType(returnUri);
            if (mimeType != null) {
                // Asynchronously loads the contact image
                RoundedCornersTransform picassoTransform = new RoundedCornersTransform();
                // 设置round corner的radius
                picassoTransform.setRadius(8);
                Picasso.get()
                        .load(returnUri)
                        .resize(128, 128)
                        .centerCrop() // Crops an image inside of the bounds specified by resize(int, int) rather than distorting the aspect ratio.
                        .transform(picassoTransform)
                        .into(mPortraitImageView);

                try {
                    mPortraitRawData = ImageUtils.getBytesFromFileURI(this, returnUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Boolean validateFields() {
        View focusView = null;
        boolean valid = true;
        if (TextUtils.isEmpty(mSelfIntroductionText.getText().toString())) {
            mSelfIntroductionText.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mSelfIntroductionText;
            valid = false;
        }

        if (TextUtils.isEmpty(mSkillsInputText.getText().toString())) {
            mSkillsInputText.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mSkillsInputText;
            valid = false;
        }

        if (TextUtils.isEmpty(mHourlyRateSelect.getText().toString())) {
            mHourlyRateSelect.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mHourlyRateSelect;
            valid = false;
        }

        if (TextUtils.isEmpty(mExpectedUsage)) {
            focusView = mUsageRadioGroup;
            valid = false;
        }

        if (mPortraitRawData.length == 0) {
            mSelectPortraitButton.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mSelectPortraitButton;
            valid = false;
        }

        if (TextUtils.isEmpty(mUserRealNameText.getText().toString())) {
            mUserRealNameText.setError(getResources().getString(R.string.error_input_field_empty));
            focusView = mUserRealNameText;
            valid = false;
        }

        if(!valid) {
            focusView.requestFocus();
        }

        return valid;
    }

    private void collectFields() {
        mUserRealName = mUserRealNameText.getText().toString();
        mHourlyRate = mHourlyRateSelect.getText().toString();
        mSelfIntroduction = mSelfIntroductionText.getText().toString();
    }

    private void onSubmit() {
        // validate input fields
        Boolean inputValid = validateFields();
        if(!inputValid) {
            Snackbar.make(mSubmitButton, R.string.error_input_field_empty, Snackbar.LENGTH_LONG).show();
            return;
        }

        collectFields();

        InProgressFragment inProgressFragment = new InProgressFragment();
        inProgressFragment.show(getSupportFragmentManager(), AppGeneral.IN_PROGRESS_FRAGMENT);
        new ApplyTask().execute();
    }

    private class ApplyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            URL url = Utils.constructURL(AppGeneral.APPLY_FOR_VERIFICATION_API);

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            JsonObject json = new JsonObject();
            json.addProperty(AppGeneral.USER_REAL_NAME, mUserRealName);
            json.addProperty(AppGeneral.HOURLY_RATE, mHourlyRate);
            json.addProperty(AppGeneral.EXPECTED_USAGE, mExpectedUsage);
            json.addProperty(AppGeneral.SELF_INTRODUCTION, mSelfIntroduction);

            // json.addProperty(AppGeneral.PORTRAIT_ATTACHED, Boolean.TRUE.toString());
            byte[] encodedImageData = Base64.getEncoder().encode(mPortraitRawData);
            // json.addProperty(AppGeneral.PORTRAIT_FILE, new String(encodedImageData));
            // json.addProperty(AppGeneral.FILE_SIZE, mPortraitRawData.length);

            String authToken = AuthenUtils.peekAuthToken(getApplicationContext());
            String responseString = null;

            OkHttpClient client = new OkHttpClient();
            RequestBody body = null;
            try {
                body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(AppGeneral.APPLICATION_DATA, URLEncoder.encode(gson.toJson(json), "UTF-8"))
                        .addFormDataPart(AppGeneral.EXPERTISE_LIST, URLEncoder.encode(gson.toJson(mExpertiseList), "UTF-8"))
                        .addFormDataPart(AppGeneral.PORTRAIT_ATTACHED, Boolean.TRUE.toString())
                        .addFormDataPart(AppGeneral.PORTRAIT_FILE, new String(encodedImageData))
                        .addFormDataPart(AppGeneral.FILE_SIZE, Integer.toString(mPortraitRawData.length))
                        .build();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Request request = new Request.Builder()
                    .url(url)
                    .header(AppGeneral.AUTHORIZATION, authToken)
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
        protected void onPostExecute(String responseString){
            InProgressFragment loadingInProgressFragment = ((InProgressFragment) getSupportFragmentManager().findFragmentByTag(AppGeneral.IN_PROGRESS_FRAGMENT));
            if (loadingInProgressFragment != null) {
                loadingInProgressFragment.dismiss();
            }

            JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();
            Boolean result = json.get(AppGeneral.RESULT).getAsBoolean();

            Snackbar snackbar;
            if (result) {
                snackbar = Snackbar.make(findViewById(R.id.container_layout),
                        R.string.submit_success, Snackbar.LENGTH_LONG);
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            finish();
                        }
                    }
                });
            } else {
                snackbar = Snackbar.make(findViewById(R.id.container_layout),
                        R.string.submit_failure, Snackbar.LENGTH_LONG);
            }
            snackbar.show();
        }
    }

}