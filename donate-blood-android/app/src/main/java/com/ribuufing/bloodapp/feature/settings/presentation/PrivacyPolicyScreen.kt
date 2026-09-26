package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gizlilik Politikası") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "KVKK ve Gizlilik Politikası",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "1. Veri Sorumlusu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Blood App olarak kişisel verilerinizin güvenliği konusunda azami hassasiyet göstermekteyiz. Bu bilinçle, uygulamayı kullanan üyelerimizin kişisel verilerinin 6698 sayılı Kişisel Verilerin Korunması Kanunu'na uygun olarak işlenmesine ve korunmasına büyük önem veriyoruz."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "2. Toplanan Kişisel Veriler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Uygulamamız aracılığıyla aşağıdaki kişisel verileriniz toplanmaktadır:
                    
                    • Ad ve Soyad
                    • İletişim Bilgileri (telefon, e-posta)
                    • Kan Grubu
                    • Konum Bilgileri
                    • Sağlık Bilgileri
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "3. Kişisel Verilerin İşlenme Amacı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Kişisel verileriniz aşağıdaki amaçlarla işlenmektedir:
                    
                    • Kan bağışı eşleştirmelerinin yapılması
                    • Acil kan ihtiyaçlarının karşılanması
                    • Kullanıcı deneyiminin iyileştirilmesi
                    • Yasal yükümlülüklerin yerine getirilmesi
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "4. Kişisel Verilerin Saklanması",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Kişisel verileriniz, işlenme amaçlarının gerektirdiği süreler boyunca saklanmaktadır. Süre geçtikten sonra kişisel verileriniz silinmekte, yok edilmekte veya anonim hale getirilmektedir."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "5. Veri Sahibi Hakları",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    KVKK'nın 11. maddesi uyarınca sahip olduğunuz haklar:
                    
                    • Kişisel verilerinizin işlenip işlenmediğini öğrenme
                    • Kişisel verileriniz işlenmişse buna ilişkin bilgi talep etme
                    • Kişisel verilerinizin işlenme amacını ve bunların amacına uygun kullanılıp kullanılmadığını öğrenme
                    • Yurt içinde veya yurt dışında kişisel verilerinizin aktarıldığı üçüncü kişileri bilme
                    • Kişisel verilerinizin eksik veya yanlış işlenmiş olması hâlinde bunların düzeltilmesini isteme
                    • KVKK'nın 7. maddesinde öngörülen şartlar çerçevesinde kişisel verilerinizin silinmesini veya yok edilmesini isteme
                """.trimIndent()
            )
        }
    }
} 