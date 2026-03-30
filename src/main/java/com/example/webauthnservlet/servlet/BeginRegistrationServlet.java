package com.example.webauthnservlet.servlet;

import com.example.webauthnservlet.SessionKeys;
import com.example.webauthnservlet.model.UserAccount;
import com.example.webauthnservlet.service.UserStore;
import com.example.webauthnservlet.service.WebAuthnService;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * パスキー登録開始 API を提供するサーブレットです。
 *
 * @author concador.hiro@gmail.com
 */
public final class BeginRegistrationServlet extends JsonServletSupport {

    private final UserStore userStore;
    private final WebAuthnService webAuthnService;

    /**
     * サーブレットを生成します。
     *
     * @param userStore ユーザーストア
     * @param webAuthnService WebAuthn サービス
     */
    public BeginRegistrationServlet(UserStore userStore, WebAuthnService webAuthnService) {
        this.userStore = userStore;
        this.webAuthnService = webAuthnService;
    }

    /**
     * 登録開始要求を生成してセッションへ保存し、ブラウザへ返します。
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
        if (username == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required.");
            return;
        }

        Optional<UserAccount> user = userStore.findUser(username);
        if (!user.isPresent()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found.");
            return;
        }

        PublicKeyCredentialCreationOptions options = webAuthnService.startRegistration(user.get());
        session.setAttribute(SessionKeys.PENDING_REGISTRATION, options.toJson());
        writeJsonString(response, options.toCredentialsCreateJson());
    }
}
