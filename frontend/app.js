// Адрес backend API. Поменяйте, если сервер запущен на другом хосте/порту.
const API_BASE = 'http://localhost:8081';

const app = document.getElementById('app');
const navEl = document.getElementById('nav');

let session = loadSession();
let posts = null;
let listContext = 'feed'; // 'feed' | 'profile' — влияет только на текст пустого состояния
let expandedPosts = new Set();
let editingPostId = null;
let editingCommentId = null;
let prefillEmail = '';

// ---------- session helpers ----------

function loadSession() {
  try {
    return JSON.parse(localStorage.getItem('session'));
  } catch {
    return null;
  }
}

function saveSession(s) {
  session = s;
  localStorage.setItem('session', JSON.stringify(s));
}

function clearSession() {
  session = null;
  localStorage.removeItem('session');
}

// ---------- utils ----------

function esc(value) {
  return String(value ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}

function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  } catch {
    return '';
  }
}

let toastTimer = null;
function toast(message, isError = false) {
  const t = document.getElementById('toast');
  t.textContent = message;
  t.classList.toggle('error', isError);
  t.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.hidden = true; }, 3500);
}

// ---------- API client ----------

async function api(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (auth && session?.token) headers['Authorization'] = 'Bearer ' + session.token;

  let res;
  try {
    res = await fetch(API_BASE + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error(`Не удалось соединиться с сервером (${API_BASE}). Проверьте, что backend запущен.`);
  }

  if (res.status === 401 && session) {
    clearSession();
    location.hash = '#/login';
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }

  if (!res.ok) {
    let message;
    if (data && typeof data === 'object') {
      message = data.message || data.error || data.massage;
    }
    if (!message) message = (typeof data === 'string' && data) ? data : `${res.status} ${res.statusText}`;
    throw new Error(message);
  }

  return data;
}

// ---------- router ----------

function currentRoute() {
  return location.hash.replace(/^#/, '') || '/';
}

function renderRoute() {
  renderNav();
  const route = currentRoute();

  if (!session && route !== '/login' && route !== '/register') {
    location.hash = '#/login';
    return;
  }
  if (session && (route === '/login' || route === '/register')) {
    location.hash = '#/';
    return;
  }

  if (route === '/login') return renderAuthForm('login');
  if (route === '/register') return renderAuthForm('register');
  if (route === '/profile') return renderProfile();
  return renderFeed();
}

function renderNav() {
  const route = currentRoute();
  if (!session) {
    navEl.innerHTML = '';
    return;
  }
  navEl.innerHTML = `
    <a href="#/" class="${route === '/' ? 'active' : ''}">Лента</a>
    <a href="#/profile" class="${route === '/profile' ? 'active' : ''}">Профиль</a>
    <span class="user-name">${esc(session.name)}</span>
    <button class="linklike" id="logoutBtn">Выйти</button>
  `;
  document.getElementById('logoutBtn').onclick = () => {
    clearSession();
    posts = null;
    location.hash = '#/login';
  };
}

// ---------- auth views ----------

function renderAuthForm(mode) {
  const isLogin = mode === 'login';
  app.innerHTML = `
    <div class="auth-wrap">
      <div class="card auth-card">
        <h2>${isLogin ? 'Вход' : 'Регистрация'}</h2>
        <form id="authForm">
          ${!isLogin ? `
          <div class="field">
            <label>Имя</label>
            <input type="text" name="name" required autocomplete="name" />
          </div>` : ''}
          <div class="field">
            <label>Email</label>
            <input type="email" name="email" required autocomplete="email" value="${esc(prefillEmail)}" />
          </div>
          <div class="field">
            <label>Пароль</label>
            <input type="password" name="password" required ${!isLogin ? 'minlength="6"' : ''}
                   autocomplete="${isLogin ? 'current-password' : 'new-password'}" />
          </div>
          <button class="btn" type="submit" style="width:100%; justify-content:center;">
            ${isLogin ? 'Войти' : 'Создать аккаунт'}
          </button>
          <div class="error-text" id="authError" hidden></div>
        </form>
        <div class="hint">
          ${isLogin
            ? 'Нет аккаунта? <button class="linklike" id="switchAuth">Зарегистрироваться</button>'
            : 'Уже есть аккаунт? <button class="linklike" id="switchAuth">Войти</button>'}
        </div>
      </div>
    </div>
  `;
  prefillEmail = '';

  document.getElementById('switchAuth').onclick = () => {
    location.hash = isLogin ? '#/register' : '#/login';
  };

  document.getElementById('authForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorBox = document.getElementById('authError');
    errorBox.hidden = true;
    const submitBtn = form.querySelector('button[type=submit]');
    submitBtn.disabled = true;

    try {
      if (isLogin) {
        const payload = { email: form.email.value.trim(), password: form.password.value };
        const res = await api('/auth/login', { method: 'POST', body: payload, auth: false });
        saveSession(res);
        toast('Добро пожаловать, ' + res.name);
        location.hash = '#/';
      } else {
        const payload = {
          name: form.name.value.trim(),
          email: form.email.value.trim(),
          password: form.password.value,
        };
        await api('/users', { method: 'POST', body: payload, auth: false });
        prefillEmail = payload.email;
        toast('Аккаунт создан, теперь войдите');
        location.hash = '#/login';
      }
    } catch (err) {
      errorBox.textContent = err.message;
      errorBox.hidden = false;
    } finally {
      submitBtn.disabled = false;
    }
  });
}

// ---------- feed / profile ----------

async function renderFeed() {
  listContext = 'feed';
  app.innerHTML = `
    <form id="composerForm" class="card composer">
      <h2>Новый пост</h2>
      <div class="field">
        <label>Заголовок</label>
        <input type="text" name="title" required maxlength="200" />
      </div>
      <div class="field">
        <label>Текст</label>
        <textarea name="content" required></textarea>
      </div>
      <div class="row-end"><button class="btn" type="submit">Опубликовать</button></div>
    </form>
    <div id="postsContainer"><div class="loading">Загрузка…</div></div>
  `;

  document.getElementById('composerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const btn = form.querySelector('button');
    btn.disabled = true;
    try {
      const created = await api('/posts', {
        method: 'POST',
        body: { title: form.title.value.trim(), content: form.content.value.trim() },
      });
      posts = posts || [];
      posts.unshift(created);
      form.reset();
      renderPostsList();
      toast('Пост опубликован');
    } catch (err) {
      toast(err.message, true);
    } finally {
      btn.disabled = false;
    }
  });

  try {
    posts = await api('/posts/all');
    posts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    renderPostsList();
  } catch (err) {
    document.getElementById('postsContainer').innerHTML = `<div class="error-text">${esc(err.message)}</div>`;
  }
}

