import json
import logging 
import os 
import threading
import uuid
from datetime import  datetime, timezone
from decimal import Decimal 

from confluent_kafka import Consumer, Producer
from fastapi import FastAPI
from pydantic import BaseModel 

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="OrderFlow Fraud Service", 
    version="1.0.0",
    description="Evaluates order risk and publish fraud decisions."
)

KAFKA_BOOTSTRAP_SERVERS = os.getenv(
    "KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"
)
ORDER_CREATED_TOPIC = "order.created"
FRAUD_RESULT_TOPIC = "fraud.result"
REVIEW_THRESHOLD = Decimal(
    os.getenv("FRAUD_REVIEW_THRESHOLD", "1000.00")
)

producer = Producer(
    {"bootstrap.servers": KAFKA_BOOTSTRAP_SERVERS}
)


consumer = Consumer(
    {
        "bootstrap.servers": KAFKA_BOOTSTRAP_SERVERS,
        "group.id": "fraud-service",
        "auto.offset.reset": "earliest",
        "enable.auto.commit": False,
    }
  )

  stop_event = threading.Event()
  consumer_thread = None


class FraudDecision(BaseModel):
    orderId: str
    decision: str 
    reason: str 


def evaluate_order(amount: Decimal) -> tuple[str, str]:
    if amount <= 0:
        return "REJECTED", "Order amount must be positive"
    
    if amount > REVIEW_THRESHOLD:
        return "REVIEW_REQUIRED", "Order exceeeds the review threshold"

        return "APPROVED","Order passed the configured fraud rules"

def publish_decision(event: dict) -> None:
    producer.produce(
        FRAUD_RESULT_TOPIC,
        key=event["orderId"],
        value=json.dumps(event),
    )
    producer.flush(10)


def process_order(message: dict) -> None:
    amount = Decimal(str(message["amount"]))
    decision, reason = evaluate_order(amount)

    result = {
        "eventId": str(uuid.uuid4()),
        "orderId": message["orderId"],
        "decision": decision,
        "reason": reason,
        "occurredAt":
        datetime.now(timezone.utc).isoformat(),
    }

     publish_decision(result)

    logger.info(
        "Fraud decision published for order %s: %s",
        result["orderId"],
        decision,
    )


def consume_orders() -> None:
    consumer.subscribe([ORDER_CREATED_TOPIC])

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
                process_order(event)

                # Commit only after processing and publishing succeeds.
                consumer.commit(message=message, asynchronous=False)

            except Exception:
                logger.exception("Failed to process order event")
                # Leave the message uncommitted for subsequent recovery.
                # A production implementation needs bounded retries and a
                # dead-letter strategy to avoid repeatedly blocking a partition.

    finally:
        consumer.close()


@app.on_event("startup")
def start_consumer() -> None:
    global consumer_thread

    stop_event.clear()
    consumer_thread = threading.Thread(
        target=consume_orders,
        daemon=True,
    )
    consumer_thread.start()


@app.on_event("shutdown")
def stop_consumer() -> None:
    stop_event.set()

    if consumer_thread is not None:
        consumer_thread.join(timeout=5)

    producer.flush(5)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "service": "fraud-service"}


@app.post("/evaluate", response_model=FraudDecision)
def evaluate(request: dict) -> FraudDecision:
    amount = Decimal(str(request.get("amount", 0)))
    decision, reason = evaluate_order(amount)

    return FraudDecision(
        orderId=str(request.get("orderId", "manual-evaluation")),
        decision=decision,
        reason=reason,
    )