import argparse
import sys
from pathlib import Path
from typing import Iterable

from happymh_client import HappyMHScraper, HappyMHScraperError


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Download manga chapter images from m.happymh.com",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "input",
        help="Full mangaread URL or raw chapter code from m.happymh.com",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="downloads",
        help="Directory where images should be saved",
    )
    parser.add_argument(
        "--max-images",
        type=int,
        default=None,
        help="Limit how many images are pulled (useful for smoke testing)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Do not download files; just show what would happen",
    )
    parser.add_argument(
        "--include-json",
        action="store_true",
        help="Save the raw API payload next to the downloads for inspection",
    )
    return parser


def format_header(title: str) -> str:
    rule = "=" * len(title)
    return f"{title}\n{rule}"


def print_line(label: str, value: str) -> None:
    print(f"  - {label}: {value}")


def display_summary(meta: dict, image_urls: Iterable[str]) -> None:
    print(format_header("Chapter details"))
    title = meta.get("title") or meta.get("chapter_title") or meta.get("name") or "(unknown title)"
    print_line("Title", title)
    chapter_no = meta.get("chapter") or meta.get("chapter_no") or meta.get("chapter_no_str")
    if chapter_no is not None:
        print_line("Chapter", chapter_no)
    print_line("Images found", str(len(list(image_urls))))


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)

    scraper = HappyMHScraper()
    try:
        chapter_code = scraper.extract_code(args.input)
    except ValueError as exc:
        print(f"[error] {exc}", file=sys.stderr)
        return 1

    try:
        meta, image_urls, raw_payload = scraper.fetch_chapter(chapter_code)
    except HappyMHScraperError as exc:
        print(f"[error] {exc}", file=sys.stderr)
        return 1

    # Limit images if requested
    if args.max_images is not None:
        image_urls = image_urls[: args.max_images]

    display_summary(meta, image_urls)

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    if args.include_json:
        (output_dir / "chapter.json").write_text(raw_payload)

    if args.dry_run:
        print("\n[dry-run] Skipping downloads")
        for url in image_urls:
            print(f"  - {url}")
        return 0

    print("\nStarting downloads...")
    try:
        saved_files = scraper.download_images(image_urls, output_dir)
    except HappyMHScraperError as exc:
        print(f"[error] {exc}", file=sys.stderr)
        return 1

    print(f"\nSaved {len(saved_files)} file(s) to {output_dir}")
    for path in saved_files:
        print(f"  - {path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
