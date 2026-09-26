import Foundation

/// ViewModel'ler için fabrika metotları: bağımlılıkların nasıl bağlandığı yalnızca burada bilinir.
/// ViewModel'ler `AppContainer`'ı tanımaz; yalnızca ihtiyaç duydukları use case'leri alır (DIP/ISP).
extension AppContainer {
    func makeSettingsViewModel() -> SettingsViewModel {
        SettingsViewModel(
            deleteAccount: useCases.deleteAccount
        )
    }

    func makeNotificationSettingsViewModel() -> NotificationSettingsViewModel {
        NotificationSettingsViewModel(
            getPreferences: useCases.getNotificationPreferences,
            updatePreferences: useCases.updateNotificationPreferences,
            getHospitalsByIds: useCases.getHospitalsByIds,
            getHospitals: useCases.getHospitals,
            getAreas: useCases.getNotificationAreas,
            suggestArea: useCases.suggestNotificationArea,
            push: push
        )
    }

    func makeHomeViewModel() -> HomeViewModel {
        HomeViewModel(
            getFeed: useCases.getFeed,
            getStories: useCases.getStories,
            events: events
        )
    }

    func makeChatListViewModel() -> ChatListViewModel {
        ChatListViewModel(
            getRooms: useCases.getRooms
        )
    }

    func makeChatDetailViewModel(target: ChatTarget) -> ChatDetailViewModel {
        ChatDetailViewModel(
            openRoom: useCases.openRoom,
            getMessages: useCases.getMessages,
            sendMessage: useCases.sendMessage,
            observeMessages: useCases.observeMessages,
            reportContent: useCases.reportContent,
            blockUser: useCases.blockUser,
            target: target
        )
    }

    func makeLoginViewModel() -> LoginViewModel {
        LoginViewModel(
            signInWithApple: useCases.signInWithApple,
            signInWithGoogle: useCases.signInWithGoogle,
            signInWithEmail: useCases.signInWithEmail,
            signInAsGuest: useCases.signInAsGuest,
            isEmailLoginEnabled: config.emailLoginEnabled
        )
    }

    func makeRegistrationViewModel(
        profile: Profile,
        initialStep: RegistrationViewModel.Step = .personal,
        onCompleted: @escaping @MainActor (Profile) async -> Void,
        onSignOut: @escaping @MainActor () async -> Void
    ) -> RegistrationViewModel {
        RegistrationViewModel(
            profile: profile,
            completeProfile: useCases.completeProfile,
            setAreas: useCases.setNotificationAreas,
            areaPicker: NotificationAreaPickerModel(
                getAreas: useCases.getNotificationAreas,
                suggestArea: useCases.suggestNotificationArea),
            push: push,
            initialStep: initialStep,
            onCompleted: onCompleted,
            onSignOut: onSignOut
        )
    }

    func makeRequestDetailViewModel(requestId: UUID) -> RequestDetailViewModel {
        RequestDetailViewModel(
            getDetail: useCases.getRequestDetail,
            togglePledge: useCases.togglePledge,
            setActive: useCases.setRequestActive,
            deleteRequest: useCases.deleteRequest,
            reportContent: useCases.reportContent,
            blockUser: useCases.blockUser,
            events: events,
            requestId: requestId
        )
    }

    func makeHealthFormViewModel() -> HealthFormViewModel {
        HealthFormViewModel(
            getForm: useCases.getHealthForm,
            submitForm: useCases.submitHealthForm
        )
    }

    func makeHospitalMapViewModel() -> HospitalMapViewModel {
        HospitalMapViewModel(
            getNearby: useCases.getNearbyHospitals,
            getNearbyBloodPoints: useCases.getNearbyBloodPoints,
            getRequests: useCases.getRequests,
            getCurrentLocation: useCases.getCurrentLocation
        )
    }

    func makeProfileViewModel() -> ProfileViewModel {
        ProfileViewModel(
            getMyRequests: useCases.getMyRequests,
            getPledged: useCases.getPledgedRequests,
            setActive: useCases.setRequestActive,
            events: events
        )
    }

    func makeProfileEditViewModel(profile: Profile, onSaved: @escaping @MainActor (Profile) -> Void) -> ProfileEditViewModel {
        ProfileEditViewModel(
            updateProfile: useCases.updateProfile,
            profile: profile,
            onSaved: onSaved
        )
    }

    func makeRequestFormViewModel(editing request: BloodRequest?, prefill: RequestPrefill? = nil) -> RequestFormViewModel {
        RequestFormViewModel(
            saveRequest: useCases.saveRequest,
            getRequestDetail: useCases.getRequestDetail,
            getHospitals: useCases.getHospitals,
            events: events,
            editing: request,
            prefill: prefill
        )
    }

    func makeNotificationInboxViewModel() -> NotificationInboxViewModel {
        NotificationInboxViewModel(
            getInbox: useCases.getInbox,
            markRead: useCases.markRead,
            events: events
        )
    }
}
