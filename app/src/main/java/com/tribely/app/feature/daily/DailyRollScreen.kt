package com.tribely.app.feature.daily

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.model.DailyRollState
import com.tribely.app.core.data.model.MemberSubmissionStatus
import com.tribely.app.core.data.repository.AuthRepository
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyPink
import com.tribely.app.core.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DailyRollScreen(
    onLoggedOut: () -> Unit = {},
    onFullscreenChanged: (Boolean) -> Unit = {},
    viewModel: DailyRollViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val authRepo = remember { AuthRepository(sessionManager) }
    val scope = rememberCoroutineScope()

    val groupId by sessionManager.groupIdFlow.collectAsState(initial = null)
    val userName by sessionManager.userNameFlow.collectAsState(initial = null)
    val groupName by sessionManager.groupNameFlow.collectAsState(initial = null)

    val uiState by viewModel.uiState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(fullscreenIndex, capturedFile) {
        onFullscreenChanged(fullscreenIndex != null || capturedFile != null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null && file.exists() && file.length() > 0) {
            capturedFile = file
        } else {
            file?.delete()
        }
        pendingCameraFile = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (file, uri) = ImageUtils.createCameraImageFile(context)
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Без доступа к камере не получится сделать фото 📷",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun launchCamera() {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (cameraGranted) {
            val (file, uri) = ImageUtils.createCameraImageFile(context)
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(groupId) {
        groupId?.let { viewModel.loadRoll(it) }
    }

    val fsIndex = fullscreenIndex
    val successData = (uiState as? DailyRollUiState.Success)?.data
    if (fsIndex != null && successData != null && successData.submissions.isNotEmpty()) {
        FullscreenPhotoViewer(
            submissions = successData.submissions,
            initialIndex = fsIndex,
            onClose = { fullscreenIndex = null },
            onReact = { submissionId, type ->
                viewModel.toggleReaction(submissionId, type)
            }
        )
        return
    }

    val captured = capturedFile
    if (captured != null) {
        val challengeText = (uiState as? DailyRollUiState.Success)?.data?.challenge?.textRu ?: ""
        val rollId = (uiState as? DailyRollUiState.Success)?.data?.roll?.id

        PhotoPreviewScreen(
            photoFile = captured,
            challengeText = challengeText,
            isUploading = uploadState is UploadState.Uploading,
            errorMessage = (uploadState as? UploadState.Failed)?.message,
            onRetake = {
                if (uploadState !is UploadState.Uploading) {
                    captured.delete()
                    capturedFile = null
                    viewModel.resetUpload()
                }
            },
            onSubmit = { caption ->
                val currentGroupId = groupId
                if (rollId != null && currentGroupId != null) {
                    scope.launch {
                        val bytes = withContext(Dispatchers.IO) {
                            ImageUtils.compressImageToBytes(captured)
                        }
                        viewModel.submitPhoto(rollId, bytes, currentGroupId, caption)
                    }
                }
            }
        )

        LaunchedEffect(uploadState) {
            if (uploadState is UploadState.Done) {
                captured.delete()
                capturedFile = null
                Toast.makeText(context, "Принято! +50 XP 🎉", Toast.LENGTH_SHORT).show()
                viewModel.resetUpload()
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        when (val state = uiState) {
            is DailyRollUiState.Loading -> LoadingView()
            is DailyRollUiState.Error -> ErrorView(
                message = state.message,
                onRetry = { groupId?.let { viewModel.loadRoll(it) } },
                onLogout = {
                    scope.launch {
                        authRepo.signOut()
                        onLoggedOut()
                    }
                }
            )
            is DailyRollUiState.Success -> DailyRollContent(
                state = state.data,
                userName = userName ?: "",
                groupName = groupName ?: "",
                onMakePhoto = { launchCamera() },
                onPhotoClick = { index -> fullscreenIndex = index }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = TribelyAccent)
            Spacer(Modifier.height(16.dp))
            Text(
                "Загружаем бросок дня...",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "❌ Ошибка",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) {
            Text("Попробовать снова", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onLogout) {
            Text("Выйти", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun DailyRollContent(
    state: DailyRollState,
    userName: String,
    groupName: String,
    onMakePhoto: () -> Unit,
    onPhotoClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 140.dp)
    ) {
        Header(
            groupName = groupName,
            completedCount = state.completedCount,
            totalMembers = state.totalMembers
        )

        Spacer(Modifier.height(16.dp))

        TimerCard(members = state.members)

        Spacer(Modifier.height(28.dp))

        ChallengeCard(
            categoryRu = challengeCategoryRu(state.challenge.category),
            text = state.challenge.textRu,
            mediaTypeRu = if (state.challenge.mediaType == "video") "Видео" else "Фото",
            groupName = groupName,
            totalMembers = state.totalMembers
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "КТО УЖЕ ВЫПОЛНИЛ · ${state.completedCount} ИЗ ${state.totalMembers}",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(8.dp))

        state.members.forEach { member ->
            MemberRow(
                member = member,
                isCurrentUser = member.displayName == userName
            )
            Spacer(Modifier.height(6.dp))
        }

        if (state.mySubmissionId == null) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onMakePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Сделать фото", fontWeight = FontWeight.ExtraBold)
            }
        }

        if (state.submissions.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            SubmissionsFeed(
                submissions = state.submissions,
                onSubmissionClick = onPhotoClick
            )
        }
    }
}

@Composable
private fun Header(
    groupName: String,
    completedCount: Int,
    totalMembers: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = groupName.uppercase(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Бросок дня",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$completedCount/$totalMembers уже выполнили",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TimerCard(members: List<MemberSubmissionStatus>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "СЛЕДУЮЩИЙ БРОСОК",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "до 9:00 утра",
                    color = TribelyAccent,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                members.take(4).forEachIndexed { index, member ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = (-8 * index).dp)
                            .clip(CircleShape)
                            .background(memberColor(index))
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.displayName.take(1).uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    categoryRu: String,
    text: String,
    mediaTypeRu: String,
    groupName: String,
    totalMembers: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TribelyPink.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TribelyPink.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✨ ЗАДАНИЕ ДНЯ",
                    color = TribelyPink,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TribelyPink.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$categoryRu · $mediaTypeRu",
                        color = TribelyPink,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "👥 $groupName · $totalMembers чел",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberSubmissionStatus,
    isCurrentUser: Boolean
) {
    val bgColor = if (member.hasSubmitted) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.02f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(memberColorByName(member.displayName)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.displayName.take(1).uppercase(),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "(ты)",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = if (member.hasSubmitted) "выполнено" else "ждём…",
                    color = if (member.hasSubmitted) {
                        TribelyAccent
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (member.hasSubmitted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TribelyAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = TribelyAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "+50 XP",
                            color = TribelyAccent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun memberColor(index: Int): Color {
    val palette = listOf(
        Color(0xFFFF3EA5),
        Color(0xFFD4FF00),
        Color(0xFF00E5FF),
        Color(0xFFFFAA00),
        Color(0xFFA855F7)
    )
    return palette[Math.floorMod(index, palette.size)]
}

private fun memberColorByName(name: String): Color {
    return memberColor(name.hashCode())
}

private fun challengeCategoryRu(category: String): String = when (category) {
    "photo" -> "Фото"
    "video" -> "Видео"
    "action" -> "Действие"
    "creative" -> "Креатив"
    "household" -> "Бытовое"
    else -> category
}
