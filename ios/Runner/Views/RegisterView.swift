import SwiftUI

struct RegisterView: View {
    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var authService: AuthService

    var body: some View {
        VStack(spacing: 20) {
            Text("Yeni Hesap Oluştur")
                .font(.title).bold()
                .foregroundColor(.blueGrey)

            VStack(spacing: 0) {
                CustomTextField(text: $name, placeholder: "Ad Soyad", icon: "person")
                Divider()
                CustomTextField(text: $email, placeholder: "E-posta", icon: "envelope")
                Divider()
                CustomSecureField(text: $password, placeholder: "Şifre", icon: "lock")
                Divider()
                CustomSecureField(text: $confirmPassword, placeholder: "Şifre Tekrar", icon: "lock.rotation")
            }
            .background(Color(.systemGray6))
            .cornerRadius(16)

            Button(action: handleRegister) {
                if isLoading {
                    ProgressView().progressViewStyle(CircularProgressViewStyle(color: .white))
                } else {
                    Text("KAYIT OL")
                        .bold()
                        .frame(maxWidth: .infinity)
                }
            }
            .padding()
            .background(Color.blueGrey)
            .foregroundColor(.white)
            .cornerRadius(16)
            .disabled(isLoading)

            Spacer()
        }
        .padding(30)
        .navigationTitle("Kayıt Ol")
    }

    func handleRegister() {
        guard !name.isEmpty, !email.isEmpty, password == confirmPassword else { return }
        isLoading = true
        Task {
            do {
                _ = try await authService.registerWithEmail(email: email, password: password)
                dismiss()
            } catch {
                print("Hata: \(error.localizedDescription)")
            }
            isLoading = false
        }
    }
}
