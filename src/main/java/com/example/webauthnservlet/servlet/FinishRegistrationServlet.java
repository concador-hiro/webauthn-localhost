package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.service.UserStore;
import com.example.webauthnservlet.service.WebAuthnService;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * パスキー登録完了 API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class FinishRegistrationServlet extends JsonServletSupport {

    private final UserStore userStore;
    private final WebAuthnService webAuthnService;

    /**
     * サーブレットを生成します。
     *
     * @param userStore ユーザーストア
     * @param webAuthnService WebAuthn サービス
     */
    public FinishRegistrationServlet(UserStore userStore, WebAuthnService webAuthnService) {
        this.userStore = userStore;
        this.webAuthnService = webAuthnService;
    }

    /**
     * 登録結果を検証し、成功時はストアへ保存します。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @throws IOException レスポンス出力に失敗した場合
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required.");
            return;
        }

        String username = (String) session.getAttribute(SessionKeys.AUTHENTICATED_USERNAME);
        String requestJson = (String) session.getAttribute(SessionKeys.PENDING_REGISTRATION);
        if (username == null || requestJson == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Registration ceremony not started.");
            return;
        }

        try {
            String credentialJson = readBody(request);
            PublicKeyCredentialCreationOptions requestOptions = PublicKeyCredentialCreationOptions.fromJson(requestJson);
            RegistrationResult result = webAuthnService.finishRegistration(requestOptions, credentialJson);
            userStore.addRegistration(username, result);
            session.removeAttribute(SessionKeys.PENDING_REGISTRATION);

            writeJson(response, mapOf(
                "ok", true,
                "username", username,
                "discoverable", result.isDiscoverable().orElse(null),
                "userVerified", result.isUserVerified()
            ));
        } catch (Exception e) {
            // 失敗時は中途半端な登録要求を残さないように消します。
            session.removeAttribute(SessionKeys.PENDING_REGISTRATION);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
