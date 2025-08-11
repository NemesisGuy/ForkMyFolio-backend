# API Documentation & Structure

This document provides details on the API's standardized response structure and the interactive documentation available via Swagger UI.

## API Response Structure

All API responses (both for successful requests and errors) are wrapped in a standardized JSON structure:

```json
{
  "status": "success", // Possible values: "success", "fail", "unauthorized", "forbidden", "validation_failed", "error"
  "data": "<T>",       // The actual data payload for the request, or null if not applicable. The type 'T' varies per endpoint.
  "errors": [          // An array of error details, typically empty for successful responses.
    {
      "field": "fieldName", // Name of the field that caused the error. Can be "general" for non-field-specific errors.
      "message": "A descriptive error message."
    }
  ]
}
```

## Swagger UI (Interactive Documentation)

Once the application is running, the interactive Swagger UI documentation can be accessed at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

You can use the "Authorize" button on Swagger UI to authenticate using a JWT access token obtained from the `/auth/login` endpoint. The format is `Bearer <your_jwt_token>`.