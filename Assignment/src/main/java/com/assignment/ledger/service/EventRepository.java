package com.assignment.ledger.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.assignment.ledger.model.Event;

public interface EventRepository extends JpaRepository<Event, String> { 

List<Event> findByAccountIdOrderByEventTimestampAscCreatedAtAsc(String accountId); 

 @Query("SELECT COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount ELSE -e.amount END ), 0) FROM Event e WHERE e.accountId = :accountId ") 
 BigDecimal getBalance(String accountId); 

} 

 