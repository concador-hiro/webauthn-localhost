package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.service.WebAuthnService;
import com.yubico.webauthn.AssertionRequest;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * パスキーログイン開始 API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class BeginAssertionServlet extends JsonServletSupport {

    private final WebAuthnService webAuthnService;

    /**
     * サーブレットを生成します。
     *
     * @param webAuthnService WebAuthn サービス
     */
    public BeginAssertionServlet(WebAuthnService webAuthnService) {
        this.webAuthnService = webAuthnService;
    }

    /**
     * 認証開始要求を生成してセッションへ保存し、ブラウザへ返します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        AssertionRequest assertionRequest = webAuthnService.startAssertion();
        session.setAttribute(SessionKeys.PENDING_ASSERTION, assertionRequest.toJson());
        writeJsonString(response, assertionRequest.toCredentialsGetJson());
    }
}
