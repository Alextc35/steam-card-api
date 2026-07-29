# Project Review

## Current State

Steam Card API is in a strong release-candidate state for personal use and public embedding:

- Clear Spring Boot package boundaries: controllers, clients, services, mappers, models, SVG renderers, validation, exceptions, and config.
- Public JSON and SVG endpoints are covered by controller tests.
- Steam Web API, Store API, CDN validation, image fallback, and renderer behavior are covered by unit tests.
- Secrets are externalized through environment variables.
- Docker development and production flows are present.
- SVG responses include cache headers, ETags, and `nosniff`.
- Image embedding is constrained to official Steam hosts and bounded by size/content-type checks.
- Showcase profile cards include Steam-style status badges, 100 inline SVG country flags, profile links, footer link, game-store cover link, and no-game placeholders.

## Refactors Completed In This Pass

- Centralized cacheable binary/SVG HTTP responses in `HttpResourceResponses`.
- Replaced generic `Object` return types in profile sub-endpoints with concrete response types.
- Reused `SteamCardService` for generic rendered resource creation and ETag hashing.
- Added CI workflow for Maven verification and Docker image build.
- Expanded ignore rules for `.env.*` secret variants.
- Added deployment documentation and this project review.
- Added Spring Boot build metadata so `/` reports the packaged version instead of a hardcoded value.
- Added `CountryFlagRenderer` for the supported top 100 Steam country codes.
- Added game-cover links to Steam Store app pages and local SVG placeholders for missing game artwork.

## Remaining Refactors

These are useful, but not blockers for publishing:

- Split `SteamProfileCardRenderer` into layout-specific renderers. It is currently the largest file and will be easier to evolve if `compact`, `normal`, `showcase`, `hero`, `minimal`, and `library` each live behind a shared profile-rendering interface.
- Keep `CountryFlagRenderer` data-driven if the flag set grows beyond the current top 100 codes.
- Add SVG snapshot tests for the showcase layout. Existing string tests protect behavior, but snapshots would catch accidental visual markup shifts.
- Add OpenAPI documentation for JSON endpoints.
- Add optional proxy or application rate limiting before broad public traffic.

## Release Checklist

- `./mvnw verify` passes locally.
- `docker build -t steam-card-api:latest .` passes.
- `docker run` works with production environment variables.
- `/actuator/health` returns `UP`.
- `/api/steam/card.svg?...imageMode=embedded` renders from the public hostname.
- The public hostname uses HTTPS.
- The Steam API key is configured as a secret, not committed and not baked into the image.
- README embed URLs use the final public hostname.
- The MIT License file is included before publication.
- A backend-capable host is selected for the API; Hostinger Website Builder alone should only embed the public SVG URL.
