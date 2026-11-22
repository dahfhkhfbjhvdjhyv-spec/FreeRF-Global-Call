package com.pixelpro.studio

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Ink = Color(0xFF10152B)
private val Violet = Color(0xFF7C5CFF)
private val Mint = Color(0xFF4DE2C5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PixelProApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PixelProApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var photo by remember { mutableStateOf<Uri?>(null) }
    val clips = remember { mutableStateListOf<Uri>() }
    var message by remember { mutableStateOf("আপনার সৃজনশীল কাজ শুরু করুন") }
    var busy by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { photo = uri; message = "ছবি নির্বাচন করা হয়েছে" }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        clips.clear(); clips.addAll(uris); message = if (uris.isEmpty()) "কোনো ভিডিও নির্বাচন করা হয়নি" else "${uris.size}টি ভিডিও ক্লিপ যোগ হয়েছে"
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Violet, secondary = Mint, background = Ink, surface = Color(0xFF1B2140))) {
        Scaffold(containerColor = Ink, topBar = {
            TopAppBar(
                title = { Column { Text("PixelPro Studio", fontWeight = FontWeight.Bold); Text("Create beyond limits", style = MaterialTheme.typography.labelSmall, color = Mint) } },
                navigationIcon = { Icon(Icons.Default.AutoAwesome, null, tint = Mint, modifier = Modifier.padding(start = 16.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink, titleContentColor = Color.White)
            )
        }) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(150.dp).background(RoundedCornerShape(24.dp), androidx.compose.ui.graphics.Brush.linearGradient(listOf(Violet, Color(0xFF252D55)))).padding(20.dp)) {
                        Column(Modifier.align(Alignment.CenterStart)) {
                            Text("Make every frame shine", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Photo • Video • AI tools", color = Mint)
                            Text("এক অ্যাপেই আপনার সব এডিটিং", color = Color.White)
                        }
                    }
                }
                item { Text("ফটো এডিটিং", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeatureTile("ছবি বাছাই", "গ্যালারি থেকে", Icons.Default.AddPhotoAlternate, Modifier.weight(1f)) { photoPicker.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) }
                        FeatureTile("HD Upscale", "ছবির সাইজ বাড়ান", Icons.Default.HighQuality, Modifier.weight(1f)) { if (photo == null) message = "আগে একটি ছবি বাছাই করুন" else {
                            busy = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { upscaleImage(context, photo!!) }
                                message = result ?: "ছবি প্রসেস করা যায়নি"
                                busy = false
                            }
                        } }
                    }
                }
                item {
                    photo?.let { AsyncImage(model = it, contentDescription = "Selected photo", modifier = Modifier.fillMaxWidth().height(210.dp)) }
                }
                item { Text("ভিডিও স্টুডিও", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                item {
                    FeatureTile("ভিডিও ক্লিপ যোগ করুন", "একাধিক ভিডিও নির্বাচন", Icons.Default.VideoLibrary, Modifier.fillMaxWidth()) {
                        videoPicker.launch(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    }
                }
                item {
                    Text("এডিটিং টাইমলাইন · ${clips.size}টি ক্লিপ", color = Mint, fontWeight = FontWeight.SemiBold)
                }
                itemsIndexed(clips) { index, uri ->
                    Row(Modifier.fillMaxWidth().background(Color(0xFF1B2140), RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, null, tint = Mint)
                        Text("ক্লিপ ${index + 1}", color = Color.White, modifier = Modifier.weight(1f).padding(start = 10.dp))
                        IconButton(onClick = { clips.remove(uri) }) { Icon(Icons.Default.Close, null, tint = Color.LightGray) }
                    }
                }
                item {
                    Button(onClick = { message = if (clips.size < 2) "ভিডিও জোড়ার জন্য অন্তত ২টি ক্লিপ বাছাই করুন" else "ক্লিপগুলো টাইমলাইনে সাজানো হয়েছে। ভিডিও এক্সপোর্ট ইঞ্জিন পরবর্তী ধাপে যুক্ত হবে।" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
                        Icon(Icons.Default.MergeType, null); Spacer(Modifier.width(8.dp)); Text("ভিডিওগুলো একসাথে জুড়ুন")
                    }
                }
                item {
                    Text("আরও AI টুলস", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                        listOf("AI ছবি পরিষ্কার ও ডিনয়েজ", "ভিডিও Stabilization", "Background Remove", "ট্রানজিশন ও কাট/ট্রিম", "সাবটাইটেল ও টেক্সট", "4K Export (ডিভাইস সাপোর্ট সাপেক্ষে)").forEach {
                            Row(Modifier.fillMaxWidth().background(Color(0xFF1B2140), RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoFixHigh, null, tint = Mint); Text(it, color = Color.White, modifier = Modifier.padding(start = 10.dp))
                            }
                        }
                    }
                }
                item {
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Mint)
                    Text(message, color = Mint, style = MaterialTheme.typography.bodyMedium)
                    Text("নোট: বর্তমান HD Upscale হলো সাধারণ রিসাইজ; AI সুপার-রেজোলিউশন ও ভিডিও রেন্ডারার এখনো যুক্ত হয়নি।", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FeatureTile(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2140)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Mint, modifier = Modifier.size(28.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun upscaleImage(context: android.content.Context, uri: Uri): String? {
    return try {
        val source = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return null
        val targetW = 1920
        val targetH = (source.height.toFloat() * targetW / source.width).toInt().coerceAtLeast(1)
        val output = Bitmap.createScaledBitmap(source, targetW, targetH, true)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "PixelPro_HD_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PixelProStudio")
        }
        val outUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        context.contentResolver.openOutputStream(outUri)?.use { output.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        source.recycle(); output.recycle()
        "HD রিসাইজ সম্পন্ন। গ্যালারির Pictures/PixelProStudio ফোল্ডারে সেভ হয়েছে। এটি AI detail recovery নয়।"
    } catch (_: Exception) { null }
}
