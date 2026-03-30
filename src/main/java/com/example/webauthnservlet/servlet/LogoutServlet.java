package com.example.webauthnservlet.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ログアウト API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class LogoutServlet extends JsonServletSupport {

    /**
     * セッションを破棄してログイン画面への戻り先を返します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        writeJson(response, mapOf(
            "ok", true,
            "redirect", "/app/login.html"
        ));
    }
}
