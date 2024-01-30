package com.otpless.fedo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Base64;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.otpless.BuildConfig;
import com.otpless.fedo.models.OtplessPublicKeyCredential;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteData;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteRequest;
import com.otpless.fedo.models.WebAuthnRegistrationInitData;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;
import com.otpless.fedo.models.WebAuthnUser;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;

import java.util.ArrayList;

public class OtplessWebAuthnManager {

    private static String TAG = "OtplessWAM";

    private static final int WEBAUTHN_REGISTER_REQUEST_CODE = 9767355;
    private static final int BASE64_FLAG = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    private final Fido2ApiClient fidoApiClient;
    private final Activity activity;

    private String lastRequestId = "";

    private ApiCallback<PendingIntent> callback;

    public OtplessWebAuthnManager(final Activity activity) {
        fidoApiClient = Fido.getFido2ApiClient(activity);
        this.activity = activity;
    }

    public void initRegistration(final WebAuthnRegistrationInitRequest request, ApiCallback<PendingIntent> callback) {
        this.callback = callback;
        ApiManager.getInstance().initWebAuthnRegistration(request, new ApiCallback<WebAuthnBaseResponse<WebAuthnRegistrationInitData>>() {
            @Override
            public void onSuccess(WebAuthnBaseResponse<WebAuthnRegistrationInitData> data) {
                lastRequestId = data.getRequestId();
                PublicKeyCredentialCreationOptions publicKey = makePublicKeyOption(data.getData());
                fidoApiClient.getRegisterPendingIntent(publicKey)
                        .addOnSuccessListener(pendingIntent -> {
                            try {
                                activity.startIntentSenderForResult(
                                        pendingIntent.getIntentSender(),WEBAUTHN_REGISTER_REQUEST_CODE, null, 0, 0, 0
                                );
                            } catch (IntentSender.SendIntentException e) {
                                callback.onError(e);
                            }
                        })
                        .addOnFailureListener(callback::onError);
            }

            @Override
            public void onError(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode != WEBAUTHN_REGISTER_REQUEST_CODE) return;
        if (resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                callback.onError(new Exception("error intent data"));
                return;
            }
            byte[] data = intent.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA);
            if (data == null) {
                callback.onError(new Exception("error byte array data"));
                return;
            }
            final PublicKeyCredential credential = PublicKeyCredential.deserializeFromBytes(data);
            if (credential.getResponse() instanceof AuthenticatorErrorResponse) {
                this.callback.onError(new Exception(credential.getResponse().toString()));
            } else {
                final WebAuthnRegistrationCompleteRequest request = new WebAuthnRegistrationCompleteRequest(
                        this.lastRequestId, encodeBase64(credential.getRawId())
                );
                completeRegistration(request);
            }
            return;
        }
        callback.onError(new Exception("User cancelled"));
    }

    void completeRegistration(final WebAuthnRegistrationCompleteRequest request) {
        ApiManager.getInstance().completeWebAuthnRegistration(request, new ApiCallback<WebAuthnBaseResponse<WebAuthnRegistrationCompleteData>>() {
            @Override
            public void onSuccess(WebAuthnBaseResponse<WebAuthnRegistrationCompleteData> data) {

            }

            @Override
            public void onError(Throwable exception) {
                OtplessWebAuthnManager.this.callback.onError(exception);
            }
        });
    }


    private PublicKeyCredentialCreationOptions makePublicKeyOption(final WebAuthnRegistrationInitData registrationInitData) {
        final PublicKeyCredentialCreationOptions.Builder builder = new PublicKeyCredentialCreationOptions.Builder();
        // setting authn user
        final WebAuthnUser authnUser = registrationInitData.getUser();
        final PublicKeyCredentialUserEntity user = new PublicKeyCredentialUserEntity(
                decodeBase64(authnUser.getId()), authnUser.getName(), null, authnUser.getDisplayName()
        );
        builder.setUser(user);
        // setting for RP
        final WebAuthnUser rp = registrationInitData.getRp();
        final PublicKeyCredentialRpEntity rpEntity = new PublicKeyCredentialRpEntity(
                rp.getId(), rp.getName(), null
        );
        builder.setRp(rpEntity);
        // setting challenge
        builder.setChallenge(decodeBase64(registrationInitData.getChallenge()));
        // setting timeout
        builder.setTimeoutSeconds((double) registrationInitData.getTimeout());
        // setting public credentials
        final ArrayList<PublicKeyCredentialParameters> arrayList = new ArrayList<>();
        for (final OtplessPublicKeyCredential credential : registrationInitData.getPubKeyCredParams()) {
            arrayList.add(new PublicKeyCredentialParameters(credential.getType(), credential.getAlgorithm()));
        }
        builder.setParameters(arrayList);
        // setting the authentication selection
        final AuthenticatorSelectionCriteria.Builder authenticationBuilder = new AuthenticatorSelectionCriteria.Builder();
        try {
            authenticationBuilder.setAttachment(Attachment.fromString(registrationInitData.getAuthenticatorSelection().getAuthenticatorAttachment()));
        } catch (Attachment.UnsupportedAttachmentException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return builder.build();
    }

    private byte[] decodeBase64(final String base64) {
        return Base64.decode(base64, BASE64_FLAG);
    }

    private String encodeBase64(final byte[] data) {
        return Base64.encodeToString(data, BASE64_FLAG);
    }
}
