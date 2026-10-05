# Checkbox Detection

Detects the checkboxes of a document image or PDF and tells which ones are checked.
Documentation (architecture, pipeline, quality, improvements): `Readme.pdf`.

The only requirement is **Docker with Compose v2** (`docker compose version`). Nothing else is installed on the machine.

## Start

Backend and frontend:

```bash
docker compose up --build
```

In the background:

```bash
docker compose up --build -d
```

Only the backend:

```bash
docker compose up --build backend
```

| What | URL |
|---|---|
| UI | http://localhost:3000 |
| API, challenge contract | http://localhost:8080/detect |
| API, with page numbers and page sizes | http://localhost:8080/v2/detect |
| Through the UI's proxy | http://localhost:3000/api/detect and http://localhost:3000/api/v2/detect |
| Health check | http://localhost:8080/actuator/health |

## Commands

| To do this | Run |
|---|---|
| See what is running | `docker compose ps` |
| Follow the logs | `docker compose logs -f` (or `docker compose logs -f backend`) |
| Rebuild one service | `docker compose up --build -d backend` (or `frontend`) |
| Restart a service | `docker compose restart backend` |
| Stop and remove the containers | `docker compose down` |
| Run the backend tests | `docker build --target build ./backend` |

## Try it

Challenge contract (only `boxes`, each with `bbox` and `is_checked`; for a PDF, the boxes of all pages in page order):

```bash
curl -F "file=@samples/form-2-market-conditions-addendum.pdf;type=application/pdf" http://localhost:8080/detect
```

Extended contract (adds `page` to each box and the size of every page in `pages`):

```bash
curl -F "file=@samples/form-2-market-conditions-addendum.pdf;type=application/pdf" http://localhost:8080/v2/detect
curl -F "file=@page.png;type=image/png" http://localhost:8080/v2/detect
```

---

# Configuration

All settings live in `backend/controller/src/main/resources/application.yml`. Every value is validated when the
backend starts: a wrong value (a negative size, a share above 1, `max-box-size` smaller than `min-box-size`, an empty
list of scales) stops the application with a message that names the setting, so a mistake shows up at once and never
in the middle of a request.

## How to change a setting

Without editing the file, set it as an environment variable of the `backend` service in `docker-compose.yml`. Write
the key in upper case and turn dots and dashes into underscores (`app.detection.min-box-size` becomes
`APP_DETECTION_MIN_BOX_SIZE`). Lists are comma separated.

```yaml
  backend:
    environment:
      APP_FILES_ALLOWED_TYPES: png,jpeg      # turns PDF support off
      APP_DETECTION_MIN_BOX_SIZE: "24"
      APP_PREPROCESSING_UPSCALE_FACTORS: 1,2
```

Apply it with `docker compose up -d` (the container is recreated, no rebuild needed).

A **pixel** is the unit of almost every detection setting. A page is a grid of pixels, and the detector never looks at
anything bigger than that: a "box of 50 pixels" is a checkbox whose side is 50 dots wide in the image being analyzed.
A PDF page is first drawn as an image (see `app.pdf.dpi`), and the sizes refer to that image.

## 1. Files and limits

These settings decide what is accepted before any detection work starts. They protect the server from files that are
too large or that would need too much memory to open.

| Key | Default | Allowed values |
|---|---|---|
| `app.files.allowed-types` | `png,jpeg,pdf` | any non-empty subset of `png`, `jpeg`, `pdf` |
| `app.image.max-dimension` | `12000` | integer > 0 |
| `app.image.max-pixels` | `50000000` | integer > 0 |
| `app.pdf.max-pages` | `20` | integer > 0 |
| `app.pdf.dpi` | `300` | integer > 0 |

**`app.files.allowed-types`** is the list of file types the service accepts. The real type is read from the first
bytes of the file (its signature), never from the content type the client declares, so renaming a PDF to `.png` does
not work. A file whose type is not on the list is answered with `400` and a message that lists the allowed types.
Removing `pdf` turns PDF support off completely; the service still starts. Every type on the list must have a
validator and an extractor in the code, otherwise the application refuses to start.

