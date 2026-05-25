package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc

 class AssignmentApplicationTests{
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testIdempotency() throws Exception {

        String json = """
        {
          "eventId": "evt-1",
          "accountId": "acct-1",
          "type": "CREDIT",
          "amount": 100,
          "currency": "USD",
          "eventTimestamp": "2026-05-01T10:00:00Z"
        }
        """;

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        // duplicate
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testOutOfOrderEvents() throws Exception {

        // Later event first
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "eventId": "evt-2",
                  "accountId": "acct-2",
                  "type": "DEBIT",
                  "amount": 50,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-10T10:00:00Z"
                }
                """));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "eventId": "evt-3",
                  "accountId": "acct-2",
                  "type": "CREDIT",
                  "amount": 100,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-01T10:00:00Z"
                }
                """));

        mockMvc.perform(get("/events?account=acct-2"))
                .andExpect(status().isOk());
    }

    @Test
    void testBalance() throws Exception {

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "eventId": "evt-4",
                  "accountId": "acct-3",
                  "type": "CREDIT",
                  "amount": 200,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-01T10:00:00Z"
                }
                """));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "eventId": "evt-5",
                  "accountId": "acct-3",
                  "type": "DEBIT",
                  "amount": 50,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-02T10:00:00Z"
                }
                """));

        mockMvc.perform(get("/accounts/acct-3/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150));
    }

    @Test
    void testValidation() throws Exception {

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "eventId": "",
                  "amount": -10
                }
                """))
                .andExpect(status().isBadRequest());
    }
}