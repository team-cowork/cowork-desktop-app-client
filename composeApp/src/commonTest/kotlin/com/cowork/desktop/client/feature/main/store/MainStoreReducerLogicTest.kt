package com.cowork.desktop.client.feature.main.store

import com.cowork.desktop.client.domain.model.MeetingNoteTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MainStoreReducerLogicTest {

    @Test
    fun selectingProjectClearsSelectedChannel() {
        val state = MainStore.State(
            selectedChannelId = 11,
            selectedProjectId = 22,
            chatDraft = "다른 채널로 보내면 안 되는 초안",
        )

        val result = state.reduceProjectSelection(projectId = 33)

        assertNull(result.selectedChannelId)
        assertEquals(33, result.selectedProjectId)
        assertEquals("", result.chatDraft)
    }

    @Test
    fun moveItemAppliesRequestedOrderOnce() {
        val result = listOf(1L, 2L, 3L).moveItem(fromIndex = 0, toIndex = 1)

        assertEquals(listOf(2L, 1L, 3L), result)
    }

    @Test
    fun inactiveMeetingTemplateIsNotUsedAsCreationFallback() {
        val state = MainStore.State(
            createNoteTitle = "주간 회의",
            meetingNoteTemplates = listOf(
                MeetingNoteTemplate(
                    id = 1,
                    channelId = 2,
                    name = "비활성 템플릿",
                    isActive = false,
                    createdBy = 3,
                    sections = emptyList(),
                ),
            ),
        )

        assertNull(state.activeTemplate)
        assertFalse(state.canSubmitNote)
    }
}
