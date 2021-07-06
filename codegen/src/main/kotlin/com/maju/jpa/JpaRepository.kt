package com.maju.jpa

import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.QueryByExampleExecutor


/**
 * JPA specific extension of [org.springframework.data.repository.Repository].
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Sander Krabbenborg
 * @author Jesse Wouters
 */
@NoRepositoryBean
interface JpaRepository<T, ID> : PagingAndSortingRepository<T, ID>,
    QueryByExampleExecutor<T> {
    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findAll()
	 */
    override fun findAll(): List<T>

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
	 */
    override fun findAll(sort: Sort): List<T>

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
	 */
    override fun findAllById(ids: Iterable<ID>): List<T>

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
	 */
    override fun <S : T?> saveAll(entities: Iterable<S>): List<S>

    /**
     * Flushes all pending changes to the database.
     */
    fun flush()

    /**
     * Saves an entity and flushes changes instantly.
     *
     * @param entity entity to be saved. Must not be null.
     * @return the saved entity
     */
    fun <S : T?> saveAndFlush(entity: S): S

    /**
     * Saves all entities and flushes changes instantly.
     *
     * @param entities entities to be deleted. Must not be null.
     * @return the saved entities
     * @since 2.5
     */
    fun <S : T?> saveAllAndFlush(entities: Iterable<S>?): List<S>?

    /**
     * Deletes the given entities in a batch which means it will create a single query. This kind of operation leaves JPAs
     * first level cache and the database out of sync. Consider flushing the [EntityManager] before calling this
     * method.
     *
     * @param entities entities to be deleted. Must not be null.
     */
    @Deprecated("Use {@link #deleteAllInBatch(Iterable)} instead.")
    fun deleteInBatch(entities: Iterable<T>?) {
        deleteAllInBatch(entities)
    }

    /**
     * Deletes the given entities in a batch which means it will create a single query. This kind of operation leaves JPAs
     * first level cache and the database out of sync. Consider flushing the [EntityManager] before calling this
     * method.
     *
     * @param entities entities to be deleted. Must not be null.
     * @since 2.5
     */
    fun deleteAllInBatch(entities: Iterable<T>?)

    /**
     * Deletes the entities identified by the given ids using a single query. This kind of operation leaves JPAs first
     * level cache and the database out of sync. Consider flushing the [EntityManager] before calling this method.
     *
     * @param ids the ids of the entities to be deleted. Must not be null.
     * @since 2.5
     */
    fun deleteAllByIdInBatch(ids: Iterable<ID>?)

    /**
     * Deletes all entities in a batch call.
     */
    fun deleteAllInBatch()

    /**
     * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
     * implemented this is very likely to always return an instance and throw an
     * [javax.persistence.EntityNotFoundException] on first access. Some of them will reject invalid identifiers
     * immediately.
     *
     * @param id must not be null.
     * @return a reference to the entity with the given identifier.
     * @see EntityManager.getReference
     */
    @Deprecated("use {@link JpaRepository#getById(ID)} instead.")
    fun getOne(id: ID): T

    /**
     * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
     * implemented this is very likely to always return an instance and throw an
     * [javax.persistence.EntityNotFoundException] on first access. Some of them will reject invalid identifiers
     * immediately.
     *
     * @param id must not be null.
     * @return a reference to the entity with the given identifier.
     * @see EntityManager.getReference
     * @since 2.5
     */
    fun getById(id: ID): T

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.springframework.data.domain.Example)
	 */
    override fun <S : T?> findAll(example: Example<S>): List<S>

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.springframework.data.domain.Example, org.springframework.data.domain.Sort)
	 */
    override fun <S : T?> findAll(example: Example<S>, sort: Sort): List<S>
}
