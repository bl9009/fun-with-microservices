package fwd.common.kv;

public class KvObject {

    private KvTransaction transaction;

    protected KvObject(KvStore store) {
        this.transaction = store.initTransaction();
    }

    public void multi() {
        transaction.multi();
    }

    public void exec() throws TransactionFailedException {
        transaction.exec();
    }

    public void discard() {
        transaction.discard();
    }

    public void watch(String... keys) {
        transaction.watch(keys);
    }

    protected KvTransaction getTransaction() {
        return transaction;
    }
}
