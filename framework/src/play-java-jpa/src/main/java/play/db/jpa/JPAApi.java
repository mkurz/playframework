/*
 * Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.jpa;

import java.util.function.Function;

import javax.persistence.EntityManager;

/**
 * JPA API.
 */
public interface JPAApi {

    /**
     * Initialise JPA entity manager factories.
     *
     * @return JPAApi instance
     */
    public JPAApi start();

    /**
     * Get a newly created EntityManager for the specified persistence unit name.
     *
     * @param name The persistence unit name
     * @return EntityManager for the specified persistence unit name
     */
    public EntityManager em(String name);

    /**
     * Run a block of code with a newly created EntityManager.
     *
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    public <T> T withTransaction(Function<EntityManager, T> block);

    /**
     * Run a block of code with a newly created EntityManager for the named Persistence Unit.
     *
     * @param name The persistence unit name
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    public <T> T withTransaction(String name, Function<EntityManager, T> block);

    /**
     * Run a block of code with a newly created EntityManager for the named Persistence Unit.
     *
     * @param name The persistence unit name
     * @param readOnly Is the transaction read-only?
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    public <T> T withTransaction(String name, boolean readOnly, Function<EntityManager, T> block);
    
    /**
     * Run a block of code with a newly created EntityManager for the named Persistence Unit.
     *
     * @param name The persistence unit name
     * @param readOnly Is the transaction read-only?
     * @param storeEmInHttpContext Store the entity manager in the Http.Context?
     * @param block Block of code to execute
     * @param <T> type of result
     * @return code execution result
     */
    public <T> T withTransaction(String name, boolean readOnly, boolean storeEmInHttpContext, Function<EntityManager, T> block);

    /**
     * Close all entity manager factories.
     */
    public void shutdown();
}
