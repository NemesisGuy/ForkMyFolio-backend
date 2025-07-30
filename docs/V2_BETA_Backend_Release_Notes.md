# ForkMyFolio v2.0.0-BETA: Backend Release Notes

This document details the key backend enhancements, new features, and architectural refinements that power the v2.0.0-BETA release. These changes focus on expanding data portability, improving analytics, and solidifying our API architecture.

---

## 1. New Feature: Multi-Format Portfolio Downloads

To support the new "Download Center" on the frontend, a suite of new public-facing endpoints has been added to the `PortfolioController`. These endpoints allow any visitor to download a user's portfolio in various formats.

### New API Endpoints

| Method | Path                 | Controller Method                 | Description                                       |
| :----- | :------------------- | :-------------------------------- | :------------------------------------------------ |
| `GET`  | `/{slug}/pdf`        | `downloadPortfolioAsPdf`          | Generates and serves a PDF version of the portfolio. |
| `GET`  | `/{slug}/markdown`   | `downloadPortfolioAsMarkdown`     | Generates and serves a Markdown version of the portfolio. |
| `GET`  | `/{slug}/vcard`      | `downloadVCard`                   | Generates and serves a vCard (.vcf) contact file. |

### Implementation Details

-   **File Streams**: These endpoints return a raw `byte[]` stream, not JSON.
-   **`@SkipApiResponseWrapper`**: A custom annotation is used to bypass our standard JSON response wrapper, ensuring the file is delivered correctly to the browser.
-   **Content-Disposition**: The `Content-Disposition` header is set to `attachment`, which prompts the user's browser to download the file with a clean, dynamically generated filename (e.g., `PeterBuckinghan-Portfolio.pdf`).

---

## 2. New Feature: Visitor Analytics Tracking

We've introduced a non-invasive, AOP-based system to track key visitor interactions for analytics.

-   **`@TrackVisitor` Annotation**: This new annotation can be placed on any controller method to automatically record a specific event.
-   **`VisitorStatType` Enum**: Defines the types of events we can track, such as `PDF_DOWNLOAD`, `MARKDOWN_DOWNLOAD`, and `VCARD_DOWNLOAD`.
-   **Aspect-Oriented Programming (AOP)**: The tracking logic is completely decoupled from the business logic of the controller, making it easy to add or remove tracking from any endpoint without modifying its code.

**Example from `PortfolioController.java`:**
3. Architectural SolidificationThe v2 release formalizes the architectural rules that ensure the backend remains clean, scalable, and maintainable.Core Philosophy: Separation of Concerns•Controller Layer (@RestController): Thin layer responsible only for handling HTTP requests/responses and DTOs. It contains no business logic.•Service Layer (@Service): Contains all business logic. It operates exclusively on domain models (@Entity objects) and is completely unaware of HTTP or DTOs.•Repository Layer (@Repository): Simple interfaces for database access.Strict Data FlowThe flow of data is strictly enforced to maintain separation:Request (DTO) → Controller → Service (operates on Entity) → Repository → DatabaseThis ensures that our internal domain models are never exposed directly to the outside world and that business logic is centralized and reusable.API Consistency•Standardized Response Wrapper: All JSON API responses are wrapped in a consistent ApiResponseWrapper<T> structure, providing a predictable format for both successful data and detailed errors.•Security First: We continue to use UUIDs for all external-facing IDs and a robust JWT authentication flow with a secure HttpOnly refresh token cookie.These backend enhancements provide a powerful and stable foundation for the new frontend features, completing the vision for the v2.0.0-BETA release.Kotlin---

## 3. Architectural Solidification

The v2 release formalizes the architectural rules that ensure the backend remains clean, scalable, and maintainable.

### Core Philosophy: Separation of Concerns

-   **Controller Layer (`@RestController`)**: Thin layer responsible only for handling HTTP requests/responses and DTOs. **It contains no business logic.**
-   **Service Layer (`@Service`)**: Contains all business logic. It operates exclusively on domain models (`@Entity` objects) and is completely unaware of HTTP or DTOs.
-   **Repository Layer (`@Repository`)**: Simple interfaces for database access.

### Strict Data Flow

The flow of data is strictly enforced to maintain separation:

`Request (DTO)` → `Controller` → `Service (operates on Entity)` → `Repository` → `Database`

This ensures that our internal domain models are never exposed directly to the outside world and that business logic is centralized and reusable.

### API Consistency

-   **Standardized Response Wrapper**: All JSON API responses are wrapped in a consistent `ApiResponseWrapper<T>` structure, providing a predictable format for both successful data and detailed errors.
-   **Security First**: We continue to use UUIDs for all external-facing IDs and a robust JWT authentication flow with a secure `HttpOnly` refresh token cookie.

These backend enhancements provide a powerful and stable foundation for the new frontend features, completing the vision for the v2.0.0-BETA release.