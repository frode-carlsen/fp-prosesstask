package no.nav.vedtak.felles.prosesstask.impl.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Transaction handler. Delegates to EntityManager to handle transactions.
 */
public abstract class TransactionHandler<R> {

    public R apply(EntityManager em) throws Exception {
        boolean commit = false;
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            return doWork(em);
        }

        tx.begin();
        try {
            R o = doWork(em);
            commit = true;
            return o;
        } catch (SavepointRolledbackException e) {
            // vi skal fortsatt committe, kun et 'savepoint' er rullet tilbake.
            commit = true;
            throw e;
        } finally {
            if (tx.isActive()) {
                if (commit && !tx.getRollbackOnly()) {
                    tx.commit();
                } else {
                    tx.rollback();
                }
            }
        }
    }

    protected abstract R doWork(EntityManager entityManager) throws Exception; // NOSONAR

    public interface Work<R> {
        R doWork(EntityManager em) throws Exception; // NOSONAR
    }

}