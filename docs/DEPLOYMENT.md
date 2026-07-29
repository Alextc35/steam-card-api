# Deployment Guide

This API is designed to run as a single stateless HTTP service. It does not need a database, Redis, or persistent disk.

## Requirements

- Java 21 runtime or a Docker-compatible host.
- Outbound HTTPS access to Steam Web API, Steam Store API, and Steam CDN hosts.
- A public HTTPS hostname if the card will be embedded in GitHub READMEs or websites.
- Secret environment variables configured outside the image:

```env
STEAM_API_KEY=...
STEAM_DEFAULT_VANITY=Alextc35
STEAM_DEFAULT_ID=
STEAM_CACHE_TTL=PT5M
STEAM_LIVE_CACHE_TTL=PT15S
STEAM_IMAGE_CACHE_TTL=PT1H
STEAM_HTTP_CONNECT_TIMEOUT=PT5S
STEAM_HTTP_READ_TIMEOUT=PT10S
STEAM_MAX_EMBEDDED_IMAGE_BYTES=1048576
```

Use exactly one default identity: `STEAM_DEFAULT_VANITY` or `STEAM_DEFAULT_ID`.

## Build

```bash
docker build -t steam-card-api:latest .
```

The production image runs `mvn verify` during build, copies only the JAR into a Java 21 JRE image, and runs as a non-root user.

## Run

```bash
docker run --rm -p 8080:8080 \
  -e STEAM_API_KEY=replace_with_your_key \
  -e STEAM_DEFAULT_VANITY=Alextc35 \
  steam-card-api:latest
```

Healthcheck:

```bash
curl -fsS http://localhost:8080/actuator/health
```

Profile card smoke test:

```bash
curl -I "http://localhost:8080/api/steam/card.svg?vanity=Alextc35&layout=showcase&gameImage=portrait&imageMode=embedded"
```

Expected headers include `200`, `Content-Type: image/svg+xml;charset=UTF-8`, `Cache-Control`, `ETag`, and `X-Content-Type-Options: nosniff`.

## Publish A Container Image

Use any registry supported by your host. Example shape:

```bash
docker tag steam-card-api:latest ghcr.io/<owner>/steam-card-api:latest
docker push ghcr.io/<owner>/steam-card-api:latest
```

Then configure the hosting service to run that image, expose port `8080`, and inject the environment variables as secrets.

## Reverse Proxy

Terminate TLS at the proxy or platform edge, then forward traffic to the container on port `8080`.

Recommended public paths:

```text
https://your-domain.example/api/steam/card.svg
https://your-domain.example/api/steam/game/730/card.svg
https://your-domain.example/actuator/health
```

The application already emits cache headers and ETags for SVG and cover resources. Keep CDN/proxy caching conservative for `/api/steam/card.svg` if live status matters; `STEAM_LIVE_CACHE_TTL=PT15S` is the application-side default.

## Domain Notes For alextc.es

With `alextc.es` on Hostinger, keep these pieces separate:

- The public domain or subdomain can point to wherever the API runs.
- Hostinger Website Builder can embed the generated SVG once the API has a public HTTPS URL.
- The Steam Card API itself needs a Java 21 or Docker-capable backend host. If the current Hostinger plan is static/site-builder only, use a separate backend host and point `steam.alextc.es` or `api.alextc.es` to it.
- Keep the Steam API key in that backend host's secret environment variables.

Recommended first production URL shape:

```text
https://steam.alextc.es/api/steam/card.svg?vanity=Alextc35&layout=showcase&gameImage=portrait&imageMode=embedded
```

## GitHub README Usage

After deployment, replace the hostname:

```html
<img
  src="https://your-domain.example/api/steam/card.svg?vanity=Alextc35&theme=github-dark&layout=showcase&gameImage=portrait&imageMode=embedded"
  alt="Steam profile card">
```

Use `imageMode=embedded` for GitHub README compatibility. Use `imageMode=external` when the consumer allows external images inside SVG and smaller SVG responses matter more.

## Operational Checklist

- Keep `STEAM_API_KEY` in platform secrets only.
- Do not bake `.env` into container images.
- Enable HTTPS on the public hostname.
- Prefer a dedicated API subdomain such as `steam.alextc.es` or `api.alextc.es`.
- Monitor `/actuator/health`.
- Keep `STEAM_LIVE_CACHE_TTL` low enough for status changes, but high enough to avoid unnecessary Steam API calls.
- Add proxy-level rate limiting before making the API public to unknown traffic.
- Add a `LICENSE` file before publishing as open source.