**`app.image.max-dimension`** is the largest allowed width *or* height, in pixels. It applies to uploaded images and
to every PDF page after it would be drawn (see `app.pdf.dpi`). The size is read from the image header before the
image is decoded, so a tiny file that declares a gigantic size is rejected without being opened (this is the defence
against decompression bombs). Raise it only if you really need to analyze larger pictures, and raise
`app.image.max-pixels` with it.

**`app.image.max-pixels`** is the largest allowed *width × height*. It is the setting that actually protects memory:
the detector keeps several copies of the page in memory (decoded, gray, ink map, and enlarged versions), each one
holding one value per pixel, so memory grows linearly with this number. At the default, 50 million pixels is roughly
a 7000 × 7000 image; a letter page at 300 dpi is 2550 × 3300 = 8.4 million. The container has a 2 GB limit, and the
JVM may use 75 % of it as heap.

**`app.pdf.max-pages`** rejects a PDF with more pages than this (`400`, and the message says how many pages the file
has). It bounds the worst-case processing time of one request, because every page is drawn and analyzed.

**`app.pdf.dpi`** is the resolution at which each PDF page is turned into an image. A PDF has no pixels of its own,
only sizes in points (1/72 inch), so the page must be drawn at some resolution before it can be analyzed. The image
size is `points / 72 × dpi`: a letter page (612 × 792 points) is 2550 × 3300 pixels at 300 dpi.
- *Higher* (for example 400): checkboxes become bigger and the lines thicker, and there is more detail for the rules to
  use, but the image has more pixels (it grows with the square of the dpi), so it is slower and uses more memory, and a
  large page can exceed `max-dimension` or `max-pixels`. Each page is checked against those two limits *before* it is
  drawn, using the size its metadata declares, and the whole PDF is rejected if one page would be too large.
- *Lower* (for example 150): faster and lighter, but the boxes shrink in proportion. The checkboxes of the sample forms
  measure 30 to 55 px at 300 dpi, so at 150 dpi they measure 15 to 28 px, around `min-box-size`: the smaller ones fall
  under it and are only found thanks to the scale search described below. On the sample pages reduced to 150 and 120 dpi
  with smooth interpolation, the detections amount to about 85 % of the real checkboxes (about 95 % at 300 dpi), and to
  64 % at 96 dpi.

The upload limit itself (`spring.servlet.multipart.*`) is in the next section.

## 2. Upload and server concurrency

| Key | Default | Meaning |
|---|---|---|
| `spring.servlet.multipart.max-file-size` | `10MB` | largest file in an upload |
| `spring.servlet.multipart.max-request-size` | `11MB` | largest whole request (file plus form overhead) |
| `spring.servlet.multipart.file-size-threshold` | `1MB` | uploads above this size are written to disk instead of kept in memory |
| `server.tomcat.threads.max` | `8` | requests processed at the same time |
| `server.tomcat.accept-count` | `50` | connections that wait in line when all threads are busy |
| `server.tomcat.connection-timeout` | `10s` | how long the server waits for a client that stays silent |
| `server.tomcat.max-swallow-size` | `2MB` | how much of a rejected or aborted upload the server keeps reading |
| `server.shutdown` | `graceful` | on stop, finish the requests in flight before exiting |

**Upload limits.** A file above `max-file-size` is answered with `413`. The whole upload is streamed to a temporary
file instead of being held in memory, which is why no chunked or resumable upload is needed at this size. The proxy
in front (nginx) has the same 11 MB limit, so it rejects an oversized upload immediately with the same `413` message
(with a higher proxy limit, an upload between the two limits took 10 seconds to be rejected).

**`threads.max`** is the most important concurrency setting. Detection is CPU- and memory-heavy: one request can hold
the uploaded file plus every rendered page of a PDF. With 8 threads at most 8 requests are processed at once; the
rest wait in the `accept-count` queue (50), and beyond that connections are refused. *Raising it* serves more users
at the same time but multiplies the worst-case memory (each request can hold a 10 MB upload plus every rendered page
of a PDF), and with 2 CPUs more threads mostly just make each request slower. *Lowering it* protects
memory and makes the service predictable under load, at the price of longer queues. Measured on the default container:
a single 20-page PDF peaks near 1.7 GB of the 2 GB, and two or three of them at once exhaust the heap (the JVM exits and
the container restarts, so the requests in flight fail), while eight 7000 × 7000 images at once are fine. For PDF-heavy
use, lower this value to 2.

