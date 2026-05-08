package com.tribely.app.feature.battles

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tribely.app.core.data.model.SubmissionWithAuthor
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyCyan
import com.tribely.app.core.util.ImageUtils
import com.tribely.app.feature.feed.FeedPostCard
import java.io.File

@Composable
fun BattleDetailScreen(
    battleId: String,
    onBack: () -> Unit,
    viewModel: BattleDetailViewModel = viewModel(
        factory = BattleDetailViewModelFactory(battleId)
    )
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val mySubmission = state.submissions.firstOrNull { it.authorIsMe }

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null && file.exists() && file.length() > 0) {
            runCatching { ImageUtils.compressImageToBytes(file) }
                .onSuccess { bytes ->
                    capturedBytes = bytes
                    showPreview = true
                }
                .onFailure {
                    Toast.makeText(context, "Ошибка фото", Toast.LENGTH_SHORT).show()
                    file.delete()
                }
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
            Toast.makeText(context, "Нужно разрешение", Toast.LENGTH_SHORT).show()
        }
    }

    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val (file, uri) = ImageUtils.createCameraImageFile(context)
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.uploadDone) {
        if (state.uploadDone) {
            showPreview = false
            capturedBytes = null
            viewModel.resetUploadFlag()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (showPreview && capturedBytes != null) {
            BattlePhotoPreview(
                photoBytes = capturedBytes!!,
                isUploading = state.isUploading,
                onCancel = {
                    showPreview = false
                    capturedBytes = null
                },
                onSubmit = { caption ->
                    viewModel.upload(capturedBytes!!, caption)
                }
            )
            return@Column
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TribelyAccent)
                }
            }

            state.battle != null -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ChallengeBanner(
                            theme = state.battle!!.theme,
                            status = state.battle!!.status
                        )
                    }

                    if (mySubmission == null && state.battle!!.status == "live") {
                        item {
                            UploadCta(
                                isUploading = state.isUploading,
                                onClick = { openCamera() }
                            )
                        }
                    }

                    item {
                        Text(
                            text = if (state.submissions.isEmpty()) {
                                "ПОКА НИКОГО"
                            } else {
                                "УЧАСТНИКИ · ${state.submissions.size}"
                            },
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    if (state.submissions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Пока никто не загрузил",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        itemsIndexed(state.submissions) { index, submission ->
                            val feedSubmission = SubmissionWithAuthor(
                                id = submission.id,
                                userId = submission.userId,
                                authorName = submission.authorName,
                                authorAvatarColor = 0L,
                                mediaUrl = submission.signedPhotoUrl ?: submission.photoUrl,
                                mediaType = "photo",
                                createdAt = submission.createdAt.toString(),
                                isMine = submission.authorIsMe,
                                caption = submission.caption,
                                reactions = submission.reactions
                            )

                            FeedPostCard(
                                submission = feedSubmission,
                                challengeText = null,
                                bgColorIndex = index,
                                onPhotoClick = { },
                                onReact = { type -> viewModel.toggleReaction(submission.id, type) },
                                onMenuClick = {
                                    Toast.makeText(context, "Скоро меню", Toast.LENGTH_SHORT).show()
                                },
                                onCommentClick = {
                                    Toast.makeText(context, "Скоро: комментарии", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ChallengeBanner(theme: String, status: String) {
    val (label, bgColor) = when (status) {
        "live" -> "⚡ ИДЁТ" to TribelyCyan
        "finished" -> "🏁 ЗАВЕРШЁН" to TribelyAccent
        else -> "" to Color.Gray
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = bgColor
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = label,
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = theme,
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 27.sp
            )
        }
    }
}

@Composable
private fun UploadCta(
    isUploading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isUploading) { onClick() },
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = TribelyAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text("Загружаем...", color = Color.White, fontSize = 14.sp)
            } else {
                Text(text = "📸", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Сделай свою версию",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Камера откроется для съёмки",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BattlePhotoPreview(
    photoBytes: ByteArray,
    isUploading: Boolean,
    onCancel: () -> Unit,
    onSubmit: (String?) -> Unit
) {
    var caption by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoBytes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = caption,
            onValueChange = { if (it.length <= 200) caption = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Добавь подпись (необязательно)",
                    color = Color.White.copy(alpha = 0.3f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TribelyCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                cursorColor = TribelyCyan
            )
        )

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isUploading) { onCancel() },
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Отмена",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isUploading) {
                        onSubmit(caption.takeIf { it.isNotBlank() })
                    },
                color = TribelyAccent
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "📤 Загрузить",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
