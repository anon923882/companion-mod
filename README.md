# happymh scraper CLI

A small CLI for downloading image galleries (chapters) from `m.happymh.com`. It keeps the same text-first formatting style as the previous tooling while swapping the source from gofile to happymh.

## Usage

```bash
python cli.py https://m.happymh.com/mangaread/<chapter_code>
```

Options:

- `-o, --output` – Directory to store images.
- `--max-images` – Stop after downloading a set number of images (handy for quick tests).
- `--dry-run` – Print what would be downloaded without fetching files.
- `--include-json` – Save the raw API payload for debugging.

The scraper uses `cloudscraper` to bypass Cloudflare and will surface helpful error messages if a code is invalid or expired.

## Setup

Install dependencies with:

```bash
python -m pip install -r requirements.txt
```
