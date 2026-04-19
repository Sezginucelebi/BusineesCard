import SwiftUI

struct CardEditorView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var authService: AuthService

    var card: BusinessCard? = nil

    @State private var name = ""
    @State private var title = ""
    @State private var company = ""
    @State private var phones = ""
    @State private var email = ""
    @State private var website = ""
    @State private var address = ""
    @State private var selectedColor = "0xFFE3F2FD"

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Kişisel Bilgiler")) {
                    TextField("Ad Soyad", text: $name)
                    TextField("Ünvan", text: $title)
                    TextField("Şirket", text: $company)
                }

                Section(header: Text("İletişim Bilgileri")) {
                    TextField("Telefon", text: $phones).keyboardType(.phonePad)
                    TextField("E-posta", text: $email).keyboardType(.emailAddress).autocapitalization(.none)
                    TextField("Web Sitesi", text: $website).keyboardType(.URL).autocapitalization(.none)
                    TextEditor(text: $address).frame(height: 80)
                }

                Section(header: Text("Kart Rengi")) {
                    ColorPickerRow(selectedColor: $selectedColor)
                }

                Button(action: saveCard) {
                    Text("KAYDET")
                        .bold()
                        .frame(maxWidth: .infinity)
                        .foregroundColor(.white)
                }
                .listRowBackground(Color.blueGrey)
            }
            .navigationTitle(card == nil ? "Yeni Kart" : "Kartı Düzenle")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("İptal") { dismiss() }
                }
            }
            .onAppear(perform: setupFields)
        }
    }

    private func setupFields() {
        if let card = card {
            name = card.name
            title = card.title
            company = card.company
            phones = card.phones
            email = card.email
            website = card.website
            address = card.address
            selectedColor = card.cardColor
        }
    }

    func saveCard() {
        let userId = authService.currentUser?.uid ?? ""
        let newCard = BusinessCard(
            id: card?.id,
            name: name,
            title: title,
            company: company,
            address: address,
            phones: phones,
            email: email,
            website: website,
            cardColor: selectedColor,
            fontStyle: "Default",
            userId: userId
        )

        Task {
            if card == nil {
                try? await DatabaseService.shared.insertCard(card: newCard)
            } else {
                try? await DatabaseService.shared.updateCard(card: newCard)
            }
            dismiss()
        }
    }
}

struct ColorPickerRow: View {
    @Binding var selectedColor: String
    let colors = [
        "0xFFE3F2FD", "0xFFE8F5E9", "0xFFFFF3E0",
        "0xFFF3E5F5", "0xFFFAFAFA", "0xFF2C2C2C"
    ]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 15) {
                ForEach(colors, id: \.self) { colorHex in
                    ZStack {
                        Circle()
                            .fill(Color(hex: colorHex.replacingOccurrences(of: "0xFF", with: "")))
                            .frame(width: 40, height: 40)
                            .overlay(Circle().stroke(Color.gray.opacity(0.3), lineWidth: 1))

                        if selectedColor == colorHex {
                            Image(systemName: "check")
                                .foregroundColor(colorHex == "0xFF2C2C2C" ? .white : .blue)
                        }
                    }
                    .onTapGesture {
                        selectedColor = colorHex
                    }
                }
            }
            .padding(.vertical, 5)
        }
    }
}