async function renderProfile() {
  listContext = 'profile';
  app.innerHTML = `
    <div class="card">
      <h2>Мой профиль</h2>
      <form id="profileForm">
        <div class="field">
          <label>Имя</label>
          <input type="text" name="name" required value="${esc(session.name)}" />
        </div>
        <div class="field">
          <label>Email</label>
          <input type="email" name="email" required value="${esc(session.email)}" />
        </div>
        <div class="field">
          <label>Новый пароль</label>
          <input type="password" name="password" minlength="6" placeholder="Оставьте пустым, чтобы не менять" />
        </div>
        <div class="field">
          <label>Роль</label>
          <input type="text" value="${esc(session.role)}" disabled />
        </div>
        <div class="row-end"><button class="btn" type="submit">Сохранить</button></div>
        <div class="error-text" id="profileError" hidden></div>
      </form>
    </div>
    <div class="section-title">Мои посты</div>
    <div id="postsContainer"><div class="loading">Загрузка…</div></div>
  `;

  document.getElementById('profileForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const errorBox = document.getElementById('profileError');
    errorBox.hidden = true;
    const btn = form.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
      const body = { name: form.name.value.trim(), email: form.email.value.trim() };
      if (form.password.value) body.password = form.password.value;
      const updated = await api('/users/' + session.id, { method: 'PUT', body });
      saveSession({ ...session, name: updated.name, email: updated.email });
      form.password.value = '';
      renderNav();
      toast('Профиль обновлён');
    } catch (err) {
      errorBox.textContent = err.message;
      errorBox.hidden = false;
    } finally {
      btn.disabled = false;
    }
  });

  try {
    const all = await api('/posts/all');
    posts = all
      .filter((p) => p.authorId === session.id)
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    renderPostsList();
  } catch (err) {
    document.getElementById('postsContainer').innerHTML = `<div class="error-text">${esc(err.message)}</div>`;
  }
}

