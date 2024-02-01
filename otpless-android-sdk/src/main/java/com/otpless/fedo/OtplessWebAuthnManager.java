package com.otpless.fedo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Base64;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.otpless.BuildConfig;
import com.otpless.fedo.models.OtplessPublicKeyCredential;
import com.otpless.fedo.models.WebAuthnAllowedCredential;
import com.otpless.fedo.models.WebAuthnAuthenticatorAttestationData;
import com.otpless.fedo.models.WebAuthnAuthenticatorAttestationLoginData;
import com.otpless.fedo.models.WebAuthnLoginCompleteRequest;
import com.otpless.fedo.models.WebAuthnLoginInitData;
import com.otpless.fedo.models.WebAuthnLoginInitRequest;
import com.otpless.fedo.models.WebAuthnPublicCredential;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteData;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteRequest;
import com.otpless.fedo.models.WebAuthnRegistrationInitData;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;
import com.otpless.fedo.models.WebAuthnUser;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OtplessWebAuthnManager {

    private static final int WEBAUTHN_REGISTER_REQUEST_CODE = 9767355;
    private static final int WEBAUTHN_SIGNIN_REQUEST_CODE = 9767356;
    private static final int BASE64_FLAG = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    private final Fido2ApiClient fidoApiClient;
    private final Activity activity;

    private String lastRequestId = "";

    private ApiCallback<JSONObject> callback;

    public OtplessWebAuthnManager(final Activity activity) {
        fidoApiClient = Fido.getFido2ApiClient(activity);
        this.activity = activity;
    }

    public void initRegistration(final WebAuthnRegistrationInitRequest request, ApiCallback<JSONObject> callback) {
        this.callback = callback;
        ApiManager.getInstance().initWebAuthnRegistration(request, new ApiCallback<WebAuthnBaseResponse<WebAuthnRegistrationInitData>>() {
            @Override
            public void onSuccess(WebAuthnBaseResponse<WebAuthnRegistrationInitData> data) {
                lastRequestId = data.getRequestId();
                PublicKeyCredentialCreationOptions publicKey = makePublicKeyCreationOption(data.getData());
                fidoApiClient.getRegisterPendingIntent(publicKey)
                        .addOnSuccessListener(pendingIntent -> {
                            try {
                                activity.startIntentSenderForResult(
                                        pendingIntent.getIntentSender(),WEBAUTHN_REGISTER_REQUEST_CODE, null, 0, 0, 0
                                );
                            } catch (IntentSender.SendIntentException e) {
                                Utility.debugLog(e);
                                callback.onError(e);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Utility.debugLog(e);
                            callback.onError(e);
                        });
            }

            @Override
            public void onError(Throwable exception) {
                Utility.debugLog(exception);
                exception.printStackTrace();
            }
        });
    }

    public void initLogin(final WebAuthnLoginInitRequest request, ApiCallback<JSONObject> callback) {
        this.callback = callback;
        ApiManager.getInstance().initWebAuthnLogin(request, new ApiCallback<WebAuthnBaseResponse<WebAuthnLoginInitData>>() {
            @Override
            public void onSuccess(WebAuthnBaseResponse<WebAuthnLoginInitData> data) {
                lastRequestId = data.getRequestId();
                final PublicKeyCredentialRequestOptions publicKey = makePublicKeyRequestOption(data.getData());
                fidoApiClient.getSignPendingIntent(publicKey)
                        .addOnSuccessListener(pendingIntent -> {
                            try {
                                activity.startIntentSenderForResult(
                                        pendingIntent.getIntentSender(),WEBAUTHN_SIGNIN_REQUEST_CODE, null, 0, 0, 0
                                );
                            } catch (IntentSender.SendIntentException e) {
                                Utility.debugLog(e);
                                callback.onError(e);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Utility.debugLog(e);
                            callback.onError(e);
                        });
            }

            @Override
            public void onError(Throwable exception) {
                Utility.debugLog(exception);
                exception.printStackTrace();
            }
        });
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case WEBAUTHN_REGISTER_REQUEST_CODE:
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
                    final String error = credential.getResponse().toString();
                    Utility.debugLog("public key credential register: " + error);
                    if (credential.getResponse() instanceof AuthenticatorErrorResponse) {
                        this.callback.onError(new Exception(error));
                    } else {
                        final WebAuthnRegistrationCompleteRequest request = makeRegistrationCompleteRequest(credential);
                        completeRegistration(request);
                    }
                    return;
                }
                callback.onError(new Exception("User cancelled"));
                break;
            case WEBAUTHN_SIGNIN_REQUEST_CODE:
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
                    final String error = credential.getResponse().toString();
                    Utility.debugLog("public key credential login: " + error);
                    if (credential.getResponse() instanceof AuthenticatorErrorResponse) {
                        this.callback.onError(new Exception(error));
                    } else {
                        final WebAuthnBaseResponse<WebAuthnLoginCompleteRequest> request = makeLoginCompleteRequest(credential);
                        completeLogin(request);
                    }
                    return;
                }
                callback.onError(new Exception("User cancelled"));
                break;
        }
    }

    void completeRegistration(final WebAuthnRegistrationCompleteRequest request) {
        ApiManager.getInstance().completeWebAuthnRegistration(request, new ApiCallback<WebAuthnRegistrationCompleteData>() {
            @Override
            public void onSuccess(WebAuthnRegistrationCompleteData data) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userId", data.getUserId());
                } catch (JSONException e) {

                } ;
                OtplessWebAuthnManager.this.callback.onSuccess(jsonObject);
            }

            @Override
            public void onError(Throwable exception) {
                OtplessWebAuthnManager.this.callback.onError(exception);
            }
        });
    }

    void completeLogin(final WebAuthnBaseResponse<WebAuthnLoginCompleteRequest> request) {
        ApiManager.getInstance().completeWebAuthnLogin(request, new ApiCallback<WebAuthnRegistrationCompleteData>() {
            @Override
            public void onSuccess(WebAuthnRegistrationCompleteData data) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userId", data.getUserId());
                } catch (JSONException e) {

                } ;
                OtplessWebAuthnManager.this.callback.onSuccess(jsonObject);
            }

            @Override
            public void onError(Throwable exception) {
                OtplessWebAuthnManager.this.callback.onError(exception);
            }
        });
    }


    private PublicKeyCredentialCreationOptions makePublicKeyCreationOption(final WebAuthnRegistrationInitData registrationInitData) {
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

    public PublicKeyCredentialRequestOptions makePublicKeyRequestOption(final WebAuthnLoginInitData initData) {
        PublicKeyCredentialRequestOptions.Builder builder = new PublicKeyCredentialRequestOptions.Builder();
        builder.setChallenge(decodeBase64(initData.getChallenge()));
        // setting allowed credentials
        final ArrayList<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
        for (final WebAuthnAllowedCredential credential: initData.getAllowCredentials()) {
            final ArrayList<Transport> transports = new ArrayList<>();
            for (final String str: credential.getTransports()) {
                try {
                    final Transport transport = Transport.fromString(str);
                    transports.add(transport);
                } catch (Transport.UnsupportedTransportException exception) {
                    Utility.debugLog(exception);
                }
            }

            final PublicKeyCredentialDescriptor descriptor = new PublicKeyCredentialDescriptor(
                    credential.getType(), decodeBase64(credential.getId()) ,  transports
            );
            descriptors.add(descriptor);
        }
        builder.setAllowList(descriptors);
        builder.setRpId(initData.getRpId());
        builder.setTimeoutSeconds((double)initData.getTimeout());
        return builder.build();
    }

    private WebAuthnRegistrationCompleteRequest makeRegistrationCompleteRequest(final PublicKeyCredential credential) {
        final AuthenticatorAttestationResponse response = (AuthenticatorAttestationResponse) credential.getResponse();
        final WebAuthnAuthenticatorAttestationData attestationData = new WebAuthnAuthenticatorAttestationData(
                encodeBase64(response.getClientDataJSON()), encodeBase64(response.getAttestationObject())
        );
        final WebAuthnPublicCredential webAuthnPublicCredential = new WebAuthnPublicCredential(
                credential.getId(),    encodeBase64(credential.getRawId()), credential.getType(), attestationData
        ) ;
        return new WebAuthnRegistrationCompleteRequest(this.lastRequestId, webAuthnPublicCredential);
    }

    private WebAuthnBaseResponse<WebAuthnLoginCompleteRequest> makeLoginCompleteRequest(final PublicKeyCredential credential) {
        final AuthenticatorAssertionResponse response = (AuthenticatorAssertionResponse) credential.getResponse();
        String userHandle = null;
        if (response.getUserHandle() != null) {
            userHandle = encodeBase64(response.getUserHandle());
        }
        final WebAuthnAuthenticatorAttestationLoginData attestationLoginData = new WebAuthnAuthenticatorAttestationLoginData(
                encodeBase64(response.getClientDataJSON()), encodeBase64(response.getAuthenticatorData()),
                encodeBase64(response.getSignature()), userHandle
        );
        final WebAuthnLoginCompleteRequest webAuthnPublicCredential = new WebAuthnLoginCompleteRequest (
                credential.getId(), encodeBase64(credential.getRawId()), credential.getType(), attestationLoginData
        );
        return new WebAuthnBaseResponse<>(lastRequestId, webAuthnPublicCredential);
    }

    private byte[] decodeBase64(final String base64) {
        return Base64.decode(base64, BASE64_FLAG);
    }

    private String encodeBase64(final byte[] data) {
        return Base64.encodeToString(data, BASE64_FLAG);
    }
}
