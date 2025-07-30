# Frontend Migration Guide: Upgrading to ForkMyFolio v2.0.0

Welcome, frontend team! This guide helps you update the ForkMyFolio frontend (Vue.js) to work with the new v2.0.0 backend, which now supports multiple users, each with their own portfolio (e.g., `forkmyfolio.com/jane-doe`). The backend has been revamped to handle user accounts, public portfolio pages, and admin features. This document explains the changes you need to make to the frontend, with clear steps and Vue.js examples to get you started.

## 1. What’s New in v2.0.0?

The backend has shifted from a single-user app to a multi-user platform. Here’s what this means for the frontend:

- **Multiple Users**: Each user has their own portfolio data, accessed via a unique `slug` (e.g., `jane-doe`).
- **New API Structure**: APIs are grouped into:
  - **Public**: For viewing portfolios (e.g., `/api/v1/portfolios/jane-doe`) or logging in.
  - **User**: For managing your own data (e.g., `/api/v1/me/projects`), requires login.
  - **Admin**: For managing all users (e.g., `/api/v1/admin/users`), requires admin access.
- **Authentication**: Uses JSON Web Tokens (JWTs) with a short-lived `accessToken` and an HttpOnly `refreshToken` cookie.
- **Visibility Controls**: Users can toggle whether their portfolio sections (e.g., projects, skills) are public or private.
- **Standard Responses**: All API calls return a consistent JSON format for easy handling.

See `API_ENDPOINTS.md` for the full list of endpoints.

## 2. Update Your API Calls

### 2.1. New Base Path

All API calls now start with `/api/v1/` instead of the old paths.

| Old Path (Example) | New Path (Example)            | What It Does                          |
|--------------------|-------------------------------|---------------------------------------|
| `/projects`        | `/api/v1/me/projects`         | Gets the logged-in user’s projects.   |
| `/skills`          | `/api/v1/me/skills`           | Gets the logged-in user’s skills.     |
| `/profile`         | `/api/v1/me/profile`          | Gets the logged-in user’s profile.    |

**Action**: Update your API client (e.g., Axios) to use the new base URL:

```javascript
// src/api/index.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://forkmyfolio.com/api/v1', // Update to your production URL
});

export default api;
```

### 2.2. Resource IDs

- **Old**: Resources used numeric IDs (e.g., `/projects/1`).
- **New**: Resources use UUID strings (e.g., `/api/v1/me/projects/123e4567-e89b-12d3-a456-426614174000`).

**Action**: Update all API calls to use `uuid` fields from API responses instead of `id`.

## 3. Handle Authentication

The backend uses JWTs for authentication. Here’s how to manage login, registration, token refresh, and logout in Vue.js.

### 3.1. Login and Register

- **Login**: `POST /api/v1/auth/login`
- **Register**: `POST /api/v1/auth/register`
- **What You Get**: A JSON response with an `accessToken` and user details, plus an HttpOnly `refreshToken` cookie set by the backend.

**Example (Login)**:

```javascript
// src/views/Login.vue
import { authStore } from '../stores/auth';
import api from '../api';

export default {
  data() {
    return { email: '', password: '' };
  },
  methods: {
    async login() {
      try {
        const response = await api.post('/auth/login', {
          email: this.email,
          password: this.password,
        });
        if (response.data.status === 'success') {
          authStore.setAuth(response.data.data.user, response.data.data.accessToken);
          this.$router.push('/dashboard');
        } else {
          alert('Login failed: ' + response.data.errors[0]?.message);
        }
      } catch (error) {
        alert('Error: ' + (error.response?.data.errors[0]?.message || 'Network issue'));
      }
    },
  },
};
```

**Register**: Similar to login, use `POST /api/v1/auth/register` with `username`, `email`, and `password`.

### 3.2. Auth Store

Create a store to manage authentication state:

```javascript
// src/stores/auth.js
import { reactive } from 'vue';
import api from '../api';

export const authStore = reactive({
  loggedIn: false,
  accessToken: null,
  user: null,
  roles: [],
  setAuth(user, accessToken) {
    this.loggedIn = true;
    this.user = user;
    this.accessToken = accessToken;
    this.roles = user.roles;
  },
  async refresh() {
    try {
      const response = await api.post('/auth/refresh-token');
      if (response.data.status === 'success') {
        this.accessToken = response.data.data.accessToken;
        return true;
      }
    } catch (error) {
      this.logout();
    }
    return false;
  },
  logout() {
    api.post('/auth/logout');
    this.loggedIn = false;
    this.accessToken = null;
    this.user = null;
    this.roles = [];
  },
});
```

