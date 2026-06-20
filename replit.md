# Mod-1.0 — AI Moderation API

An AI-powered moderation API designed for Discord and other bots. Detects toxicity, hate speech, harassment, spam, violence, self-harm, sexual content, and profanity with confidence scores and action recommendations.

## Tech Stack
- Python 3.11
- FastAPI
- Detoxify (ML toxicity model)
- SQLite (API key storage)
- Render (deployment target)

## Running Locally
```bash
cd mod-ai
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 5000 --reload
```

## User Preferences
- No rate limits on the API
- OpenAI-compatible API structure
- Deploy target: Render
