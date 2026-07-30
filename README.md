# Steam Card API

Steam Card API is a Java 21 and Spring Boot REST API that turns public Steam data into reusable JSON endpoints and embeddable SVG cards for GitHub READMEs, portfolios, dashboards, and personal sites.

It can render profile cards, game cards, and resolved Steam artwork from a Steam AppID:

```html
<img src="https://example.com/api/steam/card.svg?vanity=Alextc35&layout=showcase&gameImage=portrait" alt="Steam profile card">
<img src="https://example.com/api/steam/game/730/card.svg?layout=hero&gameImage=hero" alt="Steam game card">
```

## Features

- Profile JSON with nickname, avatar, status, level, friends, country, library size, and playtime data.
- Dynamic SVG profile cards with avatar, current or recent game, Steam artwork, and safe fallbacks.
- Steam-like status badges: blue online, green in-game, red offline.
- Linked profile avatar and profile name in SVG cards when Steam provides a profile URL.
- Linked game artwork and titles in SVG cards when Steam provides a game AppID.
- Compact profile cards fit wide game thumbnails without cropping important artwork.
- Inline SVG country flag badges for the top 100 Steam country codes, with ISO text fallback.
- Game JSON and game SVG cards based on Steam Store metadata and deterministic Steam image URLs.
- Cover endpoint for portrait, header, hero, icon, logo, and small capsule images.
- Configurable themes, layouts, accent color, border style, image mode, and artwork strategy.
- Localized Steam Store metadata and SVG labels for `en`, `es`, `fr`, and `de`.
- Embedded image mode for better GitHub README compatibility.
- External image mode for smaller SVGs.
- Caffeine caches for profile data, library data, Store data, image resolution, embedded images, and rendered card data.
- Consistent JSON errors with request IDs.
- Actuator health and info only.
- Docker Compose development without Maven installed on the host.
- Multi-stage production Docker image with tests, non-root runtime user, and healthcheck.

## Stack

- Java 21
- Spring Boot 3.5.16
- Maven and Maven Wrapper
- Spring Web MVC
- Spring RestClient
- Spring Validation
- Spring Cache
- Spring Actuator
- Caffeine Cache
- JUnit 5 and Mockito
- Docker and Docker Compose

The project intentionally does not use RestTemplate, WebFlux, a database, Redis, Node.js, JavaScript, or external SVG rendering engines.

## Architecture

```text
config/       Environment properties, RestClient, cache, application wiring
client/       Steam Web API, Steam Store API, and Steam CDN access
controller/   Public JSON, SVG, cover, and root endpoints
dto/steam/    External Steam Web API response records
dto/store/    External Steam Store appdetails response records
exception/    Domain exceptions and safe JSON errors
mapper/       External DTO to internal model conversion
model/        Internal API and rendering models
service/      Aggregation, game selection, image fallback, SVG resource wrapping
svg/          Pure Java SVG renderers and escaping helpers
validation/   Request parameter validation
```

Controllers never return external Steam DTOs directly. HTTP clients, mapping, business rules, image resolution, SVG rendering, and request validation are separated.

## Configuration

Create `.env` from the example:

```bash
cp .env.example .env
```

```env
STEAM_API_KEY=replace_with_your_key
STEAM_DEFAULT_VANITY=replace_with_your_vanity
STEAM_DEFAULT_ID=
STEAM_CACHE_TTL=PT5M
STEAM_LIVE_CACHE_TTL=PT15S
STEAM_IMAGE_CACHE_TTL=PT1H
STEAM_HTTP_CONNECT_TIMEOUT=PT5S
STEAM_HTTP_READ_TIMEOUT=PT10S
STEAM_MAX_EMBEDDED_IMAGE_BYTES=1048576
```

Use exactly one default identity: `STEAM_DEFAULT_VANITY` or `STEAM_DEFAULT_ID`. Requests may override the default with exactly one of `vanity` or `steamId`.

Never commit a real Steam API key. `.env` is ignored by Git and excluded from Docker builds.

