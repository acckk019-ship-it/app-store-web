package com.example.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

enum class AppColorTheme(val displayName: String, val primaryColor: Color) {
    GOLDEN_GREEN("أخضر ذهبي (الروضة الشريفة)", Color(0xFFE5C158)),
    GOLDEN_BLUE("أزرق ذهبي (السموات العلا)", Color(0xFFDFC27D)),
    CLASSIC_DARK("الكلاسيكي (اللافندر والبنفسج)", Color(0xFFD0BCFF)),
    VERITAS("ياقوت الصدق (الصدق البرنسي)", Color(0xFFE8C880)),
    OLD_EMERALD("الزمرد العتيق (القديم الوقور)", Color(0xFFC5A85C))
}

// Private backing states for dynamic theme switching
private val _emeraldDark = mutableStateOf(Color(0xFF071C0F))      // Defaults to Golden Green primary
private val _emeraldMedium = mutableStateOf(Color(0xFF0F3820))
private val _emeraldLight = mutableStateOf(Color(0xFF164E2E))
private val _emeraldSoft = mutableStateOf(Color(0xFF1E633C))

private val _goldClassic = mutableStateOf(Color(0xFFFFD700))
private val _goldLight = mutableStateOf(Color(0xFFFFF2B2))
private val _goldDark = mutableStateOf(Color(0xFF4A3B00))
private val _goldAccent = mutableStateOf(Color(0xFFECC242))

private val _ivoryWhite = mutableStateOf(Color(0xFFF5F2EB))
private val _textGreen = mutableStateOf(Color(0xFFE8F3EB))
private val _secondaryText = mutableStateOf(Color(0xFF9BAB9F))
private val _cardBackground = mutableStateOf(Color(0xFF0F3820))
private val _navigationBackground = mutableStateOf(Color(0xFF0D331D))
private val _goldRipple = mutableStateOf(Color(0x33FFD700))

// Public properties using custom getters to read from the Compose State
val EmeraldDark: Color get() = _emeraldDark.value
val EmeraldMedium: Color get() = _emeraldMedium.value
val EmeraldLight: Color get() = _emeraldLight.value
val EmeraldSoft: Color get() = _emeraldSoft.value

val GoldClassic: Color get() = _goldClassic.value
val GoldLight: Color get() = _goldLight.value
val GoldDark: Color get() = _goldDark.value
val GoldAccent: Color get() = _goldAccent.value

val IvoryWhite: Color get() = _ivoryWhite.value
val TextGreen: Color get() = _textGreen.value
val SecondaryText: Color get() = _secondaryText.value
val CardBackground: Color get() = _cardBackground.value
val NavigationBackground: Color get() = _navigationBackground.value
val GoldRipple: Color get() = _goldRipple.value

/**
 * Updates all theme state values reactively
 */
