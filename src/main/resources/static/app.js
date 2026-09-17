// Фронтенд отдаётся самим бэкендом, поэтому запросы идут на тот же адрес.
// Так работает любой способ доступа: localhost, IP в локальной сети, туннель.
const API_BASE = '';

const app = document.getElementById('app');
const navEl = document.getElementById('nav');
const topbarEl = document.getElementById('topbar');

let session = loadSession();
let posts = null;
let listContext = 'feed'; // 'feed' | 'profile' — влияет только на текст пустого состояния
let feedFilter = 'all';   // 'all' | 'mine' | 'liked'
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

function initial(name) {
  return String(name ?? '?').trim().charAt(0) || '?';
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

function formatNumber(n) {
  return Number(n || 0).toLocaleString('ru-RU');
}

function plural(n, one, few, many) {
  const a = Math.abs(n) % 100;
  const b = a % 10;
  if (a > 10 && a < 20) return many;
  if (b > 1 && b < 5) return few;
  if (b === 1) return one;
  return many;
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
    throw new Error('Не удалось соединиться с сервером. Проверьте, что backend запущен.');
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

  topbarEl.hidden = !session;
  if (!session) {
    navEl.innerHTML = '';
    return;
  }

  navEl.innerHTML = `
    <div class="who">
      <span class="ava">${esc(initial(session.name))}</span>
      <span class="hello">
        <small>Вы вошли как</small>
        <b>${esc(session.name)}</b>
      </span>
    </div>
    <nav class="nav">
      <a href="#/" class="${route === '/' ? 'on' : ''}">Лента</a>
      <a href="#/profile" class="${route === '/profile' ? 'on' : ''}">Профиль</a>
    </nav>
    <button class="circ" id="logoutBtn" title="Выйти" aria-label="Выйти">&#9099;</button>
  `;

  document.getElementById('logoutBtn').onclick = () => {
    clearSession();
    posts = null;
    location.hash = '#/login';
  };
}

// ---------- auth ----------

function renderAuthForm(mode) {
  const isLogin = mode === 'login';
  app.innerHTML = `
    <div class="auth-wrap">
      <div class="brand-lead">
        <h1>${isLogin ? 'С возвращением' : 'Создайте аккаунт'}</h1>
        <p>${isLogin ? 'Войдите, чтобы читать и писать посты' : 'Пара полей — и вы в ленте'}</p>
      </div>

      <div class="card auth-card">
        <form id="authForm">
          ${!isLogin ? `
          <div class="field">
            <label for="f-name">Имя</label>
            <input id="f-name" name="name" type="text" required autocomplete="name" placeholder="Как вас зовут" />
          </div>` : ''}
          <div class="field">
            <label for="f-email">Email</label>
            <input id="f-email" name="email" type="email" required autocomplete="email"
                   placeholder="you@example.com" value="${esc(prefillEmail)}" />
          </div>
          <div class="field">
            <label for="f-pass">Пароль</label>
            <input id="f-pass" name="password" type="password" required ${!isLogin ? 'minlength="6"' : ''}
                   autocomplete="${isLogin ? 'current-password' : 'new-password'}" placeholder="••••••••" />
          </div>
          <div style="height:6px"></div>
          <button class="btn w" type="submit">${isLogin ? 'Войти' : 'Зарегистрироваться'}</button>
          <div class="error-text" id="authError" hidden></div>
        </form>
        <p class="auth-foot">
          ${isLogin ? 'Нет аккаунта?' : 'Уже есть аккаунт?'}
          <button class="btn-link" type="button" id="switchAuth">${isLogin ? 'Зарегистрироваться' : 'Войти'}</button>
        </p>
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
        const payload = { email: form.elements.email.value.trim(), password: form.elements.password.value };
        const res = await api('/auth/login', { method: 'POST', body: payload, auth: false });
        saveSession(res);
        toast('Добро пожаловать, ' + res.name);
        location.hash = '#/';
      } else {
        const payload = {
          name: form.elements.name.value.trim(),
          email: form.elements.email.value.trim(),
          password: form.elements.password.value,
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

// ---------- лента ----------

async function renderFeed() {
  listContext = 'feed';
  app.innerHTML = `
    <section class="hero">
      <p class="hero-label">Лайков на ваших постах</p>
      <p class="hero-n" id="heroLikes">—</p>
      <p class="hero-sub" id="heroSub">&nbsp;</p>
      <button class="btn-cta" id="composeToggle">Написать пост <i>&#8599;</i></button>
    </section>

    <form id="composerForm" class="card" hidden>
      <p class="card-h">Новый пост</p>
      <div class="field">
        <label for="f-title">Заголовок</label>
        <input id="f-title" name="title" type="text" required maxlength="200" placeholder="О чём пост" />
      </div>
      <div class="field">
        <label for="f-content">Текст</label>
        <textarea id="f-content" name="content" required placeholder="Расскажите подробнее"></textarea>
      </div>
      <div class="row-end">
        <button class="btn-soft" type="button" id="composeCancel">Отмена</button>
        <button class="btn" type="submit">Опубликовать</button>
      </div>
    </form>

    <div class="chips" id="chips">
      <button class="chip ${feedFilter === 'all' ? 'on' : ''}" data-filter="all">Все</button>
      <button class="chip ${feedFilter === 'mine' ? 'on' : ''}" data-filter="mine">Мои</button>
      <button class="chip ${feedFilter === 'liked' ? 'on' : ''}" data-filter="liked">С лайками</button>
    </div>

    <div id="postsContainer"><div class="loading">Загрузка…</div></div>
  `;

  const composer = document.getElementById('composerForm');

  document.getElementById('composeToggle').onclick = () => {
    composer.hidden = !composer.hidden;
    if (!composer.hidden) {
      composer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      composer.elements.title.focus();
    }
  };
  document.getElementById('composeCancel').onclick = () => {
    composer.reset();
    composer.hidden = true;
  };

  composer.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = composer.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
      const created = await api('/posts', {
        method: 'POST',
        body: { title: composer.elements.title.value.trim(), content: composer.elements.content.value.trim() },
      });
      posts = posts || [];
      posts.unshift(created);
      composer.reset();
      composer.hidden = true;
      renderPostsList();
      updateHeroStats();
      toast('Пост опубликован');
    } catch (err) {
      toast(err.message, true);
    } finally {
      btn.disabled = false;
    }
  });

  document.getElementById('chips').addEventListener('click', (e) => {
    const chip = e.target.closest('[data-filter]');
    if (!chip) return;
    feedFilter = chip.dataset.filter;
    document.querySelectorAll('#chips .chip').forEach((c) => {
      c.classList.toggle('on', c.dataset.filter === feedFilter);
    });
    renderPostsList();
  });

  try {
    posts = await api('/posts/all');
    posts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    renderPostsList();
    updateHeroStats();
  } catch (err) {
    document.getElementById('postsContainer').innerHTML =
      `<div class="error-card">${esc(err.message)}</div>`;
  }
}

function myPosts() {
  return (posts || []).filter((p) => p.authorId === session.id);
}

function updateHeroStats() {
  const likesEl = document.getElementById('heroLikes');
  const subEl = document.getElementById('heroSub');
  if (!likesEl || !subEl) return;

  const mine = myPosts();
  const likes = mine.reduce((sum, p) => sum + (p.likesCount || 0), 0);
  const comments = mine.reduce((sum, p) => sum + (p.comments || []).length, 0);

  likesEl.textContent = formatNumber(likes);
  subEl.innerHTML = `<b>${mine.length}</b> ${plural(mine.length, 'ваш пост', 'ваших поста', 'ваших постов')}
    · <b>${comments}</b> ${plural(comments, 'комментарий', 'комментария', 'комментариев')}`;
}

// ---------- профиль ----------

async function renderProfile() {
  listContext = 'profile';
  app.innerHTML = `
    <section class="prof-hero">
      <span class="ava xl">${esc(initial(session.name))}</span>
      <p class="prof-name">${esc(session.name)}</p>
      <p class="prof-mail">${esc(session.email)}</p>
      <div class="stats">
        <div class="stat"><b id="stPosts">—</b><small>постов</small></div>
        <div class="stat"><b id="stLikes">—</b><small>лайков</small></div>
        <div class="stat"><b id="stComments">—</b><small>коммент.</small></div>
      </div>
    </section>

    <form class="card" id="profileForm">
      <p class="card-h">Настройки профиля</p>
      <div class="field">
        <label for="f-pname">Имя</label>
        <input id="f-pname" name="name" type="text" required value="${esc(session.name)}" />
      </div>
      <div class="field">
        <label for="f-pmail">Email</label>
        <input id="f-pmail" name="email" type="email" required value="${esc(session.email)}" />
      </div>
      <div class="field">
        <label for="f-ppass">Новый пароль</label>
        <input id="f-ppass" name="password" type="password" minlength="6"
               placeholder="Оставьте пустым, чтобы не менять" />
      </div>
      <div class="field">
        <label for="f-prole">Роль</label>
        <input id="f-prole" type="text" value="${esc(session.role)}" disabled />
      </div>
      <div class="row-end">
        <button class="btn" type="submit">Сохранить</button>
      </div>
      <div class="error-text" id="profileError" hidden></div>
    </form>

    <div class="sec"><h2>Мои посты</h2></div>
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
      const body = { name: form.elements.name.value.trim(), email: form.elements.email.value.trim() };
      if (form.elements.password.value) body.password = form.elements.password.value;
      const updated = await api('/users/' + session.id, { method: 'PUT', body });
      saveSession({ ...session, name: updated.name, email: updated.email });
      form.elements.password.value = '';
      renderNav();
      document.querySelector('.prof-name').textContent = updated.name;
      document.querySelector('.prof-mail').textContent = updated.email;
      document.querySelector('.prof-hero .ava').textContent = initial(updated.name);
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
    updateProfileStats();
  } catch (err) {
    document.getElementById('postsContainer').innerHTML =
      `<div class="error-card">${esc(err.message)}</div>`;
  }
}

