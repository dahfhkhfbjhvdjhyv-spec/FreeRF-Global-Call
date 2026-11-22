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
  setContent { PixelProScreen() }
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PixelProScreen() {
 val context = androidx.compose.ui.platform.LocalContext.current
 val scope = rememberCoroutineScope()
 var photo by remember { mutableStateOf<Uri?>(null) }
 val clips = remember { mutableStateListOf<Uri>() }
 var status by remember { mutableStateOf("আপনার সৃজনশীল কাজ শুরু করুন") }
 var busy by remember { mutableStateOf(false) }
 var enhancePreset by remember { mutableStateOf("Balanced") }
 val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { photo = it; status = "ছবি নির্বাচন করা হয়েছে" } }
 val pickVideos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { list ->
  clips.clear(); clips.addAll(list); status = "${list.size}টি ভিডিও ক্লিপ নির্বাচন করা হয়েছে"
 }
 MaterialTheme(colorScheme = darkColorScheme(primary = Violet, secondary = Mint, background = Ink, surface = Color(0xFF1B2140))) {
  Scaffold(containerColor = Ink, topBar = {
   TopAppBar(title = { Column { Text("PixelPro Studio", fontWeight = FontWeight.Bold); Text("Create beyond limits", color = Mint, style = MaterialTheme.typography.labelSmall) } },
    navigationIcon = { Icon(Icons.Default.AutoAwesome, null, tint = Mint, modifier = Modifier.padding(start = 16.dp)) },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink, titleContentColor = Color.White))
  }) { pad ->
   LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
    item {
     Column(Modifier.fillMaxWidth().background(Violet, RoundedCornerShape(22.dp)).padding(20.dp)) {
      Text("Make every frame shine", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      Text("Photo • Video • AI tools", color = Mint)
      Text("এক অ্যাপেই আপনার সব এডিটিং", color = Color.White)
     }
    }
    item { Text("ফটো এডিটিং", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
    item {
     Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Tile("ছবি বাছাই", "গ্যালারি থেকে", Icons.Default.AddPhotoAlternate, Modifier.weight(1f)) { pickPhoto.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) }
      Tile("Photo Enhance", "HD + Sharpen + Denoise", Icons.Default.HighQuality, Modifier.weight(1f)) {
       val chosen = photo
       if (chosen == null) status = "আগে একটি ছবি বাছাই করুন" else {
        busy = true
        val selectedPreset = enhancePreset
        scope.launch { status = withContext(Dispatchers.IO) { enhanceAndSave(context, chosen, selectedPreset) } ?: "ছবি প্রসেস করা যায়নি"; busy = false }
       }
      }
     }
    }
    item {
     Text("Enhancement Preset", color = Mint, fontWeight = FontWeight.SemiBold)
     Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Natural", "Balanced", "Detail").forEach { preset ->
       FilterChip(selected = enhancePreset == preset, onClick = { enhancePreset = preset }, label = { Text(preset) })
      }
     }
    }
    item { photo?.let { AsyncImage(model = it, contentDescription = "নির্বাচিত ছবি", modifier = Modifier.fillMaxWidth().height(210.dp)) } }
    item { Text("ভিডিও স্টুডিও", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
    item { Tile("একাধিক ভিডিও যোগ করুন", "২০টি পর্যন্ত ক্লিপ বাছাই", Icons.Default.VideoLibrary, Modifier.fillMaxWidth()) { pickVideos.launch(ActivityResultContracts.PickVisualMedia.VideoOnly) } }
    item { Text("টাইমলাইন · ${clips.size}টি ক্লিপ", color = Mint, fontWeight = FontWeight.SemiBold) }
    itemsIndexed(clips) { index, uri ->
     Row(Modifier.fillMaxWidth().background(Color(0xFF1B2140), RoundedCornerShape(12.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Movie, null, tint = Mint)
      Text("ভিডিও ক্লিপ ${index + 1}", color = Color.White, modifier = Modifier.weight(1f).padding(start = 10.dp))
      IconButton(onClick = { clips.remove(uri) }) { Icon(Icons.Default.Close, null, tint = Color.LightGray) }
     }
    }
    item {
     Button(onClick = { status = if (clips.size < 2) "ভিডিও জুড়তে অন্তত ২টি ক্লিপ বাছাই করুন" else "ক্লিপ টাইমলাইনে প্রস্তুত। ভিডিও রেন্ডার/এক্সপোর্ট ইঞ্জিন এখনো তৈরি হচ্ছে।" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
      Icon(Icons.Default.MergeType, null); Spacer(Modifier.width(8.dp)); Text("ভিডিওগুলো জুড়ে দিন")
     }
    }
    item {
     Text("আরও এডিটিং ফিচার", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
     Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Non-AI Sharpen / Deblur Filters", "Noise Reduction ও Color Enhance", "ভিডিও কাট / ট্রিম / Split", "Transition ও একাধিক ক্লিপ জোড়া", "Background Remove", "Stabilization ও Color Grading", "সাবটাইটেল, টেক্সট ও অডিও", "AI ভিডিও জেনারেশন", "4K Export (ডিভাইস সাপোর্ট সাপেক্ষে)").forEach { label ->
       Row(Modifier.fillMaxWidth().background(Color(0xFF1B2140), RoundedCornerShape(12.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AutoFixHigh, null, tint = Mint); Text(label, color = Color.White, modifier = Modifier.padding(start = 10.dp))
       }
      }
     }
    }
    item {
     if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Mint)
     Text(status, color = Mint)
     Text("Photo Enhancement Engine AI ছাড়া median denoise, unsharp sharpening, contrast/color tuning ও 1920px upscale ব্যবহার করে। হারানো আসল ডিটেইল ফিরিয়ে আনতে পারে না। ভিডিও রেন্ডারিং ও AI ভিডিও এখনো কার্যকর করা হয়নি।", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
    }
   }
  }
 }
}

@Composable
private fun Tile(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
 Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2140))) {
  Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
   Icon(icon, null, tint = Mint, modifier = Modifier.size(27.dp))
   Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
   Text(detail, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
  }
 }
}

private fun enhanceAndSave(context: android.content.Context, uri: Uri, preset: String): String? {
 return try {
  val original = decodeForEnhancement(context, uri) ?: return null
  val settings = when (preset) {
   "Natural" -> PhotoEnhancementEngine.Settings(denoise = 1, sharpen = 0.35f, contrast = 1.02f, saturation = 1.02f)
   "Detail" -> PhotoEnhancementEngine.Settings(denoise = 1, sharpen = 1.05f, contrast = 1.10f, saturation = 1.05f)
   else -> PhotoEnhancementEngine.Settings()
  }
  val enhanced = PhotoEnhancementEngine.enhance(original, settings)
  val values = ContentValues().apply {
   put(MediaStore.Images.Media.DISPLAY_NAME, "PixelPro_Enhanced_${System.currentTimeMillis()}.jpg")
   put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
   put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PixelProStudio")
  }
  val dest = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
  context.contentResolver.openOutputStream(dest)?.use { enhanced.compress(Bitmap.CompressFormat.JPEG, 96, it) }
  original.recycle(); enhanced.recycle()
  "Photo Enhancement সম্পন্ন ($preset): denoise + sharpen + color correction + 1920px upscale। Pictures/PixelProStudio-তে সেভ হয়েছে (AI নয়)।"
 } catch (_: Exception) { null }
}

private fun decodeForEnhancement(context: android.content.Context, uri: Uri): Bitmap? {
 val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
 context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
 var sample = 1
 while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 3000) sample *= 2
 val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
 return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}
