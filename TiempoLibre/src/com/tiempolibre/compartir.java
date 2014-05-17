package com.tiempolibre;

import java.util.Arrays;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;

public class compartir extends Activity {
	private UiLifecycleHelper uiHelper;
	private String URL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compartir);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Button compartir = (Button) findViewById(R.id.compartirfacebook);
		LoginButton authButton = (LoginButton) findViewById(R.id.login_button);
		StatusCallback callback = new CallBackHandler();
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		compartir.setBackgroundResource(R.drawable.facebook);
		Bundle bundle = getIntent().getExtras();
		URL = new String(bundle.getString("url"));

		authButton.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(FacebookException error) {

			}
		});

		authButton.setReadPermissions(Arrays.asList("basic_info", "email"));

		authButton.setSessionStatusCallback(new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {

				if (session.isOpened()) {

				} else if (session.isClosed()) {

				}
			}
		});

		compartir.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				click(v);
			}
		}

		);

	}

	private class CallBackHandler implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

			Log.d("facebook", "changing");

			Log.d("facebook state", state.toString());

			if (state.isOpened()) {
				Log.d("facebook", "facebook connected");
			}

			if (state.isClosed()) {
				Log.d("facebook", "facebook disconnected");
			}
		}
	}

	public void click(View v) {
		try {
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
					this).setLink(URL).build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("TAG ID", "No pudo!!");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						Log.e("Activity",
								String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						Log.i("Activity", "Success!");
					}
				});

	}
}
