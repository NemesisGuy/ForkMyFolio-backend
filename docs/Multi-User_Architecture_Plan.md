
# ForkMyFolio v2.0.0: Multi-User Architecture Plan

## 1. Vision & Core Objective

The primary goal of version 2.0.0 is to evolve ForkMyFolio from a single-instance portfolio application into a multi-user SaaS platform. Users will register accounts, manage their portfolio data, and share it via unique, clean URLs (e.g., `forkmyfolio.com/jane-doe`). The platform will support three access levels: Guest (unauthenticated, view-only public portfolios), User (authenticated, manage own data), and Admin (manage all users and moderate content). This requires changes to the data model, API design, security architecture, and frontend to support data tenancy, role-based access, and user-specific content.

## 2. Key Architectural Changes

The work is divided into four major areas:

1. **Data Model & Tenancy**: Associate portfolio data with specific users and add visibility controls.
2. **API Evolution**: Redesign APIs for user-specific data, public portfolio access, and admin management.
3. **Authentication & Registration**: Enhance registration and role-based access control.
4. **Frontend Routing & UI**: Adapt frontend for public portfolio views, user dashboards, and admin panels with role-based navigation.

## 3. Detailed Implementation Plan

### 3.1. Data Model & Tenancy

The current data model assumes a single user. To support multiple users, we introduce **data ownership** (tenancy) and visibility controls for portfolio sections.

#### Action Items:

1. **Update User Entity:**
    - Add fields to `User.java` for slug, roles, and account status, using the existing `Role` enum.
    - Example:
      ```java
      @Entity
      public class User {
          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;
 
          @Column(name = "slug", unique = true, nullable = false, length = 50)
          private String slug;
 
          @Column(nullable = false)
          private String username;
 
          @Column(nullable = false)
          private String password;
 
          @Column(nullable = false)
          private boolean active = true;
 
          @ElementCollection(fetch = FetchType.EAGER)
          @Enumerated(EnumType.STRING)
          private Set<Role> roles; // e.g., [Role.USER], [Role.ADMIN]
      }
      ```

2. **Update Entity Relationships:**
    - Add `user_id` foreign key and `visible` flag to all user-owned entities: `Project`, `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, `ContactMessage`.
    - Example for `Project.java`:
      ```java
      @Entity
      public class Project {
          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          private Long id;
 
          @ManyToOne(fetch = FetchType.LAZY)
          @JoinColumn(name = "user_id", nullable = false)
          private User user;
 
          @Column(nullable = false)
          private boolean visible = true;
 
          // Other fields (title, description, etc.)
      }
      ```
    - Apply similar changes to `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, and `ContactMessage`.

3. **Database Schema Updates:**
    - Use Flyway or Liquibase to add `slug`, `active`, and `roles` to `users` table and `user_id`, `visible` to other tables.
    - Example migration (Flyway SQL):
      ```sql
      ALTER TABLE users ADD COLUMN slug VARCHAR(50) NOT NULL UNIQUE;
      ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE users ADD COLUMN roles TEXT[] NOT NULL DEFAULT '{USER}';
      ALTER TABLE projects ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE projects ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE projects ADD CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE skills ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE skills ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE skills ADD CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE experiences ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE experiences ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE experiences ADD CONSTRAINT fk_experiences_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE qualifications ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE qualifications ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE qualifications ADD CONSTRAINT fk_qualifications_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE portfolio_profiles ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE portfolio_profiles ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE portfolio_profiles ADD CONSTRAINT fk_portfolio_profiles_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE testimonials ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE testimonials ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
      ALTER TABLE testimonials ADD CONSTRAINT fk_testimonials_user FOREIGN KEY (user_id) REFERENCES users(id);
      ALTER TABLE contact_messages ADD COLUMN user_id BIGINT NOT NULL;
      ALTER TABLE contact_messages ADD CONSTRAINT fk_contact_messages_user FOREIGN KEY (user_id) REFERENCES users(id);
      ```

4. **Data Isolation:**
    - Update repositories (`ProjectRepository`, `SkillRepository`, etc.) to filter by `user_id` and `visible` for public access.
    - Example for `ProjectRepository.java`:
      ```java
      List<Project> findByUserIdAndVisibleTrue(Long userId);
      List<Project> findByUserId(Long userId); // For authenticated user
      ```

5. **Slug Generation Logic:**
    - Implement in `UserService` to generate unique slugs.
    - Example:
      ```java
      public String generateUniqueSlug(String baseSlug) {
          String slug = baseSlug.toLowerCase().replaceAll("[^a-z0-9-]", "-");
          int counter = 1;
          String candidate = slug;
          while (userRepository.existsBySlug(candidate) || isReservedSlug(candidate)) {
              candidate = slug + "-" + counter++;
          }
          return candidate;
      }
      private boolean isReservedSlug(String slug) {
          return Set.of("api", "admin", "login", "dashboard").contains(slug);
      }
      ```

### 3.2. API Evolution

APIs must support user-specific data, public portfolio access, and admin management, with a consistent response structure.

#### Action Items:

1. **Public Portfolio Endpoints:**
    - Add `GET /api/v1/portfolios/{slug}` to retrieve visible portfolio data (no authentication).
    - Response:
      ```json
      {
        "status": "success",
        "data": {
          "user": {
            "slug": "jane-doe",
            "username": "Jane Doe"
          },
          "profile": { /* Visible PortfolioProfile data */ },
          "projects": [ /* Visible projects */ ],
          "skills": [ /* Visible skills */ ],
          "experiences": [ /* Visible experiences */ ],
          "qualifications": [ /* Visible qualifications */ ],
          "testimonials": [ /* Visible testimonials */ ]
        },
        "errors": []
      }
      ```
    - Implement in `PortfolioController.java`:
      ```java
      @GetMapping("/portfolios/{slug}")
      public ResponseEntity<ApiResponse<PortfolioDTO>> getPortfolioBySlug(@PathVariable String slug) {
          PortfolioDTO portfolio = portfolioService.getPortfolioBySlug(slug);
          return ResponseEntity.ok(new ApiResponse<>("success", portfolio, Collections.emptyList()));
      }
      ```

