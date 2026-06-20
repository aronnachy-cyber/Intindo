import re
import uuid
import time
import logging
from typing import Optional
from better_profanity import profanity

logger = logging.getLogger("mod-ai")

profanity.load_censor_words()

_detoxify_model = None
_model_loaded = False
_model_error = None


def _load_model():
    global _detoxify_model, _model_loaded, _model_error
    if _model_loaded:
        return
    try:
        from detoxify import Detoxify
        _detoxify_model = Detoxify("original")
        _model_loaded = True
        logger.info("Detoxify model loaded successfully")
    except Exception as e:
        _model_error = str(e)
        _model_loaded = True
        logger.warning(f"Detoxify failed to load, using fallback: {e}")


SPAM_PATTERNS = [
    r"(https?://\S+){3,}",
    r"(.)\1{8,}",
    r"(?i)(buy now|click here|free money|earn \$|work from home|discount code)",
    r"(?i)(discord\.gg/\S+){2,}",
    r"(?i)nitro\s*giveaway",
    r"(?i)crypto\s*(pump|invest|moon|profit)",
]

SELF_HARM_PATTERNS = [
    r"(?i)(kill myself|end my life|suicide|self.?harm|cut myself|want to die)",
]

THREAT_PATTERNS = [
    r"(?i)(i will kill|i'm going to kill|gonna kill|i'll hurt|i will hurt|shoot you|stab you)",
]

SEXUAL_PATTERNS = [
    r"(?i)(nsfw|porn|xxx|onlyfans|nude|naked)",
]


def _pattern_score(text: str, patterns: list) -> float:
    for p in patterns:
        if re.search(p, text):
            return 0.92
    return 0.0


def _ai_scores(text: str) -> dict:
    if _detoxify_model is not None:
        try:
            results = _detoxify_model.predict(text)
            return {
                "toxicity":        float(results.get("toxicity", 0)),
                "severe_toxicity": float(results.get("severe_toxicity", 0)),
                "obscene":         float(results.get("obscene", 0)),
                "threat":          float(results.get("threat", 0)),
                "insult":          float(results.get("insult", 0)),
                "identity_attack": float(results.get("identity_attack", 0)),
            }
        except Exception:
            pass

    return {
        "toxicity":        0.0,
        "severe_toxicity": 0.0,
        "obscene":         0.0,
        "threat":          0.0,
        "insult":          0.0,
        "identity_attack": 0.0,
    }


def moderate(text: str) -> dict:
    _load_model()

    ai = _ai_scores(text)

    profanity_hit = profanity.contains_profanity(text)
    spam_score    = _pattern_score(text, SPAM_PATTERNS)
    self_harm_score = max(_pattern_score(text, SELF_HARM_PATTERNS), ai.get("threat", 0) * 0.5)
    sexual_score  = max(_pattern_score(text, SEXUAL_PATTERNS), ai.get("obscene", 0))
    violence_score = max(_pattern_score(text, THREAT_PATTERNS), ai.get("threat", 0))

    toxicity_score   = max(ai["toxicity"], 0.85 if profanity_hit else 0.0)
    hate_score       = ai["identity_attack"]
    harassment_score = max(ai["insult"], ai["severe_toxicity"])

    THRESHOLD = 0.65

    categories = {
        "toxicity":   toxicity_score >= THRESHOLD,
        "hate":       hate_score >= THRESHOLD,
        "harassment": harassment_score >= THRESHOLD,
        "self-harm":  self_harm_score >= THRESHOLD,
        "sexual":     sexual_score >= THRESHOLD,
        "violence":   violence_score >= THRESHOLD,
        "spam":       spam_score >= THRESHOLD,
        "profanity":  profanity_hit,
    }

    scores = {
        "toxicity":   round(toxicity_score, 4),
        "hate":       round(hate_score, 4),
        "harassment": round(harassment_score, 4),
        "self-harm":  round(self_harm_score, 4),
        "sexual":     round(sexual_score, 4),
        "violence":   round(violence_score, 4),
        "spam":       round(spam_score, 4),
        "profanity":  round(1.0 if profanity_hit else 0.0, 4),
    }

    flagged = any(categories.values())

    action = "allow"
    if categories["violence"] or categories["self-harm"]:
        action = "delete_and_warn"
    elif categories["hate"] or categories["harassment"]:
        action = "delete"
    elif flagged:
        action = "flag"

    return {
        "flagged":          flagged,
        "action":           action,
        "categories":       categories,
        "category_scores":  scores,
        "model_backend":    "detoxify+rules" if _detoxify_model else "rules",
    }


def build_moderation_result(text: str) -> dict:
    result = moderate(text)
    return {
        "id":      f"modr-{uuid.uuid4().hex[:20]}",
        "model":   "mod-1.0",
        "created": int(time.time()),
        "results": [result],
    }


def build_analysis_result(text: str) -> dict:
    result = moderate(text)
    flagged_cats = [k for k, v in result["categories"].items() if v]
    severity = "none"
    if result["categories"]["violence"] or result["categories"]["self-harm"]:
        severity = "critical"
    elif result["categories"]["hate"] or result["categories"]["harassment"]:
        severity = "high"
    elif result["flagged"]:
        severity = "medium"

    return {
        "id":              f"anal-{uuid.uuid4().hex[:20]}",
        "model":           "mod-1.0",
        "created":         int(time.time()),
        "input":           text,
        "flagged":         result["flagged"],
        "severity":        severity,
        "action":          result["action"],
        "flagged_categories": flagged_cats,
        "categories":      result["categories"],
        "category_scores": result["category_scores"],
        "model_backend":   result["model_backend"],
        "recommendation": {
            "action":  result["action"],
            "message": _recommendation_message(result),
        },
    }


def build_classify_result(text: str, categories: Optional[list] = None) -> dict:
    result = moderate(text)
    all_cats = result["category_scores"]

    if categories:
        filtered = {k: v for k, v in all_cats.items() if k in categories}
    else:
        filtered = all_cats

    top = max(filtered, key=filtered.get) if filtered else "none"

    return {
        "id":         f"cls-{uuid.uuid4().hex[:20]}",
        "model":      "mod-1.0",
        "created":    int(time.time()),
        "input":      text,
        "top_category": top,
        "scores":     filtered,
        "flagged":    result["flagged"],
    }


def _recommendation_message(result: dict) -> str:
    if result["categories"]["violence"]:
        return "Content contains violent threats. Recommend immediate deletion and user warning."
    if result["categories"]["self-harm"]:
        return "Content may indicate self-harm. Recommend deletion and welfare check."
    if result["categories"]["hate"]:
        return "Content contains hate speech. Recommend deletion."
    if result["categories"]["harassment"]:
        return "Content is harassing. Recommend deletion or mute."
    if result["categories"]["spam"]:
        return "Content appears to be spam. Recommend deletion."
    if result["categories"]["sexual"]:
        return "Content is sexual in nature. Recommend deletion if NSFW is not allowed."
    if result["categories"]["toxicity"] or result["categories"]["profanity"]:
        return "Content is toxic or contains profanity. Recommend flagging."
    return "Content appears safe."
