package com.example.webauthnservlet.model;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

/**
 * 登録済みパスキー 1 件分の情報を保持するモデルです。
 *
 * @author concador.hiro@gmail.com
 */
public final class CredentialRecord {

    private final String username;
    private final ByteArray userHandle;
    private final PublicKeyCredentialDescriptor descriptor;
    private final ByteArray publicKeyCose;
    private volatile long signatureCount;
    private final Boolean discoverable;

    /**
     * 登録済みパスキー情報を生成します。
     *
     * @param username ユーザー名
     * @param userHandle ユーザーハンドル
     * @param descriptor クレデンシャルの識別子とメタデータ
     * @param publicKeyCose 公開鍵
     * @param signatureCount 署名カウンタ
     * @param discoverable discoverable credential かどうか
     */
    public CredentialRecord(
        String username,
        ByteArray userHandle,
        PublicKeyCredentialDescriptor descriptor,
        ByteArray publicKeyCose,
        long signatureCount,
        Boolean discoverable
    ) {
        this.username = username;
        this.userHandle = userHandle;
        this.descriptor = descriptor;
        this.publicKeyCose = publicKeyCose;
        this.signatureCount = signatureCount;
        this.discoverable = discoverable;
    }

    /**
     * ユーザー名を返します。
     *
     * @return ユーザー名
     */
    public String getUsername() {
        return username;
    }

    /**
     * ユーザーハンドルを返します。
     *
     * @return ユーザーハンドル
     */
    public ByteArray getUserHandle() {
        return userHandle;
    }

    /**
     * クレデンシャル記述子を返します。
     *
     * @return クレデンシャル記述子
     */
    public PublicKeyCredentialDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * クレデンシャル ID を返します。
     *
     * @return クレデンシャル ID
     */
    public ByteArray getCredentialId() {
        return descriptor.getId();
    }

    /**
     * 公開鍵を返します。
     *
     * @return 公開鍵
     */
    public ByteArray getPublicKeyCose() {
        return publicKeyCose;
    }

    /**
     * 署名カウンタを返します。
     *
     * @return 署名カウンタ
     */
    public long getSignatureCount() {
        return signatureCount;
    }

    /**
     * 署名カウンタを更新します。
     *
     * @param signatureCount 新しい署名カウンタ
     */
    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

    /**
     * discoverable credential かどうかを返します。
     *
     * @return discoverable credential かどうか
     */
    public Boolean getDiscoverable() {
        return discoverable;
    }

    /**
     * Yubico ライブラリが検証で利用する RegisteredCredential へ変換します。
     *
     * @return RegisteredCredential
     */
    public RegisteredCredential toRegisteredCredential() {
        return RegisteredCredential.builder()
            .credentialId(getCredentialId())
            .userHandle(userHandle)
            .publicKeyCose(publicKeyCose)
            .signatureCount(signatureCount)
            .build();
    }
}
