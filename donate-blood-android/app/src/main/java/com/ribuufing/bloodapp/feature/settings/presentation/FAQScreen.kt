package com.ribuufing.bloodapp.feature.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(navController: NavController) {
    val faqItems = remember {
        listOf(
            FAQItem(
                question = "Kan bağışı yapmak için hangi şartları taşımalıyım?",
                answer = """
                    • 18-65 yaş arasında olmalısınız
                    • En az 50 kg ağırlığında olmalısınız
                    • Nabız, tansiyon ve hemoglobin değerleriniz normal sınırlarda olmalı
                    • Son 3 ayda kan bağışı yapmamış olmalısınız
                    • Kronik bir hastalığınız olmamalı
                """.trimIndent()
            ),
            FAQItem(
                question = "Ne sıklıkla kan bağışı yapabilirim?",
                answer = "Tam kan bağışı için en az 90 gün (3 ay) ara vermeniz gerekmektedir. Trombosit bağışı için bu süre 72 saat, plazma bağışı için ise 15 gündür."
            ),
            FAQItem(
                question = "Kan bağışı öncesi nelere dikkat etmeliyim?",
                answer = """
                    • Yeterli uyku almış olmalısınız
                    • Aç olmamalısınız
                    • Alkol kullanmamış olmalısınız
                    • Son 3 gün içinde aspirin kullanmamış olmalısınız
                    • Sağlıklı olmalısınız
                """.trimIndent()
            ),
            FAQItem(
                question = "Kan bağışı ne kadar sürer?",
                answer = "Kayıt, muayene ve kan alma işlemleri dahil toplam süre yaklaşık 30-35 dakikadır. Sadece kan alma işlemi ise 5-10 dakika sürmektedir."
            ),
            FAQItem(
                question = "Kan bağışı sonrası nelere dikkat etmeliyim?",
                answer = """
                    • En az 30 dakika dinlenmelisiniz
                    • Bol sıvı tüketmelisiniz
                    • 4-5 saat ağır egzersiz yapmamalısınız
                    • Kan verdiğiniz kolu zorlamamalısınız
                    • Alkol kullanmamalısınız
                """.trimIndent()
            ),
            FAQItem(
                question = "Hangi kan grupları birbiriyle uyumludur?",
                answer = """
                    • 0 Rh(-): Sadece 0 Rh(-)
                    • 0 Rh(+): 0 Rh(-) ve 0 Rh(+)
                    • A Rh(-): 0 Rh(-) ve A Rh(-)
                    • A Rh(+): 0 Rh(-), 0 Rh(+), A Rh(-) ve A Rh(+)
                    • B Rh(-): 0 Rh(-) ve B Rh(-)
                    • B Rh(+): 0 Rh(-), 0 Rh(+), B Rh(-) ve B Rh(+)
                    • AB Rh(-): 0 Rh(-), A Rh(-), B Rh(-) ve AB Rh(-)
                    • AB Rh(+): Tüm kan grupları
                """.trimIndent()
            ),
            FAQItem(
                question = "Neden T.C. Kimlik numarası istiyorsunuz?",
                answer = """
                    T.C. Kimlik numarası, kan bağışçılarının doğru şekilde tanımlanması ve kayıtlarının güvenli bir şekilde tutulması için gereklidir. Bu sayede:
                    • T.C kimlik ile kullanicilarin gercek T.C. vatandasi olduklari dogrulanarak, platformum daha güvenli bir ortam olmasini amacliyoruz
                    • MERNIS sisteminden gerçek kimlik bilgilerinin doğrulanmasının sağlanması.
                    • Bağışçıların güvenliğinin sağlanması.
                    
                    Tüm bilgileriniz Kişisel Verileri Koruma Kanunu (KVKK) kapsamında korunmaktadır.
                """.trimIndent()
            ),
            FAQItem(
                question = "Kan bağışı sonrası kan testi sonuçlarımı öğrenebilir miyim?",
                answer = "Evet, kan bağışı sonrası yapılan testlerin sonuçları hakkında bilgi alabilirsiniz. Kan güvenliğini sağlamak amacıyla her bağışta kan grubu, hepatit B, hepatit C, HIV ve sifiliz testleri yapılmaktadır. Bu test sonuçlarınızı e-Nabız sisteminden veya kan bağışı merkezlerinden öğrenebilirsiniz."
            ),
            FAQItem(
                question = "İlaç kullanıyorum, kan bağışı yapabilir miyim?",
                answer = "Kullandığınız ilaca bağlı olarak kan bağışı yapıp yapamayacağınız değişebilir. Bazı ilaçlar geçici süreyle, bazıları ise kalıcı olarak kan bağışına engel olabilir. Antibiyotik kullananlar tedavi bitiminden 7-10 gün sonra, aspirin kullananlar 3 gün sonra kan verebilir. Tansiyon ilaçları, doğum kontrol hapları gibi bazı ilaçlar ise kan bağışına engel değildir. Kesin bilgi için bağış öncesi sağlık personeline kullandığınız ilaçlar hakkında bilgi vermelisiniz."
            ),
            FAQItem(
                question = "Dövme veya piercing yaptırdım, ne kadar süre beklemeliyim?",
                answer = "Dövme, piercing, akupunktur, hacamat gibi deri bütünlüğünü bozan işlemler sonrasında 4 ay (120 gün) süreyle kan bağışı yapılmamalıdır. Bu süre, hepatit ve HIV gibi enfeksiyon hastalıklarının test edilebilmesi için güvenli bir bekleme süresidir."
            ),
            FAQItem(
                question = "Hangi durumlarda kesinlikle kan bağışı yapamam?",
                answer = """
                    • HIV pozitif veya AIDS hastasıysanız
                    • Aktif hepatit B veya C enfeksiyonu geçiriyorsanız
                    • Kalp, akciğer, böbrek gibi hayati organlarda ciddi hastalık varsa
                    • Kanser tedavisi görüyorsanız
                    • Hamile iseniz veya doğum yaptıysanız (doğumdan sonra 6 ay)
                    • Son 12 ay içinde ameliyat geçirdiyseniz (ameliyatın türüne bağlı olarak)
                    • Riskli cinsel davranışlarınız varsa
                """.trimIndent()
            ),
            FAQItem(
                question = "Kan bağışı sağlığıma faydalı mıdır?",
                answer = """
                    Evet, düzenli kan bağışının vücudunuza çeşitli faydaları vardır:
                    • Kan yapımı uyarılır ve yenilenir
                    • Kan akışkanlığı artar, pıhtılaşma riski azalır
                    • Kalp-damar hastalıkları riski azalabilir
                    • Karaciğer yağlanması önlenebilir
                    • Demir fazlalığına bağlı hastalıklar önlenebilir
                    • Her bağışta sağlık kontrolünden geçersiniz
                """.trimIndent()
            ),
            FAQItem(
                question = "Neden aynı anda sadece 1 post paylaşabiliyorum?",
                answer = """
                    • Bu durum, uygulamanın güvenlik önlemleri ve tasarımıyla ilgilidir.
                    • Aynı anda birden fazla post paylaşımı, spam riskini artırabilir.
                    • Bu kısıtlamayla, içeriğin kalitesi ve gerçekliği sağlanmaya çalışır.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Aferez bağışı nedir?",
                answer = """
                    • Aferez bağışı, kanın sadece belirli bileşenlerini (örn. plazma, kırmızı kan hücreleri) bağışlamanızı sağlar.
                    • Bu yöntemle, tek bağışta daha fazla insanın ihtiyacı karşılanabilir.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Immuun Plazma bağışı nedir?",
                answer = """
                    • İmmün plazma, COVID-19 gibi hastalıklara karşı antikorlar içeren plazmadır.
                    • Bu plazma, hasta hastalara nakledilerek tedavi amaçlı kullanılabilir.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Kök hücresi bağışı nedir?",
                answer = """
                    • Kök hücre bağışı, kan veya kemik iliğinden kök hücre toplanmasıdır.
                    • Bu hücreler, kanser ve kemik iliği hastalıkları gibi tedavilerde kullanılabilir.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Kimliğimi bildirmem zorunlu mu?",
                answer = """
                    • Evet, kan bağışı yaparken kimliğinizi bildirmek zorunludur.
                    • Bu, bağışın güvenliği ve izlenebilirliği için gereklidir.
                    • Ayrıca, bağışçı kaydınızın tutulması için gereklidir.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Sorgulama formundaki tüm bilgileri doldurmak zorunlu mu?",
                answer = """
                    • Sorgulama formunun içeriği Sağlık Bakanlığı tarafından belirlenmiştir ve tüm soruların cevaplanması zorunludur.
                    • Bu bilgiler, bağışın güvenliği ve sağlığınız için önemlidir.
                    • Eksik bilgi, bağışınızın reddedilmesine neden olabilir.
                    • Lütfen tüm soruları eksiksiz ve doğru bir şekilde cevaplayın.
                    """.trimIndent()
            ),
            FAQItem(
                question = "Neden aynı anda sadece 1 post paylaşabiliyorum?",
                answer = """
                    • Bu durum, uygulamanın güvenlik önlemleri ve tasarımıyla ilgilidir.
                    • Aynı anda birden fazla post paylaşımı, spam riskini artırabilir.
                    • Bu kısıtlamayla, içeriğin kalitesi ve gerçekliği sağlanmaya çalışır.
                    """.trimIndent()
            ),
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredFaqItems = remember(searchQuery, faqItems) {
        if (searchQuery.isEmpty()) {
            faqItems
        } else {
            faqItems.filter { 
                it.question.contains(searchQuery, ignoreCase = true) ||
                it.answer.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Sıkça Sorulan Sorular",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // FAQ items
            if (filteredFaqItems.isEmpty()) {
                EmptySearchResult()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredFaqItems) { faqItem ->
                        ModernFAQItemCard(faqItem = faqItem)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Soru veya cevap ara...") },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara"
            )
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun EmptySearchResult() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Text(
                "Aramanızla eşleşen soru bulunamadı",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ModernFAQItemCard(faqItem: FAQItem) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation_animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Question row with accent indicator
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left accent
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(70.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                
                // Question and arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = faqItem.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "Daralt" else "Genişlet",
                            modifier = Modifier.rotate(rotationState),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Answer
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Divider(
                        modifier = Modifier.padding(bottom = 12.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = faqItem.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

data class FAQItem(
    val question: String,
    val answer: String
) 