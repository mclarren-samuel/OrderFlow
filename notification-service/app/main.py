
import json
import logging
import os
import threading

from confluent_kafka import Consumer
from fastapi import FastAPI

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="OrderFlow Notification Service",
    version="1.0.0",
)

consumer = Consumer(
    {
        "bootstrap.servers": os.getenv(
            "KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"
        ),
        "group.id": "notification-service",
        "auto.offset.reset": "earliest",
        "enable.auto.commit": False,
    }
)

stop_event = threading.Event()
consumer_thread = None


def process_notification(event: dict) -> None:
    status = event.get("status", "UNKNOWN")

    messages = {
        "APPROVED": "Your order has been approved.",
        "REJECTED": "Your order has been rejected.",
        "REVIEW_REQUIRED": "Your order requires additional review.",
        "PENDING": "Your order is being processed.",
    }

    message = messages.get(
        status,
        "Your order status has been updated.",
    )

    logger.info(
        "Customer notification: customer=%s order=%s status=%s message=%s",
        event.get("customerId"),
        event.get("orderId"),
        status,
        message,
    )


def consume_status_events() -> None:
    consumer.subscribe(["order.status"])

    try:
        while not stop_event.is_set():
            message = consumer.poll(1.0)

            if message is None:
                continue

            if message.error():
                logger.error("Kafka consumer error: %s", message.error())
                continue

            try:
                event = json.loads(message.value().decode("utf-8"))
                process_notification(event)

                consumer.commit(message=message, asynchronous=False)

            except Exception:
                logger.exception("Notification processing failed")

    finally:
        consumer.close()


@app.on_event("startup")
def start_consumer() -> None:
    global consumer_thread

    stop_event.clear()
    consumer_thread = threading.Thread(
        target=consume_status_events,
        daemon=True,
    )
    consumer_thread.start()


@app.on_event("shutdown")
def stop_consumer() -> None:
    stop_event.set()

    if consumer_thread is not None:
        consumer_thread.join(timeout=5)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "service": "notification-service"}