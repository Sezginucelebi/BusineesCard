import SwiftUI

struct RootView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        Group {
            if authService.currentUser != nil {
                CardListView()
            } else {
                LoginView()
            }
        }
    }
}
