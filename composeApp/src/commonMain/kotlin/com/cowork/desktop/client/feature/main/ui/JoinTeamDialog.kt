package com.cowork.desktop.client.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cowork.desktop.client.data.repository.TeamRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun JoinTeamDialog(
    onDismiss: () -> Unit,
    onJoined: () -> Unit,
    repository: TeamRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    var inviteCode by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(Modifier.width(420.dp).padding(24.dp)) {
                Text("초대 링크로 팀 참여", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("초대 URL의 마지막 코드 또는 초대 코드를 입력하세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    inviteCode,
                    { inviteCode = it.substringAfterLast('/').trim() },
                    label = { Text("초대 코드") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    Button(
                        onClick = {
                            scope.launch {
                                isJoining = true
                                error = null
                                runCatching { repository.joinTeam(inviteCode.trim()) }
                                    .onSuccess { onJoined() }
                                    .onFailure { error = it.userMessage() }
                                isJoining = false
                            }
                        },
                        enabled = inviteCode.isNotBlank() && !isJoining,
                    ) { Text(if (isJoining) "참여 중…" else "팀 참여") }
                }
            }
        }
    }
}