2. **Private User-Specific Endpoints:**
    - Update CRUD endpoints to scope to the authenticated user’s `user_id`.
    - Example for `ProjectController.java`:
      ```java
      @PreAuthorize("hasRole('USER')")
      @GetMapping("/projects")
      public ResponseEntity<ApiResponse<List<ProjectDTO>>> getUserProjects() {
          Long userId = getAuthenticatedUserId();
          List<ProjectDTO> projects = projectService.findByUserId(userId);
          return ResponseEntity.ok(new ApiResponse<>("success", projects, Collections.emptyList()));
      }
      @PreAuthorize("hasRole('USER')")
      @PutMapping("/projects/{id}")
      public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(@PathVariable Long id, @RequestBody ProjectDTO dto) {
          Long userId = getAuthenticatedUserId();
          ProjectDTO updated = projectService.updateProject(id, userId, dto);
          return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
      }
      ```

3. **Admin Endpoints:**
    - Add `/api/v1/admin/users` for user management (list, edit, delete, suspend).
    - Example for `AdminController.java`:
      ```java
      @PreAuthorize("hasRole('ADMIN')")
      @GetMapping("/admin/users")
      public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
          List<UserDTO> users = userService.getAllUsers();
          return ResponseEntity.ok(new ApiResponse<>("success", users, Collections.emptyList()));
      }
      @PreAuthorize("hasRole('ADMIN')")
      @PutMapping("/admin/users/{id}")
      public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
          UserDTO updated = userService.updateUser(id, dto);
          return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
      }
      @PreAuthorize("hasRole('ADMIN')")
      @DeleteMapping("/admin/users/{id}")
      public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
          userService.deleteUser(id);
          return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
      }
      ```

4. **Visibility Toggles:**
    - Add endpoints to toggle `visible` flag for entities (e.g., `PUT /api/v1/projects/{id}/visibility`).
    - Example:
      ```java
      @PreAuthorize("hasRole('USER')")
      @PutMapping("/projects/{id}/visibility")
      public ResponseEntity<ApiResponse<ProjectDTO>> toggleProjectVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> visibility) {
          Long userId = getAuthenticatedUserId();
          ProjectDTO updated = projectService.toggleVisibility(id, userId, visibility.get("visible"));
          return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
      }
      ```

5. **API Versioning and Documentation:**
    - Use `/api/v1/` prefix for all endpoints.
    - Update `OpenApiConfig.java` to document public, user, and admin endpoints with examples.

### 3.3. Authentication & Registration

Enhance the existing JWT-based authentication system for multi-user support and role management.

#### Action Items:

1. **Enhance User Registration:**
    - Update `/auth/register` to include slug generation and default `Role.USER`.
    - Request payload:
      ```json
      {
        "username": "Jane Doe",
        "email": "jane@example.com",
        "password": "securePassword123"
      }
      ```
    - Response:
      ```json
      {
        "status": "success",
        "data": {
          "user": {
            "id": 1,
            "slug": "jane-doe",
            "username": "Jane Doe",
            "email": "jane@example.com",
            "roles": ["USER"]
          },
          "accessToken": "jwt-token"
        },
        "errors": []
      }
      ```

2. **Role-Based Permissions:**
    - Use Spring Security with `@EnableMethodSecurity`:
      ```java
      @Configuration
      @EnableMethodSecurity
      public class SecurityConfig {
          @Bean
          public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
              http.authorizeHttpRequests(auth -> auth
                  .requestMatchers("/api/v1/portfolios/**").permitAll()
                  .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                  .requestMatchers("/api/v1/**").hasAnyRole("USER", "ADMIN")
                  .anyRequest().permitAll());
              return http.build();
          }
      }
      ```

3. **Profile Management:**
    - Add `PUT /api/v1/users/me/profile` for users to update their profile:
      ```java
      @PreAuthorize("hasRole('USER')")
      @PutMapping("/users/me/profile")
      public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody UserDTO dto) {
          Long userId = getAuthenticatedUserId();
          UserDTO updated = userService.updateProfile(userId, dto);
          return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
      }
      ```

4. **Admin Features:**
    - Add admin capabilities to reset passwords, suspend/ban users, or moderate slugs:
      ```java
      @PreAuthorize("hasRole('ADMIN')")
      @PostMapping("/admin/users/{id}/reset-password")
      public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Long id) {
          userService.resetPassword(id);
          return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
      }
      ```

5. **Security Enhancements:**
    - Enforce slug blacklist (e.g., `api`, `admin`) in `UserService`.
    - Add rate-limiting to admin endpoints using Spring Boot’s `Bucket4j` or similar.
    - Log admin actions (e.g., user deletion) to `application.log`.

### 3.4. Frontend Routing & UI

The frontend will use Vue.js for a single-page application (SPA) with role-based navigation and visibility controls.

#### Action Items:

1. **Public Portfolio Pages:**
    - Create a route for `forkmyfolio.com/{slug}` to display visible portfolio data.
    - Fetch from `GET /api/v1/portfolios/{slug}`.
    - Example Vue component (`PortfolioPage.vue`):
      ```vue
      <template>
        <div class="portfolio-container" v-if="portfolio">
          <h1>{{ portfolio.user.username }}'s Portfolio</h1>
          <section v-if="portfolio.profile.visible">
            <h2>Profile</h2>
            <p>{{ portfolio.profile.bio }}</p>
          </section>
          <section v-if="portfolio.projects.length">
            <h2>Projects</h2>
            <div v-for="project in portfolio.projects" :key="project.id" v-if="project.visible">
              <h3>{{ project.title }}</h3>
            </div>
          </section>
        </div>
      </template>
      <script>
      export default {
        data() { return { portfolio: null }; },
        async created() {
          const { data } = await axios.get(`/api/v1/portfolios/${this.$route.params.slug}`);
          this.portfolio = data.data;
        }
      };
      </script>
      <style>
      .portfolio-container { animation: fadeIn 1s ease-in; }
      @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
      </style>
      ```

2. **User Dashboard:**
    - Create `forkmyfolio.com/dashboard` for authenticated users to manage profile, projects, skills, etc.
    - Include visibility toggles for each section.
    - Example (`Dashboard.vue`):
      ```vue
      <template>
        <div>
          <h1>Dashboard</h1>
          <section>
            <h2>Projects</h2>
            <div v-for="project in projects" :key="project.id">
              <h3>{{ project.title }}</h3>
              <label>
                <input type="checkbox" v-model="project.visible" @change="toggleVisibility(project.id, project.visible)">
                Visible
              </label>
            </div>
          </section>
        </div>
      </template>
      <script>
      export default {
        data() { return { projects: [] }; },
        async created() {
          const { data } = await axios.get('/api/v1/projects', { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
          this.projects = data.data;
        },
        methods: {
          async toggleVisibility(id, visible) {
            await axios.put(`/api/v1/projects/${id}/visibility`, { visible }, { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
          }
        }
      };
      </script>
      ```

