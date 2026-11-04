from decimal import Decimal

from app.main import evaluate_order


def test_small_order_is_approved():
    decision, _ = evaluate_order(Decimal("100.00"))
    assert decision == "APPROVED"


def test_large_order_requires_review():
    decision, _ = evaluate_order(Decimal("1500.00"))
    assert decision == "REVIEW_REQUIRED"


def test_zero_amount_is_rejected():
    decision, _ = evaluate_order(Decimal("0"))
    assert decision == "REJECTED"