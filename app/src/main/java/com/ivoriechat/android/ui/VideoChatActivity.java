package com.ivoriechat.android.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ivoriechat.android.R;
import com.ivoriechat.android.utils.AppGeneral;

public class VideoChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        if (savedInstanceState == null) {
            VideoChatFragment videoChatFragment = new VideoChatFragment();
            videoChatFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, videoChatFragment, AppGeneral.VIDEO_CHAT_FRAGMENT)
                    .commit();
        }
    }
}