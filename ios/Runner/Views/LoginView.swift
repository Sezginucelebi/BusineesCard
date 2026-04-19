import SwiftUI

struct LoginView: View {
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @EnvironmentObject var authService: AuthService

    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Spacer()

                VStack(spacing: 8) {
                    Image(systemName: "contact.emergency.fill")
                        .font(.system(size: 80))
                        .foregroundColor(.blueGrey)

                    Text("BusineesCard")
                        .font(.largeTitle)
                        .bold()
                        .foregroundColor(.blueGrey)
                }

                Spacer()

                VStack(spacing: 16) {
                    CustomTextField(text: $email, placeholder: "E-posta", icon: "envelope")
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)

                    CustomSecureField(text: $password, placeholder: "Şifre", icon: "lock")
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(16)

                Button(action: handleLogin) {
                    if isLoading {
                        ProgressView().progressViewStyle(CircularProgressViewStyle(color: .white))
                    } else {
                        Text("Giriş Yap")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                    }
                }
                .padding()
                .background(Color.blueGrey)
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(isLoading)

                NavigationLink(destination: RegisterView()) {
                    Text("Henüz hesabınız yok mu? Kayıt Olun")
                        .font(.subheadline)
                        .foregroundColor(.blueGrey)
                }

                Spacer()
            }
            .padding(32)
            .navigationBarHidden(true)
        }
    }

    func handleLogin() {
        guard !email.isEmpty, !password.isEmpty else { return }
        isLoading = true
        Task {
            do {
                _ = try await authService.signInWithEmail(email: email, password: password)
            } catch {
                print("Hata: \(error.localizedDescription)")
            }
            isLoading = false
        }
    }
}