## Run With Docker Compose

```bash
docker compose up --build
```

The API runs at:

```text
http://localhost:8080
```

Compose uses `maven:3.9.11-eclipse-temurin-21`, mounts the repository into `/workspace`, stores dependencies in a persistent `maven-cache` volume, loads `.env`, and runs `./mvnw spring-boot:run` when the wrapper exists.

## Local Development

With Java 21 installed:

```bash
./mvnw spring-boot:run
```

Run the full verification suite:

```bash
./mvnw verify
```

Inside Docker:

```bash
docker compose run --rm api ./mvnw verify
```

Tests do not call the real Steam API.

## Project Documentation

- [Deployment guide](docs/DEPLOYMENT.md)
- [Project review and release checklist](docs/PROJECT_REVIEW.md)

## Endpoints

### API Info

```bash
curl http://localhost:8080/
```

```json
{
  "name": "Steam Card API",
  "status": "ok",
  "version": "0.0.1-SNAPSHOT",
  "documentation": "/",
  "profile": "/api/steam/profile",
  "library": "/api/steam/library",
  "recent": "/api/steam/recent",
  "stats": "/api/steam/stats",
  "card": "/api/steam/card.svg",
  "game": "/api/steam/game/{appId}",
  "gameCard": "/api/steam/game/{appId}/card.svg",
  "cover": "/api/steam/game/{appId}/cover",
  "health": "/actuator/health"
}
```

### Profile JSON

```bash
curl "http://localhost:8080/api/steam/profile?vanity=Alextc35"
curl "http://localhost:8080/api/steam/profile?steamId=7656119xxxxxxxxxx&lang=en"
```

### Profile SVG Card

```bash
curl "http://localhost:8080/api/steam/card.svg?vanity=Alextc35&layout=showcase&gameImage=portrait"
curl "http://localhost:8080/api/steam/card.svg?vanity=Alextc35&layout=hero&gameImage=hero&imageMode=embedded"
curl "http://localhost:8080/api/steam/card.svg?steamId=7656119xxxxxxxxxx&theme=github-light&layout=normal"
```

SVG responses include:

```text
Content-Type: image/svg+xml;charset=UTF-8
Cache-Control: public, max-age=300
ETag: "..."
X-Content-Type-Options: nosniff
```

`If-None-Match` is supported for SVG and cover responses.

Profile SVG card layouts include:

- Avatar and profile name link to the Steam profile URL.
- Country codes from the supported top 100 list render as small inline SVG flag badges; unsupported codes fall back to text.
- The main game artwork links to `https://store.steampowered.com/app/{appId}` when a game is available.
- The footer `alextc.es` label links to `https://alextc.es`.
- `IN-GAME` uses a Steam-style green badge.
- `ONLINE` uses the Steam blue palette.
- `OFFLINE` uses a red badge and shows `Last session` below the status.
- When a game is active, the game eyebrow reads `Currently playing`.
- The `compact` layout uses a wide fitted thumbnail for small capsules and headers.
- The `minimal` layout uses larger profile/status elements and only shows the game title while the profile is in-game.
- If no recent or owned game is available, the cover area renders a local SVG placeholder instead of an empty panel.

Supported flag country codes:

```text
CN US RU BR DE GB TR PL FR CA JP KR IN ES IT UA AU MX ID VN
AR NL SE FI NO CZ RO TW TH PH MY HU BE AT PT DK CH CL GR NZ
IE SG RS HR SK BG KZ BY ZA SA AE IL EG PK BD LK NP MA DZ TN
PE CO VE EC UY PY BO CR PA GT DO HN SV NI LU SI LT LV EE IS
GE AM AZ UZ KG MN HK MO NG KE GH ET QA KW BH OM JO LB CY MT
```

### Game JSON

```bash
curl "http://localhost:8080/api/steam/game/730"
```

The response contains internal game data, Store metadata where available, platforms, genres, release date, price information, and all resolved artwork URLs.

### Game SVG Card

