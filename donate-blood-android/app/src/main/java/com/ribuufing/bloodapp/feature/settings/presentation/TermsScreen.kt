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
fun TermsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kullanım Koşulları") },
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
                text = "Kullanım Koşulları ve Şartlar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "1. Hizmet Kullanımı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Blood App'i kullanarak aşağıdaki koşulları kabul etmiş olursunuz:
                    
                    • Uygulamayı yalnızca yasal amaçlar için kullanacağınızı
                    • Başkalarının haklarını ihlal etmeyeceğinizi
                    • Doğru ve güncel bilgiler sağlayacağınızı
                    • Hesap güvenliğinizi koruyacağınızı
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "2. Kullanıcı Sorumlulukları",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Uygulama kullanıcısı olarak:
                    
                    • Sağlık durumunuz hakkında doğru bilgi vermeyi
                    • Randevularınıza sadık kalmayı
                    • Diğer kullanıcılara saygılı davranmayı
                    • Acil durumlar dışında gereksiz kan talebi oluşturmamayı
                    • Kişisel bilgilerinizi güncel tutmayı
                    
                    taahhüt edersiniz.
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "3. Sorumluluk Reddi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Blood App:
                    
                    • Kan bağışı sürecinde oluşabilecek tıbbi komplikasyonlardan
                    • Kullanıcılar arası iletişimden doğabilecek anlaşmazlıklardan
                    • Üçüncü taraf hizmetlerden
                    • İnternet bağlantısı kaynaklı sorunlardan
                    
                    sorumlu tutulamaz.
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "4. Gizlilik",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Kişisel verileriniz:
                    
                    • KVKK kapsamında işlenir
                    • Üçüncü taraflarla paylaşılmaz
                    • Güvenli bir şekilde saklanır
                    • Yalnızca belirtilen amaçlar için kullanılır
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "5. Değişiklikler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Blood App, bu kullanım koşullarını önceden haber vermeksizin değiştirme hakkını saklı tutar. Değişiklikler uygulamada yayınlandığı tarihte yürürlüğe girer."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "6. İletişim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Kullanım koşulları ile ilgili sorularınız için info@bloodapp.com adresinden bizimle iletişime geçebilirsiniz."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Son güncelleme: 1 Mart 2024",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 