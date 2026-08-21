# Enroute User Manager

A desktop admin tool for managing forum users on the Enroute backend
(Ktor/Kotlin, SQLite). It talks to the same backend the website uses, over
plain HTTPS/HTTP — it never touches the database file directly.

Built with Kotlin + Compose Multiplatform Desktop.

## Requirements

- JDK 17+ (the Gradle build will auto-provision one via the foojay-resolver
  plugin if you don't have one)
- Gradle (or just use the wrapper once it's generated — see below)

## Backend prerequisites

This client depends on the admin routes added to the Ktor backend in
`user-admin.kt`:

- `GET  /api/admin/users` — list/search users
- `GET  /api/admin/users/{id}` — user detail
- `PATCH /api/admin/users/{id}/role` — change role (admin only)
- `POST /api/admin/users/{id}/timeout` — temporary suspension
- `POST /api/admin/users/{id}/untimeout` — clear a timeout
- `POST /api/admin/users/{id}/ban` — permanent ban
- `POST /api/admin/users/{id}/unban` — lift a ban
- `DELETE /api/admin/users/{id}` — permanently delete a user (admin only)

Make sure your backend deployment includes those routes and has run its
schema migration (adds `users.banned` and `users.timeout_until`) before
using this client against it.

Only accounts with the `moderator` or `admin` role can use these endpoints;
role changes and deletion require `admin` specifically.

## Running in development

```bash
gradle wrapper      # one-time, if you don't already have ./gradlew
./gradlew run
```

On first launch, enter your backend's URL (e.g. `https://forum.example.com`
or an SSH-tunnelled `http://127.0.0.1:5000`) along with an admin/moderator
username and password.

## Building a distributable

```bash
./gradlew packageDistributionForCurrentOS
```

This produces a native installer (`.dmg` / `.msi` / `.deb` depending on your
OS) under `build/compose/binaries/main/`.

## Notes on remote access

Since your database lives on your webserver and isn't exposed directly,
this client always goes through the backend's HTTP API — over the public
internet if the backend is deployed there, or through an SSH tunnel /
`cloudflared` tunnel if you're pointing it at a local dev instance (see the
backend repo's `how2run` notes).

## Theming

Colors and fonts are pulled from the main website's palette in
`theme/Theme.kt` (dark navy background, blue accents, Space Grotesk/Inter).
Desktop Compose can't load Google Fonts at runtime the same way a browser
does — if you want an exact font match, bundle the `.ttf` files under
`src/main/resources/font/` and load them with `FontFamily(Font(...))`
instead of the system-font fallback currently used there.