3. **Role-Based Navigation:**
    - Conditionally render navigation based on user roles.
    - Example (`App.vue`):
      ```vue
      <template>
        <nav>
          <router-link to="/">Home</router-link>
          <router-link v-if="!auth.loggedIn" to="/login">Login</router-link>
          <router-link v-if="auth.isUser" to="/dashboard">Dashboard</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users">Admin Panel</router-link>
        </nav>
        <router-view />
      </template>
      <script>
      import { authStore } from './authStore';
      export default {
        computed: {
          auth() {
            return {
              loggedIn: authStore.loggedIn,
              isUser: authStore.roles.includes('USER'),
              isAdmin: authStore.roles.includes('ADMIN')
            };
          }
        }
      };
      </script>
      ```

4. **Auth Store:**
    - Manage JWT tokens and roles in Vue.
    - Example (`authStore.js`):
      ```javascript
      import { reactive } from 'vue';
      export const authStore = reactive({
        loggedIn: false,
        accessToken: null,
        roles: [],
        async login(credentials) {
          const { data } = await axios.post('/api/v1/auth/login', credentials);
          this.accessToken = data.data.accessToken;
          this.roles = data.data.user.roles;
          this.loggedIn = true;
        },
        async refresh() {
          const { data } = await axios.post('/api/v1/auth/refresh-token');
          this.accessToken = data.data.accessToken;
        }
      });
      ```

5. **UI Enhancements:**
    - Use Tailwind CSS for responsive, modern styling.
    - Apply fade-in animations for portfolio sections.
    - Example CSS in `PortfolioPage.vue` (above).

## 4. Implementation Timeline

- **Week 1-2: Data Model & Tenancy**
    - Update entities with `user_id`, `visible`, `roles`.
    - Implement migrations and slug generation.
- **Week 3-4: API Evolution**
    - Develop public portfolio and admin endpoints.
    - Update user-specific endpoints with visibility toggles.
    - Enhance Swagger documentation.
- **Week 5-6: Authentication & Registration**
    - Enhance registration with roles and slug.
    - Update security for role-based access.
    - Implement admin and profile management endpoints.
- **Week 7-8: Frontend Routing & UI**
    - Build public portfolio pages and user dashboard.
    - Implement role-based navigation and visibility toggles.
    - Apply Tailwind CSS and animations.
- **Week 9-10: Testing & Deployment**
    - Test endpoints, role enforcement, and UI.
    - Update Docker for production.
    - Deploy with monitoring and logging.

## 5. Testing Strategy

- **Unit Tests**: Test slug generation, role assignment, and visibility logic.
- **Integration Tests**: Verify API endpoints with mocked users and roles.
- **End-to-End Tests**: Simulate guest access, user management, and admin actions.
- **Security Tests**: Ensure data isolation, role enforcement, and slug blacklist.
- Run with `mvn test` and review `target/surefire-reports`.

## 6. Deployment Considerations

- **Database**: Configure PostgreSQL with indexes on `slug`, `user_id`, `visible`.
- **Environment Variables**: Set `JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS` for `forkmyfolio.com`.
- **Docker**: Include migration scripts in `Dockerfile`.
- **Monitoring**: Log user and admin actions using SLF4J in `com.forkmyfolio`.
- **Rate-Limiting**: Apply to admin endpoints using `Bucket4j`.

## 7. Risks & Mitigations

- **Risk**: Slug collisions or reserved slugs.
    - **Mitigation**: Enforce uniqueness and blacklist reserved slugs.
- **Risk**: Data leakage across users.
    - **Mitigation**: Filter by `user_id` in all queries; test isolation.
- **Risk**: Public endpoint performance.
    - **Mitigation**: Cache responses with Spring Cache or Redis.
- **Risk**: Admin abuse or unauthorized access.
    - **Mitigation**: Rate-limit and audit admin actions; enforce HTTPS.

## 8. Future Considerations

- **Super Admin Role**: Add `Role.SUPERADMIN` for managing admins.
- **Analytics**: Track portfolio views via `GET /api/v1/portfolios/{slug}`.
- **Customization**: Allow users to customize portfolio themes.
- **Social Sharing**: Add share buttons for portfolios.
- **Admin Dashboard**: Expand with analytics and logs.

This plan transforms ForkMyFolio into a secure, scalable, multi-user SaaS platform with clear role-based access and a modern frontend experience.