function updateProfileStats() {
  const p = document.getElementById('stPosts');
  if (!p) return;
  const likes = (posts || []).reduce((sum, x) => sum + (x.likesCount || 0), 0);
  const comments = (posts || []).reduce((sum, x) => sum + (x.comments || []).length, 0);
  p.textContent = formatNumber((posts || []).length);
  document.getElementById('stLikes').textContent = formatNumber(likes);
  document.getElementById('stComments').textContent = formatNumber(comments);
}

function refreshStats() {
  if (listContext === 'profile') updateProfileStats();
  else updateHeroStats();
}

// ---------- список постов ----------

function visiblePosts() {
  if (!posts) return [];
  if (listContext !== 'feed') return posts;
  if (feedFilter === 'mine') return posts.filter((p) => p.authorId === session.id);
  if (feedFilter === 'liked') return posts.filter((p) => p.liked);
  return posts;
}

function renderPostsList() {
  const target = document.getElementById('postsContainer');
  if (!target) return;

  const list = visiblePosts();

  if (list.length === 0) {
    let text = 'Пока нет постов. Будьте первым!';
    if (listContext === 'profile') text = 'У вас пока нет постов.';
    else if (feedFilter === 'mine') text = 'Вы ещё ничего не публиковали.';
    else if (feedFilter === 'liked') text = 'Вы пока никому не поставили лайк.';
    target.innerHTML = `<div class="empty">${text}</div>`;
    return;
  }

  target.innerHTML = list.map(postCardHtml).join('');
  list.forEach(attachPostHandlers);
}

