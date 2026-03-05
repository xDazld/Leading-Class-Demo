"use strict";

const webAuthn = new WebAuthn();

async function register() {
    const username = document.getElementById('username').value.trim();
    const btn = document.getElementById('registerBtn');
    const msg = document.getElementById('statusMsg');

    if (!username) {
        show(msg, 'error', '\u26a0\ufe0f Please enter a username.');
        return;
    }

    btn.disabled = true;
    btn.textContent = '\u23f3 Creating passkey\u2026';
    msg.className = '';
    msg.style.display = 'none';

    try {
        await webAuthn.register({username: username, displayName: username});
        show(msg, 'success', '\u2705 Account created! Redirecting to your dashboard\u2026');
        setTimeout(function () {
            window.location.href = '/dashboard';
        }, 1200);
    } catch (err) {
        console.error(err);
        show(msg, 'error', '\u274c Registration failed: ' + (err.message || err));
        btn.disabled = false;
        btn.textContent = 'Register with Passkey';
    }
}

function show(el, cls, text) {
    el.className = cls;
    el.textContent = text;
    el.style.display = 'block';
}