**`connection-timeout`** closes connections where the client opens a socket and sends nothing, and **`max-swallow-size`**
caps how much of an upload that was rejected (for example for being too large) the server keeps reading before it
drops the connection, so a client cannot keep a thread busy by sending a huge body that will be refused anyway.

## 3. Preprocessing: black and white, and scales

These settings control how the page is prepared before looking for checkboxes.

| Key | Default | Allowed values |
|---|---|---|
| `app.preprocessing.binarization` | `both` | `global`, `local`, `both` |
| `app.preprocessing.local-window` | `25` | integer >= 0 |
| `app.preprocessing.local-delta` | `12` | integer >= 0 |
| `app.preprocessing.upscale-factors` | `1,2,3,4` | non-empty list of integers > 0 |
| `app.preprocessing.enough-boxes` | `5` | integer > 0 |
| `app.preprocessing.max-working-pixels` | `40000000` | integer > 0 |

**Binarization** turns every pixel into *ink* (part of a dark stroke) or *paper* (background). The rules that follow
are much simpler on two values than on 256 gray levels. The question is what gray level separates ink from paper.
- `global`: one gray level for the whole page, chosen automatically by Otsu's method (it picks the level that best
  separates the dark and the light pixels of *that* page; there is no fixed number). Sharp and clean on scans and on
  PDFs. It loses faint lines, and a large dark area (the gray background of a PDF viewer in a screenshot) distorts the
  choice for the whole page.
- `local`: each pixel is compared with the average of the square of pixels around it, and it is ink if it is darker
  than that average by more than `local-delta`. A faint 1-pixel line on white paper is darker than its surroundings,
  so it counts; a flat dark background is as dark as its own surroundings, so it does not. Slightly noisier on dense
  text.
- `both` (default): a pixel is ink if either method says so. It has the advantages of both and costs a few extra false
  detections on pages that contain only text. Use `global` for clean scans if you want the stricter behaviour.

**`local-window`** is the side, in pixels, of the square a pixel is compared with (`local` and `both` only). It has to
be larger than the thickness of the lines you want to keep, because inside a thick stroke every pixel is as dark as
its neighbours and would not count as ink. *Smaller* than the line thickness: thick lines turn hollow. *Much larger*:
the comparison approaches the page-wide average and the method loses its advantage. `0` disables the local test.

**`local-delta`** is how many gray levels (out of 255) darker than its surroundings a pixel must be to count as ink
(`local` and `both` only). *Lower* (for example 6): fainter lines are kept, and so is more noise and paper texture.
*Higher* (for example 25): only clearly drawn lines survive, and faint 1-pixel borders of a small screenshot are lost.

**Scales (`upscale-factors`).** All the detection rules are written in absolute pixels (a checkbox is between 20 and
100 pixels wide, a side is a thin stroke, and so on). They fit a PDF drawn at 300 dpi, where the boxes of the samples are 30 to 55 px, but
a screenshot of the same page shows boxes of 10 to 17 px with faint borders, below the minimum, so the rules reject
them. Enlarging the image *before* analysis brings the boxes to the size the rules expect: a 12 px box measures 24 px
at scale 2 and 36 px at scale 3. The setting is the list of factors tried, in order. A factor of 2 doubles width and
height (four times the pixels), 3 and 4 triple and quadruple them (9 and 16 times the pixels); the enlargement uses
bicubic interpolation, which smooths the new pixels instead of making blocks.

The search works like this: scale 1 is tried first; if it finds fewer than `enough-boxes` checkboxes, the next
factor is tried; it stops at the first factor that finds enough, and if none does it returns the result of the factor
that found the most. Coordinates found on an enlarged image are divided by the factor, so the response always refers to the
image that was received. *Fewer factors* (for example `1`) means the fastest processing and no help for screenshots.
*More or larger factors* can rescue smaller boxes, but every factor costs time and memory in proportion to its square,
and a strong enlargement can make letters reach the size of a box and create false detections.

