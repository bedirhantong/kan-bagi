import Foundation

/// Sıkça sorulan sorular (Android uygulamasındaki içerikle aynı; Türkçe).
struct FAQItem: Identifiable, Hashable, Sendable {
    let question: String
    let answer: String
    var id: String { question }
}

enum FAQCatalog {
    static let items: [FAQItem] = [
        FAQItem(
            question: "Kan bağışı yapmak için hangi şartları taşımalıyım?",
            answer: """
        • 18-65 yaş arasında olmalısınız
        • En az 50 kg ağırlığında olmalısınız
        • Nabız, tansiyon ve hemoglobin değerleriniz normal sınırlarda olmalı
        • Son 3 ayda kan bağışı yapmamış olmalısınız
        • Kronik bir hastalığınız olmamalı
        """
        ),
        FAQItem(
            question: "Ne sıklıkla kan bağışı yapabilirim?",
            answer: """
        Tam kan bağışı için en az 90 gün (3 ay) ara vermeniz gerekmektedir. Trombosit bağışı için bu süre 72 saat, plazma bağışı için ise 15 gündür.
        """
        ),
        FAQItem(
            question: "Kan bağışı öncesi nelere dikkat etmeliyim?",
            answer: """
        • Yeterli uyku almış olmalısınız
        • Aç olmamalısınız
        • Alkol kullanmamış olmalısınız
        • Son 3 gün içinde aspirin kullanmamış olmalısınız
        • Sağlıklı olmalısınız
        """
        ),
        FAQItem(
            question: "Kan bağışı ne kadar sürer?",
            answer: """
        Kayıt, muayene ve kan alma işlemleri dahil toplam süre yaklaşık 30-35 dakikadır. Sadece kan alma işlemi ise 5-10 dakika sürmektedir.
        """
        ),
        FAQItem(
            question: "Kan bağışı sonrası nelere dikkat etmeliyim?",
            answer: """
        • En az 30 dakika dinlenmelisiniz
        • Bol sıvı tüketmelisiniz
        • 4-5 saat ağır egzersiz yapmamalısınız
        • Kan verdiğiniz kolu zorlamamalısınız
        • Alkol kullanmamalısınız
        """
        ),
        FAQItem(
            question: "Hangi kan grupları birbiriyle uyumludur?",
            answer: """
        • 0 Rh(-): Sadece 0 Rh(-)
        • 0 Rh(+): 0 Rh(-) ve 0 Rh(+)
        • A Rh(-): 0 Rh(-) ve A Rh(-)
        • A Rh(+): 0 Rh(-), 0 Rh(+), A Rh(-) ve A Rh(+)
        • B Rh(-): 0 Rh(-) ve B Rh(-)
        • B Rh(+): 0 Rh(-), 0 Rh(+), B Rh(-) ve B Rh(+)
        • AB Rh(-): 0 Rh(-), A Rh(-), B Rh(-) ve AB Rh(-)
        • AB Rh(+): Tüm kan grupları
        """
        ),
        FAQItem(
            question: "Kan bağışı sonrası kan testi sonuçlarımı öğrenebilir miyim?",
            answer: """
        Evet, kan bağışı sonrası yapılan testlerin sonuçları hakkında bilgi alabilirsiniz. Kan güvenliğini sağlamak amacıyla her bağışta kan grubu, hepatit B, hepatit C, HIV ve sifiliz testleri yapılmaktadır. Bu test sonuçlarınızı e-Nabız sisteminden veya kan bağışı merkezlerinden öğrenebilirsiniz.
        """
        ),
        FAQItem(
            question: "İlaç kullanıyorum, kan bağışı yapabilir miyim?",
            answer: """
        Kullandığınız ilaca bağlı olarak kan bağışı yapıp yapamayacağınız değişebilir. Bazı ilaçlar geçici süreyle, bazıları ise kalıcı olarak kan bağışına engel olabilir. Antibiyotik kullananlar tedavi bitiminden 7-10 gün sonra, aspirin kullananlar 3 gün sonra kan verebilir. Tansiyon ilaçları, doğum kontrol hapları gibi bazı ilaçlar ise kan bağışına engel değildir. Kesin bilgi için bağış öncesi sağlık personeline kullandığınız ilaçlar hakkında bilgi vermelisiniz.
        """
        ),
        FAQItem(
            question: "Dövme veya piercing yaptırdım, ne kadar süre beklemeliyim?",
            answer: """
        Dövme, piercing, akupunktur, hacamat gibi deri bütünlüğünü bozan işlemler sonrasında 4 ay (120 gün) süreyle kan bağışı yapılmamalıdır. Bu süre, hepatit ve HIV gibi enfeksiyon hastalıklarının test edilebilmesi için güvenli bir bekleme süresidir.
        """
        ),
        FAQItem(
            question: "Hangi durumlarda kesinlikle kan bağışı yapamam?",
            answer: """
        • HIV pozitif veya AIDS hastasıysanız
        • Aktif hepatit B veya C enfeksiyonu geçiriyorsanız
        • Kalp, akciğer, böbrek gibi hayati organlarda ciddi hastalık varsa
        • Kanser tedavisi görüyorsanız
        • Hamile iseniz veya doğum yaptıysanız (doğumdan sonra 6 ay)
        • Son 12 ay içinde ameliyat geçirdiyseniz (ameliyatın türüne bağlı olarak)
        • Riskli cinsel davranışlarınız varsa
        """
        ),
        FAQItem(
            question: "Kan bağışı sağlığıma faydalı mıdır?",
            answer: """
        Evet, düzenli kan bağışının vücudunuza çeşitli faydaları vardır:
        • Kan yapımı uyarılır ve yenilenir
        • Kan akışkanlığı artar, pıhtılaşma riski azalır
        • Kalp-damar hastalıkları riski azalabilir
        • Karaciğer yağlanması önlenebilir
        • Demir fazlalığına bağlı hastalıklar önlenebilir
        • Her bağışta sağlık kontrolünden geçersiniz
        """
        ),
        FAQItem(
            question: "Aferez bağışı nedir?",
            answer: """
        • Aferez bağışı, kanın sadece belirli bileşenlerini (örn. plazma, kırmızı kan hücreleri) bağışlamanızı sağlar.
        • Bu yöntemle, tek bağışta daha fazla insanın ihtiyacı karşılanabilir.
        """
        ),
        FAQItem(
            question: "Immuun Plazma bağışı nedir?",
            answer: """
        • İmmün plazma, COVID-19 gibi hastalıklara karşı antikorlar içeren plazmadır.
        • Bu plazma, hasta hastalara nakledilerek tedavi amaçlı kullanılabilir.
        """
        ),
        FAQItem(
            question: "Kök hücresi bağışı nedir?",
            answer: """
        • Kök hücre bağışı, kan veya kemik iliğinden kök hücre toplanmasıdır.
        • Bu hücreler, kanser ve kemik iliği hastalıkları gibi tedavilerde kullanılabilir.
        """
        ),
        FAQItem(
            question: "Kimliğimi bildirmem zorunlu mu?",
            answer: """
        • Evet, kan bağışı yaparken kimliğinizi bildirmek zorunludur.
        • Bu, bağışın güvenliği ve izlenebilirliği için gereklidir.
        • Ayrıca, bağışçı kaydınızın tutulması için gereklidir.
        """
        ),
        FAQItem(
            question: "Sorgulama formundaki tüm bilgileri doldurmak zorunlu mu?",
            answer: """
        • Sorgulama formunun içeriği Sağlık Bakanlığı tarafından belirlenmiştir ve tüm soruların cevaplanması zorunludur.
        • Bu bilgiler, bağışın güvenliği ve sağlığınız için önemlidir.
        • Eksik bilgi, bağışınızın reddedilmesine neden olabilir.
        • Lütfen tüm soruları eksiksiz ve doğru bir şekilde cevaplayın.
        """
        ),
    ]
}
