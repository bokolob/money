package ru.sedano.db.interfaces;

public interface TransferMoney extends Operation<TransferMoney.TransferResult> {

    Account getFrom();

    Account getTo();

    class TransferResult {
        boolean ok;
        String error;

        public TransferResult(boolean ok, String error) {
            this.ok = ok;
            this.error = error;
        }

        public boolean isOk() {
            return ok;
        }

        public String getError() {
            return error;
        }
    }
}
