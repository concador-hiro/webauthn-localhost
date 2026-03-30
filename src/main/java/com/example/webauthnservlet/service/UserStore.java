package com.example.webauthnservlet.service;

import com.example.webauthnservlet.model.CredentialRecord;
import com.example.webauthnservlet.model.UserAccount;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ユーザー情報と登録済みパスキーをメモリ上で管理するストアです。
 * このサンプルでは永続化を行わず、アプリ再起動で内容は消えます。
 *
 * @author concador.hiro@gmail.com
 */
public final class UserStore implements CredentialRepository {

    private final ConcurrentMap<String, UserAccount> usersByUsername = new ConcurrentHashMap<String, UserAccount>();
    private final ConcurrentMap<String, String> usernameByHandle = new ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, CredentialRecord> credentialsById = new ConcurrentHashMap<String, CredentialRecord>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * ストアを生成します。
     */
    public UserStore() {
    }

    /**
     * ユーザーを新規作成します。
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return 作成したユーザー
     */
    private UserAccount createUser(String username, String password) {
        byte[] handle = new byte[32];
        secureRandom.nextBytes(handle);
        String displayName = username;
        UserAccount account = new UserAccount(username, displayName, password, new ByteArray(handle));
        usersByUsername.put(account.getUsername(), account);
        usernameByHandle.put(account.getUserHandle().getBase64Url(), account.getUsername());
        return account;
    }

    /**
     * ユーザー名からユーザー情報を取得します。
     *
     * @param username ユーザー名
     * @return ユーザー情報
     */
    public Optional<UserAccount> findUser(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * パスワードを検証します。
     * このチェックポイントでは簡易実装として、パスワードは常に {@code passwd} 固定です。
     * 未登録ユーザーは初回ログイン時に自動作成します。
     *
     * @param username ユーザー名
     * @param password パスワード
     * @return 認証成功なら true
     */
    public boolean validatePassword(String username, String password) {
        if (!"passwd".equals(password)) {
            return false;
        }
        UserAccount account = usersByUsername.get(username);
        if (account == null) {
            createUser(username, password);
            return true;
        }
        return account.getPassword().equals(password);
    }

    /**
     * 指定ユーザーの登録済みパスキー数を返します。
     *
     * @param username ユーザー名
     * @return 登録済みパスキー数
     */
    public int getCredentialCount(String username) {
        int count = 0;
        for (CredentialRecord record : credentialsById.values()) {
            if (record.getUsername().equals(username)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 登録成功結果をストアへ追加します。
     *
     * @param username ユーザー名
     * @param result 登録結果
     */
    public void addRegistration(String username, RegistrationResult result) {
        UserAccount account = usersByUsername.get(username);
        if (account == null) {
            throw new IllegalArgumentException("Unknown user: " + username);
        }
        CredentialRecord record = new CredentialRecord(
            username,
            account.getUserHandle(),
            result.getKeyId(),
            result.getPublicKeyCose(),
            result.getSignatureCount(),
            result.isDiscoverable().orElse(null)
        );
        credentialsById.put(record.getCredentialId().getBase64Url(), record);
    }

    /**
     * 署名カウンタを更新します。
     *
     * @param credentialId クレデンシャル ID
     * @param signatureCount 新しい署名カウンタ
     */
    public void updateSignatureCount(ByteArray credentialId, long signatureCount) {
        CredentialRecord record = credentialsById.get(credentialId.getBase64Url());
        if (record != null) {
            record.setSignatureCount(signatureCount);
        }
    }

    /**
     * 指定ユーザーに紐づくクレデンシャル記述子一覧を返します。
     *
     * @param username ユーザー名
     * @return クレデンシャル記述子の集合
     */
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        Set<PublicKeyCredentialDescriptor> result = new LinkedHashSet<PublicKeyCredentialDescriptor>();
        for (CredentialRecord record : credentialsById.values()) {
            if (record.getUsername().equals(username)) {
                result.add(record.getDescriptor());
            }
        }
        return result;
    }

    /**
     * ユーザー名からユーザーハンドルを取得します。
     *
     * @param username ユーザー名
     * @return ユーザーハンドル
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        UserAccount account = usersByUsername.get(username);
        return account == null ? Optional.<ByteArray>empty() : Optional.of(account.getUserHandle());
    }

    /**
     * ユーザーハンドルからユーザー名を取得します。
     *
     * @param userHandle ユーザーハンドル
     * @return ユーザー名
     */
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.ofNullable(usernameByHandle.get(userHandle.getBase64Url()));
    }

    /**
     * クレデンシャル ID とユーザーハンドルの組み合わせで登録情報を検索します。
     *
     * @param credentialId クレデンシャル ID
     * @param userHandle ユーザーハンドル
     * @return RegisteredCredential
     */
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        CredentialRecord record = credentialsById.get(credentialId.getBase64Url());
        if (record == null) {
            return Optional.empty();
        }
        if (!record.getUserHandle().equals(userHandle)) {
            return Optional.empty();
        }
        return Optional.of(record.toRegisteredCredential());
    }

    /**
     * クレデンシャル ID に一致する登録情報をすべて返します。
     *
     * @param credentialId クレデンシャル ID
     * @return RegisteredCredential の集合
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        CredentialRecord record = credentialsById.get(credentialId.getBase64Url());
        if (record == null) {
            return Collections.emptySet();
        }
        Set<RegisteredCredential> result = new LinkedHashSet<RegisteredCredential>();
        result.add(record.toRegisteredCredential());
        return result;
    }

    /**
     * 指定ユーザーに紐づく登録済みパスキー一覧を返します。
     *
     * @param username ユーザー名
     * @return 登録済みパスキー一覧
     */
    public List<CredentialRecord> listCredentials(String username) {
        List<CredentialRecord> result = new ArrayList<CredentialRecord>();
        for (CredentialRecord record : credentialsById.values()) {
            if (record.getUsername().equals(username)) {
                result.add(record);
            }
        }
        return result;
    }
}