```<xaiArtifact artifact_id="3136ad82-9745-465b-95bd-134f62fcc4b6" artifact_version_id="b75ef4b6-6055-4b84-ad3a-dce9698c5fcc" title="ForkMyFolio-v2.0.0-Architecture-Plan.md" contentType="text/markdown">

# ForkMyFolio v2.0.0: Multi-User Architecture Plan

## 1. Vision & Core Objective

The primary goal of version 2.0.0 is to evolve ForkMyFolio from a single-instance portfolio application into a multi-user SaaS platform. Users will register accounts, manage their portfolio data, and share it via unique, clean URLs (e.g., `forkmyfolio.com/jane-doe`). The platform will support three access levels: Guest (unauthenticated, view-only public portfolios), User (authenticated, manage own data), and Admin (manage all users and moderate content). This requires changes to the data model, API design, security architecture, and frontend to support data tenancy, role-based access, and user-specific content.

## 2. Key Architectural Changes

The work is divided into four major areas:

1. **Data Model & Tenancy**: Associate portfolio data with specific users and add visibility controls.
2. **API Evolution**: Redesign APIs for user-specific data, public portfolio access, and admin management.
3. **Authentication & Registration**: Enhance registration and role-based access control.
4. **Frontend Routing & UI**: Adapt frontend for public portfolio views, user dashboards, and admin panels with role-based navigation.

## 3. Detailed Implementation Plan

### 3.1. Data Model & Tenancy

The current data model assumes a single user. To support multiple users, we introduce **data ownership** (tenancy) and visibility controls for portfolio sections.

#### Action Items:

1. **Update User Entity:**
   - Add fields to `User.java` for slug, roles, and account status, using the existing `Role` enum.
   - Example:
     ```java
     @Entity
     public class User {
         @Id
         @GeneratedValue(strategy = GenerationType.IDENTITY)
         private Long id;

         @Column(name = "slug", unique = true, nullable = false, length = 50)
         private String slug;

         @Column(nullable = false)
         private String username;

         @Column(nullable = false)
         private String password;

         @Column(nullable = false)
         private boolean active = true;

         @ElementCollection(fetch = FetchType.EAGER)
         @Enumerated(EnumType.STRING)
         private Set<Role> roles; // e.g., [Role.USER], [Role.ADMIN]
     }
     ```

2. **Update Entity Relationships:**
   - Add `user_id` foreign key and `visible` flag to all user-owned entities: `Project`, `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, `ContactMessage`.
   - Example for `Project.java`:
     ```java
     @Entity
     public class Project {
         @Id
         @GeneratedValue(strategy = GenerationType.IDENTITY)
         private Long id;

         @ManyToOne(fetch = FetchType.LAZY)
         @JoinColumn(name = "user_id", nullable = false)
         private User user;

         @Column(nullable = false)
         private boolean visible = true;

         // Other fields (title, description, etc.)
     }
     ```
   - Apply similar changes to `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, and `ContactMessage`.

3. **Database Schema Updates:**
   - Use Flyway or Liquibase to add `slug`, `active`, and `roles` to `users` table and `user_id`, `visible` to other tables.
   - Example migration (Flyway SQL):
     ```sql
     ALTER TABLE users ADD COLUMN slug VARCHAR(50) NOT NULL UNIQUE;
     ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE users ADD COLUMN roles TEXT[] NOT NULL DEFAULT '{USER}';
     ALTER TABLE projects ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE projects ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE projects ADD CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE skills ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE skills ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE skills ADD CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE experiences ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE experiences ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE experiences ADD CONSTRAINT fk_experiences_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE qualifications ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE qualifications ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE qualifications ADD CONSTRAINT fk_qualifications_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE portfolio_profiles ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE portfolio_profiles ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE portfolio_profiles ADD CONSTRAINT fk_portfolio_profiles_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE testimonials ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE testimonials ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE testimonials ADD CONSTRAINT fk_testimonials_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE contact_messages ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE contact_messages ADD CONSTRAINT fk_contact_messages_user FOREIGN KEY (user_id) REFERENCES users(id);
     ```

4. **Data Isolation:**
   - Update repositories (`ProjectRepository`, `SkillRepository`, etc.) to filter by `user_id` and `visible` for public access.
   - Example for `ProjectRepository.java`:
     ```java
     List<Project> findByUserIdAndVisibleTrue(Long userId);
     List<Project> findByUserId(Long userId); // For authenticated user
     ```

5. **Slug Generation Logic:**
   - Implement in `UserService` to generate unique slugs.
   - Example:
     ```java
     public String generateUniqueSlug(String baseSlug) {
         String slug = baseSlug.toLowerCase().replaceAll("[^a-z0-9-]", "-");
         int counter = 1;
         String candidate = slug;
         while (userRepository.existsBySlug(candidate) || isReservedSlug(candidate)) {
             candidate = slug + "-" + counter++;
         }
         return candidate;
     }
     private boolean isReservedSlug(String slug) {
         return Set.of("api", "admin", "login", "dashboard").contains(slug);
     }
     ```

### 3.2. API Evolution

APIs must support user-specific data, public portfolio access, and admin management, with a consistent response structure.

#### Action Items:

1. **Public Portfolio Endpoints:**
   - Add `GET /api/v1/portfolios/{slug}` to retrieve visible portfolio data (no authentication).
   - Response:
     ```json
     {
       "status": "success",
       "data": {
         "user": {
           "slug": "jane-doe",
           "username": "Jane Doe"
         },
         "profile": { /* Visible PortfolioProfile data */ },
         "projects": [ /* Visible projects */ ],
         "skills": [ /* Visible skills */ ],
         "experiences": [ /* Visible experiences */ ],
         "qualifications": [ /* Visible qualifications */ ],
         "testimonials": [ /* Visible testimonials */ ]
       },
       "errors": []
     }
     ```
   - Implement in `PortfolioController.java`:
     ```java
     @GetMapping("/portfolios/{slug}")
     public ResponseEntity<ApiResponse<PortfolioDTO>> getPortfolioBySlug(@PathVariable String slug) {
         PortfolioDTO portfolio = portfolioService.getPortfolioBySlug(slug);
         return ResponseEntity.ok(new ApiResponse<>("success", portfolio, Collections.emptyList()));
     }
     ```

2. **Private User-Specific Endpoints:**
   - Update CRUD endpoints to scope to the authenticated user’s `user_id`.
   - Example for `ProjectController.java`:
     ```java
     @PreAuthorize("hasRole('USER')")
     @GetMapping("/projects")
     public ResponseEntity<ApiResponse<List<ProjectDTO>>> getUserProjects() {
         Long userId = getAuthenticatedUserId();
         List<ProjectDTO> projects = projectService.findByUserId(userId);
         return ResponseEntity.ok(new ApiResponse<>("success", projects, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/projects/{id}")
     public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(@PathVariable Long id, @RequestBody ProjectDTO dto) {
         Long userId = getAuthenticatedUserId();
         ProjectDTO updated = projectService.updateProject(id, userId, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

3. **Admin Endpoints:**
   - Add `/api/v1/admin/users` for user management (list, edit, delete, suspend).
   - Example for `AdminController.java`:
     ```java
     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/admin/users")
     public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
         List<UserDTO> users = userService.getAllUsers();
         return ResponseEntity.ok(new ApiResponse<>("success", users, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('ADMIN')")
     @PutMapping("/admin/users/{id}")
     public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
         UserDTO updated = userService.updateUser(id, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('ADMIN')")
     @DeleteMapping("/admin/users/{id}")
     public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
         userService.deleteUser(id);
         return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
     }
     ```

4. **Visibility Toggles:**
   - Add endpoints to toggle `visible` flag for entities (e.g., `PUT /api/v1/projects/{id}/visibility`).
   - Example:
     ```java
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/projects/{id}/visibility")
     public ResponseEntity<ApiResponse<ProjectDTO>> toggleProjectVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> visibility) {
         Long userId = getAuthenticatedUserId();
         ProjectDTO updated = projectService.toggleVisibility(id, userId, visibility.get("visible"));
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

5. **API Versioning and Documentation:**
   - Use `/api/v1/` prefix for all endpoints.
   - Update `OpenApiConfig.java` to document public, user, and admin endpoints with examples.

### 3.3. Authentication & Registration

Enhance the existing JWT-based authentication system for multi-user support and role management.

#### Action Items:

1. **Enhance User Registration:**
   - Update `/auth/register` to include slug generation and default `Role.USER`.
   - Request payload:
     ```json
     {
       "username": "Jane Doe",
       "email": "jane@example.com",
       "password": "securePassword123"
     }
     ```
   - Response:
     ```json
     {
       "status": "success",
       "data": {
         "user": {
           "id": 1,
           "slug": "jane-doe",
           "username": "Jane Doe",
           "email": "jane@example.com",
           "roles": ["USER"]
         },
         "accessToken": "jwt-token"
       },
       "errors": []
     }
     ```

2. **Role-Based Permissions:**
   - Use Spring Security with `@EnableMethodSecurity`:
     ```java
     @Configuration
     @EnableMethodSecurity
     public class SecurityConfig {
         @Bean
         public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
             http.authorizeHttpRequests(auth -> auth
                 .requestMatchers("/api/v1/portfolios/**").permitAll()
                 .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                 .requestMatchers("/api/v1/**").hasAnyRole("USER", "ADMIN")
                 .anyRequest().permitAll());
             return http.build();
         }
     }
     ```

3. **Profile Management:**
   - Add `PUT /api/v1/users/me/profile` for users to update their profile:
     ```java
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/users/me/profile")
     public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody UserDTO dto) {
         Long userId = getAuthenticatedUserId();
         UserDTO updated = userService.updateProfile(userId, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

4. **Admin Features:**
   - Add admin capabilities to reset passwords, suspend/ban users, or moderate slugs:
     ```java
     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/admin/users/{id}/reset-password")
     public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Long id) {
         userService.resetPassword(id);
         return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
     }
     ```

5. **Security Enhancements:**
   - Enforce slug blacklist (e.g., `api`, `admin`) in `UserService`.
   - Add rate-limiting to admin endpoints using Spring Boot’s `Bucket4j` or similar.
   - Log admin actions (e.g., user deletion) to `application.log`.

### 3.4. Frontend Routing & UI

The frontend will use Vue.js for a single-page application (SPA) with role-based navigation and visibility controls.

#### Action Items:

1. **Public Portfolio Pages:**
   - Create a route for `forkmyfolio.com/{slug}` to display visible portfolio data.
   - Fetch from `GET /api/v1/portfolios/{slug}`.
   - Example Vue component (`PortfolioPage.vue`):
     ```vue
     <template>
       <div class="portfolio-container" v-if="portfolio">
         <h1>{{ portfolio.user.username }}'s Portfolio</h1>
         <section v-if="portfolio.profile.visible">
           <h2>Profile</h2>
           <p>{{ portfolio.profile.bio }}</p>
         </section>
         <section v-if="portfolio.projects.length">
           <h2>Projects</h2>
           <div v-for="project in portfolio.projects" :key="project.id" v-if="project.visible">
             <h3>{{ project.title }}</h3>
           </div>
         </section>
       </div>
     </template>
     <script>
     export default {
       data() { return { portfolio: null }; },
       async created() {
         const { data } = await axios.get(`/api/v1/portfolios/${this.$route.params.slug}`);
         this.portfolio = data.data;
       }
     };
     </script>
     <style>
     .portfolio-container { animation: fadeIn 1s ease-in; }
     @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
     </style>
     ```

2. **User Dashboard:**
   - Create `forkmyfolio.com/dashboard` for authenticated users to manage profile, projects, skills, etc.
   - Include visibility toggles for each section.
   - Example (`Dashboard.vue`):
     ```vue
     <template>
       <div>
         <h1>Dashboard</h1>
         <section>
           <h2>Projects</h2>
           <div v-for="project in projects" :key="project.id">
             <h3>{{ project.title }}</h3>
             <label>
               <input type="checkbox" v-model="project.visible" @change="toggleVisibility(project.id, project.visible)">
               Visible
             </label>
           </div>
         </section>
       </div>
     </template>
     <script>
     export default {
       data() { return { projects: [] }; },
       async created() {
         const { data } = await axios.get('/api/v1/projects', { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
         this.projects = data.data;
       },
       methods: {
         async toggleVisibility(id, visible) {
           await axios.put(`/api/v1/projects/${id}/visibility`, { visible }, { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
         }
       }
     };
     </script>
     ```

3. **Role-Based Navigation:**
   - Conditionally render navigation based on user roles.
   - Example (`App.vue`):
     ```vue
     <template>
       <nav>
         <router-link to="/">Home</router-link>
         <router-link v-if="!auth.loggedIn" to="/login">Login</router-link>
         <router-link v-if="auth.isUser" to="/dashboard">Dashboard</router-link>
         <router-link v-if="auth.isAdmin" to="/admin/users">Admin Panel</router-link>
       </nav>
       <router-view />
     </template>
     <script>
     import { authStore } from './authStore';
     export default {
       computed: {
         auth() {
           return {
             loggedIn: authStore.loggedIn,
             isUser: authStore.roles.includes('USER'),
             isAdmin: authStore.roles.includes('ADMIN')
           };
         }
       }
     };
     </script>
     ```

4. **Auth Store:**
   - Manage JWT tokens and roles in Vue.
   - Example (`authStore.js`):
     ```javascript
     import { reactive } from 'vue';
     export const authStore = reactive({
       loggedIn: false,
       accessToken: null,
       roles: [],
       async login(credentials) {
         const { data } = await axios.post('/api/v1/auth/login', credentials);
         this.accessToken = data.data.accessToken;
         this.roles = data.data.user.roles;
         this.loggedIn = true;
       },
       async refresh() {
         const { data } = await axios.post('/api/v1/auth/refresh-token');
         this.accessToken = data.data.accessToken;
       }
     });
     ```

5. **UI Enhancements:**
   - Use Tailwind CSS for responsive, modern styling.
   - Apply fade-in animations for portfolio sections.
   - Example CSS in `PortfolioPage.vue` (above).

## 4. Implementation Timeline

- **Week 1-2: Data Model & Tenancy**
  - Update entities with `user_id`, `visible`, `roles`.
  - Implement migrations and slug generation.
- **Week 3-4: API Evolution**
  - Develop public portfolio and admin endpoints.
  - Update user-specific endpoints with visibility toggles.
  - Enhance Swagger documentation.
- **Week 5-6: Authentication & Registration**
  - Enhance registration with roles and slug.
  - Update security for role-based access.
  - Implement admin and profile management endpoints.
- **Week 7-8: Frontend Routing & UI**
  - Build public portfolio pages and user dashboard.
  - Implement role-based navigation and visibility toggles.
  - Apply Tailwind CSS and animations.
- **Week 9-10: Testing & Deployment**
  - Test endpoints, role enforcement, and UI.
  - Update Docker for production.
  - Deploy with monitoring and logging.

## 5. Testing Strategy

- **Unit Tests**: Test slug generation, role assignment, and visibility logic.
- **Integration Tests**: Verify API endpoints with mocked users and roles.
- **End-to-End Tests**: Simulate guest access, user management, and admin actions.
- **Security Tests**: Ensure data isolation, role enforcement, and slug blacklist.
- Run with `mvn test` and review `target/surefire-reports`.

## 6. Deployment Considerations

- **Database**: Configure PostgreSQL with indexes on `slug`, `user_id`, `visible`.
- **Environment Variables**: Set `JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS` for `forkmyfolio.com`.
- **Docker**: Include migration scripts in `Dockerfile`.
- **Monitoring**: Log user and admin actions using SLF4J in `com.forkmyfolio`.
- **Rate-Limiting**: Apply to admin endpoints using `Bucket4j`.

## 7. Risks & Mitigations

- **Risk**: Slug collisions or reserved slugs.
  - **Mitigation**: Enforce uniqueness and blacklist reserved slugs.
- **Risk**: Data leakage across users.
  - **Mitigation**: Filter by `user_id` in all queries; test isolation.
- **Risk**: Public endpoint performance.
  - **Mitigation**: Cache responses with Spring Cache or Redis.
- **Risk**: Admin abuse or unauthorized access.
  - **Mitigation**: Rate-limit and audit admin actions; enforce HTTPS.

## 8. Future Considerations

- **Super Admin Role**: Add `Role.SUPERADMIN` for managing admins.
- **Analytics**: Track portfolio views via `GET /api/v1/portfolios/{slug}`.
- **Customization**: Allow users to customize portfolio themes.
- **Social Sharing**: Add share buttons for portfolios.
- **Admin Dashboard**: Expand with analytics and logs.

This plan transforms ForkMyFolio into a secure, scalable, multi-user SaaS platform with clear role-based access and a modern frontend experience.

</xaiArtifact>

# ForkMyFolio v2.0.0: Multi-User Architecture Plan

## 1. Vision & Core Objective

The primary goal of version 2.0.0 is to evolve ForkMyFolio from a single-instance portfolio application into a multi-user SaaS platform. Users will register accounts, manage their portfolio data, and share it via unique, clean URLs (e.g., `forkmyfolio.com/jane-doe`). The platform will support three access levels: Guest (unauthenticated, view-only public portfolios), User (authenticated, manage own data), and Admin (manage all users and moderate content). This requires changes to the data model, API design, security architecture, and frontend to support data tenancy, role-based access, and user-specific content.

## 2. Key Architectural Changes

The work is divided into four major areas:

1. **Data Model & Tenancy**: Associate portfolio data with specific users and add visibility controls.
2. **API Evolution**: Redesign APIs for user-specific data, public portfolio access, and admin management.
3. **Authentication & Registration**: Enhance registration and role-based access control.
4. **Frontend Routing & UI**: Adapt frontend for public portfolio views, user dashboards, and admin panels with role-based navigation.

## 3. Detailed Implementation Plan

### 3.1. Data Model & Tenancy

The current data model assumes a single user. To support multiple users, we introduce **data ownership** (tenancy) and visibility controls for portfolio sections.

#### Action Items:

1. **Update User Entity:**
   - Add fields to `User.java` for slug, roles, and account status, using the existing `Role` enum.
   - Example:
     ```java
     @Entity
     public class User {
         @Id
         @GeneratedValue(strategy = GenerationType.IDENTITY)
         private Long id;

         @Column(name = "slug", unique = true, nullable = false, length = 50)
         private String slug;

         @Column(nullable = false)
         private String username;

         @Column(nullable = false)
         private String password;

         @Column(nullable = false)
         private boolean active = true;

         @ElementCollection(fetch = FetchType.EAGER)
         @Enumerated(EnumType.STRING)
         private Set<Role> roles; // e.g., [Role.USER], [Role.ADMIN]
     }
     ```

2. **Update Entity Relationships:**
   - Add `user_id` foreign key and `visible` flag to all user-owned entities: `Project`, `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, `ContactMessage`.
   - Example for `Project.java`:
     ```java
     @Entity
     public class Project {
         @Id
         @GeneratedValue(strategy = GenerationType.IDENTITY)
         private Long id;

         @ManyToOne(fetch = FetchType.LAZY)
         @JoinColumn(name = "user_id", nullable = false)
         private User user;

         @Column(nullable = false)
         private boolean visible = true;

         // Other fields (title, description, etc.)
     }
     ```
   - Apply similar changes to `Skill`, `Experience`, `Qualification`, `PortfolioProfile`, `Testimonial`, and `ContactMessage`.

3. **Database Schema Updates:**
   - Use Flyway or Liquibase to add `slug`, `active`, and `roles` to `users` table and `user_id`, `visible` to other tables.
   - Example migration (Flyway SQL):
     ```sql
     ALTER TABLE users ADD COLUMN slug VARCHAR(50) NOT NULL UNIQUE;
     ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE users ADD COLUMN roles TEXT[] NOT NULL DEFAULT '{USER}';
     ALTER TABLE projects ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE projects ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE projects ADD CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE skills ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE skills ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE skills ADD CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE experiences ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE experiences ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE experiences ADD CONSTRAINT fk_experiences_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE qualifications ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE qualifications ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE qualifications ADD CONSTRAINT fk_qualifications_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE portfolio_profiles ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE portfolio_profiles ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE portfolio_profiles ADD CONSTRAINT fk_portfolio_profiles_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE testimonials ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE testimonials ADD COLUMN visible BOOLEAN NOT NULL DEFAULT true;
     ALTER TABLE testimonials ADD CONSTRAINT fk_testimonials_user FOREIGN KEY (user_id) REFERENCES users(id);
     ALTER TABLE contact_messages ADD COLUMN user_id BIGINT NOT NULL;
     ALTER TABLE contact_messages ADD CONSTRAINT fk_contact_messages_user FOREIGN KEY (user_id) REFERENCES users(id);
     ```

4. **Data Isolation:**
   - Update repositories (`ProjectRepository`, `SkillRepository`, etc.) to filter by `user_id` and `visible` for public access.
   - Example for `ProjectRepository.java`:
     ```java
     List<Project> findByUserIdAndVisibleTrue(Long userId);
     List<Project> findByUserId(Long userId); // For authenticated user
     ```

5. **Slug Generation Logic:**
   - Implement in `UserService` to generate unique slugs.
   - Example:
     ```java
     public String generateUniqueSlug(String baseSlug) {
         String slug = baseSlug.toLowerCase().replaceAll("[^a-z0-9-]", "-");
         int counter = 1;
         String candidate = slug;
         while (userRepository.existsBySlug(candidate) || isReservedSlug(candidate)) {
             candidate = slug + "-" + counter++;
         }
         return candidate;
     }
     private boolean isReservedSlug(String slug) {
         return Set.of("api", "admin", "login", "dashboard").contains(slug);
     }
     ```

### 3.2. API Evolution

APIs must support user-specific data, public portfolio access, and admin management, with a consistent response structure.

#### Action Items:

1. **Public Portfolio Endpoints:**
   - Add `GET /api/v1/portfolios/{slug}` to retrieve visible portfolio data (no authentication).
   - Response:
     ```json
     {
       "status": "success",
       "data": {
         "user": {
           "slug": "jane-doe",
           "username": "Jane Doe"
         },
         "profile": { /* Visible PortfolioProfile data */ },
         "projects": [ /* Visible projects */ ],
         "skills": [ /* Visible skills */ ],
         "experiences": [ /* Visible experiences */ ],
         "qualifications": [ /* Visible qualifications */ ],
         "testimonials": [ /* Visible testimonials */ ]
       },
       "errors": []
     }
     ```
   - Implement in `PortfolioController.java`:
     ```java
     @GetMapping("/portfolios/{slug}")
     public ResponseEntity<ApiResponse<PortfolioDTO>> getPortfolioBySlug(@PathVariable String slug) {
         PortfolioDTO portfolio = portfolioService.getPortfolioBySlug(slug);
         return ResponseEntity.ok(new ApiResponse<>("success", portfolio, Collections.emptyList()));
     }
     ```

2. **Private User-Specific Endpoints:**
   - Update CRUD endpoints to scope to the authenticated user’s `user_id`.
   - Example for `ProjectController.java`:
     ```java
     @PreAuthorize("hasRole('USER')")
     @GetMapping("/projects")
     public ResponseEntity<ApiResponse<List<ProjectDTO>>> getUserProjects() {
         Long userId = getAuthenticatedUserId();
         List<ProjectDTO> projects = projectService.findByUserId(userId);
         return ResponseEntity.ok(new ApiResponse<>("success", projects, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/projects/{id}")
     public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(@PathVariable Long id, @RequestBody ProjectDTO dto) {
         Long userId = getAuthenticatedUserId();
         ProjectDTO updated = projectService.updateProject(id, userId, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

3. **Admin Endpoints:**
   - Add `/api/v1/admin/users` for user management (list, edit, delete, suspend).
   - Example for `AdminController.java`:
     ```java
     @PreAuthorize("hasRole('ADMIN')")
     @GetMapping("/admin/users")
     public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
         List<UserDTO> users = userService.getAllUsers();
         return ResponseEntity.ok(new ApiResponse<>("success", users, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('ADMIN')")
     @PutMapping("/admin/users/{id}")
     public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
         UserDTO updated = userService.updateUser(id, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     @PreAuthorize("hasRole('ADMIN')")
     @DeleteMapping("/admin/users/{id}")
     public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
         userService.deleteUser(id);
         return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
     }
     ```

4. **Visibility Toggles:**
   - Add endpoints to toggle `visible` flag for entities (e.g., `PUT /api/v1/projects/{id}/visibility`).
   - Example:
     ```java
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/projects/{id}/visibility")
     public ResponseEntity<ApiResponse<ProjectDTO>> toggleProjectVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> visibility) {
         Long userId = getAuthenticatedUserId();
         ProjectDTO updated = projectService.toggleVisibility(id, userId, visibility.get("visible"));
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

5. **API Versioning and Documentation:**
   - Use `/api/v1/` prefix for all endpoints.
   - Update `OpenApiConfig.java` to document public, user, and admin endpoints with examples.

### 3.3. Authentication & Registration

Enhance the existing JWT-based authentication system for multi-user support and role management.

#### Action Items:

1. **Enhance User Registration:**
   - Update `/auth/register` to include slug generation and default `Role.USER`.
   - Request payload:
     ```json
     {
       "username": "Jane Doe",
       "email": "jane@example.com",
       "password": "securePassword123"
     }
     ```
   - Response:
     ```json
     {
       "status": "success",
       "data": {
         "user": {
           "id": 1,
           "slug": "jane-doe",
           "username": "Jane Doe",
           "email": "jane@example.com",
           "roles": ["USER"]
         },
         "accessToken": "jwt-token"
       },
       "errors": []
     }
     ```

2. **Role-Based Permissions:**
   - Use Spring Security with `@EnableMethodSecurity`:
     ```java
     @Configuration
     @EnableMethodSecurity
     public class SecurityConfig {
         @Bean
         public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
             http.authorizeHttpRequests(auth -> auth
                 .requestMatchers("/api/v1/portfolios/**").permitAll()
                 .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                 .requestMatchers("/api/v1/**").hasAnyRole("USER", "ADMIN")
                 .anyRequest().permitAll());
             return http.build();
         }
     }
     ```

3. **Profile Management:**
   - Add `PUT /api/v1/users/me/profile` for users to update their profile:
     ```java
     @PreAuthorize("hasRole('USER')")
     @PutMapping("/users/me/profile")
     public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody UserDTO dto) {
         Long userId = getAuthenticatedUserId();
         UserDTO updated = userService.updateProfile(userId, dto);
         return ResponseEntity.ok(new ApiResponse<>("success", updated, Collections.emptyList()));
     }
     ```

4. **Admin Features:**
   - Add admin capabilities to reset passwords, suspend/ban users, or moderate slugs:
     ```java
     @PreAuthorize("hasRole('ADMIN')")
     @PostMapping("/admin/users/{id}/reset-password")
     public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable Long id) {
         userService.resetPassword(id);
         return ResponseEntity.ok(new ApiResponse<>("success", null, Collections.emptyList()));
     }
     ```

5. **Security Enhancements:**
   - Enforce slug blacklist (e.g., `api`, `admin`) in `UserService`.
   - Add rate-limiting to admin endpoints using Spring Boot’s `Bucket4j` or similar.
   - Log admin actions (e.g., user deletion) to `application.log`.

### 3.4. Frontend Routing & UI

The frontend will use Vue.js for a single-page application (SPA) with role-based navigation and visibility controls.

#### Action Items:

1. **Public Portfolio Pages:**
   - Create a route for `forkmyfolio.com/{slug}` to display visible portfolio data.
   - Fetch from `GET /api/v1/portfolios/{slug}`.
   - Example Vue component (`PortfolioPage.vue`):
     ```vue
     <template>
       <div class="portfolio-container" v-if="portfolio">
         <h1>{{ portfolio.user.username }}'s Portfolio</h1>
         <section v-if="portfolio.profile.visible">
           <h2>Profile</h2>
           <p>{{ portfolio.profile.bio }}</p>
         </section>
         <section v-if="portfolio.projects.length">
           <h2>Projects</h2>
           <div v-for="project in portfolio.projects" :key="project.id" v-if="project.visible">
             <h3>{{ project.title }}</h3>
           </div>
         </section>
       </div>
     </template>
     <script>
     export default {
       data() { return { portfolio: null }; },
       async created() {
         const { data } = await axios.get(`/api/v1/portfolios/${this.$route.params.slug}`);
         this.portfolio = data.data;
       }
     };
     </script>
     <style>
     .portfolio-container { animation: fadeIn 1s ease-in; }
     @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
     </style>
     ```

2. **User Dashboard:**
   - Create `forkmyfolio.com/dashboard` for authenticated users to manage profile, projects, skills, etc.
   - Include visibility toggles for each section.
   - Example (`Dashboard.vue`):
     ```vue
     <template>
       <div>
         <h1>Dashboard</h1>
         <section>
           <h2>Projects</h2>
           <div v-for="project in projects" :key="project.id">
             <h3>{{ project.title }}</h3>
             <label>
               <input type="checkbox" v-model="project.visible" @change="toggleVisibility(project.id, project.visible)">
               Visible
             </label>
           </div>
         </section>
       </div>
     </template>
     <script>
     export default {
       data() { return { projects: [] }; },
       async created() {
         const { data } = await axios.get('/api/v1/projects', { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
         this.projects = data.data;
       },
       methods: {
         async toggleVisibility(id, visible) {
           await axios.put(`/api/v1/projects/${id}/visibility`, { visible }, { headers: { Authorization: `Bearer ${this.auth.accessToken}` } });
         }
       }
     };
     </script>
     ```

3. **Role-Based Navigation:**
   - Conditionally render navigation based on user roles.
   - Example (`App.vue`):
     ```vue
     <template>
       <nav>
         <router-link to="/">Home</router-link>
         <router-link v-if="!auth.loggedIn" to="/login">Login</router-link>
         <router-link v-if="auth.isUser" to="/dashboard">Dashboard</router-link>
         <router-link v-if="auth.isAdmin" to="/admin/users">Admin Panel</router-link>
       </nav>
       <router-view />
     </template>
     <script>
     import { authStore } from './authStore';
     export default {
       computed: {
         auth() {
           return {
             loggedIn: authStore.loggedIn,
             isUser: authStore.roles.includes('USER'),
             isAdmin: authStore.roles.includes('ADMIN')
           };
         }
       }
     };
     </script>
     ```

4. **Auth Store:**
   - Manage JWT tokens and roles in Vue.
   - Example (`authStore.js`):
     ```javascript
     import { reactive } from 'vue';
     export const authStore = reactive({
       loggedIn: false,
       accessToken: null,
       roles: [],
       async login(credentials) {
         const { data } = await axios.post('/api/v1/auth/login', credentials);
         this.accessToken = data.data.accessToken;
         this.roles = data.data.user.roles;
         this.loggedIn = true;
       },
       async refresh() {
         const { data } = await axios.post('/api/v1/auth/refresh-token');
         this.accessToken = data.data.accessToken;
       }
     });
     ```

5. **UI Enhancements:**
   - Use Tailwind CSS for responsive, modern styling.
   - Apply fade-in animations for portfolio sections.
   - Example CSS in `PortfolioPage.vue` (above).

## 4. Implementation Timeline

- **Week 1-2: Data Model & Tenancy**
  - Update entities with `user_id`, `visible`, `roles`.
  - Implement migrations and slug generation.
- **Week 3-4: API Evolution**
  - Develop public portfolio and admin endpoints.
  - Update user-specific endpoints with visibility toggles.
  - Enhance Swagger documentation.
- **Week 5-6: Authentication & Registration**
  - Enhance registration with roles and slug.
  - Update security for role-based access.
  - Implement admin and profile management endpoints.
- **Week 7-8: Frontend Routing & UI**
  - Build public portfolio pages and user dashboard.
  - Implement role-based navigation and visibility toggles.
  - Apply Tailwind CSS and animations.
- **Week 9-10: Testing & Deployment**
  - Test endpoints, role enforcement, and UI.
  - Update Docker for production.
  - Deploy with monitoring and logging.

## 5. Testing Strategy

- **Unit Tests**: Test slug generation, role assignment, and visibility logic.
- **Integration Tests**: Verify API endpoints with mocked users and roles.
- **End-to-End Tests**: Simulate guest access, user management, and admin actions.
- **Security Tests**: Ensure data isolation, role enforcement, and slug blacklist.
- Run with `mvn test` and review `target/surefire-reports`.

## 6. Deployment Considerations

- **Database**: Configure PostgreSQL with indexes on `slug`, `user_id`, `visible`.
- **Environment Variables**: Set `JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS` for `forkmyfolio.com`.
- **Docker**: Include migration scripts in `Dockerfile`.
- **Monitoring**: Log user and admin actions using SLF4J in `com.forkmyfolio`.
- **Rate-Limiting**: Apply to admin endpoints using `Bucket4j`.

## 7. Risks & Mitigations

- **Risk**: Slug collisions or reserved slugs.
  - **Mitigation**: Enforce uniqueness and blacklist reserved slugs.
- **Risk**: Data leakage across users.
  - **Mitigation**: Filter by `user_id` in all queries; test isolation.
- **Risk**: Public endpoint performance.
  - **Mitigation**: Cache responses with Spring Cache or Redis.
- **Risk**: Admin abuse or unauthorized access.
  - **Mitigation**: Rate-limit and audit admin actions; enforce HTTPS.

## 8. Future Considerations

- **Super Admin Role**: Add `Role.SUPERADMIN` for managing admins.
- **Analytics**: Track portfolio views via `GET /api/v1/portfolios/{slug}`.
- **Customization**: Allow users to customize portfolio themes.
- **Social Sharing**: Add share buttons for portfolios.
- **Admin Dashboard**: Expand with analytics and logs.


