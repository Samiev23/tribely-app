package com.tribely.app.feature.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.model.Group
import com.tribely.app.core.data.repository.GroupRepository
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyPink
import com.tribely.app.core.ui.theme.TribelyTheme
import kotlinx.coroutines.launch

private enum class Choice { CREATE, JOIN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectScreen(
    onGroupReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val groupRepo = remember { GroupRepository() }
    val scope = rememberCoroutineScope()

    val userName = sessionManager.userNameFlow.collectAsState(initial = null).value ?: "..."

    var choice by remember { mutableStateOf<Choice?>(null) }
    var groupName by remember { mutableStateOf("Семья") }
    var inviteCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        groupRepo.getMyGroups()
            .getOrNull()
            ?.firstOrNull()
            ?.let { group ->
                sessionManager.saveGroup(group.id, group.name, group.inviteCode)
                onGroupReady()
            }
    }

    fun handleSubmit() {
        if (isLoading) return
        errorText = null
        isLoading = true
        scope.launch {
            val result: Result<Group> = when (choice) {
                Choice.CREATE -> groupRepo.createGroup(groupName.ifBlank { "Моя группа" })
                Choice.JOIN -> groupRepo.joinGroupByCode(inviteCode)
                null -> return@launch
            }
            result
                .onSuccess { group ->
                    sessionManager.saveGroup(group.id, group.name, group.inviteCode)
                    isLoading = false
                    onGroupReady()
                }
                .onFailure {
                    val msg = it.message.orEmpty()
                    errorText = when {
                        msg.contains("Invalid invite code", ignoreCase = true) ->
                            "Код не найден. Проверь правильность."
                        msg.contains("Not authenticated", ignoreCase = true) ->
                            "Сессия истекла, перезайди в приложение."
                        else -> msg.ifBlank { "Что-то пошло не так" }
                    }
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))

        // Шапка с приветствием
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "С возвращением",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = userName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "последний шаг",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "С кем будешь\nсоревноваться?",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 44.sp
        )

        Spacer(Modifier.height(24.dp))

        // Карточка "Создать новую"
        ChoiceCard(
            isSelected = choice == Choice.CREATE,
            accentColor = TribelyAccent,
            icon = Icons.Filled.Add,
            title = "Создать новую",
            description = "Семья или компания друзей. Пригласишь по ссылке.",
            onClick = {
                choice = if (choice == Choice.CREATE) null else Choice.CREATE
                errorText = null
            }
        )

        AnimatedVisibility(
            visible = choice == Choice.CREATE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "НАЗВАНИЕ ГРУППЫ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { if (it.length <= 50) groupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Например: Семья Ивановых") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TribelyAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Карточка "Войти по коду"
        ChoiceCard(
            isSelected = choice == Choice.JOIN,
            accentColor = TribelyPink,
            icon = Icons.Filled.PersonAdd,
            title = "Войти по коду",
            description = "У тебя уже есть приглашение от друга или семьи.",
            onClick = {
                choice = if (choice == Choice.JOIN) null else Choice.JOIN
                errorText = null
            }
        )

        AnimatedVisibility(
            visible = choice == Choice.JOIN,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "КОД ПРИГЛАШЕНИЯ (6 СИМВОЛОВ)",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = {
                        if (it.length <= 6) inviteCode = it.uppercase().filter { c -> c.isLetterOrDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("ABC123") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TribelyPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        // Ошибка
        if (errorText != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "❌ $errorText",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(32.dp))

        // CTA кнопка
        val canSubmit = when (choice) {
            Choice.CREATE -> groupName.isNotBlank()
            Choice.JOIN -> inviteCode.length == 6
            null -> false
        }

        Button(
            onClick = { handleSubmit() },
            enabled = canSubmit && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = when (choice) {
                        Choice.CREATE -> "Создать и поехали"
                        Choice.JOIN -> "Присоединиться"
                        null -> "Выбери вариант"
                    },
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChoiceCard(
    isSelected: Boolean,
    accentColor: Color,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) accentColor else Color.White.copy(alpha = 0.04f)
    val contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onBackground

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.Black else accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    color = if (isSelected) Color.Black.copy(alpha = 0.7f) else contentColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505, widthDp = 360, heightDp = 760)
@Composable
private fun GroupSelectScreenPreview() {
    TribelyTheme {
        GroupSelectScreen()
    }
}
