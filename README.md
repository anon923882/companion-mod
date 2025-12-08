# HappyMH Chapter Scraper CLI

Interactive CLI for scraping image URLs from chapters on [m.happymh.com](https://m.happymh.com/) using the jina.ai proxy for reliable retrieval. The tool preserves the friendly interactive format of the previous CLI while swapping the old gofile.io flow for targeted HappyMH scraping.

## Features
- Interactive prompts with familiar styling.
- Validates HappyMH chapter/gallery URLs.
- Fetches chapter pages through `https://r.jina.ai/` to bypass blockers.
- Extracts and lists all image sources from the page.
- Optionally saves the collected image URLs to a text file using the chapter slug.

## Requirements
Install dependencies with:

```bash
pip install -r requirements.txt
```

## Usage
Run the CLI and paste a chapter URL when prompted:

```bash
python cli.py
```

You can also display help information:

```bash
python cli.py --help
```

Example chapter URL:
```
https://m.happymh.com/mangaread/yIjM5ATMyATMzATMxATMzATM5QTMzATMycTM2ATM2ATMzATM1UTMzUTM0ETM5ATMyETM1QTMyUTM
```

After scraping, choose whether to save the list of image links to a file.
