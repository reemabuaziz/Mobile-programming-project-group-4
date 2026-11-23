package com.group04.materialexchange

import com.group04.materialexchange.domain.*

import org.junit.Assert.*
import org.junit.Test

class ExchangeRulesTest {
    private val item=MaterialItem("1","Arduino","Boards","Good",1,"Free","Library","owner","Owner",photo="arduino")
    private val validDraft=ListingDraft("Arduino","Boards","Good","1","Free","Library","arduino")
    @Test fun universityEmailRejectsLookalikeDomains() {
        assertTrue(ExchangeRules.validEmail("2240005931@iau.edu.sa"))
        assertFalse(ExchangeRules.validEmail("user@iau.edu.sa.evil.com"))
        assertFalse(ExchangeRules.validEmail("user@gmail.com"))
        assertFalse(ExchangeRules.validEmail("@iau.edu.sa"))
    }
    @Test fun invalidQuantitiesAndMissingPhotoAreRejected() {
        assertNull(ExchangeRules.draftError(validDraft))
        listOf("0","-1","1.5","1000","").forEach { assertNotNull(ExchangeRules.draftError(validDraft.copy(quantity=it))) }
        assertNotNull(ExchangeRules.draftError(validDraft.copy(photo="")))
    }
    @Test fun usersCannotRequestOwnOrUnavailableItems() {
        assertNotNull(ExchangeRules.requestError(item,"owner",emptyList()))
        assertNotNull(ExchangeRules.requestError(item.copy(status="Reserved"),"other",emptyList()))
        assertNull(ExchangeRules.requestError(item,"other",emptyList()))
    }
    @Test fun pendingAndAcceptedRequestsBlockDuplicatesButRejectedCanRetry() {
        val request=MaterialRequest("r","1","Arduino","other","Other","owner")
        assertNotNull(ExchangeRules.requestError(item,"other",listOf(request)))
        assertNotNull(ExchangeRules.requestError(item,"other",listOf(request.copy(status="Accepted"))))
        assertNull(ExchangeRules.requestError(item,"other",listOf(request.copy(status="Rejected"))))
    }
}
