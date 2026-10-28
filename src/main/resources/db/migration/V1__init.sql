CREATE TABLE idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    response_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    request_json TEXT NOT NULL,
    rules_fired TEXT NOT NULL,
    decision VARCHAR(16) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE FUNCTION reject_audit_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is append-only: % is forbidden', TG_OP;
END;
$$;

CREATE TRIGGER audit_log_reject_update_delete
BEFORE UPDATE OR DELETE ON audit_log
FOR EACH ROW EXECUTE FUNCTION reject_audit_mutation();
