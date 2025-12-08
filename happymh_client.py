import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Tuple
from urllib.parse import urlparse

import cloudscraper


API_BASE = "https://m.happymh.com"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "Accept": "application/json, text/plain, */*",
    "X-Requested-With": "XMLHttpRequest",
}


class HappyMHScraperError(RuntimeError):
    """Base exception raised when the scraper cannot continue."""


@dataclass
class ChapterPayload:
    metadata: dict
    image_urls: List[str]
    raw: str


class HappyMHScraper:
    def __init__(self) -> None:
        self.scraper = cloudscraper.create_scraper()

    @staticmethod
    def extract_code(input_value: str) -> str:
        """Extract the chapter code from a full URL or return the raw code."""
        if not input_value:
            raise ValueError("No chapter URL or code was provided")

        if re.match(r"^[A-Za-z0-9]+$", input_value):
            return input_value

        parsed = urlparse(input_value)
        if not parsed.path:
            raise ValueError("Could not parse mangaread code from input")

        parts = [segment for segment in parsed.path.split("/") if segment]
        if not parts:
            raise ValueError("No path components found in URL")

        if parts[0] != "mangaread":
            raise ValueError("Expected a mangaread URL from m.happymh.com")

        if len(parts) < 2:
            raise ValueError("No chapter code present in the URL")

        return parts[1]

    def fetch_chapter(self, code: str) -> Tuple[dict, List[str], str]:
        payload = self._fetch_reading_payload(code)
        image_urls = self._extract_images(payload)
        if not image_urls:
            raise HappyMHScraperError("No images were returned by the API. The code may be invalid or expired.")
        meta = payload.get("data") or payload.get("chapter") or payload.get("manga") or {}
        return meta, image_urls, json.dumps(payload, ensure_ascii=False, indent=2)

    def _fetch_reading_payload(self, code: str) -> dict:
        params = {"code": code}
        response = self.scraper.get(f"{API_BASE}/v2.0/apis/manga/reading", params=params, headers=HEADERS)
        try:
            data = response.json()
        except json.JSONDecodeError as exc:
            raise HappyMHScraperError(f"Unexpected response from API: {response.status_code}") from exc

        if response.status_code != 200 or data.get("status") not in (200, "200", None):
            message = data.get("msg") or f"API call failed with status {response.status_code}"
            raise HappyMHScraperError(message)

        return data

    def _extract_images(self, payload: dict) -> List[str]:
        """Pull image URLs from the API response using a few common key names."""
        candidates = []

        data_section = payload.get("data") or {}
        if isinstance(data_section, dict):
            for key in ("images", "imgs", "image_list", "page_list", "pages"):
                value = data_section.get(key)
                if isinstance(value, list):
                    candidates.extend(self._flatten_images(value))

        if not candidates and isinstance(payload, dict):
            for key in ("images", "imgs"):
                value = payload.get(key)
                if isinstance(value, list):
                    candidates.extend(self._flatten_images(value))

        return [url for url in candidates if isinstance(url, str)]

    @staticmethod
    def _flatten_images(value: Iterable) -> List[str]:
        flattened: List[str] = []
        for item in value:
            if isinstance(item, str):
                flattened.append(item)
            elif isinstance(item, dict):
                for key in ("url", "image", "img", "src"):
                    if key in item:
                        flattened.append(item[key])
                        break
        return flattened

    def download_images(self, image_urls: Iterable[str], output_dir: Path) -> List[Path]:
        saved: List[Path] = []
        for index, url in enumerate(image_urls, start=1):
            try:
                response = self.scraper.get(url, headers={"User-Agent": HEADERS["User-Agent"]}, timeout=30)
            except Exception as exc:  # noqa: BLE001
                raise HappyMHScraperError(f"Failed to fetch image {url}: {exc}") from exc

            if response.status_code != 200:
                raise HappyMHScraperError(f"Image request failed ({response.status_code}) for {url}")

            suffix = Path(urlparse(url).path).suffix or ".jpg"
            target = output_dir / f"page-{index:03d}{suffix}"
            target.write_bytes(response.content)
            saved.append(target)
        return saved
