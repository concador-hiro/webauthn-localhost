package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.service.UserStore;
import com.example.webauthnservlet.service.WebAuthnService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * パスキーログイン完了 API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class FinishAssertionServlet extends JsonServletSupport {

    private final UserStore userStore;
    private final WebAuthnService webAuthnService;

    /**
     * サーブレットを生成します。
     *
     * @param userStore ユーザーストア
     * @param webAuthnService WebAuthn サービス
     */
    public FinishAssertionServlet(UserStore userStore, WebAuthnService webAuthnService) {
        this.userStore = userStore;
        this.webAuthnService = webAuthnService;
    }

    /**
     * 認証結果を検証し、新しいセッションにログイン状態を再設定します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Assertion ceremony not started.");
            return;
        }

        String requestJson = (String) session.getAttribute(SessionKeys.PENDING_ASSERTION);
        if (requestJson == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Assertion ceremony not started.");
            return;
        }

        try {
            String credentialJson = readBody(request);
            AssertionRequest assertionRequest = AssertionRequest.fromJson(requestJson);
            AssertionResult result = webAuthnService.finishAssertion(assertionRequest, credentialJson);
            userStore.updateSignatureCount(result.getCredential().getCredentialId(), result.getSignatureCount());

            // 検証前の状態が残らないように、いったん現在のセッションを破棄して作り直します。
            session.invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(SessionKeys.AUTHENTICATED_USERNAME, result.getUsername());
            newSession.setAttribute(SessionKeys.AUTH_METHOD, "passkey");

            writeJson(response, mapOf(
                "ok", true,
                "username", result.getUsername(),
                "userVerified", result.isUserVerified(),
                "redirect", "/app/menu.html"
            ));
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
