package com.example.webauthnservlet.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JSON 入出力を行うサーブレットの共通処理をまとめた抽象基底クラスです。
 *
 * @author concador.hiro@gmail.com
 */
abstract class JsonServletSupport extends HttpServlet {

    /** JSON 変換で使う共通 ObjectMapper です。 */
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * リクエストボディを文字列として読み込みます。
     *
     * @param request HTTP リクエスト
     * @return リクエストボディ文字列
     * @throws IOException 読み込みに失敗した場合
     */
    protected String readBody(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1024];
        int read;
        java.io.Reader reader = request.getReader();
        // 文字数が不定なので Reader を最後まで読み切ります。
        while ((read = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }

    /**
     * 可変長引数から簡易的に Map を生成します。
     *
     * @param values キー、値、キー、値... の順で渡す配列
     * @return 生成した Map
     */
    protected Map<String, Object> mapOf(Object... values) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            result.put(String.valueOf(values[i]), values[i + 1]);
        }
        return result;
    }

    /**
     * オブジェクトを JSON としてレスポンスへ出力します。
     *
     * @param response HTTP レスポンス
     * @param body 出力するオブジェクト
     * @throws IOException 出力に失敗した場合
     */
    protected void writeJson(HttpServletResponse response, Object body) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }

    /**
     * すでに JSON 文字列になっている値をそのままレスポンスへ出力します。
     *
     * @param response HTTP レスポンス
     * @param rawJson JSON 文字列
     * @throws IOException 出力に失敗した場合
     */
    protected void writeJsonString(HttpServletResponse response, String rawJson) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(rawJson);
    }

    /**
     * エラー JSON をレスポンスへ出力します。
     *
     * @param response HTTP レスポンス
     * @param status HTTP ステータス
     * @param message エラーメッセージ
     * @throws IOException 出力に失敗した場合
     */
    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, mapOf("ok", false, "error", message));
    }
}
