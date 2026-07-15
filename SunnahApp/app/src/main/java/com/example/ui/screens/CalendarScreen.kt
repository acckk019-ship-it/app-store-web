package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SunnahViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// Data class representing an Islamic sacred event/occasion
data class IslamicOccasion(
    val title: String,
    val islamicDate: String,
    val description: String,
    val virtue: String,
    val bestDeeds: List<String>,
    val tag: String, // "annual", "periodic", "months"
    val iconName: String = ""
)

// Data class representing Gregorian month overlap
data class GregorianMonthData(
    val nameEn: String,
    val nameAr: String,
    val overlappingHijri: String,
    val totalDays: Int,
    val startDayOfWeek: Int, // 0 for Sunday, 1 for Monday, etc.
    val prominentDays: Map<Int, String> // Map of day to event description
)

// Data class representing Hijri month overlap
data class HijriMonthData(
    val nameAr: String,
    val nameEn: String,
    val overlappingGregorian: String,
    val totalDays: Int, // usually 29 or 30
    val prominentDays: Map<Int, String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: SunnahViewModel,
    modifier: Modifier = Modifier
) {
    var primaryTabState by remember { mutableStateOf(0) } // 0 = Calendar view, 1 = Rich Occasions list
    var calendarModeIsGregorian by remember { mutableStateOf(true) } // True = Gregorian view with Hijri subtext, False = Hijri view with Gregorian subtext
    var selectedYear by remember { mutableStateOf(2026) }
    
    // Zoom detail state for calendar clicks
    var activeMonthDetailIndex by remember { mutableStateOf<Int?>(null) }
    var activeDetailDayNum by remember { mutableStateOf<Int?>(null) }
    var activeDetailDayText by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 2026 accurate overlapping Hijri values
    val gregorianMonths2026 = listOf(
        GregorianMonthData("JANUARY", "يناير", "رجب - ١٢ شعبان ١٤٤٧ هـ", 31, 4, mapOf(13 to "الأيام البيض (١٣ رجب)", 14 to "الأيام البيض (١٤ رجب)", 15 to "الأيام البيض (١٥ رجب)", 27 to "ذكرى الإسراء والمعراج المباركة (على القول الأشهر)")),
        GregorianMonthData("FEBRUARY", "فبراير", "١٣ شعبان - ١١ رمضان ١٤٤٧ هـ", 28, 0, mapOf(1 to "الأيام البيض (١٣ شعبان)", 2 to "الأيام البيض (١٤ شعبان)", 3 to "ليلة النصف من شعبان المباركة", 18 to "غرة شهر رمضان المبارك ١٤٤٧ هـ")),
        GregorianMonthData("MARCH", "مارس", "١٢ رمضان - ١٢ شوال ١٤٤٧ هـ", 31, 0, mapOf(1 to "الأيام البيض (١٣ رمضان)", 2 to "الأيام البيض (١٤ رمضان)", 3 to "الأيام البيض (١٥ رمضان)", 10 to "ذكرى غزوة بدر الكبرى (١٧ رمضان)", 15 to "فتح مكة المكرمة (٢٠ رمضان)", 28 to "ليلة القدر المباركة (وتريات العشر الأواخر)", 20 to "غرة شهر شوال وعيد الفطر السعيد")),
        GregorianMonthData("APRIL", "أبريل", "١٣ شوال - ١٢ ذو القعدة ١٤٤٧ هـ", 30, 3, mapOf(1 to "الأيام البيض (١٣ شوال)", 2 to "الأيام البيض (١٤ شوال)", 3 to "الأيام البيض (١٥ شوال)", 19 to "غرة شهر ذي القعدة الحرام")),
        GregorianMonthData("MAY", "مايو", "١٣ ذو القعدة - ١٤ ذو الحجة ١٤٤٧ هـ", 31, 5, mapOf(1 to "الأيام البيض (١٣ ذو القعدة)", 2 to "الأيام البيض (١٤ ذو القعدة)", 3 to "الأيام البيض (١٥ ذو القعدة)", 18 to "غرة ذو الحجة - بدء العشر الأوائل المباركة", 27 to "يوم عرفة المعظم (٩ ذو الحجة)", 28 to "يوم النحر وعيد الأضحى المبارك")),
        GregorianMonthData("JUNE", "يونيو", "١٥ ذو الحجة ١٤٤٧ - ١٥ محرم ١٤٤٨ هـ", 30, 1, mapOf(17 to "غرة محرم الحرام - رأس السنة الهجرية ١٤٤٨ هـ", 25 to "يوم تاسوعاء (٩ محرم)", 26 to "يوم عاشوراء العظيم (١٠ محرم)")),
        GregorianMonthData("JULY", "يوليو", "١٦ محرم - ١٦ صفر ١٤٤٨ هـ", 31, 3, mapOf(1 to "رابع أيام التشريق وعتبة النوافل", 29 to "الأيام البيض (١٣ صفر)", 30 to "الأيام البيض (١٤ صفر)", 31 to "الأيام البيض (١٥ صفر)")),
        GregorianMonthData("AUGUST", "أغسطس", "١٧ صفر - ١٨ ربيع الأول ١٤٤٨ هـ", 31, 6, mapOf(16 to "غرة شهر ربيع الأول الأنور", 27 to "ذكرى المولد النبوي الشريف (١٢ ربيع الأول)", 28 to "الأيام البيض (١٣ ربيع الأول)")),
        GregorianMonthData("SEPTEMBER", "سبتمبر", "١٩ ربيع الأول - ١٨ ربيع الآخر ١٤٤٨ هـ", 30, 2, mapOf(14 to "غرة شهر ربيع الآخر", 26 to "الأيام البيض (١٣ ربيع الآخر)", 27 to "الأيام البيض (١٤ ربيع الآخر)", 28 to "الأيام البيض (١٥ ربيع الآخر)")),
        GregorianMonthData("OCTOBER", "أكتوبر", "١٩ ربيع الآخر - ٢٠ جمادى الأولى ١٤٤٨ هـ", 31, 4, mapOf(13 to "غرة شهر جمادى الأولى", 25 to "الأيام البيض (١٣ جمادى الأولى)", 26 to "الأيام البيض (١٤ جمادى الأولى)", 27 to "الأيام البيض (١٥ جمادى الأولى)")),
        GregorianMonthData("NOVEMBER", "نوفمبر", "٢١ جمادى الأولى - ٢٠ جمادى الآخرة ١٤٤٨ هـ", 30, 0, mapOf(12 to "غرة شهر جمادى الآخرة", 24 to "الأيام البيض (١٣ جمادى الآخرة)", 25 to "الأيام البيض (١٤ جمادى الآخرة)", 26 to "الأيام البيض (١٥ جمادى الآخرة)")),
        GregorianMonthData("DECEMBER", "ديسمبر", "٢١ جمادى الآخرة ١٤٤٨ - ٢٢ رجب ١٤٤٨ هـ", 31, 2, mapOf(11 to "غرة شهر رجب الحرام ١٤٤٨ هـ", 23 to "الأيام البيض (١٣ رجب الحرام)", 24 to "الأيام البيض (١٤ رجب الحرام)", 25 to "الأيام البيض (١٥ رجب الحرام)"))
    )

    // Accurate overviews for Hijri Lunar Months (1447-1448 AH)
    val hijriMonthsData = listOf(
        HijriMonthData("المحرم (الحرام)", "MUHARRAM", "يونيو - يوليو ٢٠٢٦ م", 30, mapOf(9 to "يوم تاسوعاء (سنة صيام لتخالف اليهود)", 10 to "يوم عاشوراء (عظيم يكفر السنة الماضية)", 13 to "أول الأيام البيض المضيئة", 14 to "ثاني الأيام البيض المضيئة", 15 to "ثالث الأيام البيض المضيئة")),
        HijriMonthData("صفر", "SAFAR", "يوليو - أغسطس ٢٠٢٦ م", 29, mapOf(13 to "البيضاء الأولى", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("ربيع الأول", "RABI AL-AWWAL", "أغسطس - سبتمبر ٢٠٢٦ م", 30, mapOf(12 to "المولد النبوي الشريف والاستبشار بسيرته العطرة", 13 to "البيضاء الأولى", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("ربيع الآخر", "RABI AL-AKHIR", "سبتمبر - أكتوبر ٢٠٢٦ م", 29, mapOf(13 to "البيضاء الأولى", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("جمادى الأولى", "JUMADA AL-AWWAL", "أكتوبر - نوفمبر ٢٠٢٦ م", 30, mapOf(13 to "البيضاء الأولى", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("جمادى الآخرة", "JUMADA AL-AKHIR", "نوفمبر - ديسمبر ٢٠٢٦ م", 29, mapOf(13 to "البيضاء الأولى", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("رجب (الحرام)", "RAJAB", "ديسمبر ٢٠٢٦ - يناير ٢٠٢٧ م", 30, mapOf(1 to "أول ليلة يستجاب فيها الدعاء", 13 to "الأيام البيض الحرام", 14 to "الأيام البيض الحرام", 15 to "الأيام البيض الحرام", 27 to "ليلة الإسراء والمعراج المعظمة")),
        HijriMonthData("شعبان", "SHA'BAN", "يناير - فبراير ٢٠٢٦ م", 29, mapOf(3 to "ليلة النصف من شعبان (مغفرة الذنوب للقلوب السليمة)", 13 to "الأيام البيض لشعبان", 14 to "الأيام البيض لشعبان", 15 to "الأيام البيض لشعبان", 29 to "ترقب هلال شهر الطاعة رمضان المبارك")),
        HijriMonthData("رمضان (المعظم)", "RAMADAN", "فبراير - مارس ٢٠٢٦ م", 30, mapOf(1 to "أول أيام الصيام الميمون", 17 to "ذكرى غزوة بدر الكبرى", 20 to "ذكرى فتح مكة الأعظم", 21 to "ليلة ٢١ - بدء وتر العشر الأواخر والتهجد", 23 to "ليلة ٢٣ رمضان المبارك ليلة القدر المرتقبة", 25 to "ليلة ٢٥ رمضان العتق والدعاء", 27 to "ليلة ٢٧ ليلة قدر تحرى باليقين", 29 to "ليلة ٢٩ آخر أوتار رمضان")),
        HijriMonthData("شوال", "SHAWWAL", "مارس - أبريل ٢٠٢٦ م", 29, mapOf(1 to "يوم عيد الفطر السعيد المبهج وصيد جوائز العابدين", 2 to "بدء صيام الست من شوال (تعدل الدهر)", 13 to "البيضاء الأولى ممتزجة بالست المندوبة", 14 to "البيضاء الثانية", 15 to "البيضاء الثالثة")),
        HijriMonthData("ذو القعدة (الحرام)", "DHU AL-QI'DAH", "أبريل - مايو ٢٠٢٦ م", 30, mapOf(13 to "الأيام البيض بالأشهر الحرم", 14 to "الأيام البيض بالأشهر الحرم", 15 to "الأيام البيض بالأشهر الحرم")),
        HijriMonthData("ذو الحجة (الحرام)", "DHU AL-HIJJAH", "مايو - يونيو ٢٠٢٦ م", 29, mapOf(1 to "بدء عشر ذي الحجة العظيمة (أحب الأيام لله)", 9 to "يوم عرفة المشهود (تكفير سنتين من الخطايا وصيام مبرور)", 10 to "يوم النحر الأكبر وعيد الأضحى والتقرب بالأضاحي", 11 to "يوم القر (التشريق الأول)", 12 to "يوم النفر الأول (التشريق الثاني)", 13 to "يوم النفر الثاني (التشريق الثالث)"))
    )

    // The legendary detailed database of sacred Muslim days (Virtues + Recommended Best Deeds)
    val importantOccasions = listOf(
        // Annual Peak Days
        IslamicOccasion(
            title = "شهر رمضان المبارك والفرقان",
            islamicDate = "الشهور الإسلامية العظمى (٩ رمضان)",
            description = "سيد الشهور، وفيه نزل القرآن الكريم هدى للناس، وفيه ليلة تجل عن ألف شهر في البركة والمغفرة والعتق الأبدي.",
            virtue = "تفتح أبواب الجنة وتغلق أبواب جهنم وتصفد الشياطين. خلوف فم الصائم أطيب عند الله من ريح المسك. مَنْ صَامَ رَمَضَانَ إِيمَانًا وَاحْتِسَابًا غُفِرَ لَهُ مَا تَقَدَّمَ مِنْ ذَنْبِهِ.",
            bestDeeds = listOf(
                "الالتزام بالصيام التام صوناً للجوارح واللسان عن اللغو والغيبة.",
                "إقامة صلاة التراويح والتهجد والخشوع الطويل في الركوع والسجود.",
                "ختم القرآن الكريم تلاوة وتدبراً وعرضاً على أهلك وصحبتك.",
                "الإكثار من الصدقات وتفطير الصائمين المحتاجين لتنال مثل أجرهم دون كلفة من أجورهم.",
                "الاعتكاف ولزوم المساجد في العشر الأواخر التماساً لليلة القدر العلية."
            ),
            tag = "annual",
            iconName = "🌙"
        ),
        IslamicOccasion(
            title = "عشر ذي الحجة المباركة",
            islamicDate = "من ١ إلى ١٠ ذي الحجة الحرام",
            description = "أعظم أيام الدنيا قاطبة وأحبها إلى الله سبحانه، اقسم الله بها في التنزيل لإظهار جلال شأنها.",
            virtue = "عن ابن عباس رضي الله عنهما قال: قال رسول الله ﷺ: «ما من أيام العمل الصالح فيها أحب إلى الله من هذه الأيام» يعني أيام العشر. قالوا: يا رسول الله، ولا الجهاد في سبيل الله؟ قال: «ولا الجهاد في سبيل الله، إلا رجل خرج بنفسه وماله، فلم يرجع من ذلك بشيء».",
            bestDeeds = listOf(
                "الإكثار والاتصال بالتكبير المطلق والمقيد (الله أكبر والحمد لله ولا إله إلا الله).",
                "صيام الأيام التسعة الأوائل وتخصيص الجهد للتعبد والانعزال عن شواغل الدنيا.",
                "الأضحية الشرعية تقرباً ومواساة للفقراء وبذلاً لله ذو النعمة والفضل.",
                "أداء فريضة الحج لمن استطاع إليه سبيلاً ليرجع كيوم ولدته أمه بلا ذنب.",
                "صلة الأرحام المنقطعة وبذل السلام والصفح الجميل عن كل من آذاك."
            ),
            tag = "annual",
            iconName = "🕋"
        ),
        IslamicOccasion(
            title = "يوم عرفة الأغر",
            islamicDate = "٩ ذي الحجة الحرام",
            description = "يوم كمال الدين وتمام النعمة على الأمة المحمدية، ويوم المباهاة الإلهية بعباده الواقفين في صعيد عرفات الطاهر.",
            virtue = "صيامه يكفر ذنوب السنتين الماضية والقابلة بإذن الله للمسلم المقيم. وهو اليوم الذي ما رؤي الشيطان فيه أصغر ولا أدحر ولا أحقر منه لكثرة ما يرى من تنزل الرأفة ومغفرة الخطايا العظام.",
            bestDeeds = listOf(
                "صيام هذا اليوم المبارك لغير الحاج تبركاً وتقرباً للغفور.",
                "ترديد الدعاء النبوي الجامع: «خير الدعاء دعاء يوم عرفة، وخير ما قلت أنا والنبيون من قبلي: لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير».",
                "الإلحاح بالدعاء الشامل لوالديك والذرية والشفاء والفرج وسداد الديون للمسلمين بعد صلاة العصر حتى غروب الشمس.",
                "التوبة النصوح والإقلاع المؤكد عن كل كبيرة أو مظلمة للعباد."
            ),
            tag = "annual",
            iconName = "🌄"
        ),
        IslamicOccasion(
            title = "ليلة القدر الشريفة",
            islamicDate = "أوتار العشر الأواخر من رمضان",
            description = "ليلة مقدسة عظمى تفوق في عبادتها وفضلها وثوابها ألف شهر من الأعوام الخالية من الطاعات والبر والاجتهاد.",
            virtue = "«مَنْ قَامَ لَيْلَةَ الْقَدْرِ إِيمَانًا وَاحْتِسَابًا غُفِرَ لَهُ مَا تَقَدَّمَ مِنْ ذَنْبِهِ»، وفيها تقدر مقادير العباد وأرزاقهم وآجالهم للعام القابل.",
            bestDeeds = listOf(
                "إحياء الليلة بأسرها بالصلاة والتهجد وصلاة الوتر بخشوع ودموع.",
                "ملازمة وإكثار الدعاء النبوي بتعليم الحبيب المصطفى لعائشة: «اللهم إنك عفو تحب العفو فاعف عني».",
                "التصدق الحثيث الخفي ولو بقمة شق تمرة أو مال يسير خالص لوجه الكريم لتكتب صدقة ألف شهر.",
                "قراءة وتدبر سورة القدر والملك وتفكر القلوب في جلال ليلة الملائكة والروح."
            ),
            tag = "annual",
            iconName = "✨"
        ),
        IslamicOccasion(
            title = "يوم عاشوراء ونجاة موسى عليه السلام",
            islamicDate = "١٠ المحرم الحرام",
            description = "يوم مشهود نجى الله عز وجل فيه نبي الله موسى وقومه وأغرق فرعون وجنوده الظالمين، فصامه موسى شكراً وصامه نبينا اتباعاً وشكراً.",
            virtue = "عن رسول الله ﷺ قال: «صيام يوم عاشوراء، أحتسب على الله أن يكفر السنة التي قبله». ويندب صيام يوم قبله لتفادي التشبه بغير المسلمين.",
            bestDeeds = listOf(
                "صيام اليوم التاسع (تاسوعاء) واليوم العاشر (عاشوراء) من شهر المحرم الحرام.",
                "مضاعفة الاستغفار والبكاء حزناً وندماً على خطايا السنة المنصرمة ليدخل العام الجديد مبرأ.",
                "التوسعة على الأهل والأبناء بالرزق والطعام فقد أثر فيه سعة الرزق طوال السنتين بموجب الأثر.",
                "الصدقة الكريمة والبر باليتامى والأرامل وكبار السن."
            ),
            tag = "annual",
            iconName = "🕌"
        ),
        // Periodic Days of Grace
        IslamicOccasion(
            title = "يوم الجمعة العيد الأسبوعي المبارك",
            islamicDate = "كل يوم جمعة أسبوعياً",
            description = "أفضل أيام الأسبوع، فيه خلق الله تعالى آدم، وفيه أدخل الجنة، وفيه أخرج منها، وفيه تقوم الساعة.",
            virtue = "فيه ساعة مخصصة للاستجابة لا يوافقها مسلم يسأل الله تعالى خيراً إلا استجاب لصالح وعاجل دعوته، والخطوات إليها تمحو الموبقات الكبيرة.",
            bestDeeds = listOf(
                "الاغتسال التام ولبس أحسن الثياب والتطيب والتبكير ل صلاة الجمعة والمشي الهادئ بسكينة.",
                "قراءة سورة الكهف الشريفة لتضيء لك من النور ما بين الجمعتين.",
                "مضاعفة كثرة الصلاة والسلام على النبي المصطفى ﷺ (مئة مرة أو تزيد من الجلال).",
                "التطوع بالدعاء والمكوث في المسجد تحرياً ودراسة لساعة الإجابة المباركة بين العصر والمغرب."
            ),
            tag = "periodic",
            iconName = "🕌"
        ),
        IslamicOccasion(
            title = "الأيام البيض الثلاثة المضيئة",
            islamicDate = "١٣، ١٤، ١٥ من كل شهر هجري",
            description = "الأيام المباركة التي يكتمل فيها قرص القمر كأبهى ما يكون بالبهاء والنور المنساب.",
            virtue = "صيام هذه الأيام الثلاثة من كل شهر قمري يعادل ويباهي صيام الدهر كاملاً (العمر كله)، لأن الحسنة عند الله بعشر أمثالها وصيامها تصفية لقلبك.",
            bestDeeds = listOf(
                "التحري الحريص لصيام الأيام الثلاثة دفعة واحدة شهرياً بلا تفريط.",
                "استغلال صفاء العقل والجسم للتأمل في آيات الخلق وجمال فلك السموات وقت اكتمال القمر.",
                "شكر الله على نعمة الصحة والاستطالة بالنوافل، وقضاء حوائج الضعفاء الإخوان."
            ),
            tag = "periodic",
            iconName = "🌕"
        ),
        IslamicOccasion(
            title = "صيام الاثنين والخميس النبوي",
            islamicDate = "كل اثنين وخميس من كل أسبوع",
            description = "اليومان المباركان المفضلان لدى رسول الله ﷺ لإظهار الطاعات والذل والفقر لله الحليم العليم.",
            virtue = "تُرفع فيهما الأعمال الطيبة والقبيحة إلى حضرة الله عز وجل، فقال نبينا ﷺ: «فأحب أن يعرض عملي وأنا صائم»، وفيهما يغفر لكل مسلم لم يشاحن أخاه.",
            bestDeeds = listOf(
                "المداومة على الصيام بهذين اليومين طلباً لرفعة الدرجات في صحائف رفع أعمالك.",
                "عقد الهدنة والصلح الشامل وكف الشحناء والخصومة والمحاسبة والمصالحة مع من هجرتهم لتفتح لك مغفرة الاثنين والخميس.",
                "تقديم صدقة خفية صبيحة يوم الاثنين والخميس والتمسك بكل ما تيسر من بر للوالدين."
            ),
            tag = "periodic",
            iconName = "📅"
        ),
        // Sacred Months and Virtues
        IslamicOccasion(
            title = "الأشهر الحُرُم الأربعة الحصينة",
            islamicDate = "ذو القعدة، ذو الحجة، المحرم، ورجب",
            description = "الأشهر الأربعة التي اختارها الله وعظم هيبتها وقدسيتها بين شهور السنة كلها، وحرم فيها العبث أو القتال وبدء الظلم والاعتداء.",
            virtue = "قال تعالى: «فَلَا تَظْلِمُوا فِيهِنَّ أَنفُسَكُمْ». المعصية والذنب والسيئة تعظم وتتضاعف عقوبتها في هذه الأشهر تعظيماً لحرمة ملك الله، وبالمقابل فإن الطاعة والصدقة والبر تزداد أجورها أضعافاً مضاعفة.",
            bestDeeds = listOf(
                "احذر أشد الحذر ذنوب الخلوات والتفريط في الفرائض وبذاءة اللسان مع الناس في هذه الفصول العلية.",
                "التوسع بصدقة السر وإغاثة الملهوف والمستجير ورعاية شؤون عائلة فقيرة من جيرانك.",
                "ملازمة صوم النوافل والاستزادة الدائمة من قراءة أوراد القرآن والهدوء النفسي العارم.",
                "الاستغفار المتواصل دبر كل صلاة لتنقية كتاب صحائفك من شوائب الظلم والآثام."
            ),
            tag = "months",
            iconName = "🛡️"
        ),
        IslamicOccasion(
            title = "فضيلة شهر شعبان المعظم",
            islamicDate = "الشهر الممهد لرمضان (٨ شعبان)",
            description = "الشهر الكريم الذي يغفل الناس عنه بين عظمتي رجب ورمضان، وهو معسكر الحبيب لترويض النفوس وتهيئتها للقاء الضيف الكريم رمضان.",
            virtue = "تُرفع فيه أعمال العام كله إلى رب العالمين وكان أكثر شهر يستديم المصطفى صيامه حتى يقترب من تمامه ترويضاً وطاعة.",
            bestDeeds = listOf(
                "صيام أغلب أيام شعبان تذليلاً للبدن والنفس على الصيام الطويل القادم بلا مشقة.",
                "الصلح المبكر مع الأقارب والشركاء حتى تستجلب مغفرة ليلة النصف من شعبان المذهلة.",
                "قراءة القرآن العظيم (كان السلف يسمون شعبان شهر القراء استعداداً لتدبر رمضان).",
                "الإكثار من الصلاة على محب القلوب ومعلم الأبرار محمد ﷺ في هذا الشهر."
            ),
            tag = "months",
            iconName = "🌸"
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header 
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
                    .border(
                        0.5.dp,
                        GoldClassic.copy(alpha = 0.3f),
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "التقويم الهجري والمناسبات",
                            tint = GoldClassic,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = "مواقيت الفضل والتقويم الهجري",
                            color = IvoryWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif
                        )

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("تقويم عام ٢٠٢٦ م المتطابق مع ١٤٤٧ - ١٤٤٨ هـ")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "معلومات",
                                tint = GoldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "جدول فضائل الأيام المباركة على مدار السنة، مستلهمة من هدي خير البرية ﷺ ومواقيت العبادات العطرة",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Tabs: Left = التقويم السنوي, Right = دليل المناسبات والفضائل
            TabRow(
                selectedTabIndex = primaryTabState,
                containerColor = Color.Transparent,
                contentColor = GoldClassic,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[primaryTabState]),
                        color = GoldClassic
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = primaryTabState == 0,
                    onClick = { primaryTabState = 0 },
                    text = {
                        Text(
                            "التقويم المزدوج السنوي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (primaryTabState == 0) GoldClassic else SecondaryText
                        )
                    },
                    modifier = Modifier.testTag("tab_yearly_calendar")
                )
                Tab(
                    selected = primaryTabState == 1,
                    onClick = { primaryTabState = 1 },
                    text = {
                        Text(
                            "دليل نفحات ومناسبات العام",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (primaryTabState == 1) GoldClassic else SecondaryText
                        )
                    },
                    modifier = Modifier.testTag("tab_sacred_events")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content with elegant animations
            AnimatedContent(
                targetState = primaryTabState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "primary_tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Yearly Interactive Calendar
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            // Calendar Top Controls (Year and Hijri/Gregorian Selector)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Year switcher (Locked around 2026/1447-1448 for historical precision)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { 
                                            if (selectedYear > 2025) selectedYear-- 
                                        },
                                        modifier = Modifier.size(32.dp).background(CardBackground, CircleShape)
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "السابق", tint = GoldClassic, modifier = Modifier.size(18.dp))
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (calendarModeIsGregorian) "$selectedYear م" else "${selectedYear - 579} هـ",
                                            color = GoldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (calendarModeIsGregorian) "${selectedYear - 579} - ${selectedYear - 578} هـ" else "$selectedYear م",
                                            color = SecondaryText,
                                            fontSize = 9.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { 
                                            if (selectedYear < 2027) selectedYear++ 
                                        },
                                        modifier = Modifier.size(32.dp).background(CardBackground, CircleShape)
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "التالي", tint = GoldClassic, modifier = Modifier.size(18.dp))
                                    }
                                }

                                // Interactive Hijri / Gregorian Toggle
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(CardBackground)
                                        .border(0.5.dp, GoldClassic.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { calendarModeIsGregorian = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (calendarModeIsGregorian) GoldClassic else Color.Transparent,
                                            contentColor = if (calendarModeIsGregorian) EmeraldDark else SecondaryText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 11.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("ميلادي", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = { calendarModeIsGregorian = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!calendarModeIsGregorian) GoldClassic else Color.Transparent,
                                            contentColor = if (!calendarModeIsGregorian) EmeraldDark else SecondaryText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 11.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("هجري", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "💡 اضغط على الكروت لاستعراض تفاصيل الأيام المشهورة التي تتجلى فيها الفضائل المستحبة وصلاة النوافل.",
                                color = TextGreen.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid of 12 Months
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (calendarModeIsGregorian) {
                                    // Gregorian rows (2 columns in grid)
                                    val rows = gregorianMonths2026.chunked(2)
                                    items(rows) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { month ->
                                                MonthCard(
                                                    title = month.nameAr,
                                                    subTitle = month.nameEn,
                                                    overlapText = month.overlappingHijri,
                                                    prominentCount = month.prominentDays.size,
                                                    onClick = {
                                                        activeMonthDetailIndex = gregorianMonths2026.indexOf(month)
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            // Handle edge case if row is not full
                                            if (rowItems.size < 2) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                } else {
                                    // Hijri rows (2 columns in grid)
                                    val rows = hijriMonthsData.chunked(2)
                                    items(rows) { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { month ->
                                                MonthCard(
                                                    title = month.nameAr,
                                                    subTitle = month.nameEn,
                                                    overlapText = month.overlappingGregorian,
                                                    prominentCount = month.prominentDays.size,
                                                    primaryBg = true,
                                                    onClick = {
                                                        activeMonthDetailIndex = hijriMonthsData.indexOf(month) + 12 // Shifted to distinguish from gregorian
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (rowItems.size < 2) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Rich Islamic Occasions (TABS INSIDE TABS!)
                        var secondaryTabState by remember { mutableStateOf(0) } // 0=Peak events, 1=Periodic Days, 2=Sacred Months
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Secondary Inner Tab Selector
                            ScrollableTabRow(
                                selectedTabIndex = secondaryTabState,
                                containerColor = CardBackground,
                                edgePadding = 12.dp,
                                contentColor = GoldAccent,
                                indicator = { tabPositions ->
                                    if (secondaryTabState < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[secondaryTabState]),
                                            color = GoldAccent
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(0.5.dp, GoldClassic.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            ) {
                                Tab(
                                    selected = secondaryTabState == 0,
                                    onClick = { secondaryTabState = 0 },
                                    text = {
                                        Text(
                                            "🌙 المناسبات السنوية",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (secondaryTabState == 0) GoldLight else SecondaryText
                                        )
                                    },
                                    modifier = Modifier.testTag("subtab_annual")
                                )
                                Tab(
                                    selected = secondaryTabState == 1,
                                    onClick = { secondaryTabState = 1 },
                                    text = {
                                        Text(
                                            "⏳ الورد الأسبوعي والشهر",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (secondaryTabState == 1) GoldLight else SecondaryText
                                        )
                                    },
                                    modifier = Modifier.testTag("subtab_periodic")
                                )
                                Tab(
                                    selected = secondaryTabState == 2,
                                    onClick = { secondaryTabState = 2 },
                                    text = {
                                        Text(
                                            "🛡️ الأشهر الحُرُم والفضيلة",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (secondaryTabState == 2) GoldLight else SecondaryText
                                        )
                                    },
                                    modifier = Modifier.testTag("subtab_months")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actionable filtered list
                            val filteredOccasions = remember(secondaryTabState) {
                                val filterTag = when (secondaryTabState) {
                                    0 -> "annual"
                                    1 -> "periodic"
                                    else -> "months"
                                }
                                importantOccasions.filter { it.tag == filterTag }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredOccasions) { occasion ->
                                    OccasionDetailedCard(
                                        occasion = occasion,
                                        onCopyClick = {
                                            val sharedString = buildString {
                                                appendLine("🌟 ${occasion.title} (${occasion.islamicDate}) 🌟")
                                                appendLine()
                                                appendLine("📖 فضل هذا اليوم: ${occasion.virtue}")
                                                appendLine()
                                                appendLine("📌 أفضل الأعمال والسنن تطبيقاً فيه:")
                                                occasion.bestDeeds.forEach { deed ->
                                                    appendLine("- $deed")
                                                }
                                                appendLine("\nتمت المشاركة من تطبيق السنن النبوية المطهرة")
                                            }
                                            clipboardManager.setText(AnnotatedString(sharedString))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("تم نسخ بطاقة فضل «${occasion.title}» بنجاح")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- POPUP DIALOGS ---

        // 1. Detailed Month Dialog with Day Picker Grid (Zoom view)
        activeMonthDetailIndex?.let { index ->
            val isHijri = index >= 12
            val actualIdx = if (isHijri) index - 12 else index
            
            val totalDays = if (isHijri) hijriMonthsData[actualIdx].totalDays else gregorianMonths2026[actualIdx].totalDays
            val monthTitle = if (isHijri) hijriMonthsData[actualIdx].nameAr else gregorianMonths2026[actualIdx].nameAr
            val monthSub = if (isHijri) hijriMonthsData[actualIdx].nameEn else gregorianMonths2026[actualIdx].nameEn
            val overlapText = if (isHijri) hijriMonthsData[actualIdx].overlappingGregorian else gregorianMonths2026[actualIdx].overlappingHijri
            val prominentMap = if (isHijri) hijriMonthsData[actualIdx].prominentDays else gregorianMonths2026[actualIdx].prominentDays
            val startDay = if (isHijri) 1 else gregorianMonths2026[actualIdx].startDayOfWeek // fallback to 1

            Dialog(onDismissRequest = { activeMonthDetailIndex = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .wrapContentHeight()
                        .border(1.5.dp, GoldClassic, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$monthTitle ($monthSub)",
                                    color = GoldClassic,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = overlapText,
                                    color = SecondaryText,
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(onClick = { activeMonthDetailIndex = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = SecondaryText)
                            }
                        }

                        Divider(color = GoldClassic.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                        // Briefing
                        Text(
                            text = "اضغط على الأيام المضيئة بالأخضر أو الذهب لاكتشاف المناسبات الميمونة وأوراد الطاعات المندوبة ومطالعة السنن.",
                            color = IvoryWhite,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Right,
                            style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        // Custom Grid rendering of month days in rows of 7
                        val weekdays = listOf("أحد", "نثن", "ثلاث", "أربع", "خميس", "جمعة", "سبت")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            weekdays.reversed().forEach { day ->
                                Text(
                                    text = day,
                                    color = GoldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Render days
                        val totalSlots = totalDays + startDay
                        val slots = (0 until totalSlots).chunked(7)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            slots.forEach { rowSlots ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // We display right-to-left for Arabic aesthetic
                                    rowSlots.reversed().forEach { slot ->
                                        val dayNum = slot - startDay + 1
                                        if (slot < startDay || dayNum > totalDays) {
                                            // Empty cell
                                            Spacer(modifier = Modifier.width(36.dp).height(32.dp))
                                        } else {
                                            val eventName = prominentMap[dayNum]
                                            val hasEvent = eventName != null
                                            val dayIsFasting = eventName?.contains("صيام") == true || eventName?.contains("البيض") == true
                                            
                                            val cellColor = when {
                                                dayIsFasting -> GoldClassic.copy(alpha = 0.25f)
                                                hasEvent -> EmeraldSoft.copy(alpha = 0.4f)
                                                else -> CardBackground
                                            }

                                            val borderCol = when {
                                                dayIsFasting -> GoldClassic
                                                hasEvent -> GoldAccent
                                                else -> Color.Transparent
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(cellColor)
                                                    .border(
                                                        if (borderCol != Color.Transparent) 1.dp else 0.5.dp,
                                                        if (borderCol != Color.Transparent) borderCol else GoldClassic.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        activeDetailDayNum = dayNum
                                                        activeDetailDayText = eventName ?: "يوم هنيء مبارك، لم تُرصد فيه مناسبة سنوية كبرى محددة، لكنه فرصة ثمينة لإكمال تحدي الـ ٢٠٠٠ سنة نبوية وسن صلاة الضحى والوتر وصيام التطوع!"
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = dayNum.toString(),
                                                        color = if (hasEvent) GoldLight else IvoryWhite,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (hasEvent) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (hasEvent) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(GoldClassic)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Day detail preview if a day is clicked inside the dialog
                        activeDetailDayNum?.let { dayNum ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .border(0.5.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تفاصيل نفحة اليوم",
                                            color = GoldClassic,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "اليوم $dayNum من الشهر",
                                            color = GoldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeDetailDayText ?: "",
                                        color = IvoryWhite,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCard(
    title: String,
    subTitle: String,
    overlapText: String,
    prominentCount: Int,
    primaryBg: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (prominentCount > 0) GoldAccent.copy(alpha = 0.4f) else GoldClassic.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("month_card_${title}"),
        colors = CardDefaults.cardColors(containerColor = if (primaryBg) EmeraldMedium else CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prominentCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldClassic.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$prominentCount مناسبات",
                            color = GoldLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = title,
                    color = GoldClassic,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Right
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subTitle,
                color = SecondaryText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )

            Divider(color = GoldClassic.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

            Text(
                text = overlapText,
                color = IvoryWhite,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OccasionDetailedCard(
    occasion: IslamicOccasion,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GoldClassic.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(EmeraldLight.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = GoldClassic,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = occasion.title,
                            color = GoldClassic,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = occasion.islamicDate,
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Right
                        )
                    }
                    Text(
                        text = occasion.iconName.ifEmpty { "🌙" },
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overview/Description
            Text(
                text = occasion.description,
                color = IvoryWhite,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Right,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Virtue Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldDark.copy(alpha = 0.6f))
                    .border(0.5.dp, GoldClassic.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "📖 فضائل مأثورة:",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = occasion.virtue,
                        color = TextGreen,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Justify,
                        style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recommended deeds list
            Text(
                text = "📌 أفضل الأعمال والسنن تطبيقاً فيه:",
                color = GoldClassic,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            occasion.bestDeeds.forEachIndexed { idx, deed ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = deed,
                        color = IvoryWhite,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    Text(
                        text = "•",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