**`enough-boxes`** is the evidence needed to stop: a real page with checkboxes usually has many more than 5, and a
page that gives fewer than 5 is probably showing them too small. *Raise it* if your documents are mostly forms with
very few checkboxes and you want the enlargements tried more often; *lower it* to stop earlier.

**`max-working-pixels`** is a memory guard: an enlargement (a factor above 1) whose enlarged image would have more
pixels than this is skipped. Scale 1 is never skipped, so a page above this limit is still analyzed at its original size.
The enlarged size is `width × height × factor²`. Example: a 2550 × 4200 page (10.7 million pixels) at factor 2 would
have 42.8 million pixels, so with the default (40 million) factor 2 is skipped for that page, while a 2550 × 3300 page
(8.4 million) at factor 2 gets 33.7 million and is allowed. In practice a full page of a 300 dpi PDF is rarely enlarged,
which is what you want because it does not need it, and small screenshots (the case that needs it) can be enlarged up
to 4 times.

## 4. Detection

These settings are the thresholds of the rules that decide whether a square is a checkbox. The pipeline finds the
vertical sides of possible boxes, builds a candidate square from each pair of aligned sides, and keeps a candidate only
if every rule accepts it.

| Key | Default | Allowed values |
|---|---|---|
| `app.detection.min-box-size` | `20` | integer > 0 |
| `app.detection.max-box-size` | `100` | integer >= `min-box-size` |
| `app.detection.min-outline-coverage` | `1.0` | 0.0 to 1.0 |
| `app.detection.min-corner-ink-share` | `0.4` | 0.0 to 1.0 |
| `app.detection.side-clearance` | `3` | integer >= 0 |
| `app.detection.max-side-ink-share` | `0.02` | 0.0 to 1.0 |
| `app.detection.checked-ink-share` | `0.07` | 0.0 to 1.0 |

**`min-box-size` and `max-box-size`** are the smallest and largest side of a checkbox, in pixels (both width and
height must be within them). The minimum is the most delicate setting: it must stay *above the size of a letter*, because a
couple of vertical strokes from letters (an "H", or the "n" and "g" of a word) can look like the two sides of a small
box, and everything between the minimum and the maximum is a candidate. *Lowering the minimum* (to catch small boxes)
brings more false detections in text; if the boxes are small, prefer enlarging the image with the scales instead of
lowering this number. *Raising the maximum* admits bigger squares (image cells, frames) that are not checkboxes.
These two values also drive other limits: a side must be at least 80 % of `min-box-size` tall, and at most
`max(3, min-box-size / 2)` pixels thick.

**`min-outline-coverage`** is the share of each of the four sides that must be drawn. A position counts as drawn if
there is ink within 2 pixels of the line. At `1.0` not a single position may be missing: *a single white gap means
there is no square*. This is the rule that rejects letters. *Lowering it* (for example `0.9`) tolerates broken or
faded outlines, which recovers damaged boxes, at the cost of accepting more shapes that are not boxes.

**`min-corner-ink-share`** is the share of ink each of the four corners (an area of 12 % of the side) must have.
A drawn square has ink in all four corners, but round letters such as "o", "O" or "Q" fill the middle of their
bounding square and leave the corners empty, even though they pass the other rules. *Raising it* (for example `0.6`)
rejects more round shapes but may reject boxes with rounded corners or a gap at a corner. *Lowering it* admits more
of them.

**`side-clearance` and `max-side-ink-share`** work together. A checkbox is not squeezed between neighbours on both of its
sides: the rule looks at a strip `side-clearance` pixels wide just outside the left side and another outside the right
side, and requires at least one of them to be free, meaning its share of ink is at most `max-side-ink-share` (2 %).
A box may have a label or a table border beside it, but not both; a stroke taken from the middle of a word has letters
glued to both sides. The top and bottom 25 % of the height are ignored, because there the table lines that form the box
continue past it. `side-clearance: 0` turns the rule off. *A larger clearance* is stricter and can reject boxes that
are close to their label; a larger `max-side-ink-share` is more tolerant.