### 3.3. Token Refresh on 401 Errors

If an API call returns `401 Unauthorized`, the `accessToken` has expired. Call `POST /api/v1/auth/refresh-token` to get a new one, then retry the request.

**Example (Axios Interceptor)**:

```javascript
// src/api/index.js
import axios from 'axios';
import { authStore } from '../stores/auth';

const api = axios.create({
  baseURL: 'https://forkmyfolio.com/api/v1',
});

api.interceptors.request.use(config => {
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      if (await authStore.refresh()) {
        error.config.headers.Authorization = `Bearer ${authStore.accessToken}`;
        return api(error.config);
      }
      authStore.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export default api;
```

### 3.4. Logout

- **Endpoint**: `POST /api/v1/auth/logout`
- **Action**: Clear the auth store and redirect to `/login`.

```javascript
// src/views/Dashboard.vue
import { authStore } from '../stores/auth';

export default {
  methods: {
    logout() {
      authStore.logout();
      this.$router.push('/login');
    },
  },
};
```

## 4. Update the Dashboard

The user dashboard (e.g., `/dashboard`) lets users manage their portfolio (projects, skills, etc.). All API calls now use `/api/v1/me/` and UUIDs.

### 4.1. Fetching and Updating Data

Update all data-fetching calls to use the new endpoints. Example for projects:

```javascript
// src/views/Dashboard.vue
import { authStore } from '../stores/auth';
import api from '../api';

export default {
  data() {
    return { projects: [] };
  },
  async created() {
    try {
      const response = await api.get('/me/projects');
      if (response.data.status === 'success') {
        this.projects = response.data.data;
      } else {
        alert('Error: ' + response.data.errors[0]?.message);
      }
    } catch (error) {
      alert('Error: ' + (error.response?.data.errors[0]?.message || 'Network issue'));
    }
  },
};
```

### 4.2. Visibility Toggles

Users can toggle whether portfolio sections are public (e.g., `visible: true`). Add toggles for each entity (profile, projects, skills, etc.).

**Example (Project Visibility)**:

```javascript
// src/views/Dashboard.vue
import api from '../api';

export default {
  data() {
    return { projects: [] };
  },
  methods: {
    async toggleProjectVisibility(uuid, visible) {
      try {
        const response = await api.put(`/me/projects/${uuid}/visibility`, { visible });
        if (response.data.status === 'success') {
          const project = this.projects.find(p => p.uuid === uuid);
          project.visible = response.data.data.visible;
        } else {
          alert('Error: ' + response.data.errors[0]?.message);
        }
      } catch (error) {
        alert('Error: ' + (error.response?.data.errors[0]?.message || 'Network issue'));
      }
    },
  },
};
```

**Template**:

```vue
<template>
  <div v-for="project in projects" :key="project.uuid">
    <h3>{{ project.title }}</h3>
    <label>
      <input type="checkbox" v-model="project.visible" @change="toggleProjectVisibility(project.uuid, $event.target.checked)">
      Public
    </label>
  </div>
</template>
```

**Entities**: Apply similar logic for `profile`, `skills`, `experiences`, `qualifications`, and `testimonials` (e.g., `PUT /api/v1/me/skills/{uuid}/visibility`).

## 5. Create Public Portfolio Pages

Add a dynamic route (e.g., `/:slug`) to display public portfolios using `GET /api/v1/portfolios/{slug}`. Only show sections marked `visible: true`.

**Example**:

```vue
// src/views/PortfolioPage.vue
<template>
  <div v-if="portfolio">
    <h1>{{ portfolio.user.username }}'s Portfolio</h1>
    <section v-if="portfolio.profile?.visible">
      <h2>About</h2>
      <p>{{ portfolio.profile.summary }}</p>
    </section>
    <section v-if="portfolio.projects?.length">
      <h2>Projects</h2>
      <div v-for="project in portfolio.projects" :key="project.uuid" v-if="project.visible">
        <h3>{{ project.title }}</h3>
      </div>
    </section>
  </div>
</template>
<script>
import api from '../api';

export default {
  data() {
    return { portfolio: null };
  },
  async created() {
    try {
      const response = await api.get(`/portfolios/${this.$route.params.slug}`);
      if (response.data.status === 'success') {
        this.portfolio = response.data.data;
      } else {
        this.$router.push('/404');
      }
    } catch (error) {
      this.$router.push('/404');
    }
  },
};
</script>
<style>
.portfolio-container { animation: fadeIn 1s ease-in; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
```

