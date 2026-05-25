package com.assignment.ledger.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assignment.ledger.model.Event;

@Service
public class EventService {

    @Autowired
    private EventRepository repo;

    public Event createEvent(Event event) {
        // Idempotency check
        return repo.findById(event.getEventId())
                   .orElseGet(() -> repo.save(event));
    }

    public Event getEvent(String id) {
        return repo.findById(id)
                   .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<Event> getEventsByAccount(String accountId) {
        return repo.findByAccountIdOrderByEventTimestampAscCreatedAtAsc(accountId);
    }

    public BigDecimal getBalance(String accountId) {
        return repo.getBalance(accountId);
    }
}