**`checked-ink-share`** is the share of ink inside the box (ignoring its border: 24 % of the side is left out on each
side, at least 4 px) above which the box is reported as checked. Any mark counts: an X, a tick or a scribble. The value
is low on purpose, but above the specks of scanner noise. *Raise it* (for example `0.10`) if empty boxes on noisy scans
come out as checked; *lower it* (for example `0.04`) if light ticks are missed.

## 5. Fixed internal values (not configurable)

These values are in the code and are changed by editing it. They are listed to explain the behaviour of the settings
above.

| Stage | Value |
|---|---|
| Finding sides | shortest side: 80 % of `min-box-size`; thickest stroke: `max(3, min-box-size / 2)` px; no maximum length (stacked boxes form one long stroke) |
| Building candidates | two sides pair up if they are at most `max-box-size` apart horizontally and start and end at the same height within 15 % of the height (at least 3 px) |
| Stacked boxes | sides at least 1.5 times taller than the distance between them are cut at the horizontal lines that cross them (rows with at least 85 % ink); each side is adjusted to the column that really has ink (at least 20 % of the middle rows) |
| Size and proportion rule | width / height between 0.8 and 1.25 |
| Corner rule | each corner is 12 % of the side (at least 2 px) |
| Free side rule | the top and bottom 25 % of the height are ignored |
| Closed outline rule | 2 px of tolerance around each side; the 2 px at each end are not checked (they are the corners) |
| Duplicates | two squares overlapping by 40 % or more are the same box; the smaller one is kept |
| Dominant size | squares within 20 % of each other's size form a group; with at least 4 squares in the biggest group, a group is kept only if it has at least 20 % of the squares of the biggest one and at least 2 |
| Checked or empty | border left out of the interior: 24 % of the side (at least 4 px) |

## 6. Proxy and containers

These are not in `application.yml` but they limit the service as much as the settings above.

| Where | Value | Effect |
|---|---|---|
| Proxy (`frontend/nginx.conf`) | 5 requests per second per client IP, burst of 10 | beyond that the proxy answers `429`; it does not apply to port 8080, which reaches the backend directly |
| Proxy | `client_max_body_size 11m`, header and body timeouts of 15 s | oversized or very slow uploads are cut at the proxy |
| Proxy | `proxy_read_timeout 60s` | a detection that takes longer than 60 s through the proxy fails (the backend itself has no per-request time limit) |
| Backend container | 2 GB of memory, 2 CPUs, 256 processes, `/tmp` of 256 MB in memory | the JVM may use 75 % of the memory as heap and exits on `OutOfMemoryError`; `restart: unless-stopped` brings it back clean |
| Frontend container | 128 MB of memory, 0.5 CPU, 64 processes, `/tmp` of 64 MB | the proxy only serves static files and forwards requests |
| Both containers | read-only filesystem, no capabilities, `no-new-privileges`, log rotation (3 files of 10 MB) | hardening; the backend gets 30 s to finish requests in flight when it is stopped |
| Both ports | published on `127.0.0.1` only | nothing is reachable from other machines; remove `127.0.0.1:` from `ports` in `docker-compose.yml` to change it |

## Which setting to touch (suggestions, not measured results)

| Symptom | Try |
|---|---|
| Boxes are missed in a screenshot | keep `binarization: both`, make sure `upscale-factors` includes 2, 3, 4; lower `local-delta` a little; zoom in before capturing |
| Boxes with broken outlines are missed | lower `min-outline-coverage` to `0.9` or `0.95` (expect more false detections) |
| Letters or text are detected as boxes | raise `min-corner-ink-share` (`0.5`), raise `min-box-size`, use `binarization: global` on clean scans |
| Empty boxes reported as checked | raise `checked-ink-share` |
| Light ticks reported as empty | lower `checked-ink-share` |
| PDFs take too long | lower `app.pdf.dpi` (not below about 150 for forms like the samples) or `app.pdf.max-pages` |
| Memory errors with big files | lower `app.image.max-pixels` or `server.tomcat.threads.max` |
| Boxes that are 57 x 45 px are missed | not configurable: the 0.8 to 1.25 proportion is a fixed value (see the PDF, section 4) |