```bash
curl "http://localhost:8080/api/steam/game/730/card.svg?layout=showcase"
curl "http://localhost:8080/api/steam/game/730/card.svg?layout=hero&gameImage=hero"
```

Game SVG card titles link to `https://store.steampowered.com/app/{appId}`. Layouts with a cover image link the cover too; hero backgrounds remain visual only.

### Resolved Cover

```bash
curl "http://localhost:8080/api/steam/game/730/cover?type=portrait"
curl "http://localhost:8080/api/steam/game/730/cover?type=header"
curl "http://localhost:8080/api/steam/game/730/cover?type=hero"
```

Supported `type` values:

- `portrait`
- `header`
- `hero`
- `icon`
- `logo`
- `small`

### Library, Recent Games, Stats

```bash
curl "http://localhost:8080/api/steam/library?vanity=Alextc35"
curl "http://localhost:8080/api/steam/recent?vanity=Alextc35"
curl "http://localhost:8080/api/steam/stats?vanity=Alextc35"
```

## Card Parameters

`/api/steam/card.svg` accepts:

| Parameter | Values |
| --- | --- |
| `steamId` | 15 to 20 digits |
| `vanity` | Letters, digits, underscore, hyphen |
| `theme` | `dark`, `light`, `steam`, `dracula`, `nord`, `github-dark`, `github-light` |
| `layout` | `compact`, `normal`, `showcase`, `hero`, `minimal` |
| `gameImage` | `none`, `icon`, `header`, `portrait`, `hero`, `logo`, `small`, `auto` |
| `imageMode` | `embedded`, `external` |
| `border` | `rounded`, `square`, `none` |
| `lang` | `es`, `en`, `fr`, `de`; localizes Steam Store metadata and SVG labels |
| `accent` | Six hex characters, for example `66c0f4` |

All parameters are length-limited and parsed against strict allowlists. CSS, XML, URLs, and arbitrary user-controlled content are not accepted as styling input.

## GitHub README Example

```html
<picture>
  <source
    media="(prefers-color-scheme: dark)"
    srcset="https://example.com/api/steam/card.svg?vanity=Alextc35&theme=github-dark&layout=showcase&gameImage=portrait">
  <source
    media="(prefers-color-scheme: light)"
    srcset="https://example.com/api/steam/card.svg?vanity=Alextc35&theme=github-light&layout=showcase&gameImage=portrait">
  <img
    src="https://example.com/api/steam/card.svg?vanity=Alextc35&layout=showcase&gameImage=portrait"
    alt="Steam profile card">
</picture>
```

For GitHub, `imageMode=embedded` is the default recommendation because nested external images inside SVGs can be restricted by proxies and renderers.

## Steam Artwork Resolution

The API does not depend only on Store API image fields. It also builds deterministic Steam CDN URLs from the AppID:

```text
https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/{appid}/header.jpg
https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/{appid}/library_600x900.jpg
https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/{appid}/library_hero.jpg
https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/{appid}/logo.png
https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/{appid}/capsule_184x69.jpg
https://media.steampowered.com/steamcommunity/public/images/apps/{appid}/{img_icon_url}.jpg
```

Primary image strategy:

- `compact`: fitted small capsule or header thumbnail
- `normal`: header
- `showcase`: portrait cover
- `hero`: hero image
- `minimal`: no large image

Fallback strategy:

- Portrait: library cover, header, hero, Store background, local SVG placeholder
- Hero: hero image, Store background, header, theme background
- Icon: Web API icon, logo, small capsule, local SVG placeholder

The placeholder SVG is generated locally and includes the game label, AppID, theme colors, and "Artwork unavailable".

## Image Security

Embedded mode downloads Steam artwork server-side and converts it to a Base64 data URI. The CDN client protects that path with:

- HTTPS-only URLs
- Official Steam host allowlist
- No user-provided image URLs
- No redirects to untrusted hosts
- Short timeouts
- Maximum byte size from `STEAM_MAX_EMBEDDED_IMAGE_BYTES`
- Allowed MIME types: `image/jpeg`, `image/png`, `image/webp`
- Rejection of localhost, private IP, and metadata-service style URLs by allowlist
- No logging of binary content or API keys