fun updateAppTheme(theme: AppColorTheme) {
    when (theme) {
        AppColorTheme.GOLDEN_GREEN -> {
            _emeraldDark.value = Color(0xFF05180C)      // Deep forest green
            _emeraldMedium.value = Color(0xFF0E2F1A)    // Elegant card background green
            _emeraldLight.value = Color(0xFF144024)     // Elevated green
            _emeraldSoft.value = Color(0xFF1B5130)      // Active green accent
            
            _goldClassic.value = Color(0xFFE5C158)      // Rich Golden accent 
            _goldLight.value = Color(0xFFFFF2B2)        // Soft sand highlight 
            _goldDark.value = Color(0xFF3B2E0B)         // Deep dark gold text
            _goldAccent.value = Color(0xFFDFC27D)       // Warm gold
            
            _ivoryWhite.value = Color(0xFFF5F3EB)       // Warm parchment text
            _textGreen.value = Color(0xFFE2EFE5)        // Soft light green hadith container text
            _secondaryText.value = Color(0xFF8B9D90)     // Muted green-gray 
            _cardBackground.value = Color(0xFF0E2F1A)
            _navigationBackground.value = Color(0xFF081C10)
            _goldRipple.value = Color(0x33E5C158)
        }
        AppColorTheme.GOLDEN_BLUE -> {
            _emeraldDark.value = Color(0xFF081220)      // Deep Ocean Night Blue
            _emeraldMedium.value = Color(0xFF101E33)    // Royal Islamic Blue
            _emeraldLight.value = Color(0xFF162A45)     // Elevated blue
            _emeraldSoft.value = Color(0xFF223E61)      // Slate blue container
            
            _goldClassic.value = Color(0xFFE5C158)      // Beautiful Sand Gold
            _goldLight.value = Color(0xFFF7E1A6)        // Cream light gold
            _goldDark.value = Color(0xFF332A0D)         // Bronze color
            _goldAccent.value = Color(0xFFDFC27D)       // Celestial gold
            
            _ivoryWhite.value = Color(0xFFEBF0F6)       // Cool White text
            _textGreen.value = Color(0xFFD4E3F4)        // Celestial light blue hadith text
            _secondaryText.value = Color(0xFF8394AC)     // Soft blue-gray text
            _cardBackground.value = Color(0xFF101E33)
            _navigationBackground.value = Color(0xFF060D17)
            _goldRipple.value = Color(0x33E5C158)
        }
        AppColorTheme.CLASSIC_DARK -> {
            _emeraldDark.value = Color(0xFF1C1B1F)      // Deep Slate Background
            _emeraldMedium.value = Color(0xFF2B2930)    // Tonal Card/Surface Background
            _emeraldLight.value = Color(0xFF323038)     // Tonal Elevation surface
            _emeraldSoft.value = Color(0xFF4A4458)      // Deep Indigo-Gray container
            
            _goldClassic.value = Color(0xFFD0BCFF)      // Vibrant Lavender Accent (Primary)
            _goldLight.value = Color(0xFFE8DEF8)        // Soft Lavender highlight 
            _goldDark.value = Color(0xFF381E72)         // Deep Velvet Purple 
            _goldAccent.value = Color(0xFFBEA6FF)       // Glow Lavender highlight
            
            _ivoryWhite.value = Color(0xFFE6E1E5)       // Main Off-White Text
            _textGreen.value = Color(0xFFF2ECEF)        // Readable light cream-highlight
            _secondaryText.value = Color(0xFF938F99)    // Muted Gray-Purple text
            _cardBackground.value = Color(0xFF2B2930)
            _navigationBackground.value = Color(0xFF2B2930)
            _goldRipple.value = Color(0x33D0BCFF)
        }
        AppColorTheme.VERITAS -> {
            _emeraldDark.value = Color(0xFF1E0E12)      // Deep Veritas Ruby Black
            _emeraldMedium.value = Color(0xFF2E171C)    // Velvet Burgundy
            _emeraldLight.value = Color(0xFF3E2228)     // Deep Rouge Card
            _emeraldSoft.value = Color(0xFF553239)      // Dusty pink-burgundy container
            
            _goldClassic.value = Color(0xFFE8C880)      // Noble Pure Gold
            _goldLight.value = Color(0xFFFCEECC)        // Warm champagne
            _goldDark.value = Color(0xFF452F0C)         // Dark bronze
            _goldAccent.value = Color(0xFFD4B165)       // Warm golden veritas accent
            
            _ivoryWhite.value = Color(0xFFFBF2F3)       // Rosy warm text
            _textGreen.value = Color(0xFFFCEDE0)        // Soft linen text container
            _secondaryText.value = Color(0xFFA68D90)     // Soft brick-gray
            _cardBackground.value = Color(0xFF2E171C)
            _navigationBackground.value = Color(0xFF13090B)
            _goldRipple.value = Color(0x33E8C880)
        }
        AppColorTheme.OLD_EMERALD -> {
            _emeraldDark.value = Color(0xFF0F1B15)      // Classic Emerald/Islamic Dark Canvas 
            _emeraldMedium.value = Color(0xFF1B2E24)    // Traditional Mosque Deep Emerald
            _emeraldLight.value = Color(0xFF264032)     // Tonal moss
            _emeraldSoft.value = Color(0xFF335442)      // Active green highlight
            
            _goldClassic.value = Color(0xFFC5A85C)      // Ancient Patina Gold
            _goldLight.value = Color(0xFFE6D6B0)        // Vintage parchment
            _goldDark.value = Color(0xFF2A2109)         // Rich traditional oil bronze
            _goldAccent.value = Color(0xFFD9BF77)       // Royal palace gold
            
            _ivoryWhite.value = Color(0xFFF4F0E6)       // Antique Manuscript Ivory text
            _textGreen.value = Color(0xFFECF5EE)        // Tinted green study container text
            _secondaryText.value = Color(0xFF829488)     // Historic sage text color
            _cardBackground.value = Color(0xFF1B2E24)
            _navigationBackground.value = Color(0xFF13231B)
            _goldRipple.value = Color(0x33C5A85C)
        }
    }
}
