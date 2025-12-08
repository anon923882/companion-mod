"""Interactive CLI for scraping HappyMH chapter image URLs via jina.ai."""
from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path
from textwrap import dedent
from typing import Iterable, List
from urllib.parse import urljoin, urlparse

import requests
from bs4 import BeautifulSoup


BANNER = r"""
╔════════════════════════════════════════════════════╗
║         HappyMH Chapter Scraper CLI                ║
║  Repurposed for pulling chapter images via jina.ai ║
╚════════════════════════════════════════════════════╝
"""

PROMPT_PREFIX = "➜"


@dataclass
class ChapterResult:
    url: str
    images: List[str]


class CliFormatter:
    @staticmethod
    def title(text: str) -> str:
        return f"\n{text}\n" + "=" * len(text)

    @staticmethod
    def bullet_list(items: Iterable[str]) -> str:
        return "\n".join(f" • {item}" for item in items)

    @staticmethod
    def warn(text: str) -> str:
        return f"[!] {text}"

    @staticmethod
    def success(text: str) -> str:
        return f"[✓] {text}"


class HappyMHScraper:
    def __init__(self, session: requests.Session | None = None) -> None:
        self.session = session or requests.Session()
        self.session.headers.update(
            {
                "User-Agent": "HappyMHScraper/1.0 (+https://r.jina.ai/)",
            }
        )

    def validate_url(self, url: str) -> str:
        parsed = urlparse(url if url.startswith("http") else f"https://{url}")
        if not parsed.scheme:
            parsed = parsed._replace(scheme="https")
        if "happymh.com" not in parsed.netloc:
            raise ValueError("Only URLs from m.happymh.com are supported.")
        if not parsed.path:
            raise ValueError("URL must include a chapter path.")
        normalized = parsed.geturl()
        if parsed.scheme != "https":
            normalized = parsed._replace(scheme="https").geturl()
        return normalized

    def jina_proxy_url(self, url: str) -> str:
        return f"https://r.jina.ai/{url}"

    def fetch_chapter_html(self, url: str) -> str:
        response = self.session.get(self.jina_proxy_url(url), timeout=20)
        response.raise_for_status()
        return response.text

    def extract_images(self, html: str, base_url: str) -> List[str]:
        soup = BeautifulSoup(html, "html.parser")
        images: List[str] = []
        for img in soup.find_all("img"):
            src = img.get("src") or ""
            if not src:
                continue
            src = urljoin(base_url, src)
            if src not in images:
                images.append(src)
        return images

    def scrape(self, url: str) -> ChapterResult:
        validated_url = self.validate_url(url)
        html = self.fetch_chapter_html(validated_url)
        images = self.extract_images(html, validated_url)
        return ChapterResult(url=validated_url, images=images)


class HappyMHCLI:
    def __init__(self, scraper: HappyMHScraper | None = None) -> None:
        self.scraper = scraper or HappyMHScraper()

    def run(self) -> None:
        print(BANNER)
        print("Interactive CLI ready. Paste a HappyMH chapter URL to continue.\n")
        while True:
            try:
                url = input(f"{PROMPT_PREFIX} Chapter URL (or 'q' to quit): ").strip()
            except (KeyboardInterrupt, EOFError):
                print("\nExiting.")
                return

            if not url:
                print(CliFormatter.warn("Please paste a chapter URL to continue."))
                continue
            if url.lower() in {"q", "quit", "exit"}:
                print("Bye!")
                return

            self.handle_url(url)

            again = input(f"{PROMPT_PREFIX} Scrape another? (y/N): ").strip().lower()
            if again != "y":
                print("Done. Happy reading!")
                return

    def handle_url(self, url: str) -> None:
        try:
            result = self.scraper.scrape(url)
        except ValueError as exc:
            print(CliFormatter.warn(str(exc)))
            return
        except requests.HTTPError as exc:
            print(CliFormatter.warn(f"Request failed: {exc}"))
            return
        except requests.RequestException as exc:
            print(CliFormatter.warn(f"Network error: {exc}"))
            return

        if not result.images:
            print(CliFormatter.warn("No images were found in this chapter."))
            return

        print(CliFormatter.title("Chapter images"))
        print(CliFormatter.bullet_list(result.images))

        save_choice = input(
            f"{PROMPT_PREFIX} Save image list to file? (y/N): "
        ).strip().lower()
        if save_choice == "y":
            path = self.save_to_file(result)
            print(CliFormatter.success(f"Saved image URLs to {path}"))

    def save_to_file(self, result: ChapterResult) -> Path:
        filename = self.suggest_filename(result.url)
        path = Path.cwd() / filename
        with path.open("w", encoding="utf-8") as fh:
            fh.write("\n".join(result.images))
        return path

    @staticmethod
    def suggest_filename(url: str) -> str:
        parsed = urlparse(url)
        slug = parsed.path.strip("/").replace("/", "_")
        slug = slug or "chapter"
        return f"{slug}_images.txt"


def main(argv: list[str] | None = None) -> int:
    argv = argv or sys.argv[1:]
    if argv and argv[0] in {"-h", "--help"}:
        print(
            dedent(
                """
                HappyMH Chapter Scraper CLI
                Paste a HappyMH chapter/gallery URL to collect the image links via jina.ai.

                Usage: python cli.py
                """
            ).strip()
        )
        return 0

    HappyMHCLI().run()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
