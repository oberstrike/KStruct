package com.maju.jpa

import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*


/**
 * Interface to allow execution of Query by Example [Example] instances.
 *
 * @param <T>
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 1.12
</T> */
interface QueryByExampleExecutor<T> {
    /**
     * Returns a single entity matching the given [Example] or null if none was found.
     *
     * @param example must not be null.
     * @return a single entity matching the given [Example] or [Optional.empty] if none was found.
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if the Example yields more than one result.
     */
    fun <S : T?> findOne(example: Example<S>?): Optional<S>?

    /**
     * Returns all entities matching the given [Example]. In case no match could be found an empty [Iterable]
     * is returned.
     *
     * @param example must not be null.
     * @return all entities matching the given [Example].
     */
    fun <S : T?> findAll(example: Example<S>?): Iterable<S>?

    /**
     * Returns all entities matching the given [Example] applying the given [Sort]. In case no match could be
     * found an empty [Iterable] is returned.
     *
     * @param example must not be null.
     * @param sort the [Sort] specification to sort the results by, must not be null.
     * @return all entities matching the given [Example].
     * @since 1.10
     */
    fun <S : T?> findAll(example: Example<S>?, sort: Sort?): Iterable<S>?

    /**
     * Returns a [Page] of entities matching the given [Example]. In case no match could be found, an empty
     * [Page] is returned.
     *
     * @param example must not be null.
     * @param pageable can be null.
     * @return a [Page] of entities matching the given [Example].
     */
    fun <S : T?> findAll(example: Example<S>?, pageable: Pageable?): Page<S>?

    /**
     * Returns the number of instances matching the given [Example].
     *
     * @param example the [Example] to count instances for. Must not be null.
     * @return the number of instances matching the [Example].
     */
    fun <S : T?> count(example: Example<S>?): Long

    /**
     * Checks whether the data store contains elements that match the given [Example].
     *
     * @param example the [Example] to use for the existence check. Must not be null.
     * @return true if the data store contains elements that match the given [Example].
     */
    fun <S : T?> exists(example: Example<S>?): Boolean
}
