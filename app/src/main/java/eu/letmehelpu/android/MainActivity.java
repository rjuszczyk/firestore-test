package eu.letmehelpu.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import eu.letmehelpu.android.conversation.ConversationActivity;
import eu.letmehelpu.android.conversationlist.ConversationListActivity;
import eu.letmehelpu.android.messaging.MessagingManager;
import eu.letmehelpu.android.messaging.MessagingTokenStoreage;
import eu.letmehelpu.android.messaging.UserIdStoreage;
import eu.letmehelpu.android.network.Helper;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Button next = findViewById(R.id.next);
        final TextView userId = findViewById(R.id.user_id);
        userId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(!TextUtils.isEmpty(textView.getText())) {
                    navigateNext(Integer.parseInt(textView.getText().toString()));
                }
                return false;
            }
        });

        userId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                next.setEnabled(!TextUtils.isEmpty(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessagingManager manager = new MessagingManager(new UserIdStoreage(getSharedPreferences("messaging", MODE_PRIVATE)), new MessagingTokenStoreage(getSharedPreferences("messaging", MODE_PRIVATE)));
                manager.putUserId(Long.valueOf(userId.getText().toString()));
                navigateNext(Integer.parseInt(userId.getText().toString()));
            }
        });
    }

    private void navigateNext(int userId) {
        startActivity(ConversationListActivity.Companion.createIntent(this, userId));
    }
}