// ---------- posts list / cards ----------

function renderPostsList() {
  const target = document.getElementById('postsContainer');
  if (!target) return;

  if (!posts || posts.length === 0) {
    target.innerHTML = `<div class="empty">${
      listContext === 'profile' ? 'У вас пока нет постов.' : 'Пока нет постов. Будьте первым!'
    }</div>`;
    return;
  }

  target.innerHTML = posts.map(postCardHtml).join('');
  posts.forEach(attachPostHandlers);
}

function postCardHtml(post) {
  const isOwner = session && post.authorId === session.id;
  const isAdmin = session && session.role === 'ADMIN';
  const canManage = isOwner || isAdmin;
  const isEditing = editingPostId === post.id;
  const commentsOpen = expandedPosts.has(post.id);

  const body = isEditing ? `
    <form class="post-edit-form" data-action="save-post-edit">
      <div class="field"><label>Заголовок</label><input type="text" name="title" value="${esc(post.title)}" required /></div>
      <div class="field"><label>Текст</label><textarea name="content" required>${esc(post.content)}</textarea></div>
      <div class="row-end">
        <button type="button" class="btn-outline btn-small" data-action="cancel-post-edit">Отмена</button>
        <button type="submit" class="btn btn-small">Сохранить</button>
      </div>
    </form>
  ` : `
    <div class="post-header">
      <div>
        <p class="post-title">${esc(post.title)}</p>
        <p class="post-meta">${esc(post.authorName)} · ${formatDate(post.createdAt)}${isOwner ? '<span class="tag">вы</span>' : ''}</p>
      </div>
      ${canManage ? `
      <div class="post-owner-actions">
        <button class="btn-ghost btn-small" data-action="edit-post">Изменить</button>
        <button class="btn-danger btn-small" data-action="delete-post">Удалить</button>
      </div>` : ''}
    </div>
    <p class="post-content">${esc(post.content)}</p>
    <div class="post-actions">
      <button class="like-btn ${post.liked ? 'liked' : ''}" data-action="like">${post.liked ? '♥' : '♡'} ${post.likesCount}</button>
      <button class="comments-toggle" data-action="toggle-comments">Комментарии (${(post.comments || []).length})</button>
    </div>
  `;

  const commentsHtml = `
    <div class="comments" data-comments ${commentsOpen ? '' : 'hidden'}>
      <div data-comments-list>${
        (post.comments || []).length
          ? post.comments.map((c) => commentHtml(c)).join('')
          : '<div class="empty" style="padding:10px 0;">Комментариев пока нет</div>'
      }</div>
      <form class="comment-form" data-action="add-comment">
        <input type="text" name="text" placeholder="Написать комментарий…" required maxlength="500" />
        <button class="btn btn-small" type="submit">Отправить</button>
      </form>
    </div>
  `;

  return `<article class="post" data-post-id="${post.id}">${body}${commentsHtml}</article>`;
}

function commentHtml(c) {
  const isOwner = session && c.authorId === session.id;
  const isAdmin = session && session.role === 'ADMIN';
  const canManage = isOwner || isAdmin;

  if (editingCommentId === c.id) {
    return `
      <div class="comment" data-comment-id="${c.id}">
        <form class="comment-form" data-action="save-comment-edit" style="flex:1;">
          <input type="text" name="text" value="${esc(c.text)}" required maxlength="500" />
          <button type="button" class="btn-ghost btn-small" data-action="cancel-comment-edit">Отмена</button>
          <button class="btn btn-small" type="submit">ОК</button>
        </form>
      </div>
    `;
  }

  return `
    <div class="comment" data-comment-id="${c.id}">
      <div class="comment-body">
        <div class="comment-meta">${esc(c.authorName)} · ${formatDate(c.createdAt)}</div>
        <div class="comment-text">${esc(c.text)}</div>
      </div>
      ${canManage ? `
      <div class="comment-actions">
        <button class="btn-ghost btn-small" data-action="edit-comment" title="Изменить">✎</button>
        <button class="btn-danger btn-small" data-action="delete-comment" title="Удалить">✕</button>
      </div>` : ''}
    </div>
  `;
}

