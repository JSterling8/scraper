package com.jms.scraper.repository;

import com.jms.scraper.model.Odd;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;

/**
 * Created by anon on 21/02/2016.
 */
@Repository
public interface OddRepository extends PagingAndSortingRepository<Odd, Long> {
    Iterable<Odd> findByMatchDate(Date date);
    Iterable<Odd> findByTeamOneOrTeamTwo(String teamOne, String teamTwo);
}
