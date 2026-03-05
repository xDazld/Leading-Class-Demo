"use strict";

const webAuthn = new WebAuthn();

async function login() {
    const btn = document.getElementById('loginBtn');
    const msg = document.getElementById('statusMsg');

    btn.disabled = true;
    btn.textContent = '\u23f3 Verifying passkey\u2026';
    msg.className = '';
    msg.style.display = 'none';

    try {
        await webAuthn.login();
        show(msg, 'success', '\u2705 Signed in! Redirecting\u2026');
        setTimeout(function () {
            window.location.href = '/dashboard';
        }, 1000);
    } catch (err) {
        console.error(err);
        show(msg, 'error', '\u274c Sign-in failed: ' + (err.message || err));
        btn.disabled = false;
        btn.textContent = 'Sign In with Passkey';
    }
}

function show(el, cls, text) {
    el.className = cls;
    el.textContent = text;
    el.style.display = 'block';
}

