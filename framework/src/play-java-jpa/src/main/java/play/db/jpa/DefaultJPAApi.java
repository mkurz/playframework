/*
 * Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.jpa;

import play.inject.ApplicationLifecycle;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.*;

/**
 * Default implementation of the JPA API.
 */
public class DefaultJPAApi implements JPAApi {

    private final JPAConfig jpaConfig;

    private final Map<String, EntityManagerFactory> emfs = new HashMap<>();

    public DefaultJPAApi(JPAConfig jpaConfig) {
        this.jpaConfig = jpaConfig;
    }

    @Singleton
    public static class JPAApiProvider implements Provider<JPAApi> {
        private final JPAApi jpaApi;

        @Inject
        public JPAApiProvider(JPAConfig jpaConfig, ApplicationLifecycle lifecycle) {
            // dependency on db api ensures that the databases are initialised
            jpaApi = new DefaultJPAApi(jpaConfig);
            lifecycle.addStopHook(() -> {
                jpaApi.shutdown();
                return CompletableFuture.completedFuture(null);
            });
            jpaApi.start();
        }

        @Override
        public JPAApi get() {
            return jpaApi;
        }
    }

    /**
     * Initialise JPA entity manager factories.
     */
    @Override
    public JPAApi start() {
        jpaConfig.persistenceUnits().forEach(persistenceUnit ->
                emfs.put(persistenceUnit.name, Persistence.createEntityManagerFactory(persistenceUnit.unitName))
        );
        return this;
    }

    /**
     * Get a newly created EntityManager for the specified persistence unit name.
     *
     * @param name The persistence unit name
     */
    @Override
    public EntityManager em(String name) {
        EntityManagerFactory emf = emfs.get(name);
        if (emf == null) {
            return null;
        }
        return emf.createEntityManager();
    }

    /**
     * Run a block of code with a newly created EntityManager.
     *
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    @Override
    public <T> T withTransaction(Function<EntityManager, T> block) {
        return withTransaction("default", block);
    }

    /**
     * Run a block of code with a newly created EntityManager for the named Persistence Unit.
     *
     * @param name The persistence unit name
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    @Override
    public <T> T withTransaction(String name, Function<EntityManager, T> block) {
        return withTransaction(name, false, block);
    }

    /**
     * Run a block of code with a newly created EntityManager for the named Persistence Unit.
     *
     * @param name The persistence unit name
     * @param readOnly Is the transaction read-only?
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    @Override
    public <T> T withTransaction(String name, boolean readOnly, Function<EntityManager, T> block) {
        EntityManager entityManager = null;
        EntityTransaction tx = null;

        try {
            entityManager = em(name);

            if (entityManager == null) {
                throw new RuntimeException("Could not create JPA entity manager for '" + name + "'");
            }

            JPAEntityManagerContext.push(entityManager);

            if (!readOnly) {
                tx = entityManager.getTransaction();
                tx.begin();
            }

            T result = block.apply(entityManager);

            if (tx != null) {
                if(tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            }

            return result;

        } catch (Throwable t) {
            if (tx != null) {
                try { tx.rollback(); } catch (Throwable e) {}
            }
            throw t;
        } finally {
            JPAEntityManagerContext.pop();
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    /**
     * Close all entity manager factories.
     */
    @Override
    public void shutdown() {
        emfs.values().forEach(EntityManagerFactory::close);
    }

}
