package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.service.UserStore;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * パスワードログイン API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class PasswordLoginServlet extends JsonServletSupport {

    private final UserStore userStore;

    /**
     * サーブレットを生成します。
     *
     * @param userStore ユーザーストア
     */
    public PasswordLoginServlet(UserStore userStore) {
        this.userStore = userStore;
    }

    /**
     * ユーザー名とパスワードを検証し、成功時は新しいセッションに認証情報を設定します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = OBJECT_MAPPER.readValue(readBody(request), Map.class);
            String username = body.get("username") == null ? "" : String.valueOf(body.get("username"));
            String password = body.get("password") == null ? "" : String.valueOf(body.get("password"));

            if (!userStore.validatePassword(username, password)) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // セッション固定攻撃を避けるため、既存セッションを無効化してから作り直します。
            HttpSession session = request.getSession(true);
            session.setAttribute(SessionKeys.AUTHENTICATED_USERNAME, username);
            session.setAttribute(SessionKeys.AUTH_METHOD, "password");

            writeJson(response, mapOf(
                "ok", true,
                "username", username,
                "redirect", "/app/menu.html"
            ));
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
