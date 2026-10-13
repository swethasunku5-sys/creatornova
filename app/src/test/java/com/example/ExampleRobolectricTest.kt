package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.SeedData
import com.example.data.model.EngagementStatus
import com.example.data.model.VerificationStatus
import com.example.data.repository.CreatorNovaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CreatorNova", appName)
    }

    @Test
    fun `verify demo seed data contains 10 distinct creators`() {
        val creators = SeedData.demoCreators
        assertEquals(10, creators.size)
        assertTrue(creators.any { it.name == "Aanya Sharma" })
        assertTrue(creators.any { it.name == "Ananya Rao" })
        assertTrue(creators.any { it.name == "Arjun Reddy" })
    }

    @Test
    fun `verify repository engagement lifecycle transitions`() {
        val repo = CreatorNovaRepository()
        val firstEngagement = repo.engagements.value.first()
        val success = repo.updateEngagementStatus(firstEngagement.id, EngagementStatus.REVIEW, "Test review note")
        assertTrue(success)

        val updated = repo.engagements.value.first { it.id == firstEngagement.id }
        assertEquals(EngagementStatus.REVIEW, updated.status)
        assertTrue(updated.statusHistory.any { it.note == "Test review note" })
    }
}
