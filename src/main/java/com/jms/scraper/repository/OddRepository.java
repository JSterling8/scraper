package com.jms.scraper.repository;

import com.jms.scraper.model.Odd;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by anon on 21/02/2016.
 */
@Repository
public interface OddRepository extends PagingAndSortingRepository<Odd, Long> {
}
