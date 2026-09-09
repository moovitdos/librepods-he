package me.kavishdevar.librepods.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kavishdevar.librepods.BuildConfig
import me.kavishdevar.librepods.R

@Composable
fun PrivacyPolicyPage(
    onForward: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(42.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "עודכן לאחרונה: 20 ביוני 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "סקירה כללית",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "LibrePods אינה אוספת, שומרת, מוכרת או משתפת מידע אישי למטרות פרסום, ניתוח נתונים, מעקב או יצירת פרופילים. האפליקציה אינה כוללת כלי ניתוח נתונים, דיווח קריסות, טלמטריה, ערכות פרסום (SDK) או שירותי מעקב.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "כל המידע נשאר במכשיר שלך, אלא אם תבחר במפורש ליצור איתי קשר, לפתוח דיווח (issue) ב-GitHub מתוך האפליקציה, או לבצע רכישה או חסות דרך פלטפורמה של צד שלישי.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "שירותי צד שלישי",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "LibrePods מציעה כמה דרכים ליצור איתי קשר, כולל אימייל, Discord ו-GitHub Issues.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "אימייל",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "אם תיצור איתי קשר באמצעות אימייל, אקבל את כתובת האימייל שלך וכל מידע שתבחר לכלול בהודעתך. בעת שימוש בטופס יצירת הקשר בתוך LibrePods, תוכנת האימייל שלך תיפתח עם כתובת אימייל, שורת נושא וגוף הודעה ממולאים מראש שתמלא. גוף ההודעה יכלול גם מידע על גרסת LibrePods ומידע על המכשיר כדי לסייע באיתור תקלות.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "תוכל לערוך או להסיר כל אחד מפרטי המידע האלה לפני שליחת האימייל.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Discord",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "האפליקציה מספקת קישור לשרת ה-Discord של LibrePods. אם תבחר להצטרף לשרת ה-Discord, תהיה כפוף למדיניות הפרטיות של Discord.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "אינני מקבל ממך שום מידע מ-Discord מלבד מה שגלוי לציבור בשרת ה-Discord, כגון שם המשתמש שלך, תאריך ההצטרפות, שרתים משותפים וכל הודעה או תוכן שתפרסם בשרת, אלא אם תבחר לשתף אותם איתי בשרת ה-Discord.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "GitHub Issues",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "בעת פתיחת דיווח (issue) ב-GitHub דרך LibrePods, האפליקציה תמלא מראש את טופס הדיווח עם:",
                style = MaterialTheme.typography.bodyMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "• שם גרסת LibrePods וקוד הגרסה",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• יצרן ודגם המכשיר",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• פרטי ה-build של Android",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• מקור ההתקנה (Google Play או GitHub)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "מידע זה מסייע באבחון תקלות ובמתן תמיכה. שום מידע אינו נשלח באופן אוטומטי. המידע נשלח רק אם תבחר לפתוח את הדיווח ב-GitHub.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "תשלומים", style = MaterialTheme.typography.titleLarge
            )

            if (BuildConfig.PLAY_BUILD) {
                Text(
                    text = "Google Play", style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "בעת שימוש בגרסה הזמינה ב-Google Play, הרכישות מעובדות על ידי Google Play.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "LibrePods מאמתת את הרכישה מול Google Play במכשיר עצמו, ולא מול שרת מרוחק שבשליטתי. אינני מקבל ממך שום מידע עליך או על הרכישה שלך מ-Google Play. עיבוד התשלום מתבצע כולו על ידי Google Play, ואין לי גישה לשום פרט מפרטי התשלום שלך.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "GitHub Sponsors", style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "בעת שימוש בגרסת ה-FOSS הזמינה ב-GitHub, כפתור השדרוג מקשר ל-GitHub Sponsors. אם תבחר להעניק חסות ל-LibrePods, החסות שלך מעובדת על ידי GitHub.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "שם המשתמש שלך והמדינה/אזור שלך משותפים איתי כאשר אתה מעניק חסות ל-LibrePods. בהתאם להגדרות הפרטיות שלך ב-GitHub Sponsors, ייתכן שאקבל גם את כתובת האימייל שלך.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "יצירת קשר", style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "אם יש לך שאלות בנוגע למדיניות פרטיות זו, אנא צור איתי קשר באמצעות אימייל בכתובת privacy@kavish.xyz.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onForward,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.i_agree),
                    style = MaterialTheme.typography.labelMediumEmphasized
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