function postCardHtml(post) {
  const isOwner = session && post.authorId === session.id;
  const isAdmin = session && session.role === 'ADMIN';
  const canManage = isOwner || isAdmin;
  const isEditing = editingPostId === post.id;
  const commentsOpen = expandedPosts.has(post.id);
  const comments = post.comments || [];

  const head = `
    <div class="post-top">
      <span class="ava">${esc(initial(post.authorName))}</span>
      <div class="post-who">
        <b>${esc(post.authorName)}${isOwner ? '<span class="tag-you">вы</span>' : ''}</b>
        <small>${formatDate(post.createdAt)}</small>
      </div>
      ${canManage && !isEditing ? `
      <div class="post-more">
        <button class="btn-soft" data-action="edit-post">Изменить</button>
        <button class="btn-soft danger" data-action="delete-post">Удалить</button>
      </div>` : ''}
    </div>
  `;

  const body = isEditing ? `
    <form data-action="save-post-edit">
      <div class="field">
        <label for="edit-title-${post.id}">Заголовок</label>
        <input id="edit-title-${post.id}" name="title" type="text" value="${esc(post.title)}" required />
      </div>
      <div class="field">
        <label for="edit-content-${post.id}">Текст</label>
        <textarea id="edit-content-${post.id}" name="content" required>${esc(post.content)}</textarea>
      </div>
      <div class="row-end">
        <button type="button" class="btn-soft" data-action="cancel-post-edit">Отмена</button>
        <button type="submit" class="btn">Сохранить</button>
      </div>
    </form>
  ` : `
    <h3 class="post-title">${esc(post.title)}</h3>
    <p class="post-body">${esc(post.content)}</p>
    <div class="post-acts">
      <button class="act ${post.liked ? 'on' : ''}" data-action="like">
        <span class="h">${post.liked ? '&#9829;' : '&#9825;'}</span> ${post.likesCount}
      </button>
      <button class="act" data-action="toggle-comments">
        Комментарии ${comments.length}
      </button>
    </div>
  `;

  const commentsBlock = isEditing ? '' : `
    <div class="comments" ${commentsOpen ? '' : 'hidden'}>
      ${comments.length
        ? comments.map(commentHtml).join('')
        : '<p class="cmt-txt" style="color:var(--muted)">Комментариев пока нет</p>'}
      <form class="cmt-form" data-action="add-comment">
        <input id="cmt-${post.id}" name="text" type="text" placeholder="Написать комментарий…" required maxlength="500" />
        <button class="btn" type="submit">Отправить</button>
      </form>
    </div>
  `;

  return `<article class="post" data-post-id="${post.id}">${head}${body}${commentsBlock}</article>`;
}

