package com.example.pokedex.common.data.remote.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.palette.graphics.Target


import com.example.pokedex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.round

class CommonFunctions {
    suspend fun getDominantColorFromImage(
        context: Context,
        imageUrl: String?,
        index: Int = 1,
        target: Int = 1,
        alpha: Double = 1.0,
    ): Pair<Color?, Color?> {
        return withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader(context)
            val request = if (imageUrl?.takeLast(3) == "svg") {
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .decoderFactory(SvgDecoder.Factory()) // SVG usa decoder específico
                    .build()
            } else {
                ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
            }

//            model = ImageRequest.Builder(LocalContext.current).data(imageUrl).decoderFactory(SvgDecoder.Factory()).build()
            return@withContext try {
                val result = (imageLoader.execute(request) as SuccessResult).drawable
                if (result is BitmapDrawable) {
                    val bitmap = result.bitmap
                    // Verifica se o bitmap é do tipo HARDWARE e converte se necessário
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val stdBlackColor = Color.Black.toArgb()
                    val stdWhiteColor = Color.White.toArgb()
                    val palette = Palette.from(mutableBitmap).generate()

                    val targets = arrayOf(
                        Target.LIGHT_VIBRANT,
                        Target.VIBRANT,
                        Target.MUTED,
                        Target.DARK_MUTED,
                        Target.DARK_VIBRANT
                    )

                    val selectedColor: Int = when (index) {
                        1 -> palette.getDominantColor(stdBlackColor)
                        2 -> palette.getVibrantColor(stdBlackColor)
                        3 -> palette.getMutedColor(stdBlackColor)
                        4 -> palette.getDarkMutedColor(stdBlackColor)
                        5 -> palette.getDarkVibrantColor(stdBlackColor)
                        6 -> palette.getLightMutedColor(stdBlackColor)
                        7 -> palette.getLightVibrantColor(stdBlackColor)
                        8 -> palette.getColorForTarget(targets[target - 1], stdBlackColor)
                        9 -> palette.getSwatchForTarget(targets[target - 1])?.rgb ?: stdBlackColor
                        else -> stdBlackColor // Retorna a cor padrão caso o índice não seja válido
                    }

                    val colorWithAlpha = Color(selectedColor).copy(alpha.toFloat())
                    val backgroundColorLuminance = calculateLuminance(colorWithAlpha)
                    var textColorAlphared = if (backgroundColorLuminance > 0.5) {
                        Color(palette.getSwatchForTarget(targets[5 - 1])?.rgb ?: stdBlackColor)
                    } else {
                        Color(palette.getSwatchForTarget(targets[1 - 1])?.rgb ?: stdWhiteColor)
                    }
                    if (colorWithAlpha == textColorAlphared) {
                        textColorAlphared = Color.Black
                    }
//                    textColorAlphared = Color.Black
                    Pair(colorWithAlpha, textColorAlphared)

                } else {
                    Pair(null, null)
                }
            } catch (e: Exception) {
                Log.e("PokeListViewModel", "Erro ao obter cor da imagem: ${e.message}")
                Pair(null, null)
            }
        }
    }

    private fun getNegativeColor(color: Color): Color {
        val red = 255 - (color.red * 255).toInt()
        val green = 255 - (color.green * 255).toInt()
        val blue = 255 - (color.blue * 255).toInt()

        return Color(red / 255f, green / 255f, blue / 255f, color.alpha)
    }

    fun getRandomPokeImg(pokeId: Int): String {
        val urls = listOf(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/${pokeId}.svg",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokeId}.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/${pokeId}.png"
        )
        return urls.random()
    }

    fun getTypeColor(type: String, context: Context): Color {
        return when (type) {
            "fire" -> Color(ContextCompat.getColor(context, R.color.type_fire))
            "water" -> Color(ContextCompat.getColor(context, R.color.type_water))
            "grass" -> Color(ContextCompat.getColor(context, R.color.type_grass))
            "electric" -> Color(ContextCompat.getColor(context, R.color.type_electric))
            "ice" -> Color(ContextCompat.getColor(context, R.color.type_ice))
            "fighting" -> Color(ContextCompat.getColor(context, R.color.type_fighting))
            "poison" -> Color(ContextCompat.getColor(context, R.color.type_poison))
            "ground" -> Color(ContextCompat.getColor(context, R.color.type_ground))
            "flying" -> Color(ContextCompat.getColor(context, R.color.type_flying))
            "psychic" -> Color(ContextCompat.getColor(context, R.color.type_psychic))
            "bug" -> Color(ContextCompat.getColor(context, R.color.type_bug))
            "rock" -> Color(ContextCompat.getColor(context, R.color.type_rock))
            "ghost" -> Color(ContextCompat.getColor(context, R.color.type_ghost))
            "dragon" -> Color(ContextCompat.getColor(context, R.color.type_dragon))
            "dark" -> Color(ContextCompat.getColor(context, R.color.type_dark))
            "steel" -> Color(ContextCompat.getColor(context, R.color.type_steel))
            "fairy" -> Color(ContextCompat.getColor(context, R.color.type_fairy))
            else -> Color(ContextCompat.getColor(context, R.color.type_normal))
        }
    }

    fun getBarColor(barName: String, context: Context): Color {
        return when (barName) {
            "HP" -> Color(ContextCompat.getColor(context, R.color.hp_bar))
            "ATK" -> Color(ContextCompat.getColor(context, R.color.atk_bar))
            "DEF" -> Color(ContextCompat.getColor(context, R.color.def_bar))
            "XP" -> Color(ContextCompat.getColor(context, R.color.exp_bar))
            "SPD" -> Color(ContextCompat.getColor(context, R.color.spd_bar))
            else -> Color.White
        }
    }

    fun getColorByName(colorName: String): Color {
        return when (colorName.lowercase()) {
            "blue" -> Color(0x731E88E5)   // 45% opacidade
            "brown" -> Color(0x738D6E63)  // 45% opacidade
            "gray" -> Color(0x739E9E9E)   // 45% opacidade
            "green" -> Color(0x7366BB6A)  // 45% opacidade
            "pink" -> Color(0x73F48FB1)    // 45% opacidade
            "purple" -> Color(0x73AB47BC)  // 45% opacidade
            "red" -> Color(0x73E53935)     // 45% opacidade
            "white" -> Color(0x73FAFAFA)   // 45% opacidade
            "yellow" -> Color(0x73FFEB3B)  // 45% opacidade
            "hp_bar" -> Color(0xFF368F3B)  // 45% opacidade
            "atk_bar" -> Color(0xFFD83948) // 45% opacidade
            "def_bar" -> Color(0xFF0292F2) // 45% opacidade
            "exp_bar" -> Color(0xFFFFA527)  // 45% opacidade
            "spd_bar" -> Color(0xFF8DAFC8)  // 45% opacidade
            "type_normal" -> Color(0xFFA8A77A) // 45% opacidade
            "type_fire" -> Color(0xFFFF7F27) // 45% opacidade
            "type_water" -> Color(0xFF6390F0) // 45% opacidade
            "type_grass" -> Color(0xFF7AC74C) // 45% opacidade
            "type_electric" -> Color(0xFFF7D02C) // 45% opacidade
            "type_ice" -> Color(0xFF96D9D6) // 45% opacidade
            "type_fighting" -> Color(0xFFC22E28) // 45% opacidade
            "type_poison" -> Color(0xFFA33EA1) // 45% opacidade
            "type_ground" -> Color(0xFFE2BF65) // 45% opacidade
            "type_flying" -> Color(0xFFA98FF3) // 45% opacidade
            "type_psychic" -> Color(0xFFF95587) // 45% opacidade
            "type_bug" -> Color(0xFFA6B91A) // 45% opacidade
            "type_rock" -> Color(0xFFB6A136) // 45% opacidade
            "type_ghost" -> Color(0xFF735797) // 45% opacidade
            "type_dragon" -> Color(0xFF6F35FC) // 45% opacidade
            "type_dark" -> Color(0xFF705746) // 45% opacidade
            "type_steel" -> Color(0xFFB7B7CE) // 45% opacidade
            "type_fairy" -> Color(0xFFD685AD) // 45% opacidade
            else -> Color.Black // Default
        }
    }

    private fun calculateLuminance(color: Color): Double {
        // Converte os componentes da cor para o intervalo de 0 a 1
        val r = color.red
        val g = color.green
        val b = color.blue

        // Aplica a transformação para cada componente
        val rLuminance = if (r <= 0.03928) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
        val gLuminance = if (g <= 0.03928) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
        val bLuminance = if (b <= 0.03928) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

        // Calcula a luminância relativa (percebida)
        return round((0.2126 * rLuminance + 0.7152 * gLuminance + 0.0722 * bLuminance) * 100) / 100.0
    }
}