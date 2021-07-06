package com.maju.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean


/**
 * Extension of [CrudRepository] to provide additional methods to retrieve entities using the pagination and
 * sorting abstraction.
 *
 * @author Oliver Gierke
 * @see Sort
 *
 * @see Pageable
 *
 * @see Page
 */
@NoRepositoryBean
interface PagingAndSortingRepository<T, ID> : CrudRepository<T, ID> {
    /**
     * Returns all entities sorted by the given options.
     *
     * @param sort
     * @return all entities sorted by the given options
     */
    fun findAll(sort: Sort?): Iterable<T>?

    /**
     * Returns a [Page] of entities meeting the paging restriction provided in the `Pageable` object.
     *
     * @param pageable
     * @return a page of entities
     */
    fun findAll(pageable: Pageable?): Page<T>?
}
