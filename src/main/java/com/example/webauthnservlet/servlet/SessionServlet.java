package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.service.UserStore;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 現在のセッション状態を返す API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class SessionServlet extends JsonServletSupport {

    private final UserStore userStore;

    /**
     * サーブレットを生成します。
     *
     * @param userStore ユーザーストア
     */
    public SessionServlet(UserStore userStore) {
        this.userStore = userStore;
    }

    /**
     * ログイン状態、認証方法、登録済みパスキー数を返します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String username = session == null ? null : (String) session.getAttribute(SessionKeys.AUTHENTICATED_USERNAME);
        String authMethod = session == null ? null : (String) session.getAttribute(SessionKeys.AUTH_METHOD);

        if (username == null) {
            writeJson(response, mapOf(
                "ok", true,
                "authenticated", false,
                "credentialCount", 0
            ));
            return;
        }

        writeJson(response, mapOf(
            "ok", true,
            "authenticated", true,
            "username", username,
            "authMethod", authMethod,
            "credentialCount", userStore.getCredentialCount(username)
        ));
    }
}
