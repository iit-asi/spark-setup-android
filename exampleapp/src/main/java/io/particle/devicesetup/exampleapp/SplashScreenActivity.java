package io.particle.devicesetup.exampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.devicesetup.ParticleDeviceSetupLibrary;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.SEGAnalytics;

public class SplashScreenActivity extends AppCompatActivity {

    /** Splash screen duration time in milliseconds */
    private static final int DELAY = 1000;
    private ParticleCloud sparkCloud;
    private Async.AsyncApiWorker<ParticleCloud, Void> loginTask = null;
    private View bgImage;

    private static final String EMAIL = "jboal@comillas.edu";
    private static final String PASS = "200503729";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ParticleDeviceSetupLibrary.init(this.getApplicationContext());
        sparkCloud = ParticleCloudSDK.getCloud();
        bgImage = findViewById(R.id.splashBgImage);
        // Jump to MainActivity after DELAY milliseconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                attemptLogin();

            }
        }, DELAY);
    }

    @Override
    public void onBackPressed() {
        // do nothing. Protect from exiting the application when splash screen is shown
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    /**
     * Attempts to sign in the account specified by the login form.
     */
    private void login(String email, String password) {

        loginTask = Async.executeAsync(sparkCloud, new Async.ApiWork<ParticleCloud, Void>() {
            @Override
            public Void callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException {
                sparkCloud.logIn(email, password);
                return null;
            }

            @Override
            public void onTaskFinished() {
                loginTask = null;
            }

            @Override
            public void onSuccess(@NonNull Void result) {
                SEGAnalytics.identify(email);
                SEGAnalytics.track("Auth: Login success");
                ParticleDeviceSetupLibrary.startDeviceSetup(SplashScreenActivity.this, SplashScreenActivity.class);
                finish();
            }

            @Override
            public void onFailure(@NonNull ParticleCloudException error) {
                SEGAnalytics.track("Auth: Login failure");
                Snackbar.make(bgImage, "Conexión fallida", Snackbar.LENGTH_LONG)
                        //.setActionTextColor(Color.CYAN)
                        //.setActionTextColor(getResources().getColor(R.color.snackbar_action))
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                attemptLogin();
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (sparkCloud.getAccessToken() == null) {
            if (loginTask != null) {
                return;
            }

            login(EMAIL, PASS);
        }else{
            ParticleDeviceSetupLibrary.startDeviceSetup(SplashScreenActivity.this, SplashScreenActivity.class);
        }
    }
}