Allowed image hosts:

- `shared.fastly.steamstatic.com`
- `shared.akamai.steamstatic.com`
- `media.steampowered.com`
- `steamcdn-a.akamaihd.net`
- `cdn.cloudflare.steamstatic.com`
- `avatars.steamstatic.com`

If image download or validation fails, the renderer uses a local SVG placeholder.

## Cache Strategy

Caffeine caches are separated by responsibility:

- Steam profile summaries
- Recently played games
- Owned games and library data
- Steam level
- Friends
- Store API metadata
- Image resolution
- Embedded image data URIs
- Card render data

Live profile status, current game, recent games, owned-game last-played data, and card data use `STEAM_LIVE_CACHE_TTL`. Level and friend metadata use `STEAM_CACHE_TTL`. Embedded images use `STEAM_IMAGE_CACHE_TTL`. Store and image resolution caches use longer derived TTLs. Failed HTTP calls and thrown errors are not cached.

## Error Responses

JSON endpoints return consistent errors:

```json
{
  "timestamp": "2026-07-29T18:00:00Z",
  "status": 502,
  "error": "Steam API error",
  "message": "Unable to retrieve Steam profile",
  "path": "/api/steam/profile",
  "requestId": "..."
}
```

SVG endpoints return a valid fallback SVG when Steam is unavailable or the profile/game/artwork cannot be rendered.

Errors never include API keys, stack traces, internal configuration, or full sensitive URLs.

## Actuator

Only these endpoints are exposed:

- `/actuator/health`
- `/actuator/info`

The healthcheck does not reveal secrets.

## Production Docker Build

```bash
docker build -t steam-card-api .
```

The `Dockerfile`:

- Builds with Maven and Eclipse Temurin 21
- Runs `mvn verify`
- Generates Spring Boot build metadata for the `/` version field
- Copies only the generated JAR into a JRE image
- Does not copy `.env`
- Runs as a non-root user
- Exposes port `8080`
- Includes an HTTP healthcheck

Run the production image:

```bash
docker run --rm -p 8080:8080 \
  -e STEAM_API_KEY=replace_with_your_key \
  -e STEAM_DEFAULT_VANITY=replace_with_your_vanity \
  steam-card-api
```

For publication, registry push, reverse proxy, and smoke-test steps, see [Deployment guide](docs/DEPLOYMENT.md).

## Reverse Proxy Notes

Put TLS, public hostname routing, compression, and any additional rate limiting at the proxy layer. Route paths such as:

```text
https://example.com/api/steam/card.svg
https://example.com/api/steam/game/730/card.svg
```

to the container on port `8080`.

The application already emits cache headers and ETags for SVG and image responses.

For `alextc.es`, the cleanest production shape is to run this API on a Java/Docker-capable host and point a subdomain such as `steam.alextc.es` or `api.alextc.es` at it. Hostinger Website Builder can embed the final SVG URL, but the API itself needs a backend runtime, not only static site hosting.

## Limitations

- Steam private profiles may hide recent games, owned games, friends, level, or achievements.
- Store API data can be incomplete or use variable response shapes.
- Not every Steam AppID has every artwork asset.
- Embedded mode increases response size because images are included as Base64.
- Achievements are only available when the profile, game, and Steam API allow access.

## Roadmap

- Add optional achievement summaries to rendered cards.
- Add optional rate limiting middleware.
- Add more compact layouts for shields and badges.
- Add snapshot-based SVG regression tests.
- Add OpenAPI documentation.
- Split the profile SVG renderer into layout-specific classes.

## Contributing

Keep changes small, tested, and aligned with the current package structure. Do not add databases, Redis, WebFlux, JavaScript renderers, or secrets in code. Run:

```bash
./mvnw verify
docker compose config --no-interpolate
docker build -t steam-card-api .
```

before opening a pull request.

## License

This project is open source under the [MIT License](LICENSE).
