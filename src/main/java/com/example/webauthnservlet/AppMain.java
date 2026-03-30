package com.example.webauthnservlet;

import com.example.webauthnservlet.service.UserStore;
import com.example.webauthnservlet.service.WebAuthnService;
import com.example.webauthnservlet.servlet.BeginAssertionServlet;
import com.example.webauthnservlet.servlet.BeginRegistrationServlet;
import com.example.webauthnservlet.servlet.FinishAssertionServlet;
import com.example.webauthnservlet.servlet.FinishRegistrationServlet;
import com.example.webauthnservlet.servlet.LogoutServlet;
import com.example.webauthnservlet.servlet.PasswordLoginServlet;
import com.example.webauthnservlet.servlet.SessionServlet;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.servlets.DefaultServlet;

/**
 * 組み込み Tomcat を起動し、WebAuthn サンプル用のサーブレットと静的ファイルを公開する
 * エントリーポイントです。
 *
 * @author concador.hiro@gmail.com
 */
public final class AppMain {

    private static final int PORT = 8080;

    /**
     * インスタンス化を禁止します。
     */
    private AppMain() {
    }

    /**
     * アプリケーションを起動します。
     *
     * @param args コマンドライン引数
     * @throws Exception 起動時に発生した例外
     */
    public static void main(String[] args) throws Exception {
        File appHome = resolveAppHome();
        File baseDir = new File(appHome, ".tomcat");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        UserStore userStore = new UserStore();
        WebAuthnService webAuthnService = new WebAuthnService(userStore);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.getConnector();

        File webRoot = resolveWebRoot(appHome);
        Context context = tomcat.addContext("", webRoot.getAbsolutePath());
        context.addWelcomeFile("app/login.html");
        context.setSessionTimeout(30);

        Tomcat.addServlet(context, "default", new DefaultServlet());
        context.addServletMappingDecoded("/", "default");

        Tomcat.addServlet(context, "passwordLoginServlet", new PasswordLoginServlet(userStore));
        context.addServletMappingDecoded("/api/login/password", "passwordLoginServlet");

        Tomcat.addServlet(context, "sessionServlet", new SessionServlet(userStore));
        context.addServletMappingDecoded("/api/session", "sessionServlet");

        Tomcat.addServlet(context, "logoutServlet", new LogoutServlet());
        context.addServletMappingDecoded("/api/logout", "logoutServlet");

        Tomcat.addServlet(context, "beginRegistrationServlet", new BeginRegistrationServlet(userStore, webAuthnService));
        context.addServletMappingDecoded("/api/webauthn/register/begin", "beginRegistrationServlet");

        Tomcat.addServlet(context, "finishRegistrationServlet", new FinishRegistrationServlet(userStore, webAuthnService));
        context.addServletMappingDecoded("/api/webauthn/register/finish", "finishRegistrationServlet");

        Tomcat.addServlet(context, "beginAssertionServlet", new BeginAssertionServlet(webAuthnService));
        context.addServletMappingDecoded("/api/webauthn/login/begin", "beginAssertionServlet");

        Tomcat.addServlet(context, "finishAssertionServlet", new FinishAssertionServlet(userStore, webAuthnService));
        context.addServletMappingDecoded("/api/webauthn/login/finish", "finishAssertionServlet");

        tomcat.start();
        System.out.println("webauthn-servlet started: http://localhost:" + PORT + "/app/login.html");
        tomcat.getServer().await();
    }

    /**
     * 静的ファイルの公開ルートを解決します。
     * ソース実行時は {@code src/main/resources} を優先し、見つからなければアプリケーション
     * ホームをそのまま返します。
     *
     * @param appHome アプリケーションのホームディレクトリ
     * @return 静的ファイルの公開ルート
     */
    private static File resolveWebRoot(File appHome) {
        File srcResources = new File(appHome, "src/main/resources");
        if (srcResources.isDirectory()) {
            return srcResources;
        }
        return appHome;
    }

    /**
     * アプリケーションのホームディレクトリを解決します。
     * {@code -Dapp.home=...} が指定されていればそれを使い、未指定ならカレントディレクトリを使います。
     *
     * @return アプリケーションのホームディレクトリ
     */
    private static File resolveAppHome() {
        String appHome = System.getProperty("app.home");
        if (appHome != null && !appHome.trim().isEmpty()) {
            return new File(appHome);
        }
        return new File(".").getAbsoluteFile();
    }
}