function commentHtml(c) {
  const isOwner = session && c.authorId === session.id;
  const isAdmin = session && session.role === 'ADMIN';
  const canManage = isOwner || isAdmin;

  if (editingCommentId === c.id) {
    return `
      <div class="cmt" data-comment-id="${c.id}">
        <span class="ava s">${esc(initial(c.authorName))}</span>
        <form class="cmt-form" data-action="save-comment-edit" style="flex:1">
          <input id="cmt-edit-${c.id}" name="text" type="text" value="${esc(c.text)}" required maxlength="500" />
          <button type="button" class="btn-soft" data-action="cancel-comment-edit">Отмена</button>
          <button class="btn" type="submit">ОК</button>
        </form>
      </div>
    `;
  }

  return `
    <div class="cmt" data-comment-id="${c.id}">
      <span class="ava s">${esc(initial(c.authorName))}</span>
      <div class="cmt-b">
        <div class="cmt-head">
          <b>${esc(c.authorName)}</b>
          <small>${formatDate(c.createdAt)}</small>
        </div>
        <p class="cmt-txt">${esc(c.text)}</p>
      </div>
      ${canManage ? `
      <div class="cmt-acts">
        <button class="ic" data-action="edit-comment" title="Изменить">&#9998;</button>
        <button class="ic danger" data-action="delete-comment" title="Удалить">&#10005;</button>
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
      refreshStats();
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
      const title = form.elements.title.value.trim();
      const content = form.elements.content.value.trim();
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
      refreshStats();
      toast('Пост удалён');
    } catch (err) {
      toast(err.message, true);
    }
  });

  article.querySelector('[data-action="add-comment"]')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const text = form.elements.text.value.trim();
    if (!text) return;
    const btn = form.querySelector('button[type=submit]');
    btn.disabled = true;
    try {
      const created = await api('/comments', { method: 'POST', body: { text, postId: post.id } });
      post.comments = post.comments || [];
      post.comments.push(created);
      expandedPosts.add(post.id);
      renderPostsList();
      refreshStats();
    } catch (err) {
      toast(err.message, true);
      btn.disabled = false;
    }
  });

  (post.comments || []).forEach((c) => {
    const commentEl = article.querySelector(`.cmt[data-comment-id="${c.id}"]`);
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
      const text = form.elements.text.value.trim();
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
        refreshStats();
      } catch (err) {
        toast(err.message, true);
      }
    });
  });
}

// ---------- init ----------

window.addEventListener('hashchange', renderRoute);
renderRoute();
