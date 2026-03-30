package com.example.webauthnservlet.service;

import com.example.webauthnservlet.model.UserAccount;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Yubico java-webauthn-server を使って WebAuthn の登録要求と認証要求を生成し、
 * ブラウザから返ってきたレスポンスを検証するサービスです。
 *
 * @author concador.hiro@gmail.com
 */
public final class WebAuthnService {

    private final RelyingParty relyingParty;

    /**
     * サービスを生成します。
     *
     * @param userStore クレデンシャル参照用ストア
     */
    public WebAuthnService(UserStore userStore) {
        RelyingPartyIdentity relyingPartyIdentity = RelyingPartyIdentity.builder()
            .id("localhost")
            .name("Localhost WebAuthn Demo")
            .build();

        this.relyingParty = RelyingParty.builder()
            .identity(relyingPartyIdentity)
            .credentialRepository(userStore)
            .origins(Collections.singleton("http://localhost:8080"))
            .preferredPubkeyParams(Arrays.asList(
                PublicKeyCredentialParameters.ES256,
                PublicKeyCredentialParameters.RS256
            ))
            .build();
    }

    /**
     * 登録開始時にブラウザへ渡す作成オプションを生成します。
     *
     * @param account 登録対象ユーザー
     * @return 作成オプション
     */
    public PublicKeyCredentialCreationOptions startRegistration(UserAccount account) {
        return relyingParty.startRegistration(
            StartRegistrationOptions.builder()
                .user(account.toUserIdentity())
                .authenticatorSelection(
                    AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.PREFERRED)
                        .build()
                )
                .build()
        );
    }

    /**
     * ブラウザから返却された登録結果を検証します。
     *
     * @param requestOptions 登録開始時に生成したオプション
     * @param credentialJson ブラウザから返却された JSON
     * @return 登録結果
     * @throws IOException JSON 解析に失敗した場合
     * @throws RegistrationFailedException 検証に失敗した場合
     */
    public RegistrationResult finishRegistration(PublicKeyCredentialCreationOptions requestOptions, String credentialJson)
        throws IOException, RegistrationFailedException {
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response =
            PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

        return relyingParty.finishRegistration(
            FinishRegistrationOptions.builder()
                .request(requestOptions)
                .response(response)
                .build()
        );
    }

    /**
     * パスキーログイン開始時にブラウザへ渡す取得オプションを生成します。
     *
     * @return AssertionRequest
     */
    public AssertionRequest startAssertion() {
        return relyingParty.startAssertion(
            StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build()
        );
    }

    /**
     * ブラウザから返却された認証結果を検証します。
     *
     * @param requestOptions 認証開始時に生成したオプション
     * @param credentialJson ブラウザから返却された JSON
     * @return 認証結果
     * @throws IOException JSON 解析に失敗した場合
     * @throws AssertionFailedException 検証に失敗した場合
     */
    public AssertionResult finishAssertion(AssertionRequest requestOptions, String credentialJson)
        throws IOException, AssertionFailedException {
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response =
            PublicKeyCredential.parseAssertionResponseJson(credentialJson);

        return relyingParty.finishAssertion(
            FinishAssertionOptions.builder()
                .request(requestOptions)
                .response(response)
                .build()
        );
    }
}
