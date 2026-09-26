import SwiftUI

/// Uygulama dili seçimi (telefon dilinden bağımsız). Seçim anında uygulanır.
struct LanguageSettingsView: View {
    @EnvironmentObject private var languageStore: LanguageStore

    var body: some View {
        List {
            Section {
                ForEach(AppLanguage.allCases) { language in
                    Button {
                        Haptics.selection()
                        languageStore.select(language)
                    } label: {
                        HStack {
                            if let name = language.nativeName {
                                Text(verbatim: name)
                            } else {
                                Text("language.system")
                            }
                            Spacer()
                            if languageStore.language == language {
                                Image(systemName: "checkmark").font(.body.weight(.semibold)).foregroundStyle(Theme.brand)
                            }
                        }
                        .foregroundStyle(.primary)
                        .contentShape(Rectangle())
                    }
                    .accessibilityAddTraits(languageStore.language == language ? .isSelected : [])
                }
            } footer: {
                Text("language.footer")
            }
        }
        .navigationTitle("settings.language")
        .navigationBarTitleDisplayMode(.inline)
    }
}
