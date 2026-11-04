
from app.main import process_notification


def test_approved_notification_does_not_raise():
    process_notification(
        {
            "customerId": "customer-101",
            "orderId": "order-101",
            "status": "APPROVED",
        }
    )