function attachPostHandlers(post) {
  const article = document.querySelector(`.post[data-post-id="${post.id}"]`);
  if (!article) return;

  article.querySelector('[data-action="like"]')?.addEventListener('click', async () => {
    try {
      const res = await api('/likes', { method: 'POST', body: { postId: post.id } });
      post.liked = res.liked;
      post.likesCount += res.liked ? 1 : -1;
      renderPostsList();
    } catch (err) {
      toast(err.message, true);
    }
  });

  article.querySelector('[data-action="toggle-comments"]')?.addEventListener('click', () => {
    if (expandedPosts.has(post.id)) expandedPosts.delete(post.id);
    else expandedPosts.add(post.id);
    renderPostsList();
  });

  article.querySelector('[data-action="edit-post"]')?.addEventListener('click', () => {
    editingPostId = post.id;
    renderPostsList();
  });

  article.querySelector('[data-action="cancel-post-edit"]')?.addEventListener('click', () => {
    editingPostId = null;
    renderPostsList();
  });

  article.querySelector('[data-action="save-post-edit"]')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const btn = form.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
      const title = form.title.value.trim();
      const content = form.content.value.trim();
      await api('/posts/' + post.id, { method: 'PUT', body: { title, content } });
      post.title = title;
      post.content = content;
      editingPostId = null;
      renderPostsList();
      toast('Пост обновлён');
    } catch (err) {
      toast(err.message, true);
      btn.disabled = false;
    }
  });

  article.querySelector('[data-action="delete-post"]')?.addEventListener('click', async () => {
    if (!confirm('Удалить пост?')) return;
    try {
      await api('/posts/' + post.id, { method: 'DELETE' });
      posts = posts.filter((p) => p.id !== post.id);
      renderPostsList();
      toast('Пост удалён');
    } catch (err) {
      toast(err.message, true);
    }
  });

  article.querySelector('[data-action="add-comment"]')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const text = form.text.value.trim();
    if (!text) return;
    const btn = form.querySelector('button');
    btn.disabled = true;
    try {
      const created = await api('/comments', { method: 'POST', body: { text, postId: post.id } });
      post.comments = post.comments || [];
      post.comments.push(created);
      expandedPosts.add(post.id);
      renderPostsList();
    } catch (err) {
      toast(err.message, true);
      btn.disabled = false;
    }
  });

  (post.comments || []).forEach((c) => {
    const commentEl = article.querySelector(`.comment[data-comment-id="${c.id}"]`);
    if (!commentEl) return;

    commentEl.querySelector('[data-action="edit-comment"]')?.addEventListener('click', () => {
      editingCommentId = c.id;
      renderPostsList();
    });

    commentEl.querySelector('[data-action="cancel-comment-edit"]')?.addEventListener('click', () => {
      editingCommentId = null;
      renderPostsList();
    });

    commentEl.querySelector('[data-action="save-comment-edit"]')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const form = e.target;
      const text = form.text.value.trim();
      const btn = form.querySelector('button[type=submit]');
      btn.disabled = true;
      try {
        const updated = await api('/comments/' + c.id, { method: 'PUT', body: { text } });
        Object.assign(c, updated);
        editingCommentId = null;
        renderPostsList();
      } catch (err) {
        toast(err.message, true);
        btn.disabled = false;
      }
    });

    commentEl.querySelector('[data-action="delete-comment"]')?.addEventListener('click', async () => {
      if (!confirm('Удалить комментарий?')) return;
      try {
        await api('/comments/' + c.id, { method: 'DELETE' });
        post.comments = post.comments.filter((x) => x.id !== c.id);
        renderPostsList();
      } catch (err) {
        toast(err.message, true);
      }
    });
  });
}

// ---------- init ----------

window.addEventListener('hashchange', renderRoute);
renderRoute();
