package com.maju.jpa

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.util.*


/**
 * Interface for generic CRUD operations on a repository for a specific type.
 *
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Jens Schauder
 */
@NoRepositoryBean
interface CrudRepository<T, ID> : Repository<T, ID> {
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be null.
     * @return the saved entity; will never be null.
     * @throws IllegalArgumentException in case the given entity is null.
     */
    fun <S : T?> save(entity: S): S

    /**
     * Saves all given entities.
     *
     * @param entities must not be null nor must it contain null.
     * @return the saved entities; will never be null. The returned Iterable will have the same size
     * as the Iterable passed as an argument.
     * @throws IllegalArgumentException in case the given [entities][Iterable] or one of its entities is
     * null.
     */
    fun <S : T?> saveAll(entities: Iterable<S>?): Iterable<S>?

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be null.
     * @return the entity with the given id or Optional#empty() if none found.
     * @throws IllegalArgumentException if id is null.
     */
    fun findById(id: ID): Optional<T>?

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be null.
     * @return true if an entity with the given id exists, false otherwise.
     * @throws IllegalArgumentException if id is null.
     */
    fun existsById(id: ID): Boolean

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    fun findAll(): Iterable<T>?

    /**
     * Returns all instances of the type `T` with the given IDs.
     *
     *
     * If some or all ids are not found, no entities are returned for these IDs.
     *
     *
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param ids must not be null nor contain any null values.
     * @return guaranteed to be not null. The size can be equal or less than the number of given
     * ids.
     * @throws IllegalArgumentException in case the given [ids][Iterable] or one of its items is null.
     */
    fun findAllById(ids: Iterable<ID>?): Iterable<T>?

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    fun count(): Long

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be null.
     * @throws IllegalArgumentException in case the given id is null
     */
    fun deleteById(id: ID)

    /**
     * Deletes a given entity.
     *
     * @param entity must not be null.
     * @throws IllegalArgumentException in case the given entity is null.
     */
    fun delete(entity: T)

    /**
     * Deletes all instances of the type `T` with the given IDs.
     *
     * @param ids must not be null. Must not contain null elements.
     * @throws IllegalArgumentException in case the given ids or one of its elements is null.
     * @since 2.5
     */
    fun deleteAllById(ids: Iterable<ID>?)

    /**
     * Deletes the given entities.
     *
     * @param entities must not be null. Must not contain null elements.
     * @throws IllegalArgumentException in case the given entities or one of its entities is null.
     */
    fun deleteAll(entities: Iterable<T>?)

    /**
     * Deletes all entities managed by the repository.
     */
    fun deleteAll()
}
