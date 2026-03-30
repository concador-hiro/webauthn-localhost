(function () {
  'use strict';

  function $(id) {
    return document.getElementById(id);
  }

  function setStatus(message) {
    var status = $('status');
    if (status) {
      status.textContent = message;
    }
  }

  function toArrayBuffer(base64url) {
    var base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) {
      base64 += '=';
    }
    var binary = atob(base64);
    var bytes = new Uint8Array(binary.length);
    for (var i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
  }

  function fromArrayBuffer(buffer) {
    var bytes = new Uint8Array(buffer);
    var binary = '';
    for (var i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  function normalizeCreationOptions(publicKey) {
    publicKey.challenge = toArrayBuffer(publicKey.challenge);
    publicKey.user.id = toArrayBuffer(publicKey.user.id);

    if (publicKey.excludeCredentials) {
      publicKey.excludeCredentials = publicKey.excludeCredentials.map(function (credential) {
        credential.id = toArrayBuffer(credential.id);
        return credential;
      });
    }

    return publicKey;
  }

  function normalizeRequestOptions(publicKey) {
    publicKey.challenge = toArrayBuffer(publicKey.challenge);

    if (publicKey.allowCredentials) {
      publicKey.allowCredentials = publicKey.allowCredentials.map(function (credential) {
        credential.id = toArrayBuffer(credential.id);
        return credential;
      });
    }

    return publicKey;
  }

  async function postJson(url, payload) {
    var response = await fetch(url, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: payload == null ? null : JSON.stringify(payload)
    });

    var text = await response.text();
    var json = text ? JSON.parse(text) : {};

    if (!response.ok) {
      throw new Error((json && json.error) ? json.error : ('HTTP ' + response.status));
    }

    return json;
  }

  async function getJson(url) {
    var response = await fetch(url, {
      method: 'GET',
      credentials: 'same-origin',
      headers: { 'Accept': 'application/json' }
    });

    var text = await response.text();
    var json = text ? JSON.parse(text) : {};

    if (!response.ok) {
      throw new Error((json && json.error) ? json.error : ('HTTP ' + response.status));
    }

    return json;
  }

  function serializeRegistrationCredential(credential) {
    var response = credential.response;
    return {
      id: credential.id,
      rawId: fromArrayBuffer(credential.rawId),
      type: credential.type,
      response: {
        clientDataJSON: fromArrayBuffer(response.clientDataJSON),
        attestationObject: fromArrayBuffer(response.attestationObject)
      },
      clientExtensionResults: credential.getClientExtensionResults ? credential.getClientExtensionResults() : {}
    };
  }

  function serializeAssertionCredential(credential) {
    var response = credential.response;
    return {
      id: credential.id,
      rawId: fromArrayBuffer(credential.rawId),
      type: credential.type,
      response: {
        clientDataJSON: fromArrayBuffer(response.clientDataJSON),
        authenticatorData: fromArrayBuffer(response.authenticatorData),
        signature: fromArrayBuffer(response.signature),
        userHandle: response.userHandle ? fromArrayBuffer(response.userHandle) : null
      },
      clientExtensionResults: credential.getClientExtensionResults ? credential.getClientExtensionResults() : {}
    };
  }

  async function passwordLogin(event) {
    if (event) {
      event.preventDefault();
    }
    setStatus('パスワードログイン中...');
    var username = $('username').value;
    var password = $('password').value;
    var result = await postJson('/api/login/password', {
      username: username,
      password: password
    });
    setStatus('パスワードログイン成功。メニューへ移動します。');
    location.href = result.redirect || '/app/menu.html';
  }

  async function registerPasskey() {
    setStatus('パスキー登録開始...');
    var begin = await postJson('/api/webauthn/register/begin', {});
    var creationOptions = normalizeCreationOptions(begin.publicKey || begin);
    var credential = await navigator.credentials.create({ publicKey: creationOptions });
    var finish = await postJson('/api/webauthn/register/finish', serializeRegistrationCredential(credential));
    setStatus('パスキー登録成功\n' + JSON.stringify(finish, null, 2));
    await loadSession();
  }

  async function loginWithPasskey() {
    setStatus('パスキーログイン開始...');
    var begin = await postJson('/api/webauthn/login/begin', {});
    var requestOptions = normalizeRequestOptions(begin.publicKey || begin);
    var credential = await navigator.credentials.get({ publicKey: requestOptions });
    var finish = await postJson('/api/webauthn/login/finish', serializeAssertionCredential(credential));
    setStatus('パスキーログイン成功。メニューへ移動します。');
    location.href = finish.redirect || '/app/menu.html';
  }

  async function logout() {
    setStatus('ログアウト中...');
    var result = await postJson('/api/logout', {});
    setStatus('ログアウト完了。ログイン画面へ戻ります。');
    location.href = result.redirect || '/app/login.html';
  }

  async function loadSession() {
    var session = await getJson('/api/session');
    if ($('current-user')) {
      if (!session.authenticated) {
        location.href = '/app/login.html';
        return;
      }
      $('current-user').textContent = session.username || '';
      $('current-auth-method').textContent = session.authMethod || '';
      $('credential-count').textContent = String(session.credentialCount || 0);
    }
    return session;
  }

  async function initLoginPage() {
    $('password-login-form').addEventListener('submit', function (event) {
      passwordLogin(event).catch(function (error) {
        setStatus('エラー: ' + error.message);
      });
    });

    $('passkey-login-button').addEventListener('click', function () {
      loginWithPasskey().catch(function (error) {
        setStatus('エラー: ' + error.message);
      });
    });

    try {
      var session = await getJson('/api/session');
      if (session.authenticated) {
        location.href = '/app/menu.html';
      }
    } catch (error) {
      setStatus('セッション確認エラー: ' + error.message);
    }
  }

  async function initMenuPage() {
    $('register-passkey-button').addEventListener('click', function () {
      registerPasskey().catch(function (error) {
        setStatus('エラー: ' + error.message);
      });
    });

    $('logout-button').addEventListener('click', function () {
      logout().catch(function (error) {
        setStatus('エラー: ' + error.message);
      });
    });

    try {
      await loadSession();
    } catch (error) {
      setStatus('セッション確認エラー: ' + error.message);
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    var path = location.pathname;
    if (path.indexOf('/app/login.html') >= 0) {
      initLoginPage();
    } else if (path.indexOf('/app/menu.html') >= 0) {
      initMenuPage();
    }
  });
})();
