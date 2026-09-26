import Foundation

/// `supabase/08_test_users.sql` ile oluşturulan, herkese açık test hesapları.
/// Yalnızca `EMAIL_LOGIN_ENABLED=YES` iken kullanılır (Debug); üretimde bu hesaplar oluşturulmamalıdır.
struct TestAccount: Identifiable, Equatable, Sendable {
    /// `newUser`: profili boş gelir, giriş yapınca kayıt akışı açılır (SQL tekrar çalıştırılınca yine boşalır).
    enum Role: String, Sendable { case donor, secondDonor, requester, newUser }

    let role: Role
    let email: String
    let password: String
    var id: String { email }

    static let all: [TestAccount] = [
        TestAccount(role: .donor, email: "donor@test.dev", password: "Test1234!"),
        TestAccount(role: .secondDonor, email: "donor2@test.dev", password: "Test1234!"),
        TestAccount(role: .requester, email: "requester@test.dev", password: "Test1234!"),
        TestAccount(role: .newUser, email: "newuser@test.dev", password: "Test1234!"),
    ]
}
