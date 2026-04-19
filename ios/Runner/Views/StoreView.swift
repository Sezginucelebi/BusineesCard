import SwiftUI

struct StoreView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var authService: AuthService

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Image(systemName: "crown.fill")
                        .font(.system(size: 70))
                        .foregroundColor(.amber)

                    Text("Premium'a Geçin")
                        .font(.title.bold())

                    Text("Daha fazla kart oluşturun ve profesyonel kalın.")
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)

                    StoreCardItem(title: "Aylık Paket", price: "₺29.99 / Ay", limit: "2 Kart", level: .monthly)
                    StoreCardItem(title: "Yıllık Paket", price: "₺249.99 / Yıl", limit: "10 Kart", level: .yearly, isBestValue: true)

                    Text("Ödemeler App Store üzerinden güvenle yapılır.")
                        .font(.caption2)
                        .foregroundColor(.gray)
                        .padding(.top, 10)
                }
                .padding()
            }
            .navigationTitle("Mağaza")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Kapat") { dismiss() }
                }
            }
        }
    }
}

struct StoreCardItem: View {
    let title: String
    let price: String
    let limit: String
    let level: SubscriptionLevel
    var isBestValue: Bool = false

    @EnvironmentObject var authService: AuthService

    var body: some View {
        VStack(spacing: 12) {
            if isBestValue {
                Text("EN POPÜLER")
                    .font(.caption.bold())
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.amber)
                    .cornerRadius(4)
            }

            Text(title).font(.headline)
            Text(price).font(.title2.bold())
            Text(limit).foregroundColor(.secondary)

            Button(authService.getSubscriptionLevel() == level ? "Mevcut Paket" : "Satın Al") {
                Task {
                    await authService.upgradeSubscription(level: level)
                }
            }
            .buttonStyle(.borderedProminent)
            .tint(isBestValue ? .amber : .blueGrey)
            .disabled(authService.getSubscriptionLevel() == level)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.systemGray6))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isBestValue ? Color.amber : Color.clear, lineWidth: 2)
        )
    }
}
