import Foundation
import Combine
import FirebaseAuth
import GoogleSignIn

enum SubscriptionLevel: String, Codable {
    case none
    case monthly
    case yearly
}

class AuthService: ObservableObject {
    static let shared = AuthService()
    private let auth = Auth.auth()

    @Published var currentUser: User?

    private init() {
        self.currentUser = auth.currentUser
    }

    func signInWithEmail(email: String, password: String) async throws -> User? {
        let result = try await auth.signIn(withEmail: email, password: password)
        DispatchQueue.main.async { self.currentUser = result.user }
        return result.user
    }

    func registerWithEmail(email: String, password: String) async throws -> User? {
        let result = try await auth.createUser(withEmail: email, password: password)
        DispatchQueue.main.async { self.currentUser = result.user }
        return result.user
    }

    func sendPasswordResetEmail(email: String) async throws {
        try await auth.sendPasswordReset(withEmail: email)
    }

    func signOut() throws {
        try auth.signOut()
        GIDSignIn.sharedInstance.signOut()
        DispatchQueue.main.async { self.currentUser = nil }
    }

    func getSubscriptionLevel() -> SubscriptionLevel {
        let level = UserDefaults.standard.string(forKey: "sub_level") ?? "none"
        return SubscriptionLevel(rawValue: level) ?? .none
    }

    func getCardLimit() -> Int {
        let level = getSubscriptionLevel()
        switch level {
        case .monthly: return 2
        case .yearly: return 10
        case .none: return 1
        }
    }

    func upgradeSubscription(level: SubscriptionLevel) async {
        UserDefaults.standard.set(level.rawValue, forKey: "sub_level")
        // Simüle edilmiş gecikme
        try? await Task.sleep(nanoseconds: 1 * 1000000000)
        objectWillChange.send()
    }
}
