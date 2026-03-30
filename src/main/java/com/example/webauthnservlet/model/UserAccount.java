package com.example.webauthnservlet.model;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;

/**
 * パスワードログインと WebAuthn 登録に必要なユーザー情報を保持するモデルです。
 *
 * @author concador.hiro@gmail.com
 */
public final class UserAccount {

    private final String username;
    private final String displayName;
    private final String password;
    private final ByteArray userHandle;

    /**
     * ユーザー情報を生成します。
     *
     * @param username ユーザー名
     * @param displayName 画面表示用の名前
     * @param password パスワード
     * @param userHandle WebAuthn 用ユーザーハンドル
     */
    public UserAccount(String username, String displayName, String password, ByteArray userHandle) {
        this.username = username;
        this.displayName = displayName;
        this.password = password;
        this.userHandle = userHandle;
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
     * 表示名を返します。
     *
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * パスワードを返します。
     *
     * @return パスワード
     */
    public String getPassword() {
        return password;
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
     * WebAuthn の登録要求で使用する UserIdentity に変換します。
     *
     * @return UserIdentity
     */
    public UserIdentity toUserIdentity() {
        return UserIdentity.builder()
            .name(username)
            .displayName(displayName)
            .id(userHandle)
            .build();
    }
}
