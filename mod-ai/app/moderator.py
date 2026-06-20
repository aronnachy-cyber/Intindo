import re
import uuid
import time
import logging
from typing import Optional
from better_profanity import profanity

logger = logging.getLogger("mod-ai")

profanity.load_censor_words()

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

HATE_PATTERNS = [
    r"(?i)(slur|racial slur|go back to your country|you people are all)",
    r"(?i)(hate all \w+|death to \w+|exterminate \w+)",
]

HARASSMENT_PATTERNS = [
    r"(?i)(you're a loser|you're pathetic|nobody likes you|kill yourself|kys)",
    r"(?i)(you're so stupid|idiot|moron|retard)",
]


def _pattern_score(text: str, patterns: list) -> float:
    for p in patterns:
        if re.search(p, text):
            return 0.92
    return 0.0


def moderate(text: str) -> dict:
    profanity_hit   = profanity.contains_profanity(text)
    spam_score      = _pattern_score(text, SPAM_PATTERNS)
    self_harm_score = _pattern_score(text, SELF_HARM_PATTERNS)
    violence_score  = _pattern_score(text, THREAT_PATTERNS)
    sexual_score    = _pattern_score(text, SEXUAL_PATTERNS)
    hate_score      = _pattern_score(text, HATE_PATTERNS)
    harassment_score = _pattern_score(text, HARASSMENT_PATTERNS)
    toxicity_score  = 0.85 if profanity_hit else 0.0

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
        "flagged":         flagged,
        "action":          action,
        "categories":      categories,
        "category_scores": scores,
        "model_backend":   "mod-1.0-rules",
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
        "id":                 f"anal-{uuid.uuid4().hex[:20]}",
        "model":              "mod-1.0",
        "created":            int(time.time()),
        "input":              text,
        "flagged":            result["flagged"],
        "severity":           severity,
        "action":             result["action"],
        "flagged_categories": flagged_cats,
        "categories":         result["categories"],
        "category_scores":    result["category_scores"],
        "model_backend":      result["model_backend"],
        "recommendation": {
            "action":  result["action"],
            "message": _recommendation_message(result),
        },
    }


def build_classify_result(text: str, categories: Optional[list] = None) -> dict:
    result = moderate(text)
    all_cats = result["category_scores"]

    filtered = {k: v for k, v in all_cats.items() if k in categories} if categories else all_cats
    top = max(filtered, key=filtered.get) if filtered else "none"

    return {
        "id":           f"cls-{uuid.uuid4().hex[:20]}",
        "model":        "mod-1.0",
        "created":      int(time.time()),
        "input":        text,
        "top_category": top,
        "scores":       filtered,
        "flagged":      result["flagged"],
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