## 6. Role-Based Navigation

Show different navigation options based on the user’s state:
- **Guest**: Home, Login, Register, Public Portfolios (`/{slug}`).
- **User**: Add Dashboard (`/dashboard`).
- **Admin**: Add Admin Panel (`/admin/users`).

**Example (Vue Router)**:

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { authStore } from '../stores/auth';

const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/:slug', component: PortfolioPage },
  {
    path: '/dashboard',
    component: Dashboard,
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/users',
    component: AdminPanel,
    meta: { requiresAdmin: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !authStore.loggedIn) {
    return next('/login');
  }
  if (to.meta.requiresAdmin && !authStore.roles.includes('ADMIN')) {
    return next('/403');
  }
  next();
});

export default router;
```

**Navigation Component**:

```vue
// src/App.vue
<template>
  <nav>
    <router-link to="/">Home</router-link>
    <router-link v-if="!authStore.loggedIn" to="/login">Login</router-link>
    <router-link v-if="!authStore.loggedIn" to="/register">Register</router-link>
    <router-link v-if="authStore.loggedIn" to="/dashboard">Dashboard</router-link>
    <router-link v-if="authStore.roles.includes('ADMIN')" to="/admin/users">Admin Panel</router-link>
    <button v-if="authStore.loggedIn" @click="authStore.logout()">Logout</button>
  </nav>
  <router-view />
</template>
<script>
import { authStore } from './stores/auth';
export default {
  computed: { authStore: () => authStore },
};
</script>
```

## 7. Handle API Responses

All API responses use this format:

```json
{
  "status": "success", // or "fail", "unauthorized", "validation_failed", "error"
  "data": {},         // Data (e.g., projects, user) or null
  "errors": []        // Error details (e.g., [{ field: "email", message: "Invalid email" }])
}
```

**Example (Error Handling)**:

```javascript
// src/views/Dashboard.vue
async fetchSkills() {
  try {
    const response = await api.get('/me/skills');
    if (response.data.status === 'success') {
      this.skills = response.data.data;
    } else {
      this.errors = response.data.errors;
      alert('Error: ' + this.errors[0]?.message);
    }
  } catch (error) {
    this.errors = error.response?.data.errors || [{ field: 'general', message: 'Network error' }];
    alert('Error: ' + this.errors[0].message);
  }
}
```

## 8. Routing Diagram

```
Home (/) ----> Public Portfolio (/:slug)
   |              |
   |              v
Login (/login)  PortfolioPage.vue
   |
   v
Dashboard (/dashboard) ----> Admin Panel (/admin/users)
   (ROLE_USER)                (ROLE_ADMIN)
```

## 9. Frontend Actions Checklist

1. **Set Up API Client**:
   - Update Axios base URL to `/api/v1/`.
   - Add `Authorization: Bearer <token>` for authenticated calls.

2. **Implement Authentication**:
   - Handle login/register responses to store `accessToken`.
   - Set up token refresh on `401` errors.
   - Clear state on logout.

3. **Update Dashboard**:
   - Use `/api/v1/me/` endpoints for all user data (e.g., `/me/projects`).
   - Replace numeric IDs with UUIDs.
   - Add visibility toggle checkboxes for each entity.

4. **Build Public Portfolio Pages**:
   - Create `/:slug` route to fetch `/api/v1/portfolios/{slug}`.
   - Display only `visible: true` sections with Tailwind CSS styling.

5. **Add Role-Based Navigation**:
   - Show/hide links based on `authStore.loggedIn` and `authStore.roles`.
   - Protect routes with Vue Router guards.

6. **Handle Errors**:
   - Check `response.data.status` and display `response.data.errors`.

7. **Test Everything**:
   - Use Jest/Vue Test Utils for components (e.g., `PortfolioPage.vue`).
   - Use Cypress for E2E tests (e.g., login, portfolio viewing, dashboard updates).

## 10. Notes

- **Security**: Store `accessToken` in memory (not localStorage). The `refreshToken` cookie is managed by the backend.
- **Swagger UI**: Test APIs at `http://localhost:8080/swagger-ui.html` (or production URL).
- **Documentation**: Check `API_ENDPOINTS.md` for all endpoints and schemas.
- **Styling**: Use Tailwind CSS for responsive designs, as per the backend plan.
- **Performance**: Public portfolio data is cached server-side for 5 minutes. Consider client-side caching for repeated views.