CREATE TABLE webhook_subscription (
                                      id BIGSERIAL PRIMARY KEY,
                                      partner_id VARCHAR(255) NOT NULL,
                                      url TEXT NOT NULL,
                                      secret VARCHAR(255) NOT NULL,
                                      active BOOLEAN NOT NULL DEFAULT true,
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE webhook_delivery (
                                  id BIGSERIAL PRIMARY KEY,
                                  subscription_id BIGINT NOT NULL REFERENCES webhook_subscription(id),
                                  domain_event_id VARCHAR(255) NOT NULL,
                                  domain_event_type VARCHAR(100) NOT NULL,
                                  payload JSONB,
                                  attempt INT NOT NULL DEFAULT 0,
                                  status VARCHAR(50) NOT NULL, -- PENDING, RETRYING, SUCCESS, FAILED
                                  last_error TEXT,
                                  next_attempt_at TIMESTAMP WITH TIME ZONE,
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
                                  UNIQUE (subscription_id, domain_event_id)
);

CREATE INDEX idx_webhook_delivery_next_attempt ON webhook_delivery(next_attempt_at);
