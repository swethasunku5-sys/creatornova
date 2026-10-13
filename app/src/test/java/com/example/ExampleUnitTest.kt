package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test eco reach calculation formula`() {
        val expectedViewsPerItem = 85000L
        val plannedItems = 3
        val estimatedImpressions = expectedViewsPerItem * plannedItems
        assertEquals(255000L, estimatedImpressions)
    }

    @Test
    fun `test cpm calculation in inr`() {
        val costInr = 65000.0
        val impressions = 255000.0
        val cpm = (costInr / impressions) * 1000.0
        assertEquals(254.9, cpm, 0.5)
    }

    @Test
    fun `test budget utilization percentage`() {
        val allocatedCosts = 65000.0
        val totalBudget = 100000.0
        val utilization = (allocatedCosts / totalBudget) * 100.0
        assertEquals(65.0, utilization, 0.01)
    }
}
