package com.jms.scraper.repository;

import com.jms.scraper.model.Result;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by anon on 30/01/2016.
 */
@Repository
public interface ResultRepository extends PagingAndSortingRepository<Result, Long> {
}
