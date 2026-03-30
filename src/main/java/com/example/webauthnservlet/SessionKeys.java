package com.example.webauthnservlet;

/**
 * HttpSession に保存する属性名を集約した定数クラスです。
 *
 * @author concador.hiro@gmail.com
 */
public final class SessionKeys {

    /** 認証済みユーザー名を保存するキーです。 */
    public static final String AUTHENTICATED_USERNAME = "authenticatedUsername";

    /** 現在の認証方式を保存するキーです。 */
    public static final String AUTH_METHOD = "authMethod";

    /** 登録開始時に生成した RegistrationRequest を保存するキーです。 */
    public static final String PENDING_REGISTRATION = "pendingRegistration";

    /** ログイン開始時に生成した AssertionRequest を保存するキーです。 */
    public static final String PENDING_ASSERTION = "pendingAssertion";

    /**
     * インスタンス化を禁止します。
     */
    private SessionKeys() {
    }
}
