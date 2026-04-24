import SwiftUI

struct CardEditorView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var authService: AuthService

    var card: BusinessCard? = nil

    @State private var name = ""
    @State private var title = ""
    @State private var company = ""

    @State private var countryCode = "+90"
    @State private var phoneNumber = ""

    @State private var countryCode2 = "+90"
    @State private var phoneNumber2 = ""
    @State private var isPhones2Visible = false

    @State private var email = ""
    @State private var website = ""
    @State private var address = ""
    @State private var templateId = 1
    @State private var selectedColor = "0xFFE3F2FD"

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // LIVE PREVIEW (MOCKUP) - Android ile aynı görünüm
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Kart Önizleme")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.gray)

                        ZStack(alignment: .bottomTrailing) {
                            Image("card_bg_\(templateId)")
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(height: 200)
                                .clipShape(RoundedRectangle(cornerRadius: 20))
                                .shadow(radius: 5)

                            VStack(alignment: .leading, spacing: 4) {
                                Text(name.isEmpty ? "AD SOYAD" : name.uppercased())
                                    .font(.system(size: 18, weight: .black))
                                    .foregroundColor(mockupTextColor)

                                Text(title.isEmpty ? "Ünvan" : title)
                                    .font(.system(size: 12))
                                    .foregroundColor(mockupTextColor.opacity(0.8))

                                Text(company.isEmpty ? "Şirket" : company)
                                    .font(.system(size: 12))
                                    .foregroundColor(mockupTextColor.opacity(0.8))

                                Spacer()

                                VStack(alignment: .leading, spacing: 2) {
                                    if !address.isEmpty {
                                        Text(address)
                                            .font(.system(size: 8))
                                            .lineLimit(2)
                                            .frame(maxWidth: 200, alignment: .leading)
                                    }
                                    Text("\(getFlagEmoji(countryCode)) \(countryCode) \(phoneNumber)")
                                        .font(.system(size: 8))
                                    if isPhones2Visible && !phoneNumber2.isEmpty {
                                        Text("\(getFlagEmoji(countryCode2)) \(countryCode2) \(phoneNumber2)")
                                            .font(.system(size: 8))
                                    }
                                }
                                .foregroundColor(mockupTextColor.opacity(0.7))
                            }
                            .padding(20)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .frame(height: 200)
                        }
                    }
                    .padding(.horizontal)

                    // EDIT FIELDS
                    VStack(alignment: .leading, spacing: 15) {
                        Text("Bilgileri Düzenle")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.gray)

                        CustomTextField(label: "Ad Soyad", text: $name)
                        CustomTextField(label: "Ünvan", text: $title)
                        CustomTextField(label: "Şirket", text: $company)

                        // Phone 1
                        HStack(spacing: 10) {
                            VStack(alignment: .leading) {
                                Text("Kod").font(.caption2).foregroundColor(.gray)
                                TextField("", text: $countryCode)
                                    .padding(10)
                                    .background(Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                            }
                            .frame(width: 80)

                            VStack(alignment: .leading) {
                                Text("Telefon").font(.caption2).foregroundColor(.gray)
                                TextField("", text: $phoneNumber)
                                    .padding(10)
                                    .background(Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                                    .keyboardType(.phonePad)
                            }

                            if !isPhones2Visible {
                                Button(action: { isPhones2Visible = true }) {
                                    Image(systemName: "plus.circle.fill")
                                        .foregroundColor(Color(hex: "2C3E50"))
                                        .font(.title2)
                                }
                                .padding(.top, 15)
                            }
                        }

                        // Phone 2
                        if isPhones2Visible {
                            HStack(spacing: 10) {
                                TextField("Kod", text: $countryCode2)
                                    .padding(10)
                                    .background(Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                                    .frame(width: 80)

                                TextField("2. Telefon", text: $phoneNumber2)
                                    .padding(10)
                                    .background(Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                                    .keyboardType(.phonePad)

                                Button(action: { isPhones2Visible = false; phoneNumber2 = "" }) {
                                    Image(systemName: "trash.fill")
                                        .foregroundColor(.red)
                                }
                            }
                        }

                        CustomTextField(label: "E-posta", text: $email, keyboard: .emailAddress)

                        VStack(alignment: .leading, spacing: 5) {
                            Text("Adres").font(.caption2).foregroundColor(.gray)
                            TextEditor(text: $address)
                                .frame(height: 60)
                                .padding(5)
                                .background(Color.gray.opacity(0.1))
                                .cornerRadius(8)
                        }

                        CustomTextField(label: "Web Sitesi", text: $website, keyboard: .URL)
                    }
                    .padding(.horizontal)

                    Button(action: saveCard) {
                        Text("KAYDET")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color(hex: "2C3E50"))
                            .foregroundColor(.white)
                            .cornerRadius(15)
                            .shadow(radius: 3)
                    }
                    .padding(.horizontal)
                    .padding(.bottom, 30)
                }
            }
            .navigationTitle(card == nil ? "Yeni Kart" : "Kartı Düzenle")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("İptal") { dismiss() }
                }
            }
            .onAppear(perform: setupFields)
        }
    }

    var mockupTextColor: Color {
        templateId == 2 ? .white : Color(hex: "2C3E50")
    }

    func getFlagEmoji(_ code: String) -> String {
        switch code.trimmingCharacters(in: .whitespaces) {
            case "+90": return "🇹🇷"
            case "+1": return "🇺🇸"
            case "+44": return "🇬🇧"
            case "+49": return "🇩🇪"
            default: return "🏳️"
        }
    }

    private func setupFields() {
        if let card = card {
            name = card.name
            title = card.title
            company = card.company
            email = card.email
            website = card.website
            address = card.address
            templateId = card.templateId ?? 1
            selectedColor = card.cardColor

            let p1 = card.phones.components(separatedBy: " ")
            if p1.count > 1 {
                countryCode = p1[0]
                phoneNumber = p1.dropFirst().joined(separator: " ")
            } else {
                phoneNumber = card.phones
            }

            if let phone2 = card.phones2, !phone2.isEmpty {
                isPhones2Visible = true
                let p2 = phone2.components(separatedBy: " ")
                if p2.count > 1 {
                    countryCode2 = p2[0]
                    phoneNumber2 = p2.dropFirst().joined(separator: " ")
                } else {
                    phoneNumber2 = phone2
                }
            }
        }
    }

    func saveCard() {
        let userId = authService.currentUser?.uid ?? ""
        let fullPhone = "\(countryCode) \(phoneNumber)"
        let fullPhone2 = isPhones2Visible ? "\(countryCode2) \(phoneNumber2)" : nil

        let newCard = BusinessCard(
            id: card?.id,
            name: name,
            title: title,
            company: company,
            address: address,
            phones: fullPhone,
            phones2: fullPhone2,
            email: email,
            website: website,
            photoPath: card?.photoPath,
            cardColor: selectedColor,
            templateId: templateId,
            fontStyle: "Default",
            userId: userId
        )

        Task {
            if card == nil {
                _ = try? await DatabaseService.shared.insertCard(card: newCard)
            } else {
                _ = try? await DatabaseService.shared.updateCard(card: newCard)
            }
            dismiss()
        }
    }
}

struct CustomTextField: View {
    let label: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(label).font(.caption2).foregroundColor(.gray)
            TextField("", text: $text)
                .padding(10)
                .background(Color.gray.opacity(0.1))
                .cornerRadius(8)
                .keyboardType(keyboard)
        }
    }
}
